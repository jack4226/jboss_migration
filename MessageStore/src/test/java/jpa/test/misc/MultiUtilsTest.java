package jpa.test.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import jpa.util.FileUtil;
import jpa.util.HtmlConverter;
import jpa.util.HtmlToText;
import jpa.util.HtmlUtil;
import jpa.util.PhoneNumberUtil;
import jpa.util.ProductKey;
import jpa.util.StringUtil;

public class MultiUtilsTest {
	static final Logger logger = Logger.getLogger(MultiUtilsTest.class);
	
	@Test
	public void testPhoneNumberUtil() {
		assertTrue(PhoneNumberUtil.isValidPhoneNumber("1(900)123-4567"));
		assertFalse(PhoneNumberUtil.isValidPhoneNumber("1(9xy)123-4567"));
		
		String converted1 = PhoneNumberUtil.convertPhoneLetters("1 614-JOe-Cell");
		logger.info("Converted Phone Number 1: " + converted1);
		assertEquals("1 614-563-2355", converted1);
		String converted2 = PhoneNumberUtil.convertTo10DigitNumber("1 614-JO6-G0LO");
		logger.info("Converted Phone Number 2: " + converted2);
		assertEquals("6145664056", converted2);
	}
	
	@Test
	public void testProductKey() {
		assertTrue(ProductKey.validateKey("K51BJ-A0U7X-K97CX-0TYDQ-ED5AA"));
		assertFalse(ProductKey.validateKey("K51BJ-A0U7X-K97CX-0TYDQ-ED5AB"));
	}
	
	@Test
	public void testHtmlUtil() {
		assertTrue(HtmlUtil.isHTML("<p>Dear ${CustomerName}, <br></p><p>This is test template message body to ${SubscriberAddress}.</p><p>Time sent: ${CurrentDate}</p><p>Contact Email: ${ContactEmailAddress}</p><p>To unsubscribe from this mailing list, send an e-mail to: ${MailingListAddress}with \"unsubscribe\" (no quotation marks) in the subject of the message.</p>"));
		// TODO fix this
		//assertFalse(HtmlUtil.isHTML("plain no html text <nonhtml abc> tag<a abc>def"));
		//assertFalse(HtmlUtil.isHTML("plain no html text <!DOCTYPE abcde> tag<aa abc>"));
		assertTrue(HtmlUtil.isHTML("<H1 LAST_MODIFIED=\"1194988178\">Bookmarks</H1>"));
	}
	
	@Test
	public void testHtmlConverter() {
		try {
			// the HTML to convert
			byte[] bytes = FileUtil.loadFromFile("samples/","HtmlSample.html");
			HtmlConverter parser = HtmlConverter.getInstance();
			String text = parser.convertToText(new String(bytes));
			//System.out.println(text);
			assertTrue(StringUtils.startsWith(text.trim(), "A Simple HTML Example Page"));
			assertTrue(StringUtils.contains(text, "Example Page for simple HTML tags."));
			assertTrue(StringUtils.endsWith(StringUtil.trimRight(text), "which looks like this </html>."));
		} catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void testHtmlToText() {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			// 1, use FileReader
			java.net.URL url = loader.getResource("samples/HtmlSample.html");
			File file = new File(url.getFile());
			FileReader in = new FileReader(file);
			HtmlToText parser = new HtmlToText();
			String text2 = parser.parse(in);
			in.close();
			//System.out.println(text2);
			assertTrue(StringUtils.startsWith(text2, "A Simple HTML Example Page"));
			assertTrue(StringUtils.contains(text2, "Example Page for simple HTML tags."));
			assertTrue(StringUtils.endsWith(StringUtil.trimRight(text2), "which looks like this </html>."));
		} catch (Exception e) {
			fail();
		}

	}
}
