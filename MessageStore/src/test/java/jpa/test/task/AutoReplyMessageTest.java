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

import jpa.data.preload.EmailTemplateEnum;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.msg.MessageInbox;
import jpa.service.common.EmailAddressService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.task.AutoReplyMessage;
import jpa.spring.util.BoTestBase;

@org.springframework.test.annotation.Commit
public class AutoReplyMessageTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(AutoReplyMessageTest.class);
	
	@Resource
	private AutoReplyMessage task;
	@Resource
	private EmailAddressService emailService;
	@Resource
	private MessageInboxService inboxService;

	@BeforeClass
	public static void AutoReplyPrepare() {
	}

	@Test
	public void testAutoReplyMessage() throws Exception {
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
		mBean.setValue(new Date()+ " Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		mBean.setMailboxUser("testUser");
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		if (StringUtils.isNotBlank(id)) {
			mBean.setMsgRefId(Integer.parseInt(id));
		}
		mBean.setFinalRcpt("testbounce@test.com");

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments(EmailTemplateEnum.SubscribeByEmailReply.name());
		task.process(ctx);
		
		// verify results
		assertFalse(ctx.getRowIds().isEmpty());
		MessageInbox minbox = inboxService.getAllDataByPrimaryKey(ctx.getRowIds().get(0));
		assertTrue(toaddr.equals(minbox.getFromAddress().getAddress()));
		assertTrue(fromaddr.equals(minbox.getToAddress().getAddress()));
	}
}
