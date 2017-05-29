package jpa.test.task;

import static org.junit.Assert.assertNotNull;
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

import jpa.constant.EmailAddrType;
import jpa.constant.StatusId;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.service.common.EntityManagerService;
import jpa.service.task.SuspendAddress;
import jpa.spring.util.BoTestBase;

@org.springframework.test.annotation.Commit
public class SuspendAddressTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(SuspendAddressTest.class);
	
	@Resource
	private SuspendAddress task;
	@Resource
	private EmailAddressService emailService;
	
	@Resource
	EntityManagerService emService;

	@BeforeClass
	public static void SuspendAddressPrepare() {
	}

	@Test
	public void testSuspendAddress() throws Exception {
		MessageBean mBean = new MessageBean();
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		String fromaddr = "user" + digits + "@localhost";
		//String fromaddr = "event.alert@localhost";
		emailService.findSertAddress(fromaddr);
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
		String finalRcptAddr = "testbounce@test.com";
		mBean.setFinalRcpt(finalRcptAddr);

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments("$" + EmailAddrType.FINAL_RCPT_ADDR.getValue() +",$" + EmailAddrType.FROM_ADDR.getValue());
		Integer addrsSuspended = task.process(ctx);
		
		emService.clearEM();
		// verify results
		EmailAddress from = emailService.getByAddress(mBean.getFromAsString());
		assertTrue(StatusId.SUSPENDED.getValue().equals(from.getStatusId()));
		assertTrue(0<=from.getBounceCount());
		
		if (addrsSuspended != null && addrsSuspended > 1) {
			EmailAddress othr = emailService.getByAddress(finalRcptAddr);
			assertNotNull(othr);
			assertTrue(StatusId.SUSPENDED.getValue().equals(othr.getStatusId()));
			assertTrue(0<=othr.getBounceCount());
		}

	}
}
