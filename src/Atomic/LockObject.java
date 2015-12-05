package STM.Atomic;

import STM.ContentionManagers.ContentionManager;
import STM.Exceptions.AbortedException;
import STM.Exceptions.PanicException;
import STM.Transaction;
import STM.VersionClock;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class LockObject<T extends Copyable<T>> extends AtomicObject<T> {

	protected ReentrantLock lock;
	public volatile long stamp;
	public volatile T version;
	protected Transaction locker, creator;
	private static Logger LOGGER = Logger.getLogger(LockObject.class.getName());

    public LockObject(T init) {
        super(init);
		version = internalInit;
		lock = new ReentrantLock();
		creator = Transaction.getLocal();
		stamp = 0;
    }

    @SuppressWarnings("unchecked")
	@Override
    public T openRead() throws AbortedException, PanicException {
		Transaction.getLocal().incrementKarma();
		Transaction.getLocal().recency.set(VersionClock.getGlobalStamp());
        ReadSet readSet = ReadSet.getLocal();
        switch(Transaction.getLocal().getStatus()) {
        case COMMITTED:
        	return version;

        case ACTIVE:
			WriteSet writeSet = WriteSet.getLocal();
        	if (writeSet.get(this) == null) {
        		while (lock.isLocked()) {
					LOGGER.info("OpenRead: LockObj := " + this.toString() + "is locked by Transaction := " + locker);
					LOGGER.severe("Calling Contention manager resolution in OpenRead");
					ContentionManager.getLocal().resolve(Transaction.getLocal(), locker);
					Thread.yield();
        		}
        		readSet.add(this);
				//LOGGER.info("Adding a new copy of SNode with value: " + version.toString()
				//+ "in the ReadSet of Thread.." + Thread.currentThread().getName());
        		return version;
        	}
        	else {
				//LOGGER.info("Obtaining the version of SNode with value: " + version.toString()
				//		+ "from the WriteSet of Thread.." + Thread.currentThread().getName());
        		return (T) writeSet.get(this);
        	}

        case ABORTED:
			LOGGER.info("Transaction Status in OpenRead = ABORTED.. throw AbortedException");
        	throw new AbortedException();
        default:
        	throw new PanicException("Unexpected Transaction state!");	
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    public T openWrite() throws AbortedException, PanicException {
    	switch(Transaction.getLocal().getStatus()) {
    	case COMMITTED:
			return version;

		case ACTIVE:
			Transaction.getLocal().incrementKarma();
			Transaction.getLocal().recency.set(VersionClock.getGlobalStamp());
			WriteSet writeSet = WriteSet.getLocal();
    		T scratch = (T) writeSet.get(this);
    		if (scratch == null) {
				while (lock.isLocked()) {
					LOGGER.info("OpenWrite: LockObj := " + this.toString() + "is locked by Transaction := " + locker);
					LOGGER.severe("Calling Contention manager resolution in OpenWrite");
					ContentionManager.getLocal().resolve(Transaction.getLocal(), locker);
					Thread.yield();
				}
				try {
					scratch = (T) internalClass.newInstance();
					version.copyTo(scratch);
					writeSet.put(this, scratch);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
    		return scratch;
    	case ABORTED:
			LOGGER.info("In Open Write ABORTED");
        	throw new AbortedException();
        default:
        	throw new PanicException("Unexpected Transaction state!");	
    	}
    }

    @Override
    public boolean validate() {
    	switch(Transaction.getLocal().getStatus()) {
    	case COMMITTED:
    		return true;
    	case ACTIVE:
			if(stamp <= VersionClock.getReadStamp())
    			return true;
			else return false;
    	case ABORTED:
    		return false;
    	default:
    		return false;
    	}
    }

	public void unlock(){
		if (lock.isLocked() && locker == Transaction.getLocal())
			lock.unlock();
	}
	
	public boolean tryLock(long timeout, TimeUnit timeUnit) {
		boolean retValue = false;
		try {
			retValue = lock.tryLock(timeout, timeUnit);
			if (retValue)
				locker = Transaction.getLocal();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return retValue;
	}
}