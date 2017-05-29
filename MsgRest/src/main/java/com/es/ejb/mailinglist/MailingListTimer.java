package com.es.ejb.mailinglist;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
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

import jpa.model.EmailTemplate;
import jpa.model.SchedulesBlob;
import jpa.model.SchedulesBlob.DateWrapper;
import jpa.service.common.EmailTemplateService;
import jpa.spring.util.SpringUtil;
import jpa.util.BlobUtil;

import org.apache.log4j.Logger;

/**
 * Session Bean implementation class MailingListTimer
 */
@Startup
@Singleton(name = "MailingListTimer", mappedName = "ejb/MailingListTimer")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
public class MailingListTimer {
	protected static final Logger logger = Logger.getLogger(MailingListTimer.class);
	@Resource
	SessionContext context;
	private EmailTemplateService emailTemplateDao = null;
	
	static int WEEKLY = 0;
	static int BIWEEKLY = 1;
	static int MONTHLY = 2;
    /**
     * Default constructor. 
     */
    public MailingListTimer() {
    	emailTemplateDao = SpringUtil.getAppContext().getBean(EmailTemplateService.class);
    }

    @PostConstruct
	public void startUp() {
		logger.info("Entering startUp() method, starting Mailing List Timers...");
		scheduleTimerTasks();
    }
    
