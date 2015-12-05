package STM.ContentionManagers;

import STM.Exceptions.AbortedException;
import STM.Transaction;

public abstract class ContentionManager {

	static ThreadLocal<ContentionManager> local = new ThreadLocal<ContentionManager>() {
		protected ContentionManager initialValue() {
			ContentionManager manager = null;
			try {
				manager = ContentionManager.class.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return manager;
		}
	};
	
	public abstract void resolve(Transaction me, Transaction other) throws AbortedException;
	
	public static ContentionManager getLocal() {
		return local.get();
	}
	
	public static void setLocal(ContentionManager m) {
		local.set(m);
	}
}
