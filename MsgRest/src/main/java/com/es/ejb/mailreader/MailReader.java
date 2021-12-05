package com.es.ejb.mailreader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.EJBException;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Schedule;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import jpa.model.MailInbox;
import jpa.service.msgin.MailInboxService;
import jpa.service.msgin.MailReaderBo;
import jpa.spring.util.SpringUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Session Bean implementation class MailReader
 */
/*
 * Use Startup annotation to ensures that bean is loaded when server is started
 * so that life cycle callback methods (@PostConstruct) will be invoked.
 */
@Startup
@Singleton(name="MailReader",mappedName="ejb/MailReader")
@Lock(LockType.READ) // allows timers to execute in parallel
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
public class MailReader {
	protected static final Logger logger = LogManager.getLogger(MailReader.class);
	@Resource
	private SessionContext context;
	
	private int readMailInterval = 60 * 60; // 60 minutes
//	@Resource
//	private TimerService timerService;

	private final MailInboxService mailBoxSvc;
	private final ExecutorService pool;
	final int MAX_POOL_SIZE = 4;
	private int MINIMUM_WAIT = 5; // seconds
	private int INTERVAL = 10;
	
	private final List<MailReaderBo> readers = new ArrayList<MailReaderBo>();
	
	final TimerConfig mailReaderTC = new TimerConfig("mailReaderTC", false);

    /**
     * Default constructor. 
     */
    public MailReader() {
		mailBoxSvc =SpringUtil.getAppContext().getBean(MailInboxService.class);
		pool = Executors.newFixedThreadPool(MAX_POOL_SIZE);
		//pool = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
		//pool = Executors.newCachedThreadPool();
    }

    @PostConstruct
	public void startUp() {
		logger.info("Entering startUp() method, starting Mail Readers...");
		startMailReader(readMailInterval);
	}

	public void startMailReader(int interval) {
 		stopMailReader(); // stop pending timers
		readers.clear();
		readers.addAll(getMailReaders());
		// at least 5 seconds
		INTERVAL = interval < MINIMUM_WAIT ? MINIMUM_WAIT : interval;
		context.getTimerService().createSingleActionTimer(TimeUnit.SECONDS.toMillis(INTERVAL), mailReaderTC);
		logger.info("startMailReader(): MailReader Timer created to expire after " + INTERVAL
				+ " seconds.");
		// create timer to purge aged records from MSGIDDUP table.
//		context.getTimerService().createTimer(60 * 60 * 1000, "duplicateCheck");
//		logger.info("startMailReader(): duplicateCheck Timer created to expire after 1 hour.");
//		
		final TimerConfig duplicateCheck = new TimerConfig("duplicateCheck", false);
		context.getTimerService().createIntervalTimer(TimeUnit.SECONDS.toMillis(60), TimeUnit.MINUTES.toMillis(60), duplicateCheck);
		logger.info("startMailReader(): DuplicateCheck Interval Timer created to expire every 1 hour.");
	}

	private List<MailReaderBo> getMailReaders() {
		List<MailInbox> mailBoxVos = mailBoxSvc.getAll(true); // get all mailboxes
		List<MailReaderBo> readerList = new ArrayList<MailReaderBo>();
		if (mailBoxVos == null || mailBoxVos.size() == 0) {
			logger.warn("getMailReaders(): no mailbox found, exit.");
			return readerList;
		}
		for (int i = 0; i < mailBoxVos.size(); i++) {
			MailInbox mailBoxVo = mailBoxVos.get(i);
			mailBoxVo.setFromTimer(true);
			MailReaderBo reader = new MailReaderBo(mailBoxVo);
			readerList.add(reader);
			logger.info("getMailReaders(): loaded MailReaderBo: " + mailBoxVo.getMailInboxPK().getUserId() + "/"
					+ mailBoxVo.getMailInboxPK().getHostName());
		}
		return readerList;
	}

	public int getInterval() {
		return INTERVAL;
	}
	
	public void stopMailReader() {
		TimerService timerService = context.getTimerService();
		Collection<?> timers = timerService.getTimers();
		if (timers != null) {
			for (Iterator<?> it=timers.iterator(); it.hasNext(); ) {
				Timer timer = (Timer)it.next();
				stopTimer(timer);
			}
		}
	}

	private void stopTimer(Timer timer) {
		if (timer != null) {
			try {
				Object info = timer.getInfo();
				timer.cancel();
				logger.info("stopTimer(): timer stopped : " + info);
			}
			catch (NoSuchObjectLocalException e) {
				logger.error("NoSuchObjectLocalException caught", e);
			}
			catch (IllegalStateException e) {
				logger.error("IllegalStateException caught", e);
			}
		}
	}

	private java.util.Date lastUpdtTime = new java.util.Date();
	
	@Timeout
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void programmaticTimeout(Timer timer) {
		if (readers.isEmpty()) {
			logger.warn("########## Mail Reader list is empty ##########");
			return;
		}
		logger.info("programmaticTimeout() - " + timer.getInfo());
		if ("duplicateCheck".equals(timer.getInfo())) {
			// TODO purge aged records
			return;
		}
		// MailReader
		Future<?>[] futures = new Future[readers.size()];
		for (int i = 0; i < futures.length; i++) {
			MailReaderBo reader = readers.get(i);
			try {
				futures[i] = pool.submit(reader);
			}
			catch (Exception e) {
				logger.fatal("IOException caught", e);
				throw new EJBException(e.getMessage());
			}
			try { // give each thread some time to make initial connection
				Thread.sleep(500);
			}
			catch (InterruptedException e) {}
		}
		// wait for all threads to complete
		for (int i = 0; i < futures.length; i++) {
			Future<?> future = futures[i];
			try {
				future.get();
			}
			catch (InterruptedException e) {
				logger.error("InterruptedException caught: " + e.getMessage());
			}
			catch (ExecutionException e) {
				logger.error("ExecutionException caught", e);
			}
		}
		java.util.Date currTime = new java.util.Date();
		if ((currTime.getTime() - lastUpdtTime.getTime()) > (TimeUnit.MINUTES.toMillis(15))) {
			// reload mailboxes every 15 minutes
			readers.clear();
			readers.addAll(getMailReaders());
			lastUpdtTime = currTime;
		}
		context.getTimerService().createSingleActionTimer(TimeUnit.SECONDS.toMillis(INTERVAL), mailReaderTC);
	}
	
	/*
	 * XXX NOT working with JBoss 7.1.
	 */
	@Schedule(second="0/30", info="Single Timer") // expire once on the next 30th second.
	public void automaticTimeout(Timer timer) {
		logger.info("Automatic timeout occured : "  + timer.getInfo());
		startMailReader(60);
	}

	@PreDestroy
	public void shutdownThreadPool() {
		if (pool != null) {
			pool.shutdown();
		}
		try {
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				pool.shutdownNow();
			}
		}
		catch (InterruptedException e) {
			logger.error("InterruptedException caught: " + e.getMessage());
		}
		readers.clear();
	}

}
