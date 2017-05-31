package jpa.test.misc;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import jpa.exception.DataValidationException;
import jpa.util.DB2DateUtil;
import jpa.util.ExceptionUtil;

public class ExceptionUtilTest {
	static final Logger logger = Logger.getLogger(ExceptionUtilTest.class);

	@Test
	public void testExceptions() {
		
		try {
			ExceptionUtil.findException(null, Exception.class);
			fail();
		}
		catch (IllegalArgumentException e) {
			// expected
		}
		
		try {
			ExceptionUtil.findException(new Exception(), null);
			fail();
		}
		catch (IllegalArgumentException e) {
			// expected
		}
		
		try {
			DB2DateUtil.formatDate(new java.util.Date(), "bad-format");
			fail();
		}
		catch (IllegalArgumentException iae1) {
			Exception ex1 = ExceptionUtil.findException(iae1, Exception.class);
			assertNotNull(ex1);
			assertTrue(ex1 instanceof IllegalArgumentException);
			
			Exception ex2 = ExceptionUtil.findException(iae1, IllegalArgumentException.class);
			assertNotNull(ex2);
		}
		
		Exception iae2 = new IllegalArgumentException(new IOException());
		
		Exception ex3 = ExceptionUtil.findRootCause(iae2);
		assertNotNull(ex3);
		assertTrue(ex3 instanceof IOException);

		String nested = "nested exception is: java.lang.ThreadDeath\n fatal error!";
		Exception dve1 = new DataValidationException("Invalid Data", new NullPointerException(nested));
		String str1 = ExceptionUtil.findNestedStackTrace(dve1, "java.lang.ThreadDeath");
		assertNotNull(str1);
		//logger.info("Nested Exception found: " + st1);
		assertTrue(StringUtils.contains(str1, "fatal error!"));
		
		String str2 = ExceptionUtil.findCallingClass(dve1);
		assertEquals("Root class: unknown", str2);
	}
}
