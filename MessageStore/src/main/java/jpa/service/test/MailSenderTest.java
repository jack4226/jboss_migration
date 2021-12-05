package jpa.service.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.model.msg.MessageInbox;
import jpa.service.common.EmailAddressService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgout.MailSenderBo;
import jpa.service.msgout.SmtpException;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

@FixMethodOrder
public class MailSenderTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(MailSenderTest.class);
	@Autowired
	private MailSenderBo mailSenderBo;
	
	@Autowired
	private MessageInboxService msgInboxDao;
	
	@Autowired
	private EmailAddressService emailAddressDao;
	
	private static List<String> suffixes = new ArrayList<>();
	private static List<String> users = new ArrayList<>();
	
	@Test
	@Rollback(value=false)
	public void test1() { // send mail
		int loops = 2;
		for (int i = 0; i < loops; i++) {
			String suffix = StringUtils.leftPad(new Random().nextInt(100) + "", 2, "0");
			suffixes.add(suffix);
		}
		long startTime = System.currentTimeMillis();
		try {
			for (int i = 0; i < suffixes.size(); i++) {
				String suffix = suffixes.get(i);
				String user = "user" + suffix + "@localhost";
				users.add(user);
				MessageBean messageBean = new MessageBean();
				messageBean.setSubject("Test MailSender - " + suffix + " " + new java.util.Date());
				messageBean.setBody("Test MailSender Body Message - " + suffix);
				messageBean.setFrom(InternetAddress.parse("testfrom@localhost", false));
				messageBean.setTo(InternetAddress.parse(user, false));
				logger.info("testSendMail() - before calling for " + user);
				try {
					mailSenderBo.process(new MessageContext(messageBean));
				} catch (SmtpException | IOException e) {
					logger.error("Exception caught", e);
					fail();
				}
				logger.info("Email saved and sent!");
			}
			logger.info("Total Emails Queued: " + suffixes.size() + ", Time taken: "
					+ (System.currentTimeMillis() - startTime) / 1000 + " seconds");
		}
		catch (DataValidationException | AddressException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void test2() { // waitForMailEngine
		// wait for the MailEngine to add a record to MsgInbox
		try {
			Thread.sleep(WaitTimeInMillis);
		}
		catch (InterruptedException e) {}
	}
	
	@Test
	public void test3() { // verifyDatabaseRecord
		// now verify the database record added
		for (int i = 0; i < users.size(); i++) {
			String user = users.get(i);
			EmailAddress addrVo = emailAddressDao.getByAddress(user);
			assertNotNull("Address " + user + " must have been added.", addrVo);
			List<MessageInbox> list = msgInboxDao.getByToAddress(addrVo.getAddress());
			assertTrue(list.size() > 0);
			boolean found = false;
			for (MessageInbox vo : list) {
				if (vo.getMsgSubject().startsWith("Test MailSender - " + suffixes.get(i))) {
					logger.info("Message retrieved: " + PrintUtil.prettyPrint(vo, 2));
					if (user.equals(vo.getToAddress().getAddress())
							&& RuleNameEnum.SEND_MAIL.getValue().equals(vo.getRuleLogic().getRuleName())) {
						found = true;
					}
				}
			}
			assertEquals("Verify result", true, found);
		}
	}

}
