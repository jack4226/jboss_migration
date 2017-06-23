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
import jpa.service.common.EmailAddressService;
import jpa.service.common.EntityManagerService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.task.CsrReplyMessage;
import jpa.spring.util.BoTestBase;
import jpa.util.JpaUtil;

@org.springframework.test.annotation.Commit
public class CsrReplyMessageTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(CsrReplyMessageTest.class);
	
	@Resource
	private CsrReplyMessage task;
	@Resource
	private MessageInboxService inboxService;
	@Resource
	private EmailAddressService emailService;
	@Resource
	EntityManagerService emService;

	@BeforeClass
	public static void CsrReplyPrepare() {
	}

	@Test
	public void testCsrReplyMessage() throws Exception {
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		String fromaddr = "user" + digits + "@localhost";
		//String fromaddr = "testfrom@localhost";
		if (JpaUtil.isEclipseLink()) {
			// XXX work around optimistic locking error when running from Maven with EclipseLink
			emailService.findSertAddress(fromaddr);
		}
		String toaddr = "support@localhost";
		MessageBean origMsg = new MessageBean();
		try {
			origMsg.setFrom(InternetAddress.parse(fromaddr, false));
			origMsg.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		origMsg.setSubject("A Exception occured");
		origMsg.setValue(new Date()+ " Test body message.");
		origMsg.setMailboxUser("testUser");
		// get and set MsgId
		MessageInbox inbox = inboxService.getLastRecord();
		inbox = inboxService.getPrevoiusRecord(inbox);
		origMsg.setMsgId(inbox.getRowId());

		MessageBean mBean = new MessageBean();
		mBean.setOriginalMail(origMsg);
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);
		mBean.setFrom(InternetAddress.parse(toaddr, false));
		mBean.setSubject("Re: " + origMsg.getSubject());
		String replyBody = "test CSR reply message.";
		mBean.setBody(replyBody);

		emService.clearEM();
		
		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		// verify results
		assertFalse(ctx.getRowIds().isEmpty());
		MessageInbox minbox = inboxService.getAllDataByPrimaryKey(ctx.getRowIds().get(0));
		assertTrue(toaddr.equals(minbox.getFromAddress().getAddress()));
		assertTrue(fromaddr.equals(minbox.getToAddress().getAddress()));
		assertTrue(mBean.getSubject().equals(minbox.getMsgSubject()));
		logger.info("MsgInbox Body: " + minbox.getMsgBody());
		logger.info("MsgBean  Body: " + mBean.getBody());
		assertTrue(minbox.getMsgBody().indexOf(replyBody)>=0);
		assertTrue(minbox.getMsgBody().indexOf(origMsg.getBody())>0);
		assertTrue(minbox.getMsgBody().indexOf(origMsg.getSubject())>0);
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id_bean = parser.parseMsg(mBean.getBody());
		String id_ibox = parser.parseMsg(minbox.getMsgBody());
		assertTrue(id_bean.equals(id_ibox));
		assertTrue(id_bean.equals(minbox.getRowId()+""));
	}
}
