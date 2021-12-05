package jpa.test.msgin;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.service.msgin.MessageParserBo;
import jpa.spring.util.BoTestBase;
import jpa.util.FileUtil;
import jpa.util.TestUtil;

public class MessageParserTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(MessageParserTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");

	@Resource
	private MessageParserBo messageParser;
	final int fromNumber = 1;
	final int toNumber = 24;

	@BeforeClass
	public static void RuleEnginePrepare() {
	}

	@Test
	public void processgetBouncedMails() throws IOException, MessagingException {
		for (int i = fromNumber; i <= toNumber; i++) {
			byte[] mailStream = getBouncedMail(i);
			MessageBean messageBean = MessageBeanUtil.createBeanFromStream(mailStream);
			messageBean.setIsReceived(true);
			String ruleName = messageParser.parse(messageBean);
			logger.info("##### RULE NAME [" + i + "]: " + ruleName);
			if (i==24) {
				assertTrue(RuleNameEnum.HARD_BOUNCE_NO_FINAL_RCPT.getValue().equals(ruleName));
			}
			else {
				assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(ruleName));
			}
		}
	}
	
	@Test
	public void testMessageParser() throws MessagingException {
		String fileName = "BouncedMail_1.txt";
		byte[] mailStream = TestUtil.loadFromSamples(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		
		TestUtil.verifyMessageBean4BounceMail_1(msgBean);
		
		String ruleName = messageParser.parse(msgBean);
		assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(ruleName));

		TestUtil.verifyParsedMessageBean4BounceMail_1(msgBean);
	}

	private byte[] getBouncedMail(int fileNbr) throws IOException {
		byte[] mailStream = FileUtil.loadFromFile("bouncedmails/", "BouncedMail_" + fileNbr + ".txt");
		return mailStream;
	}
}
