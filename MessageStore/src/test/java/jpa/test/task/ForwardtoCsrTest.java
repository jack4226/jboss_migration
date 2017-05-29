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
import jpa.constant.TableColumnName;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.SenderData;
import jpa.model.msg.MessageInbox;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.task.ForwardToCsr;
import jpa.spring.util.BoTestBase;

@org.springframework.test.annotation.Commit
public class ForwardtoCsrTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(ForwardtoCsrTest.class);
	
	@Resource
	private ForwardToCsr task;
	@Resource
	private MessageInboxService inboxService;
	@Resource
	private SenderDataService senderService;

	@BeforeClass
	public static void ForwardPrepare() {
	}

	@Test
	public void testForwardToCsr() throws Exception {
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		String fromaddr = "user" + digits + "@localhost";
		//String fromaddr = "testfrom@localhost";
		String toaddr = "testto@localhost";
		MessageBean mBean = new MessageBean();
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
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments("$" + TableColumnName.SUBSCRIBER_CARE_ADDR.getValue());
		task.process(ctx);
		
		// verify results
		assertFalse(ctx.getRowIds().isEmpty());
		MessageInbox minbox = inboxService.getAllDataByPrimaryKey(ctx.getRowIds().get(0));
		assertTrue(fromaddr.equals(minbox.getFromAddress().getAddress()));
		SenderData sender = senderService.getBySenderId(mBean.getSenderId());
		assertTrue(sender.getSubrCareEmail().equals(minbox.getToAddress().getAddress()));
		
		assertTrue(minbox.getMsgSubject().equals(mBean.getSubject()));
		assertTrue(minbox.getMsgBody().equals(mBean.getBody()));
	}
}
