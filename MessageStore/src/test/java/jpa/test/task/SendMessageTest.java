package jpa.test.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import jpa.constant.Constants;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.msg.MessageInbox;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgin.MessageParserBo;
import jpa.service.task.SendMessage;
import jpa.spring.util.BoTestBase;

@org.springframework.test.annotation.Commit
public class SendMessageTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(SendMessageTest.class);
	
	@Resource
	private SendMessage task;
	@Resource
	private MessageInboxService inboxService;
	@Resource
	private MessageParserBo parserBo;

	@BeforeClass
	public static void SendMessagePrepare() {
	}

	@Test
	public void testSendMessage() throws Exception {
		MessageBean mBean = new MessageBean();
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		String fromaddr = "user" + digits + "@localhost";
		//String fromaddr = "event.alert@localhost";
		String toaddr = "support@localhost";
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ " Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		mBean.setMailboxUser("testUser");
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		// verify results
		assertFalse(ctx.getRowIds().isEmpty());
		MessageInbox minbox = inboxService.getAllDataByPrimaryKey(ctx.getRowIds().get(0));
		assertTrue(fromaddr.equals(minbox.getFromAddress().getAddress()));
		assertTrue(toaddr.equals(minbox.getToAddress().getAddress()));
		assertTrue(mBean.getSubject().equals(minbox.getMsgSubject()));
		assertTrue(mBean.getBody().equals(minbox.getMsgBody()));
		
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		assertTrue(id.equals(minbox.getRowId()+""));
	}
}
