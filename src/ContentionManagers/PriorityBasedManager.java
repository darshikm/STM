package STM.ContentionManagers;

import STM.Exceptions.AbortedException;
import STM.PriorityTransaction;
import STM.Transaction;

import java.util.logging.Logger;

/**
 * Created by darshikm on 12/4/15.
 */
public class PriorityBasedManager extends ContentionManager {
    private static final Logger LOGGER = Logger.getLogger(PriorityBasedManager.class.getName());
    @Override
    public void resolve(Transaction me, Transaction other) throws AbortedException {
        long t_of_me, t_of_other;
        t_of_me = me.getTransactionStamp();
        t_of_other = other.getTransactionStamp();
        if(t_of_me == t_of_other) {
            LOGGER.severe("The TIME STAMPS are equal!!");
            throw new AbortedException();
        }
        if(t_of_me < t_of_other) other.abort();
        else {
            throw new AbortedException();
        }
    }
}
