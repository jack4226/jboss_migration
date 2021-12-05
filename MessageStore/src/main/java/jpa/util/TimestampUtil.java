package jpa.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class contains some useful time stamp routines. Like converting db2 time
 * stamp to decimal string, generating db2 time stamp from a java Date object,
 * and converting time stamp format between db2 and oracle, etc.
 */
public class TimestampUtil implements java.io.Serializable {
	private static final long serialVersionUID = -6023017890883430172L;
	protected static final Logger logger = LogManager.getLogger(TimestampUtil.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	private static final int RADIX = 36;
	private static final String db2FormatPart1 = "yyyy-MM-dd-HH.mm.ss";
	private static SimpleDateFormat db2SdfPart1 = new SimpleDateFormat(db2FormatPart1);

	public static void main(String argv[]) {
		String db2tm = "1582-10-23-00.48.04.702003";
		db2tm = "0697-10-13-22.29.59.972003";
		db2tm = getCurrentDb2Tms();
		String converted = db2ToDecimalString(db2tm);
		//converted = convert_old(db2tm);
		String restored = decimalStringToDb2(converted);
		logger.info("Date: " + db2tm + ", converted: " + converted + ", restored: " + restored);
		System.err.println("Is conversion a success? " + (db2tm.equals(restored)));
		logger.info("Is (" + restored + ") valid? " + isValidDb2Timestamp(restored));
		db2tm = getCurrentDb2Tms();
		logger.info(db2tm);
		logger.info(fillWithTrailingZeros(db2ToTimestamp(db2tm).toString(),26));
		// perform some brutal force tests
		java.util.Random random = new java.util.Random(System.currentTimeMillis());
		for (int i=0; i<20000; i++) {
			db2tm = getCurrentDb2Tms();
			db2tm = db2tm.substring(0,20) + fillWithLeadingZeros(random.nextInt(999999), 6);
			converted = db2ToDecimalString(db2tm);
			restored = decimalStringToDb2(converted);
			if (!db2tm.equals(restored)) {
				System.err.println("Failed to restore Email_Id: " + db2tm + " - " + restored);
			}
			String db2tm_1 = DB2DateUtil.correctDB2Date(db2tm); // to "yyyy-MM-dd HH:mm:ss.SSSSSS"
			String db2tm_2 = fillWithTrailingZeros(db2ToTimestamp(db2tm).toString(),26);
			if (!db2tm_1.equals(db2tm_2)) {
				System.err.println("Failed to convert to sql Timestamp: " + db2tm_1 + " - " + db2tm_2);
			}
		}
	}

	//
	// ================= Static methods ===================
	//

	/** convert a db2 time stamp to a decimal string */
	public static String db2ToDecimalString(String ts) throws NumberFormatException {
		// convert a db2 time stamp to a decimal string
		return convert(ts);
	}

	/** convert a decimal string to a db2 time stamp */
	public static String decimalStringToDb2(String st) throws NumberFormatException {
		// convert a decimal string to a db2 time stamp
		long tm;
		long millis = 0;

		// remove possible CR/LF, tabs, and blanks, that are inserted by some
		// Email servers, from bounced e-mails (MS exchange server for one).
		// MS exchange server inserted \r\n\t into the Email_ID string, and it
		// caused "check digit test" error.
		StringTokenizer tokens = new StringTokenizer(st, "\r\n\t ");
		StringBuffer sb = new StringBuffer();
		while (tokens.hasMoreTokens()) {
			sb.append(tokens.nextToken());
		}
		st = sb.toString();

		String db2ts = null;
		int plusPos = st.indexOf("+");
		if (plusPos > 0) {
			// converted by old method, string contains "+", example: 130da3bfd+75c29
			tm = Long.parseLong(st.substring(0, plusPos), RADIX);
			millis = Long.parseLong(st.substring(plusPos + 1), RADIX);

			String dateString = db2SdfPart1.format(new Date(tm));

			String ssssss;
			if (Long.toString(millis).length() >= 6) {
				// current year is prepended to the milliseconds, remove it.
				ssssss = fillWithLeadingZeros(Long.toString(millis).substring(4), 6);
			}
			else {
				// make it compatible to old hex string.
				ssssss = fillWithLeadingZeros(Long.toString(millis), 6);
			}
			db2ts = dateString + "." + ssssss;
		}
		else { // new Email_ID format received, example: 1234567890123456789
			db2ts = restore(st);
		}

		if (!isValidDb2Timestamp(db2ts)) {
			throw new NumberFormatException("Converted <" + st + "> to an invalid DB2 Timestamp <"
					+ db2ts + ">");
		}
		return db2ts;
	}

	/**
	 * create a db2 time stamp using current system time, and system nanoseconds.
	 */
	public static String getCurrentDb2Tms() {
		return timestampToDb2(null);
	}

	/**
	 * convert a db2 time stamp to a Timestamp object.
	 */
	public static java.sql.Timestamp db2ToTimestamp(String ts) throws NumberFormatException {
		if (ts == null || ts.length()!=26) {
			throw new NumberFormatException("Invalid format of DB2 date received (" + ts + ")");
		}
		Date date = null;
		try {
			date = db2SdfPart1.parse(ts.substring(0,db2FormatPart1.length()));
		}
		catch (ParseException e) {
			logger.error("ParseException caught", e);
			throw new NumberFormatException("ParseException caught - " + e.getMessage());
		}
		java.sql.Timestamp tms = new java.sql.Timestamp(date.getTime());
		tms.setNanos(Integer.parseInt(ts.substring(db2FormatPart1.length()+1)) * 1000);
		return tms;
	}

	public static String timestampToDb2(java.sql.Timestamp tms) {
		if (tms == null) {
			tms = new java.sql.Timestamp(System.currentTimeMillis());
			long timeInNanos = System.nanoTime();
			tms.setNanos((int)(timeInNanos % 1000000000));
		}
		String dateString = db2SdfPart1.format(tms);
		return (dateString + "." + fillWithTrailingZeros(tms.getNanos()+"", 6));
	}

	/** return true if db2ts is a valid db2 time stamp, false otherwise. */
	public static boolean isValidDb2Timestamp(String db2ts) {
		if (db2ts == null) {
			return false;
		}
		String db2Regex = "^(\\d{4})([-])(\\d{2})(\\2)(\\d{2})([-])(\\d{2})([.])(\\d{2})(\\8)(\\d{2})([.])(\\d{6})$";
		Pattern db2Pattern = Pattern.compile(db2Regex);
		Matcher m = db2Pattern.matcher(db2ts);
		return m.find();
	}

	/** convert db2 time stamp to oracle time stamp: MM/dd/yyyy HH:mm:ss */
	public static String db2ToOracle(String ts) throws NumberFormatException {
		/*
		 * convert a db2 time stamp to a oracle date string for example:
		 * yyyy-MM-dd-HH.mm.ss.ssssss => MM/dd/yyyy HH:mm:ss
		 */
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		java.sql.Timestamp tms = db2ToTimestamp(ts);
		String dateString = formatter.format(tms);
		return dateString;
	}

	private static String fillWithLeadingZeros(String str, int len) {
		if (str.length() > len) {
			return str.substring(0, len);
		}
		else {
			return StringUtils.leftPad(str, len, '0');
		}
	}

	public static String fillWithLeadingZeros(int num, int len) {
		String str = Integer.valueOf(num).toString();
		return fillWithLeadingZeros(str, len);
	}

	public static String fillWithTrailingZeros(String str, int len) {
		if (str.length()>len) {
			return str.substring(0,len);
		}
		else {
			return StringUtils.rightPad(str, len, '0');
		}
	}

	/* methods added to generate Message Reference Id */

	// convert db2 time stamp to an Email_ID
	private static String convert(String db2ts) throws NumberFormatException {
		StringTokenizer st = new StringTokenizer(db2ts, " -.:");
		int years, months, days, hours, minutes, seconds;
		String nanosStr;

		if (st.countTokens() != 7) {
			throw new NumberFormatException("Invalid Time Stamp: " + db2ts);
		}

		years = Integer.parseInt(st.nextToken());
		months = Integer.parseInt(st.nextToken());
		days = Integer.parseInt(st.nextToken());
		hours = Integer.parseInt(st.nextToken());
		minutes = Integer.parseInt(st.nextToken());
		seconds = Integer.parseInt(st.nextToken());
		nanosStr = st.nextToken();

		myGregCal cal = new myGregCal();
		int[] daysArray = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

		int maxDays;
		if (months == 2 && cal.isLeapYear(years)) {
			maxDays = 29;
		}
		else {
			maxDays = daysArray[months - 1];
		}
		if (months < 1 || months > 12 || days < 1 || days > maxDays || hours > 23 || minutes > 59
				|| seconds > 59 || nanosStr.length() != 6) {
			throw new NumberFormatException("Invalid timestamp: " + db2ts);
		}

		String first3 = nanosStr.substring(0, 3);
		String last3 = nanosStr.substring(3);

		String Millis = fillWithLeadingZeros(years, 4) + fillWithLeadingZeros(months, 2)
				+ fillWithLeadingZeros(days, 2) + fillWithLeadingZeros(hours, 2)
				+ fillWithLeadingZeros(minutes, 2) + fillWithLeadingZeros(seconds, 2)
				+ fillWithLeadingZeros(first3, 3);

		int checkdigit = getCheckDigit(Millis + last3);

		String shuffled = shuffle(Millis + last3, checkdigit, true);

		return shuffled + checkdigit;
	}

	// restore db2 time stamp from an Email_ID
	private static String restore(String refid) throws NumberFormatException {
		//SimpleDateFormat formatter = new SimpleDateFormat(db2FormatPart1 + ".SSS");
		try {
			String oldCheckDigit = refid.substring(refid.length() - 1);
			String tmpstr = refid.substring(0, refid.length() - 1);
			String newRefid = shuffle(tmpstr, Integer.parseInt(oldCheckDigit), false);

			int split = newRefid.length() - 3;

			String Millis = newRefid.substring(0, split);
			String last3 = newRefid.substring(split);

			String newCheckDigit = Integer.valueOf(getCheckDigit(Millis + last3)).toString();
			if (!oldCheckDigit.equals(newCheckDigit)) {
				throw new NumberFormatException("checkdigit inconsistant.");
			}

			if (Millis.length() >= 17) {
				String year = Millis.substring(0, 4);
				String month = Millis.substring(4, 2 + 4);
				String date = Millis.substring(6, 2 + 6);
				String hour = Millis.substring(8, 2 + 8);
				String min = Millis.substring(10, 2 + 10);
				String sec = Millis.substring(12, 2 + 12);
				String mil = Millis.substring(14, 3 + 14);

				String dateStr = year + "-" + month + "-" + date + "-" + hour + "." + min + "."
						+ sec + "." + mil;

				return dateStr + last3;
			}
			else {
				return restore_v0(refid);
			}
		}
		catch (IndexOutOfBoundsException e) {
			logger.error("IndexOutOfBoundsException caught", e);
			throw new NumberFormatException("restore failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw new NumberFormatException("restore failed: " + e.getMessage());
		}
	}

	/*
	 * @deprecated replaced by convert(db2ts).
	 */
	static String convert_v0(String db2ts) throws NumberFormatException {
		StringTokenizer st = new StringTokenizer(db2ts, " -.:");

		long totalSeconds, totalMillis;
		int years, months, days, hours, minutes, seconds;
		String nanosStr;

		if (st.countTokens() != 7) {
			throw new NumberFormatException("Time Stamp format error! " + db2ts);
		}

		years = Integer.parseInt(st.nextToken());
		months = Integer.parseInt(st.nextToken());
		days = Integer.parseInt(st.nextToken());
		hours = Integer.parseInt(st.nextToken());
		minutes = Integer.parseInt(st.nextToken());
		seconds = Integer.parseInt(st.nextToken());
		nanosStr = st.nextToken();

		myGregCal cal = new myGregCal();

		int[] daysArray = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

		int maxDays;
		if (months == 2 && cal.isLeapYear(years)) {
			maxDays = 29;
		}
		else {
			maxDays = daysArray[months - 1];
		}

		if (months < 1 || months > 12 || days < 1 || days > maxDays || hours > 23 || minutes > 59
				|| seconds > 59) {
			throw new NumberFormatException("Invalid timestamp: " + db2ts);
		}
		cal.set(years, --months, days, hours, minutes, seconds);
		cal.set(Calendar.MILLISECOND, 0);

		totalMillis = cal.getTimeInMillis();
		totalSeconds = totalMillis / 1000L;

		String prefix = "0";
		if (totalSeconds < 0) {
			prefix = "1";
		}
		String newSeconds = prefix + Long.valueOf(Math.abs(totalSeconds)).toString();

		int checkdigit = getCheckDigit(newSeconds + nanosStr);

		// logger.info("unshuffled:"+newSeconds+nanosStr+checkdigit);
		String shuffled = shuffle(newSeconds + nanosStr, checkdigit, true);

		return shuffled + checkdigit;
	}

	/*
	 * @deprecated replaced by restore(refid).
	 */
	static String restore_v0(String refid) throws NumberFormatException {
		try {
			String oldCheckDigit = refid.substring(refid.length() - 1);
			String tmpstr = refid.substring(0, refid.length() - 1);
			String newRefid = shuffle(tmpstr, Integer.parseInt(oldCheckDigit), false);

			int split = newRefid.length() - 6;

			String Seconds = newRefid.substring(0, split);
			String Nanos = newRefid.substring(split);

			String newCheckDigit = Integer.valueOf(getCheckDigit(Seconds + Nanos)).toString();
			if (!oldCheckDigit.equals(newCheckDigit)) {
				throw new NumberFormatException("checkdigit inconsistant.");
			}
			long totalSeconds = Long.parseLong(Seconds.substring(1));
			long totalMillis = totalSeconds * 1000L;
			if (Seconds.startsWith("1")) {
				totalMillis *= -1;
			}
			myGregCal cal = new myGregCal();
			cal.setTimeInMillis(totalMillis);

			String dateString = db2SdfPart1.format(cal.getTime());

			return dateString + "." + Nanos;
		}
		catch (IndexOutOfBoundsException e) {
			logger.error("IndexOutOfBoundsException caught", e);
			throw new NumberFormatException("restore failed: " + e.getMessage());
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw new NumberFormatException("restore failed: " + e.getMessage());
		}
	}

	/*
	 * returns the check digit number of the given string.
	 */
	private static int getCheckDigit(String nbrstr) {
		// Calculate the check digit
		char multiplier[] = { 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1,
				3, 7, 1, 3, 7, 1, 3 };
		char charstr[] = nbrstr.toCharArray();
		long sum = 0L;
		for (short i = 0; i < charstr.length; i++) {
			sum += multiplier[i] * (charstr[i] & 0x0f);
		}
		sum = (10 - (sum % 10)) % 10;
		return (int) sum;
	}

	// shuffle the string
	private static String shuffle(String str, int span, boolean forward) {
		if (span == 0 || span >= str.length()) {
			return reverse(str);
		}
		String str1, str2;
		if (forward) {
			str1 = str.substring(0, span);
			str2 = str.substring(span);
		}
		else {
			str1 = str.substring(0, str.length() - span);
			str2 = str.substring(str.length() - span);
		}

		return reverse(str2) + reverse(str1);
	}

	// reverse the string, use method from StringBuffer
	private static String reverse(String str) {
		StringBuffer sb = new StringBuffer(str);
		return sb.reverse().toString();
	}

	private static class myGregCal extends GregorianCalendar {
		private static final long serialVersionUID = 4219232260493472518L;

		public myGregCal() {
			super();
		}

		public long getTimeInMillis() {
			return super.getTimeInMillis();
		}

		public void setTimeInMillis(long millis) {
			super.setTimeInMillis(millis);
		}

		public void computeFields() {
			super.computeFields();
		}

		public void computeTime() {
			super.computeTime();
		}
	}
}
