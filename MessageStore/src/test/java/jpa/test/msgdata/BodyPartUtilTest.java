package jpa.test.msgdata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.service.msgin.MessageParserBo;
import jpa.spring.util.BoTestBase;
import jpa.util.TestUtil;

public class BodyPartUtilTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(BodyPartUtilTest.class);
	static final String LF = System.getProperty("line.separator", "\n");
	
	@BeforeClass
	public static void BodyPartUtilPrepare() {
	}

	@Autowired
	private MessageParserBo msgParser;
	
	@Test
	public void testBodyPartUtil() throws MessagingException {
		String fileName = "BouncedMail_1.txt";
		MessageBean msgBean = testReadFromFile(fileName);
		assertNotNull(msgBean);

		TestUtil.verifyMessageBean4BounceMail_1(msgBean);

		// parse the message bean to set rule name
		String ruleName = msgParser.parse(msgBean);
		assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(ruleName));
		
		String finalRcpt = msgBean.getFinalRcpt();
		assertTrue("jackwnn@synnex.com.au".equals(finalRcpt));
	}

	private MessageBean testReadFromFile(String fileName) throws MessagingException {
		byte[] mailStream = TestUtil.loadFromSamples(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
}
