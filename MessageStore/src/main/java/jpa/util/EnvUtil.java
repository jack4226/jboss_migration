package jpa.util;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnvUtil {
	static final Logger logger = LogManager.getLogger(EnvUtil.class);

	public static String getEnv() {
		return (System.getProperty("env","dev"));
	}
	
	public static int getNumberAllThreads() {
		int nbThreads =  Thread.getAllStackTraces().keySet().size();
		return nbThreads;
	}
	
	public static int getNumberRunningThreads() {
		int nbRunning = 0;
		for (Thread t : Thread.getAllStackTraces().keySet()) {
		    if (Thread.State.RUNNABLE.equals(t.getState())) {
		    	nbRunning++;
		    }
		}
		return nbRunning;
	}
	
	public static int getNumberBlockedThreads() {
		int nbBlocked = 0;
		for (Thread t : Thread.getAllStackTraces().keySet()) {
		    if (Thread.State.BLOCKED.equals(t.getState())) {
		    	nbBlocked++;
		    }
		}
		return nbBlocked;
	}
	
	public static void displayAllThreads() {
		int nbThreads = 0;
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		logger.info("Display all threads:");
		for (Thread t : threadSet) {
			logger.info("Thread - " + t + " : state: " + t.getState());
			++nbThreads;
		}
		logger.info("Number of threads started by Main thread: " + nbThreads);
	}

	public static void displayRunningThreads() {
		int nbThreads = 0;
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		logger.info("Display running threads:");
		for (Thread t : threadSet) {
			//if (t.getThreadGroup() == Thread.currentThread().getThreadGroup()
			//		&& t.getState() == Thread.State.RUNNABLE) {
			if (t.getState() == Thread.State.RUNNABLE) {
				logger.info("Thread - " + t + " : state: " + t.getState());
				++nbThreads;
			}
		}
		logger.info("Number of running threads started by Main thread: " + nbThreads);
	}


}
