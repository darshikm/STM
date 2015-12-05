package STM.Atomic;

import STM.ContentionManagers.*;
import STM.Exceptions.AbortedException;
import STM.Exceptions.PanicException;
import STM.PriorityTransaction;
import STM.Transaction;
import STM.VersionClock;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class TThread extends Thread {

	private final int TIMEOUT = 10;
	private static final Logger LOGGER = Logger.getLogger(TThread.class.getName());

	public <T> T doIt(Callable<T> xaction) throws Exception {
		T result = null;
		//LOGGER.info("Do it function called");
		while (true) {
			PriorityTransaction me = new PriorityTransaction();
			PriorityTransaction.setLocal(me);
			ContentionManager.setLocal(new PriorityBasedManager());

			try {
				result = xaction.call();
				//LOGGER.info("XACTION call is made");
			}
			catch (AbortedException e) {
				LOGGER.info("The transaction of thread.." + Thread.currentThread().getName() + "was aborted by CM.");
				me.abort();
				onAbort.run();
				continue;
			}
			catch (Exception e) {
				throw new PanicException(e);
			}
			if (onValidate.call()) {
				LOGGER.info("Validate call is successful by thread.." + Thread.currentThread().getName());
				if (me.commit()) {
					onCommit.run();
					LOGGER.info("COMMIT successful" );
					return result;
				}
				else {
					LOGGER.info("Transaction COMMIT failed"); //CAS
				}
			}
			me.abort();
			onAbort.run();
			LOGGER.info("Transaction ABORTED by calling onAbort after onValidate unsuccessful");
		}
	}

	private Runnable onAbort = new Runnable() {
		@Override
		public void run() {
			LOGGER.info("Transaction Aborting from Atomic.LockObject");
			WriteSet.getLocal().unlock();
			WriteSet.getLocal().clear();
			ReadSet.getLocal().clear();
		}
	};

	private Runnable onCommit = new Runnable() {
		@Override
		public void run() {
			try {
				WriteSet writeSet = WriteSet.getLocal();
				ReadSet readSet = ReadSet.getLocal();
				VersionClock.setWriteStamp();
				long writeVersion = VersionClock.getWriteStamp();
				//set the timestamp of transaction

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
					LOGGER.info("Updating the write during the commit");
					key.stamp = writeVersion;
				}
				writeSet.unlock();
				writeSet.clear();
				readSet.clear();
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
				LOGGER.info("Atomic.WriteSet Lock TIMEOUT");
				return false;
			}
			for (LockObject<?> x : readSet.getList()) {
				if (x.lock.isLocked() && !x.lock.isHeldByCurrentThread()) {
					LOGGER.info("Object locked and held by somebody else!");
					/*
					try {
						ContentionManager.getLocal().resolve(Transaction.getLocal(), x.locker);
					}
					catch (AbortedException e) {
						return false;
					}
					*/
					return false;
				}
				if (x.stamp > VersionClock.getReadStamp()) {
					LOGGER.info("OOPS error: Stamp > Version CLOCK");
					return false;
				}
			}
			return true;
		}
	};

}
