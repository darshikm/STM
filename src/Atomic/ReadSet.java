package STM.Atomic;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ReadSet {

    static ThreadLocal<List<LockObject<?>>> list = new ThreadLocal<List<LockObject<?>>>() {
        protected synchronized List<LockObject<?>> initialValue() {
            return new ArrayList<>();
        }
    };
    static ThreadLocal<ReadSet> local = new ThreadLocal<ReadSet>() {
        protected ReadSet initialValue() {
            return null;
        }
    };
    private static final Logger LOGGER = Logger.getLogger(ReadSet.class.getName());

    public ReadSet(List<LockObject<?>> thisList) {
        list.set(thisList);
    }

    public static ReadSet getLocal() {
        return local.get();
    }

    public static void setLocal(ReadSet readSet) {
        local.set(readSet);
    }

    public void add(LockObject<?> lockObj) {
        if (!list.get().contains(lockObj))
            list.get().add(lockObj);
    }

    public List<LockObject<?>> getList() {
        return list.get();
    }

    public void clear() {
        list.get().clear();
    }
}