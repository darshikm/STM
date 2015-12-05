package STM;

import STM.Atomic.TThread;
import STM.DataStructure.TLinkedList;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/* STM basic test */

public class Main {

	public static  TLinkedList<Integer> linkedList;
	private static Integer NUM_THREADS = 50;
	public static void init_linkedList () throws Exception {
		linkedList = new TLinkedList<>(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	private static Logger LOGGER = Logger.getLogger(Main.class.getName());

	public static class Produce<T> implements Callable<T> {
		private T value;
		public Produce(T value) {
			this.value = value;
		}
		@Override
		public T call() throws Exception {
			linkedList.add((Integer) value);
			return null;
		}
	}

	public static class PrintAll<T> implements Callable<T> {
		public PrintAll() {	}
		@Override
		public T call() throws Exception {
			linkedList.printAll();
			return null;
		}
	}

	public static class Consume<T> implements Callable<T> {
		private T value;
		public Consume(T value) {
			this.value = value;
		}
		@Override
		public T call() throws Exception {
			linkedList.remove((Integer) value);
			return null;
		}
	}

    public static void main(String[] args) throws Exception {
		Main.init_linkedList();
		ExecutorService executor = Executors.newFixedThreadPool(8);
		Random random = new Random();
		for (int i=0; i<NUM_THREADS; i++) {
			int inserted = random.nextInt(i+1);
			executor.execute(new WorkerThread(new Produce<>(inserted), "thread" + i));
			if (i > 5) {
				executor.execute(new WorkerThread(new Consume<>(i-3)));
			}
		}
		executor.shutdown();
    }

	public static class WorkerThread implements Runnable {

		private Callable<Integer> action;
		private static AtomicInteger count = new AtomicInteger(0);
		String t_name;

		public WorkerThread(Callable<Integer> action) {
			this.action = action;
			t_name = "default" + action.toString();
			LOGGER.info("Count at the time of creation of thread := "
			+ t_name + "; is cnt := " + count);
			count.incrementAndGet();
		}

		public WorkerThread(Callable<Integer> action, String name) {
			this.action = action;
			t_name = name;
			LOGGER.info("Count at the time of creation of thread := "
					+ t_name + "; is cnt := " + count);
			count.incrementAndGet();
		}

		@Override
		public void run() {
			try {
				Thread.currentThread().setName(t_name);
				new TThread().doIt(action);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
