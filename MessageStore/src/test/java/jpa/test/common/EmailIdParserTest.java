package jpa.test.common;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.regex.Matcher;

import org.junit.BeforeClass;
import org.junit.Test;

import jpa.message.util.EmailIdParser;
import jpa.spring.util.BoTestBase;

public class EmailIdParserTest extends BoTestBase {

	@BeforeClass
	public static void EmailIdParserPrepare() {
	}

	@Test
	public void testEmailIdParser() {
		int msgId = 12345;
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		// embed an email id to a message
		String msgStr = "this is my message text.\n" + parser.createEmailId(msgId) + "\n...the rest";
		logger.info("Original Msg Text: " + msgStr);
		String id = parser.parseMsg(msgStr);
		logger.info("Email Id restored: " + id);
		assertTrue(new String(msgId+"").equals(id));
		
		// recode email id with the same msgId
		String msgStr2 = parser.replaceEmailId(msgStr, msgId);
		logger.info("Msg Text after replace: " + msgStr2);
		String id2 = parser.parseMsg(msgStr2);
		logger.info("Email Id restored: " + id2);
		assertTrue(new String(msgId+"").equals(id2));
		
		// replace email id with a new code
		String msgStr3 = parser.replaceKey(msgStr, parser.getBodyPattern(), 9876543);
		logger.info("Msg: " + msgStr3);
		assertTrue("9876543".equals(parser.parseMsg(msgStr3)));

		// remove email id from the message
		String msgStr4 = parser.removeKey(msgStr, parser.getBodyPattern());
		logger.info("Msg: " + msgStr4);
		assertNull(parser.parseMsg(msgStr4));
		
		Matcher matcher = parser.getBodyPattern().matcher("aaaaab \nSystem Email \r\tId: \n 10.123456.0 ... the rest");
		if (matcher.find()) {
			String matched = matcher.group(matcher.groupCount());
			logger.info("Matched String: " + matched + ", groups: " + matcher.groupCount());
		}
		else {
			logger.info("Pattern not matched.");
			fail();
		}
	}
}