	public void scheduleTimerTasks() {
		logger.info("Entering scheduleTimerTasks()...");
		// first cancel existing timers as JBoss keeps timers after reboot
		stopTimers();
		// schedule timer tasks
		TimerService timerService = context.getTimerService();
		List<EmailTemplate> templates = emailTemplateDao.getAll();
		for (Iterator<EmailTemplate> it=templates.iterator(); it.hasNext(); ) {
			EmailTemplate vo = it.next();
			SchedulesBlob blob = vo.getSchedulesBlob();
			if (blob == null) {
				logger.info("scheduleTimerTasks() - SchedulesBlob is null for templateId: "
						+ vo.getTemplateId());
				continue;
			}
			blob.setTemplateId(vo.getTemplateId());
			// schedule weekly tasks
			if (blob.getWeekly() != null) {
				for (int i = 0; i < blob.getWeekly().length; i++) {
					Calendar cal = getCalendar(blob);
					setDayOfWeek(cal, Integer.parseInt(blob.getWeekly()[i]), blob.getStartHour());
				    SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
				    clone.setTimerEvent(SchedulesBlob.Events.WEEKLY);
				    timerService.createTimer(cal.getTime(), 1000 * 60 * 60 * 24 * 7, clone);
				    logger.info("Added Timer for " + clone.getTemplateId() + " "
							+ clone.getTimerEvent().toString() + " day " + clone.getWeekly()[i]);
				}
			}
			// schedule biweekly tasks
			if (blob.getBiweekly() != null) {
				for (int i = 0; i < blob.getBiweekly().length; i++) {
					Calendar cal = getCalendar(blob);
					setDayOfWeek(cal, Integer.parseInt(blob.getBiweekly()[i]), blob.getStartHour());
				    SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
				    clone.setTimerEvent(SchedulesBlob.Events.BIWEEKLY);
				    timerService.createTimer(cal.getTime(), 1000 * 60 * 60 * 24 * 7 * 2, clone);
				    logger.info("Added Timer for " + clone.getTemplateId() + " "
							+ clone.getTimerEvent().toString() + " day " + clone.getBiweekly()[i]);
				}
			}
			// schedule monthly tasks
			if (blob.getMonthly() != null) {
				for (int i = 0; i < blob.getMonthly().length; i++) {
					Calendar cal = getCalendar(blob);
					setDayOfMonth(cal, Integer.parseInt(blob.getMonthly()[i]), blob.getStartHour());
				    SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
				    clone.setTimerEvent(SchedulesBlob.Events.MONTHLY);
				    timerService.createTimer(cal.getTime(), clone);
				    logger.info("Added Timer for " + clone.getTemplateId() + " "
							+ clone.getTimerEvent().toString() + " day " + clone.getMonthly()[i]);
				}
			}
			// schedule end of month tasks
			if (blob.getEndOfMonth()) {
				Calendar cal = getCalendar(blob);
				int dayOfMonth = setMaxDayOfMonth(cal, blob.getStartHour(), -0);
			    SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
			    clone.setTimerEvent(SchedulesBlob.Events.END_OF_MONTH);
			    timerService.createTimer(cal.getTime(), clone);
			    logger.info("Added Timer for " + clone.getTemplateId() + " "
						+ clone.getTimerEvent().toString() + " day " + dayOfMonth);
			}
			// schedule end of month minus 1 day tasks
			if (blob.getEomMinus1Day()) {
				Calendar cal = getCalendar(blob);
				int dayOfMonth = setMaxDayOfMonth(cal, blob.getStartHour(), -1);
			    SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
			    clone.setTimerEvent(SchedulesBlob.Events.EOM_MINUS_1DAY);
			    timerService.createTimer(cal.getTime(), clone);
			    logger.info("Added Timer for " + clone.getTemplateId() + " "
						+ clone.getTimerEvent().toString() + " day " + dayOfMonth);
			}
			// schedule end of month minus 2 day tasks
			if (blob.getEomMinus2Day()) {
				Calendar cal = getCalendar(blob);
				int dayOfMonth = setMaxDayOfMonth(cal, blob.getStartHour(), -2);
			    SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
			    clone.setTimerEvent(SchedulesBlob.Events.EOM_MINUS_2DAY);
			    timerService.createTimer(cal.getTime(), clone);
			    logger.info("Added Timer for " + clone.getTemplateId() + " "
						+ clone.getTimerEvent().toString() + " day " + dayOfMonth);
			}
			// schedule dates from the list
			for (int i = 0; i < blob.getDateList().length; i++) {
				DateWrapper scheduled = (DateWrapper) blob.getDateList()[i];
				if (scheduled != null && scheduled.getDate() != null) {
					Calendar scheduledTime = Calendar.getInstance();
					scheduledTime.setTime(scheduled.getDate());
					setHoursMinutes(scheduledTime, blob);
					Calendar currTime = Calendar.getInstance();
					if (scheduledTime.compareTo(currTime) > 0) {
					    SchedulesBlob clone = (SchedulesBlob) BlobUtil.deepCopy(blob);
					    timerService.createTimer(scheduledTime.getTime(), clone);
					    clone.setTimerEvent(SchedulesBlob.Events.DATE_LIST);
						logger.info("Added Timer for " + clone.getTemplateId() + " "
								+ clone.getTimerEvent().toString() + " date: "
								+ scheduledTime.getTime());
					}
					else {
						logger.warn(SchedulesBlob.Events.DATE_LIST.toString() + " - timer["
								+ (i + 1) + "] for \"" + blob.getTemplateId() + "\" has expired: "
								+ scheduledTime.getTime() + ", timer ignored.");
					}
				}
			}
		}
	}

	public void scheduleSingleTask(String templateId, int delay) {
		TimerConfig nextTask = new TimerConfig(templateId, false);
		context.getTimerService().createSingleActionTimer(TimeUnit.SECONDS.toMillis(delay), nextTask);
		logger.info("scheduleSingleTask(): MailingList Timer created to expire after " + delay + " seconds.");
	}

