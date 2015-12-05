package STM;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Transaction {
	public enum Status {ABORTED, ACTIVE, COMMITTED};

	private final AtomicReference<Status> status;

	//time stamp when the transaction is created.
	protected volatile long t_stamp;

	static ThreadLocal<Transaction> local = new ThreadLocal<Transaction>() {
		protected Transaction initialValue() {
			return new Transaction(Status.COMMITTED);
		}
	};

	public Transaction() {
		status = new AtomicReference<Status>(Status.ACTIVE);
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
	
	public boolean abort() {
		return status.compareAndSet(Status.ACTIVE, Status.ABORTED);
	}
	
	public static Transaction getLocal() {
		return local.get();
	}
	
	public static void setLocal(Transaction transaction) {
		local.set(transaction);
	}

	public long getTransactionStamp() { return this.t_stamp; }
}
