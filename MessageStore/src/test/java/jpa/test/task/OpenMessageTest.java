package jpa.test.task;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import jpa.constant.MsgStatusCode;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.msg.MessageInbox;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.task.OpenMessage;
import jpa.spring.util.BoTestBase;

@org.springframework.test.annotation.Commit
public class OpenMessageTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = LogManager.getLogger(OpenMessageTest.class);
	
	@Resource
	private OpenMessage task;
	@Resource
	private MessageInboxService inboxService;

	@BeforeClass
	public static void OpenMessagePrepare() {
	}

	@Test
	public void testOpenMessage() throws Exception {
		MessageBean mBean = new MessageBean();
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		String fromaddr = "user" + digits + "@localhost";
		//String fromaddr = "event.alert@localhost";
		String toaddr = "watched_maibox@domain.com";
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ " Test body message.");
		mBean.setMailboxUser("testUser");
		MessageInbox minbox = inboxService.getLastRecord();
		if (MsgStatusCode.OPENED.getValue().equals(minbox.getStatusId())) {
			minbox.setStatusId(MsgStatusCode.CLOSED.getValue());
			inboxService.update(minbox);
		}
		mBean.setMsgId(minbox.getRowId());

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		// verify results
		MessageInbox minbox2 = inboxService.getByRowId(mBean.getMsgId());
		assertTrue(MsgStatusCode.OPENED.getValue().equals(minbox2.getStatusId()));
	}
}
