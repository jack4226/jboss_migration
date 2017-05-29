package bankers.algorithm;

import java.util.Arrays;
import java.util.Random;

public class ResourceManager {
	final int n; // # of threads
	final int m; // # of resources
	final int[] avail; // # of available resources of each type
	final int[][] max; // # of each resource that each thread may want
	final int[][] alloc; // # of resources that each thread is using
	final int[][] need; // # of resources that each thread might still request
	// max = need + alloc
	
	final int availLow = 6;
	final int availHigh = 10;
	
	final Random random = new Random(System.currentTimeMillis());
	int nbrUnSafe = 0;

	public static void main(String[] args) {
		// 4 threads, 3 resources
		ResourceManager rm = new ResourceManager(4,3);
		try {
			// simulate 20 requests
			rm.simulate(20);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	void simulate(int numWorkers) {
		Worker[] workers = new Worker[numWorkers];
		while (nbrUnSafe < 2) {
			for (int iw=0; iw<numWorkers; iw++) {
				int i = iw;
				if (i >= n) {
					i = random.nextInt(n);
					try {
						Thread.sleep(5 + random.nextInt(10));
					}
					catch (InterruptedException e) {}
				}
				workers[iw] = new Worker(i, this);
				workers[iw].setName("worker-" + iw);
				workers[iw].start();
			}
			for (int iw=0; iw<numWorkers; iw++) {
				try {
					workers[iw].join();
				}
				catch (InterruptedException e) {}
			}
		}
		System.out.println("Program completed. Number of unsafe detected: " + nbrUnSafe);
	}

	public ResourceManager(int n, int m) {
		this.n = n;
		this.m = m;
		this.avail = new int[m];
		this.max = new int[n][m];
		this.alloc = new int[n][m];
		this.need = new int[n][m];
		 
		initialize();
	}

	private void initialize() {
		for (int j=0; j<m; j++) {
			avail[j] = availLow + random.nextInt(availHigh - availLow + 1);
			for (int i=0; i<n; i++) {
				max[i][j] = 0;
				alloc[i][j] = 0;
			}
		}
		
		for (int i=0; i<n; i++) {
			while (sum(max[i])<m) {
				buildMax(i);
			}
			need[i] = Arrays.copyOf(max[i], m);
		}
		
		System.out.println("Avail: " + Arrays.toString(avail));
		System.out.println("Max:   " + Arrays.deepToString(max));
	}
	
	private void buildMax(int i) {
		for (int j=0; j<m; j++) {
			if (max[i][j] == 0) {
				//max[i][j] = random.nextInt((int)(availHigh/2) + 2);
				max[i][j] = random.nextInt(avail[j]);
			}
		}
	}
	
	private int sum(int[] array) {
		int sum = 0;
		for (int j=0; j<array.length; j++) {
			sum += array[j];
		}
		return sum;
	}
	
	/*
	 * i - is the thread making the request
	 * request - contains the resources being requested
	 */
	public synchronized void allocate(int i, int[] request) {
		System.out.println(Thread.currentThread().getName() + " - Entering allocate: , Request="
				+ Arrays.toString(request) + ", Thread-" + i);
		// if (request > need[i]) error;
		for (int j=0; j<m; j++) {
			if (request[j] > need[i][j]) {
				throw new IllegalArgumentException("Invalid Request - Exceeded Need.");
			}
		}
		// else while (request[i] > avail) wait;
		while (!isAvailable(request)) {
			try {
				System.out.println(Thread.currentThread().getName() + " - Not enough available, wait. Thread-" + i);
				this.wait();
				System.out.println(Thread.currentThread().getName() + " - Woke up from isAvailable. Thread-" + i);
			}
			catch (InterruptedException e) {}
		}
		
		applyRequest(i, request);
		
		while (!isSafeState()) {
			System.out.println(Thread.currentThread().getName() + " - ########## Not a Safe State, wait. Thread-" + i);
			System.out.println("Avail: " + Arrays.toString(avail));
			System.out.println("Need:  " + Arrays.deepToString(need));
			System.out.println("Alloc: " + Arrays.deepToString(alloc));
			nbrUnSafe++;
			undoRequest(i, request);
			try {
				this.wait();
			}
			catch (InterruptedException e) {}
			System.out.println(Thread.currentThread().getName() + " - %%%%%%%%%% Woke up from isSafeState. Thread-" + i);
			applyRequest(i, request);
		}
		System.out.println(Thread.currentThread().getName() + " - Safe to process the request for Thread-" + i);
	}
	
	public synchronized void deallocate(int i) {
		System.out.println(Thread.currentThread().getName() + " Entering deallocate: Thread-" + i);
		for (int j=0; j<m; j++) {
			avail[j] += alloc[i][j];
			need[i][j] += alloc[i][j];
			alloc[i][j] = 0;
			if (max[i][j] != alloc[i][j] + need[i][j]) {
				throw new RuntimeException("Program logic error.");
			}
		}
		this.notify();
	}

	private void applyRequest(int i, int[] request) {
		for (int j=0; j<m; j++) {
			avail[j] -= request[j];
			alloc[i][j] += request[j];
			need[i][j] -= request[j];
			if (max[i][j] != alloc[i][j] + need[i][j]) {
				throw new RuntimeException("Program logic error.");
			}
		}
	}

	private void undoRequest(int i, int[] request) {
		for (int j=0; j<m; j++) {
			avail[j] += request[j];
			alloc[i][j] -= request[j];
			need[i][j] += request[j];
			if (max[i][j] != alloc[i][j] + need[i][j]) {
				throw new RuntimeException("Program logic error.");
			}
		}		
	}
	
	private boolean isAvailable(int[] request) {
		for (int j=0; j<m; j++) {
			if (request[j] > avail[j]) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isSafeState() {
		int[] finished = new int[n];
		int[] work = new int[m];
		for (int i=0; i<n; i++) {
			finished[i] = 0;
		}
		for (int j=0; j<m; j++) {
			work[j] = avail[j];
		}
		
		int count = 0;
		while (count++ < n) {
			for (int i=0; i<n; i++) {
				if (finished[i]==1) {
					continue;
				}
				if (isNeedMet(i, work)) {
					finished[i] = 1;
					for (int j=0; j<m; j++) {
						work[j] += alloc[i][j];
					}
				}
			}
		}
		
		if (sum(finished)==n) {
			return true;
		}
		else {
			return false;
		}
	}

	boolean isNeedMet(int i, int[] work) {
		for (int j=0; j<m; j++) {
			if (need[i][j] > work[j]) {
				return false;
			}
		}
		return true;
	}

	static class Worker extends Thread {
		
		final ResourceManager rm;
		
		final int i;
		Worker(int i, ResourceManager rm) {
			this.i = i;
			this.rm = rm;
		}
		
		@Override
		public void run() {
			//System.out.println("++++++++++" + this.getName() + " starting ...");
			int[] request = new int[rm.m];
			while (rm.sum(request) == 0) {
				buildRequest(i, request);
			}
			try {
				rm.allocate(i, request);
				try {
					Thread.sleep(50 + rm.random.nextInt(100));
				}
				catch (InterruptedException e) {}
				
				rm.deallocate(i);
			}
			catch (IllegalArgumentException e) {
				System.out.println(Thread.currentThread().getName() + " - " + e.getMessage());
			}
			catch (RuntimeException e) {
				System.out.println(Thread.currentThread().getName() + " - " + e.getMessage());
			}
			//System.out.println("==========" + this.getName() + " exiting ...");
		}

		private void buildRequest(int i, int[] request) {
			for (int j=0; j<rm.m; j++) {
				if (request[j] == 0) {
					request[j] = rm.max[i][j] == 0? 0 : rm.random.nextInt(rm.max[i][j] + 1);
				}
			}
		}

	}
}
