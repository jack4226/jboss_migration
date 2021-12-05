package jpa.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DB2DateUtil {
	protected static Logger logger = LogManager.getLogger(DB2DateUtil.class);
	static boolean isDebugEnabled = logger.isDebugEnabled();
	static boolean isInfoEnabled = logger.isInfoEnabled();
	
	/**
	 * The actual format is like 2003-04-03-09:04:47.000562
	 */
	public static final int DB2_3RD_DASH = 1;
	
	/**
	 * The actual format is like 2007-01-26-04.17.35.454335
	 */
	public static final int DB2_DASH_DOTS = 2;
	
	/**
	 * The actual format is like 2003-04-03.09.04.47.000562
	 */
	public static final int DB2_ALL_DOTS = 3;
	
	/**
	 * The actual format is like 2003-11-10 14:03:04.098809 from db2 database time stamp
	 */
	public static final int DB2 = 10;
	
	public static void main(String[] args) {
		String oldDate = "2006-12-15-12:56:18.095856";
		oldDate = "2003-04-03.09.04.47.123562";
		oldDate = "2003/04/03.09.04.47:123562";
		String newDate = correctDB2Date(oldDate);
		logger.info(oldDate);
		logger.info(newDate);
		
		logger.info(isValidBirthDate("12/25/1980"));

		String dateStr = "20001012";
		try {
			String converted = convertDateString(dateStr,"yyyyMMdd","MM/dd/yyyy");
			logger.info("From "+dateStr+" -> "+converted);
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}

		System.exit(0);
	}
	
	/**
	 * Convert date from "yyyy-MM-dd-hh.mm.ss.SSSSSS" to "yyyy-MM-dd HH:mm:ss.SSSSSS".
	 * 
	 * @param dateStr
	 * @return converted date in string.
	 */
	public static String correctDB2Date(String dateStr) {
		if (dateStr!=null) {
			if(validateDate(dateStr, DB2_DASH_DOTS) || validateDate(dateStr, DB2_ALL_DOTS)) {
				return (dateStr.substring(0, 10) + " " + dateStr.substring(11, 13)
					+ ":" + dateStr.substring(14, 16) + ":" + dateStr.substring(17));
			}
			else if (validateDate(dateStr, DB2_3RD_DASH)) {
				return dateStr.substring(0, 10) + " " + dateStr.substring(11);
			}
			else if (validateDate(dateStr, DB2)) {
				return dateStr;
			}
		}
		logger.warn("Received a non-standard DB2 Timestamp: " + dateStr);
		return correctDB2Date2(dateStr);
	}

	private static String db2Regex = "^(\\d{4})([ -\\/])(\\d{2})(\\2)(\\d{2})([ -:.])(\\d{2})([ -:.])(\\d{2})(\\8)(\\d{2})([:.])(\\d{6})$";
	private static Pattern db2Pattern = Pattern.compile(db2Regex);

	/*
	 * provide a method that is more tolerant to the format of the date.
	 * For example it will accept:
	 * 	"yyyy MM dd-hh.mm.ss:SSSSSS"
	 * 	"yyyy/MM/dd:hh.mm.ss.SSSSSS"
	 * 	"yyyy-MM-dd.hh:mm:ss.SSSSSS"
	 */
	private static String correctDB2Date2(String dateStr) {
		if (dateStr != null) {
			Matcher m = db2Pattern.matcher(dateStr);
			if (m.find() && m.groupCount() == 13) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i <= m.groupCount(); i++) {
					if (isDebugEnabled) {
						//logger.debug("[" + i + "]: " + m.group(i));
					}
					if (i == 1 || i == 3) {
						sb.append(m.group(i) + "-");
					}
					else if (i == 5) {
						sb.append(m.group(i) + " ");
					}
					else if (i == 7 || i == 9) {
						sb.append(m.group(i) + ":");
					}
					else if (i == 11) {
						sb.append(m.group(i) + ".");
					}
					else if (i == 13) {
						sb.append(m.group(i));
					}
				}
				return sb.toString();
			}
		}
		logger.error("Received a non DB2 Timestamp: " + dateStr);
		return dateStr;
	}

	/**
	 * check if this is a valid db2 date
	 * @param dateStr
	 * @return true if yes
	 */
	public static boolean isValidDB2Date(String dateStr) {
		if (StringUtils.isNotBlank(dateStr)) {
			if(validateDate(dateStr, DB2) || validateDate(dateStr, DB2_DASH_DOTS)) {
				return true;
			}
			else if (validateDate(dateStr, DB2_3RD_DASH) || validateDate(dateStr, DB2_ALL_DOTS)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * validate a birth date against format: "yyyy-MM-dd" or "MM/dd/yyyy"
	 * @param dateStr
	 * @return true if valid
	 */
	public static boolean isValidBirthDate(String dateStr) {
		if (dateStr != null) {
			if (validateBirthDate(dateStr, "yyyy-MM-dd")) {
				return true;
			}
			else if (validateBirthDate(dateStr, "MM/dd/yyyy")) {
				return true;
			}
		}
		return false;
	}
	
	
	/*
	 * Validate a date string with specified format pattern	
	 * 
	 * @param pattern must be one of format pattern constants 
	 * and match the @param date format
	 */
	private static boolean validateDate(String dateStr, int pattern) {
		
		SimpleDateFormat format = getSimpleDateFormat(pattern);
		format.setLenient(true);
		
		try {
			format.parse(dateStr);
			return true;
		}
		catch (ParseException pe) {
			logger.warn("ParseException caught: " + pe.getMessage());
			return false;
		}
	}
	
	/*
	 * Validate a date string with specified format pattern	
	 * 
	 * @param pattern must be one of format pattern constants 
	 * and match the @param date format
	 */
	private static boolean validateBirthDate(String dateStr, String pattern) {
		
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		format.setLenient(true);
		
		try {
			format.parse(dateStr);
			return true;
		}
		catch (ParseException pe) {
			logger.warn("ParseException caught: " + pe.getMessage());
			return false;
		}
	}
	
	/*
	 * Return a SimpleDateFormat object with specified format pattern
	 * @param pattern must be one of format pattern constants
	 */
	private static SimpleDateFormat getSimpleDateFormat(int pattern) {
		
		SimpleDateFormat sdf = new SimpleDateFormat();
		
		switch (pattern) {
			case DB2_3RD_DASH:
				sdf.applyPattern("yyyy-MM-dd-HH:mm:ss.SSSSSS");
				break;
			case DB2_DASH_DOTS:
				sdf.applyPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");
				break;
			case DB2_ALL_DOTS:
				sdf.applyPattern("yyyy-MM-dd.HH.mm.ss.SSSSSS");
				break;
			case DB2:
				sdf.applyPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
				break;
			default:
				// default to original pattern
				break;
		}
		
		return sdf;
	}

	/**
	 * convert date string from one format to another format
	 * @param dateStr - original date string
	 * @param fromPattern - format of original date string
	 * @param toPattern - format of the returned date string
	 * @return converted date string 
	 * @throws ParseException if any date parse error
	 */
	public static String convertDateString(String dateStr, String fromPattern, String toPattern)
		throws ParseException, IllegalArgumentException {
		
		SimpleDateFormat formatter = new SimpleDateFormat(fromPattern);
		Date date = formatter.parse(dateStr);
		
		SimpleDateFormat toFormatter = new SimpleDateFormat(toPattern);
		String newDateStr = toFormatter.format(date);
		
		return newDateStr;
	}
	
	/**
	 * convert date string from one format to another format
	 * @param dateStr - original date string
	 * @param fromPattern - format of original date string
	 * @param toPattern - format of the returned date string
	 * @return converted date string, or the original date string if any error
	 */
	public static String reformatDateString(String dateStr, String fromPattern, String toPattern) {
		
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(fromPattern);
			Date date = formatter.parse(dateStr);
			
			SimpleDateFormat toFormatter = new SimpleDateFormat(toPattern);
			String newDateStr = toFormatter.format(date);
			
			return newDateStr;
		}
		catch (ParseException | IllegalArgumentException e) {
			logger.error(e.getClass().getSimpleName() + " caught", e);
			return dateStr;
		}
	}
	
	/**
	 * Returns a date as a formatted string
	 * @param date - original date
	 * @param pattern - format to be returned
	 * @return formatted date string
	 * @throws ParseException if any error
	 */
	public static String formatDate(Date date, String pattern) throws IllegalArgumentException {
		
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String dateStr = formatter.format(date);
		
		return dateStr;
	}
}
