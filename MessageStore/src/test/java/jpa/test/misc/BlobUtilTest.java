package jpa.test.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import jpa.util.BlobUtil;

public class BlobUtilTest {
	static final Logger logger = LogManager.getLogger(BlobUtilTest.class);
	
	@Test
	public void testBlobUtil() {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.DAY_OF_MONTH, cal1.getActualMaximum(Calendar.DAY_OF_MONTH));
		Calendar cal2 = (Calendar) BlobUtil.deepCopy(cal1);
		assertEquals(cal2, cal1);
		
		logger.info("Calendar 1 before: " + cal1.getTime());
		cal1.roll(Calendar.MONTH, false);
		logger.info("Calendar 1 after:  " + cal1.getTime());
		cal2.roll(Calendar.MONTH, false);
		logger.info("Calendar 2 after:  " + cal2.getTime());
		
		byte[] cal1bytes = BlobUtil.objectToBytes(cal1);
		Calendar cal1Restored = (Calendar)BlobUtil.bytesToObject(cal1bytes);
		assertTrue("Is Object<->Bytes conversion a success? ", cal1.equals(cal1Restored));
		
		byte[] cal2xmlbytes = BlobUtil.beanToXmlBytes(cal2);
		Calendar cal2Restored = (Calendar)BlobUtil.xmlBytesToBean(cal2xmlbytes);
		assertTrue("Is Object<->XmlBytes conversion a success? ", cal2.equals(cal2Restored));
		
		assertEquals(cal1Restored, cal2Restored);
	}
}
