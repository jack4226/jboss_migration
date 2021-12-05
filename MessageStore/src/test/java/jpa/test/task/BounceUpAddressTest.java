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

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.service.task.BounceUpAddress;
import jpa.spring.util.BoTestBase;

@org.springframework.test.annotation.Commit
public class BounceUpAddressTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = LogManager.getLogger(BounceUpAddressTest.class);
	
	@Resource
	private BounceUpAddress task;
	@Resource
	private EmailAddressService emailService;

	@BeforeClass
	public static void BounceUpPrepare() {
	}

	static java.util.Map<String, String> statusMap = new java.util.HashMap<>();
	
	@Test
	public void testBounceUpAddress() throws Exception {
		MessageBean mBean = new MessageBean();
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		String fromaddr = "user" + digits + "@localhost";
		//String fromaddr = "testfrom@localhost";
		String toaddr = "testto@localhost";
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
		ctx.setTaskArguments("$From,$To,event.alert@localhost");
		task.process(ctx);
		
		// verify results
		verifyBounceCount(mBean.getFromAsString());
		verifyBounceCount(mBean.getToAsString());
		verifyBounceCount("event.alert@localhost");
	}
	
	private void verifyBounceCount(String address) {
		EmailAddress addr = emailService.getByAddress(address);
		if (addr != null) {
			assertTrue(0 < addr.getBounceCount());
			if (addr.getBounceCount() >= Constants.BOUNCE_SUSPEND_THRESHOLD) {
				assertTrue(StatusId.SUSPENDED.getValue().equals(addr.getStatusId()));
			}
		}
	}
}
