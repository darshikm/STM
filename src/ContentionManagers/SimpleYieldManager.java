package STM.ContentionManagers;

import STM.Exceptions.AbortedException;
import STM.Transaction;

import java.util.logging.Logger;

/**
 * Created by darshikm on 12/4/15.
 */
public class SimpleYieldManager extends ContentionManager {
    private static final Logger LOGGER = Logger.getLogger(SimpleYieldManager.class.getName());
    @Override
    public void resolve(Transaction me, Transaction other) throws AbortedException {
        if(other.getStatus() != Transaction.Status.ABORTED)
            throw new AbortedException();
    }
}