	public void stopTimers() {
		TimerService timerService = context.getTimerService();
		Collection<?> timers = timerService.getTimers();
		if (timers == null) return;
		for (Iterator<?> it=timers.iterator(); it.hasNext(); ) {
			Timer timer = (Timer)it.next();
			stopTimer(timer);
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
	
	private Calendar getCalendar(SchedulesBlob blob) {
		Calendar cal = Calendar.getInstance();
		setHoursMinutes(cal, blob);
	    return cal;
	}
	
	private void setHoursMinutes(Calendar cal, SchedulesBlob blob) {
		cal.set(Calendar.HOUR_OF_DAY, blob.getStartHour());
	    cal.set(Calendar.MINUTE, blob.getStartMinute());
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	}
	
	private void setDayOfWeek(Calendar cal, int dayOfWeek, int hourOfDay) {
		int calDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int calHourOfDay = currentHour();
		if (dayOfWeek < calDayOfWeek || (dayOfWeek == calDayOfWeek && hourOfDay <= calHourOfDay)) {
			cal.add(Calendar.DATE, 7);
			logger.info("setDayOfWeek() - rolled calendar forward a week: " + cal.getTime());
		}
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		logger.info("setDayOfWeek() - Timer to first expire at: " + cal.getTime());
	}

	private void setDayOfMonth(Calendar cal, int dayOfMonth, int hourOfDay) {
		int calDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		int calHourOfDay = currentHour();
		if (dayOfMonth < calDayOfMonth
				|| (dayOfMonth == calDayOfMonth && hourOfDay <= calHourOfDay)) {
			cal.add(Calendar.MONTH, 1);
			logger.info("setDayOfMonth() - rolled calendar forward a month: " + cal.getTime());
		}
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		logger.info("setDayOfMonth() - Timer to first expire at: " + cal.getTime());
	}

	private int setMaxDayOfMonth(Calendar cal, int hourOfDay, int minusDays) {
		int calDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		int calHourOfDay = currentHour();
		int dayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) + minusDays;
		if (dayOfMonth < calDayOfMonth
				|| (dayOfMonth == calDayOfMonth && hourOfDay <= calHourOfDay)) {
			cal.add(Calendar.MONTH, 1);
			dayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) + minusDays;
			logger.info("setMaxDayOfMonth() - rolled calendar forward a month: " + cal.getTime());
		}
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		logger.info("setMaxDayOfMonth() - Timer to first expire at: " + cal.getTime());
		return dayOfMonth;
	}

	public int currentHour() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	@EJB(name="mailingList", beanInterface=MailingListLocal.class)
	private MailingListLocal mailingList;

	@Timeout
	public void ejbTimeout(Timer timer) {
		logger.info("Entering ejbTimeout: " + timer.getInfo());
		if (timer.getInfo() instanceof String) {
			// call MailingList EJB
			try {
				logger.info("ejbTimeout() - invoking broadcaseBo for templateId: " + timer.getInfo());
				mailingList.broadcast((String) timer.getInfo());
			}
			catch (Exception e) {
				throw new EJBException("Exception caught", e);
			}
			return;
		}
		if (!(timer.getInfo() instanceof SchedulesBlob)) {
			logger.warn("Unknown timer trigger, exit.");
			return;
		}
		SchedulesBlob blob = (SchedulesBlob) timer.getInfo();
		if (blob == null) {
			logger.error("ejbTimeout() -  timer.getInfo() returned null.");
			return;
		}
		logger.info("ejbTimeout() - Timer expired for " + blob.getTemplateId() + " "
				+ blob.getTimerEvent());
		TimerService timerService = context.getTimerService();
		Calendar cal = getCalendar(blob);
		if (SchedulesBlob.Events.MONTHLY.equals(blob.getTimerEvent())) {
			cal.add(Calendar.MONTH, 1);
			timerService.createTimer(cal.getTime(), blob);
		}
		else if (SchedulesBlob.Events.END_OF_MONTH.equals(blob.getTimerEvent())) {
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			timerService.createTimer(cal.getTime(), blob);
		}
		else if (SchedulesBlob.Events.EOM_MINUS_1DAY.equals(blob.getTimerEvent())) {
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH - 1));
			timerService.createTimer(cal.getTime(), blob);
		}
		else if (SchedulesBlob.Events.EOM_MINUS_2DAY.equals(blob.getTimerEvent())) {
			cal.add(Calendar.MONTH, 1);
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH - 2));
			timerService.createTimer(cal.getTime(), blob);
		}
		// call MailingList EJB
		try {
			logger.info("ejbTimeout() - invoking broadcaseBo for templateId: "
					+ blob.getTemplateId());
			mailingList.broadcast(blob.getTemplateId());
		}
		catch (Exception e) {
			throw new EJBException("Exception caught", e);
		}
	}
}
