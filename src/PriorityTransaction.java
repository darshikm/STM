package STM;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by darshikm on 12/4/15.
 */
public class PriorityTransaction extends Transaction {
    public enum Status {ABORTED, ACTIVE, COMMITTED};

    //the status of current transaction
    private final AtomicReference<Status> status;

    public PriorityTransaction(int n) {

        status = new AtomicReference<Status>(Status.ACTIVE);

    }

    static ThreadLocal<PriorityTransaction> local = new ThreadLocal<PriorityTransaction>() {
        protected PriorityTransaction initialValue() {
            return new PriorityTransaction(Status.COMMITTED);
        }
    };

    protected PriorityTransaction(PriorityTransaction.Status myStatus) {
        status = new AtomicReference<Status>(myStatus);
        t_stamp = VersionClock.getGlobalStamp();
    }

    public static PriorityTransaction getLocal() {
        return local.get();
    }

    public static void setLocal(PriorityTransaction transaction) {
        local.set(transaction);
    }
}