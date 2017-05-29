package jpa.test.task;

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

import jpa.constant.StatusId;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.service.task.ActivateAddress;
import jpa.spring.util.BoTestBase;

@org.springframework.test.annotation.Commit
public class ActivateAddressTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(ActivateAddressTest.class);
	
	@Resource
	private ActivateAddress task;
	@Resource
	private EmailAddressService emailService;

	@BeforeClass
	public static void ActivateAddressPrepare() {
	}

	@Test
	public void testActivateAddress() throws Exception {
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
		mBean.setValue(new Date()+ " - Test body message." + LF + LF + "System Email Id: 10.2127.0" + LF);
		mBean.setMailboxUser("testUser");
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id = parser.parseMsg(mBean.getBody());
		if (StringUtils.isNotBlank(id)) {
			mBean.setMsgRefId(Integer.parseInt(id));
		}
		mBean.setFinalRcpt("testbounce@test.com");

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments("$From,$To,testto@test.com");
		task.process(ctx);
		
		// verify results
		EmailAddress from = emailService.getByAddress(mBean.getFromAsString());
		assertTrue(StatusId.ACTIVE.getValue().equals(from.getStatusId()));
		assertTrue(0==from.getBounceCount());
		EmailAddress to = emailService.getByAddress(mBean.getToAsString());
		assertTrue(StatusId.ACTIVE.getValue().equals(to.getStatusId()));
		assertTrue(0==to.getBounceCount());
		EmailAddress othr = emailService.getByAddress("testto@test.com");
		assertTrue(StatusId.ACTIVE.getValue().equals(othr.getStatusId()));
		assertTrue(0==othr.getBounceCount());
	}
}
