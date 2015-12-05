package STM.Atomic;

import STM.ContentionManagers.ContentionManager;
import STM.Exceptions.AbortedException;
import STM.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WriteSet {
    private static Logger LOGGER = Logger.getLogger(WriteSet.class.getName());
	static ThreadLocal<Map<LockObject<?>, Object>> map = new ThreadLocal<Map<LockObject<?>, Object>>() {
		protected synchronized Map<LockObject<?>, Object> initialValue() {
			return new HashMap<>();
		}
	};

    public WriteSet(HashMap<LockObject<?>, Object> thisMap) {
        map.set(thisMap);
    }

	static ThreadLocal<WriteSet> local = new ThreadLocal<WriteSet>() {
		protected WriteSet initialValue() {
			return null;
		}
	};

    public Object get(LockObject<?> x) {
		return map.get().get(x);
	}

	public void put(LockObject<?> x, Object y) {
		map.get().put(x, y);
	}

	public boolean tryLock(long timeout, TimeUnit timeUnit) {
		for (LockObject<?> x : map.get().keySet()) {
			while (!x.tryLock(timeout, timeUnit)) {
				try {
                    LOGGER.severe("Contention manager resolution in tryLock of WriteSet");
					ContentionManager.getLocal().resolve(Transaction.getLocal(), x.locker);
				}
				catch (AbortedException e) {
					return false;
				}
			}
		}
		return true;
	}

	public void unlock() {
		for (LockObject<?> x : map.get().keySet()) {
			x.unlock();
		}
	}

	public static WriteSet getLocal() {
		return local.get();
	}

	public static void setLocal(WriteSet writeSet) {
		local.set(writeSet);
	}

	public Set<Map.Entry<LockObject<?>, Object>>  getList() {
		return map.get().entrySet();
	}

	public void clear() {
		map.get().clear();
	}
}