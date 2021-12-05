package jpa.util;

import java.util.Calendar;
import java.util.Date;

import jpa.service.common.SenderDataService;
import jpa.spring.util.SpringUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SenderUtil {
	static final Logger logger = LogManager.getLogger(SenderUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private static SenderDataService senderService = null;
	private static final int TRIAL_DAYS = 30;
	
	private SenderUtil() {
		// static only
	}
	
	public static void main(String[] args){
		try {
			logger.info("Trial Ended? " + isTrialPeriodEnded());
			logger.info("ProductKey Valid? " + isProductKeyValid());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static SenderDataService getSenderDataService() {
		if(senderService == null) {
			senderService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		}
		return senderService;
	}

	public static boolean isTrialPeriodEnded() {
		String systemId = getSenderDataService().getSystemId();
		if (systemId != null) {
			String db2ts = null;
			try {
				db2ts = TimestampUtil.decimalStringToDb2(systemId);
			}
			catch (NumberFormatException e) {
				logger.error("Failed to convert the SystemId: " + systemId, e);
				return true;
			}
			Date date = null;
			try {
				java.sql.Timestamp tms = TimestampUtil.db2ToTimestamp(db2ts);
				date = new Date(tms.getTime());
			}
			catch (NumberFormatException e) {
				logger.error("Failed to parse the timestamp: " + db2ts, e);
				return true;
			}
			Calendar now = Calendar.getInstance();
			Calendar exp = Calendar.getInstance();
			exp.setTime(date);
			exp.add(Calendar.DAY_OF_YEAR, TRIAL_DAYS);
			if (now.before(exp)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check product key
	 * @return true if either productkey.txt or senders.SystemKey is valid.
	 */
	public static boolean isProductKeyValid() {
		String key = getSenderDataService().getSystemKey();
		return (ProductKey.validateKey(key) | ProductUtil.isProductKeyValid());
	}

}
