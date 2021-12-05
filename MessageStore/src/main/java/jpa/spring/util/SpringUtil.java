package jpa.spring.util;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jpa.util.ExceptionUtil;

public class SpringUtil {
	static final Logger logger = LogManager.getLogger(SpringUtil.class);
	
	private static AnnotationConfigApplicationContext  jmsAndDaoConfCtx = null;
	
	private static AbstractApplicationContext daoConfCtx = null;
	
	private static AnnotationConfigApplicationContext allAppsConfCtx = null;
	
	private SpringUtil() { /* static only */ }
	
	public synchronized static AbstractApplicationContext getJmsAppContext() {
		if (jmsAndDaoConfCtx == null) {
			logger.info("getJmsAppContext() - load application and datasource config, Calling class: "
					+ ExceptionUtil.findCallingClass(new Exception()));
			jmsAndDaoConfCtx = new AnnotationConfigApplicationContext();
			jmsAndDaoConfCtx.register(SpringAppConfig.class, SpringJmsConfig.class);
			jmsAndDaoConfCtx.refresh();
			jmsAndDaoConfCtx.registerShutdownHook();
		}
		return jmsAndDaoConfCtx;
	}
	
	public synchronized static AbstractApplicationContext getAppContext() {
		if (jmsAndDaoConfCtx != null) {
			return jmsAndDaoConfCtx;
		}
		if (daoConfCtx == null) {
			logger.info("getAppContext() - load JPA and datasource config only, Calling class: "
					+ ExceptionUtil.findCallingClass(new Exception()));
			daoConfCtx = new AnnotationConfigApplicationContext(SpringAppConfig.class);
			daoConfCtx.registerShutdownHook();
		}
		return daoConfCtx;
	}

	public synchronized static AbstractApplicationContext getAllAppsContext() {
		if (allAppsConfCtx == null) {
			logger.info("getTaskAppContext() - load application, datasource, and task config, Calling class: "
					+ ExceptionUtil.findCallingClass(new Exception()));
			allAppsConfCtx = new AnnotationConfigApplicationContext();
			allAppsConfCtx.register(SpringAppConfig.class, SpringJmsConfig.class, SpringTaskConfig.class);
			allAppsConfCtx.refresh();
			allAppsConfCtx.registerShutdownHook();
		}
		return allAppsConfCtx;
	}

	public static void shutDownConfigContexts() {
		if (jmsAndDaoConfCtx != null) {
			jmsAndDaoConfCtx.stop();
			jmsAndDaoConfCtx.close();
		}
		if (daoConfCtx != null) {
			daoConfCtx.stop();
			daoConfCtx.close();
		}
		if (allAppsConfCtx != null) {
			allAppsConfCtx.stop();
			allAppsConfCtx.close();
		}
	}
	
	
	private static final ThreadLocal<Stack<TransTuple>> transThreadLocal = new ThreadLocal<>();
	
	public static void beginTransaction() {
		int level = transThreadLocal.get() == null ? 0 : transThreadLocal.get().size();
		logger.info("In beginTransaction()... level = " + level + ", Calling class: "
				+ ExceptionUtil.findCallingClass(new Exception()));
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("service_"+ TX_COUNTER.get().incrementAndGet());
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		PlatformTransactionManager txmgr = null; 
		try {
			txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("transactionManager");
		}
		catch (NoSuchBeanDefinitionException e) {
			logger.error("NoSuchBeanDefinitionException caught: " + e.getMessage());
			txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("msgTransactionManager");
		}
		TransactionStatus status = txmgr.getTransaction(def);
		TransTuple tuple = new TransTuple(txmgr, status);
		if (transThreadLocal.get() == null) {
			transThreadLocal.set(new Stack<TransTuple>());
		}
		transThreadLocal.get().push(tuple);
	}

	public static void commitTransaction() {
		int level = transThreadLocal.get() == null ? 0 : transThreadLocal.get().size() - 1;
		logger.info("In commitTransaction()... level = " + level + ", Calling class: "
				+ ExceptionUtil.findCallingClass(new Exception()));
		if (transThreadLocal.get() == null || transThreadLocal.get().isEmpty()) {
			throw new IllegalStateException("No transaction is in progress.");
		}
		TransTuple tuple = transThreadLocal.get().pop();
		try {
			if (!tuple.status.isCompleted()) {
				tuple.txmgr.commit(tuple.status);
			}
		}
		finally {
		}
	}

	public static void rollbackTransaction() {
		int level = transThreadLocal.get() == null ? 0 : transThreadLocal.get().size() - 1;
		logger.info("In rollbackTransaction()... level = " + level + ", Calling class: "
				+ ExceptionUtil.findCallingClass(new Exception()));
		if (transThreadLocal.get() == null || transThreadLocal.get().isEmpty()) {
			throw new IllegalStateException("No transaction is in progress.");
		}
		TransTuple tuple = transThreadLocal.get().pop();
		try {
			if (!tuple.status.isCompleted()) {
				tuple.txmgr.rollback(tuple.status);
			}
		}
		finally {
		}
	}

	public static void clearTransaction() {
		int level = transThreadLocal.get() == null ? 0 : transThreadLocal.get().size() - 1;
		logger.info("In clearTransaction()... level = " + level + ", Calling class: "
				+ ExceptionUtil.findCallingClass(new Exception()));
		if (transThreadLocal.get() != null && !transThreadLocal.get().isEmpty()) {
			TransTuple tuple = transThreadLocal.get().pop();
			if (tuple.status != null) {
				tuple.status.setRollbackOnly();
			}
			transThreadLocal.get().clear();
		}
		if (transThreadLocal.get() != null) {
			transThreadLocal.remove();
		}
	}

	/*
	 * XXX DO NOT modify this method, unless you know what you are doing. 
	 */
	public static boolean isInTransaction() {
		boolean isInTran = TransactionSynchronizationManager.isActualTransactionActive();
		return isInTran;
	}

	private static class TransTuple {
		final PlatformTransactionManager txmgr;
		final TransactionStatus status;
		
		TransTuple(@NotNull PlatformTransactionManager txmgr, @NotNull TransactionStatus status) {
			this.txmgr = txmgr;
			this.status = status;
		}
	}

	private static final ThreadLocal<AtomicInteger> TX_COUNTER = new ThreadLocal<AtomicInteger>() {
		public AtomicInteger initialValue() {
			return new AtomicInteger(1);
		}
	};
	
	public static boolean isRunningInJunitTest() {
		StackTraceElement[] traces = Thread.currentThread().getStackTrace();
		for (int i = traces.length - 1; i > 0; i--) {
			StackTraceElement trace = traces[i];
			if (StringUtils.startsWith(trace.getClassName(), "org.junit.runners")) {
				// org.junit.runners.ParentRunner.run(ParentRunner.java:363)
				return true;
			}
			else if (StringUtils.contains(trace.getClassName(), "junit.runner")) {
				// org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:192)
				return true;
			}
		}
		return false;
	}


	public static void main(String[] args) {
		logger.info("AppContext: " + getAppContext());
		try {
			Thread.sleep(10 * 1000);
		}
		catch (InterruptedException e) {}
		
		shutDownConfigContexts();
	}
}
