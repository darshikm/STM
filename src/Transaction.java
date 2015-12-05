package STM;

import STM.Atomic.LockObject;
import STM.Atomic.ReadSet;
import STM.Atomic.WriteSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Transaction {
	public enum Status {ABORTED, ACTIVE, COMMITTED};
	private final AtomicReference<Status> status;
    private AtomicInteger karma = new AtomicInteger(0);
    private AtomicInteger erupt = new AtomicInteger(0);
    public long threshold = 1L;
    public AtomicLong timestampStart = new AtomicLong(VersionClock.getGlobalStamp());
    public AtomicLong recency = new AtomicLong(VersionClock.getGlobalStamp());
    public Thread thread = Thread.currentThread();
	protected volatile long t_stamp;

    private ReadSet rs;
    private List<LockObject<?>> t_list;
    private WriteSet ws;
    private HashMap<LockObject<?>, Object> t_WriteSet;

	static ThreadLocal<Transaction> local = new ThreadLocal<Transaction>() {
		protected Transaction initialValue() {
			return new Transaction(Status.COMMITTED);
		}
	};

	public Transaction() {
        status = new AtomicReference<Status>(Status.ACTIVE);
        t_list = new ArrayList<>();
        rs = new ReadSet(t_list);
        ReadSet.setLocal(rs);

        t_WriteSet = new HashMap<>();
        ws = new WriteSet(t_WriteSet);
        WriteSet.setLocal(ws);
	}
	
	private Transaction(Transaction.Status myStatus) {
		status = new AtomicReference<Status>(myStatus);
	}
	
	public Status getStatus() {
		return status.get();
	}
	
	public boolean commit() {
		return status.compareAndSet(Status.ACTIVE, Status.COMMITTED);
	}

	public static Transaction getLocal() {
		return local.get();
	}
	
	public static void setLocal(Transaction transaction) {
		local.set(transaction);
	}

	public long getTransactionStamp() { return this.t_stamp; }

	public void setTransactionStamp(int n) { this.t_stamp = n; }

    public boolean abort() {
        if (threshold < 2000000000)
            threshold++;
        else threshold = 0L;
        return status.compareAndSet(Status.ACTIVE, Status.ABORTED);
    }

    public Integer getKarma() {
        return karma.get();
    }

    public void incrementKarma() {
        karma.getAndIncrement();
    }

    public void clearKarma() {
        karma.set(0);
    }

    public int getErupt() {
        if (erupt.get() == 0)
            erupt.set(karma.get());
        return erupt.get();
    }

    public void setErupt(int value) {
        erupt.set(value);
    }
}
