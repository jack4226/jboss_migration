package jpa.test.msgout;

import java.io.IOException;

import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.model.msg.MessageInbox;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgin.MessageInboxBo;
import jpa.service.msgin.MessageParserBo;
import jpa.service.msgout.MessageBeanBo;
import jpa.spring.util.BoTestBase;
import jpa.util.TestUtil;

@org.springframework.test.annotation.Commit
public class MessageBeanBoTest extends BoTestBase {

	@BeforeClass
	public static void MessageBeanBoPrepare() {
	}

	@Autowired
	MessageBeanBo msgBeanBo;
	@Autowired
	MessageInboxBo inboxBo;
	@Autowired
	MessageInboxService inboxService;
	@Autowired
	MessageParserBo msgParser;

	private int msgId = -1;

	@Before
	@Rollback(value=false)
	public void prepare() {
		String fileName = "BouncedMail_1.txt";
		MessageBean msgBean = null;
		try {
			msgBean = readFromFile(fileName);
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		msgParser.parse(msgBean);
		msgId = inboxBo.saveMessage(msgBean);
	}

	@Test
	public void testMessageBeanBo() throws MessagingException, IOException {
		MessageInbox inbox = inboxService.getAllDataByPrimaryKey(msgId);
		MessageBean mBean = msgBeanBo.createMessageBean(inbox);
		TestUtil.verifyMessageBean4BounceMail_1(mBean);
	}
	
	private MessageBean readFromFile(String fileName) throws MessagingException, IOException {
		byte[] mailStream = TestUtil.loadFromSamples(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
}
