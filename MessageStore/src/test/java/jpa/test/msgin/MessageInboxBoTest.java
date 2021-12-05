package jpa.test.msgin;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.MsgStatusCode;
import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.model.msg.MessageInbox;
import jpa.model.msg.MessageStream;
import jpa.service.common.EmailAddressService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgdata.MessageStreamService;
import jpa.service.msgin.MessageInboxBo;
import jpa.service.msgin.MessageParserBo;
import jpa.spring.util.BoTestBase;
import jpa.util.TestUtil;

@org.springframework.test.annotation.Commit
public class MessageInboxBoTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = LogManager.getLogger(MessageInboxBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	MessageStreamService streamService;
	@Autowired
	MessageParserBo msgParser;
	@Autowired
	MessageInboxBo msgInboxBo;
	@Autowired
	MessageInboxService inboxService;
	@Autowired
	private EmailAddressService emailService;
	
	@Test
	public void testMessageInboxBo() throws MessagingException, IOException {
		MessageBean msgBean1 = testReadFromDatabase(100);
		assertNotNull(msgBean1);
		// parse the message bean to set rule name
		String ruleName = msgParser.parse(msgBean1);
		assertNotNull(ruleName);

		String fileName = "BouncedMail_1.txt";
		MessageBean msgBean2 = testReadFromFile(fileName);
		assertNotNull(msgBean2);
		// parse the message bean to set rule name
		ruleName = msgParser.parse(msgBean2);
		assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(ruleName));
		
		int rowId = msgInboxBo.saveMessage(msgBean2);
		logger.info("Msgid = " + rowId);
		MessageInbox inbox = TestUtil.verifyBouncedMail_1(rowId, inboxService, emailService);
		assertTrue(MsgStatusCode.PENDING.getValue().equals(inbox.getStatusId()));
	}
	
	private MessageBean testReadFromDatabase(int msgId) throws MessagingException {
		byte[] mailStream = readFromDatabase(msgId);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
	
	private MessageBean testReadFromFile(String fileName) throws MessagingException, IOException {
		byte[] mailStream = TestUtil.loadFromSamples(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
	
	private byte[] readFromDatabase(int msgId) {
		MessageStream msgStreamVo = streamService.getByMsgInboxId(msgId);
		if (msgStreamVo == null) {
			msgStreamVo = streamService.getLastRecord();
			if (msgStreamVo == null) {
				String fileName = "BouncedMail_2.txt";
				return TestUtil.loadFromSamples(fileName);
			}
		}
		logger.info("MsgStreamDao - getByPrimaryKey: "+LF+msgStreamVo);
		return msgStreamVo.getMsgStream();
	}
}
