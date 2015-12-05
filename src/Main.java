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
	
	public static TLinkedList<Integer> linkedList = new TLinkedList<>(Integer.MIN_VALUE, Integer.MAX_VALUE);
	private static Logger LOGGER = Logger.getLogger(Main.class.getName());

	public static class Produce<T> implements Callable<T> {
		private T value;
		
		public Produce(T value) {
			//LOGGER.info("Adding DataStructure.Node with value : " + value);
			this.value = value;
		}

		@Override
		public T call() throws Exception {
			//LOGGER.info("Calling Produce Callable: " + value);
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
		ExecutorService executor = Executors.newFixedThreadPool(4);
        //Random random = new Random();

		//TThread t1, t2, t3, t4;
		executor.execute(new WorkerThread(new Produce<>(5), "THREAD1"));
		executor.execute(new WorkerThread(new Produce<>(8), "THREAD2"));
		//executor.execute(new WorkerThread(new PrintAll<Integer>(), "THREAD3"));
		executor.execute(new WorkerThread(new Consume<>(5), "THREAD4"));
		executor.execute(new WorkerThread(new Produce<>(12), "THREAD3"));
		executor.execute(new WorkerThread(new Produce<>(1), "THREAD5"));
		executor.execute(new WorkerThread(new Produce<>(152)));
		executor.execute(new WorkerThread(new Produce<>(62)));
		executor.execute(new WorkerThread(new Produce<>(52)));
		executor.execute(new WorkerThread(new Produce<>(11)));
		executor.execute(new WorkerThread(new Produce<>(33), "THREAD7"));
		//executor.execute(new WorkerThread(new PrintAll<Integer>()));

		executor.shutdown();
		//linkedList.printAll();
		while (true) {
			if(executor.isShutdown()) {
				break;
			}
		}
		if(executor.isShutdown()) { linkedList.printAll(); }


		/*
		t1 = new TThread();
		t1.setName("THRD1");

		t2 = new TThread();
		t2.setName("THRD2");

		t1.doIt(new Produce<>(5));
		t2.doIt(new Produce<>(8));

		t3 = new TThread();
		t3.setName("THRD3");
		t3.doIt(new PrintAll<>());

		t4 = new TThread();
		t4.setName("THRD4");
		t4.doIt(new Consume<>(5));
		*/
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
