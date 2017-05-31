package jpa.test.misc;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import jpa.util.EnvUtil;

public class EnvUtilTest {
	static final Logger logger = Logger.getLogger(EnvUtilTest.class);
	
	@Test
	public void testEnvUtil() {
		assertEquals("dev", EnvUtil.getEnv());
		System.setProperty("env", "prod");
		assertEquals("prod", EnvUtil.getEnv());
		
		int numberThreads = 5;
		List<Thread> threadList = new ArrayList<>();
		for (int i = 0; i < numberThreads; i++) {
			Thread t = new Thread(new TestThread());
			t.setName("TestThread[" + i + "]");
			threadList.add(t);
			t.start();
		}
		
		int total = EnvUtil.getNumberAllThreads();
		int running = EnvUtil.getNumberRunningThreads();
		int blocked = EnvUtil.getNumberBlockedThreads();
		
		logger.info("Total thread count: " + total);
		
		logger.info("Running thread count: " + running);
		
		logger.info("Blocked thread count: " + blocked);
		
		assertTrue(total > numberThreads);
		assertTrue(total > running);
		assertTrue(total > blocked);
		assertTrue(total >= (running + blocked));
		
		EnvUtil.displayAllThreads();
		
		EnvUtil.displayRunningThreads();
		
		for (Thread t : threadList) {
			try {
				t.join();
			} catch (InterruptedException e) {}
		}
		logger.info("All threads are completed!");
	}
	
	
	static class TestThread implements Runnable {
		public void run() {
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				logger.error("Exception caught", e);;
			}
		}
	}
}
