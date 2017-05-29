package jpa.test.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.junit.Test;

import jpa.util.DB2DateUtil;
import jpa.util.TimestampUtil;

public class TimestampUtilTest {
	static final Logger logger = Logger.getLogger(TimestampUtilTest.class);
	
	@Test
	public void testTimestampUtil() {
		String db2tm1 = "1582-10-23-00.48.04.702003";
		assertEquals(db2tm1, TimestampUtil.decimalStringToDb2(TimestampUtil.db2ToDecimalString(db2tm1)));
		
		db2tm1 = "0697-10-13-22.29.59.972003";
		assertEquals(db2tm1, TimestampUtil.decimalStringToDb2(TimestampUtil.db2ToDecimalString(db2tm1)));
		
		db2tm1 = TimestampUtil.getCurrentDb2Tms();
		assertTrue(TimestampUtil.isValidDb2Timestamp(db2tm1));
		String converted = TimestampUtil.db2ToDecimalString(db2tm1);
		//converted = convert_old(db2tm);
		String restored = TimestampUtil.decimalStringToDb2(converted);
		logger.info("Date: " + db2tm1 + ", converted: " + converted + ", restored: " + restored);
		assertTrue("Is conversion a success? ", (db2tm1.equals(restored)));
		
		assertTrue("Is (" + restored + ") valid? ", TimestampUtil.isValidDb2Timestamp(restored));
		
		String db2tm2 = TimestampUtil.getCurrentDb2Tms();
		logger.info("DB2 Time stamp 2: " + db2tm2);
		
		Timestamp tms2 = TimestampUtil.db2ToTimestamp(db2tm2);
		assertEquals(db2tm2, TimestampUtil.timestampToDb2(tms2));
		String tmsstr2 = tms2.toString();
		logger.info("SQL Time stamp 2: " + tmsstr2);
		if (tmsstr2.length() > 23) {
			tmsstr2 = tmsstr2.substring(0, 23);
			assertEquals(23, tmsstr2.length());
			String filled = TimestampUtil.fillWithTrailingZeros(tmsstr2, 26);
			logger.info("Trailing Zero filled: " + filled);
			assertEquals(26, filled.length());
			assertEquals("000", filled.substring(23));
		}
		
		
		String oratms = TimestampUtil.db2ToOracle("2016-12-02-15.52.43.730995");
		logger.info("Oracle Time stamp: " + oratms);
		assertEquals("12/02/2016 15:52:43", oratms);
		
		// perform some brutal force tests
		java.util.Random random = new java.util.Random(System.currentTimeMillis());
		for (int i=0; i<2000; i++) {
			String db2tm3 = TimestampUtil.getCurrentDb2Tms();
			db2tm3 = db2tm3.substring(0,20) + TimestampUtil.fillWithLeadingZeros(random.nextInt(999999), 6);
			converted = TimestampUtil.db2ToDecimalString(db2tm3);
			restored = TimestampUtil.decimalStringToDb2(converted);
			assertTrue("Failed to restore Email_Id: " + db2tm3 + " - " + restored, db2tm3.equals(restored));
			String db2tm_1 = DB2DateUtil.correctDB2Date(db2tm3); // to "yyyy-MM-dd HH:mm:ss.SSSSSS"
			String db2tm_2 = TimestampUtil.fillWithTrailingZeros(TimestampUtil.db2ToTimestamp(db2tm3).toString(),26);
			assertTrue("Failed to convert to sql Timestamp: " + db2tm_1 + " - " + db2tm_2, db2tm_1.equals(db2tm_2));
		}
	}
}
