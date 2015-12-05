package STM.Atomic;

import STM.ContentionManagers.*;
import STM.Exceptions.AbortedException;
import STM.Exceptions.PanicException;
import STM.PriorityTransaction;
import STM.Transaction;
import STM.VersionClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class TThread extends Thread {
	private final int TIMEOUT = 10;
	private int retries = 0;
	private static final Logger LOGGER = Logger.getLogger(TThread.class.getName());

	public <T> T doIt(Callable<T> xaction) throws Exception {
		T result = null;
		while (retries <= 10) {
			Transaction me = new Transaction();
			Transaction.setLocal(me);
			ContentionManager.setLocal(new PublishedTimestamp());

			try {
				result = xaction.call();
			}
			catch (AbortedException e) {
				LOGGER.info("The transaction of thread.." + Thread.currentThread().getName() + " was aborted by CM.");
				me.abort();
				onAbort.run();
				retries++;
				continue;
			}
			catch (Exception e) {
				throw new PanicException(e);
			}
			if (onValidate.call()) {
				retries = 0;
				LOGGER.info("Validate call is successful by thread.." + Thread.currentThread().getName());
				if (me.commit()) {
					onCommit.run();
					LOGGER.info("COMMIT successful" );
					return result;
				}
				else {
					LOGGER.info("Transaction COMMIT failed");
				}
			}
			me.abort();
			onAbort.run();
			LOGGER.info("Transaction ABORTED by calling onAbort after onValidate unsuccessful");
		}
		return result;
	}

	private Runnable onAbort = new Runnable() {
		@Override
		public void run() {
			WriteSet.getLocal().unlock();
			WriteSet.getLocal().clear();
			ReadSet.getLocal().clear();
		}
	};

	private Runnable onCommit = new Runnable() {
		@Override
		public void run() {
			try {
				LOGGER.info("inside run of onCommit");
				WriteSet writeSet = WriteSet.getLocal();
				ReadSet readSet = ReadSet.getLocal();
				VersionClock.setWriteStamp();
				long writeVersion = VersionClock.getWriteStamp();

				for (Map.Entry<LockObject<?>,Object> entry : writeSet.getList()) {
					LockObject<?> key = entry.getKey();
					Copyable<?> destination = null;
					try {
						destination = key.openRead();
					}
					catch (AbortedException | PanicException e) {
						e.printStackTrace();
					}
					Copyable<Copyable<?>> source = (Copyable<Copyable<?>>) entry.getValue();
					source.copyTo(destination);
					key.stamp = writeVersion;
				}
				writeSet.unlock();
				writeSet.clear();
				readSet.clear();
				Transaction.getLocal().clearKarma();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private Callable<Boolean> onValidate = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			WriteSet writeSet = WriteSet.getLocal();
			ReadSet readSet = ReadSet.getLocal();
			if (!writeSet.tryLock(TIMEOUT, TimeUnit.MILLISECONDS)) {
				LOGGER.info("writeSet try lock returned false");
				return false;
			}
			for (LockObject<?> x : readSet.getList()) {
				if (x.lock.isLocked() && !x.lock.isHeldByCurrentThread()) {
					/*
					try {
						LOGGER.info("Resolving contention at time of read consistency check");
						ContentionManager.getLocal().resolve(Transaction.getLocal(), x.locker);
					}
					catch (AbortedException e) {
						return false;
					}
					*/
					return false;
				}
				if (x.stamp > VersionClock.getReadStamp()) {
					LOGGER.info("Inconsistent version error: Stamp > Version CLOCK");
					return false;
				}
			}
			return true;
		}
	};

}
