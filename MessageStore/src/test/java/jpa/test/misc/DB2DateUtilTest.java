package jpa.test.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Test;

import jpa.util.DB2DateUtil;

public class DB2DateUtilTest {
	static final Logger logger = Logger.getLogger(DB2DateUtilTest.class);
	
	@Test
	public void testDB2DateUtil() {
		String oldDate = "2006-12-15-12:56:18.095856";
		oldDate = "2003-04-03.09.04.47.123562";
		oldDate = "2003/04/03.09.04.47:123562";
		String newDate = DB2DateUtil.correctDB2Date(oldDate);
		logger.info("Date format after correction: " + newDate);
		
		assertEquals("2003-04-03 09:04:47.123562", newDate);
		assertTrue(DB2DateUtil.isValidDB2Date(newDate));
		
		assertTrue(DB2DateUtil.isValidBirthDate("12/25/1980"));

		String dateStr = "20001012";
		try {
			String converted = DB2DateUtil.convertDateString(dateStr,"yyyyMMdd","MM/dd/yyyy");
			logger.info("From "+dateStr+" -> "+converted);
			assertEquals(converted, "10/12/2000");
		}
		catch (Exception e) {
			fail();
		}
	}
}
