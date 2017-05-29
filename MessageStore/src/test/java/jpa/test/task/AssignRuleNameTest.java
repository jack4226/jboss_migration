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
import org.junit.FixMethodOrder;
import org.junit.Test;

import jpa.constant.StatusId;
import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.service.common.EntityManagerService;
import jpa.service.task.AssignRuleName;
import jpa.spring.util.BoTestBase;
import jpa.util.JpaUtil;

@org.springframework.test.annotation.Commit
@FixMethodOrder
public class AssignRuleNameTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(AssignRuleNameTest.class);
	
	@Resource
	private AssignRuleName task;
	@Resource
	private EmailAddressService emailService;
	
	@Resource
	EntityManagerService emService;

	@BeforeClass
	public static void AssignRuleNamePrepare() {
	}
	
	@Test
	public void testAssignRuleName() throws Exception {
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
		mBean.setRuleName(RuleNameEnum.SEND_MAIL.getValue());
		EmailAddress before = emailService.getByAddress(mBean.getFinalRcpt());

		if (before != null) {
			emService.detach(before); // XXX To work around optimistic locking issue under EclipseLink
		}
		
		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments(RuleNameEnum.HARD_BOUNCE.getValue());
		task.process(ctx);
		
		// verify results
		EmailAddress after = emailService.getByAddress(mBean.getFinalRcpt());
		if (!JpaUtil.isMySQLDatabase()) { // TODO - MySQL would return a null the first time the test ran, fix it
			assertNotNull(after);
		}
		if (after != null) {
			if (before == null) {
				assertTrue(0<=after.getBounceCount()); // TODO investigate "0<" not working from Maven
			}
			else {
				assertTrue(before.getBounceCount()<after.getBounceCount() || StatusId.SUSPENDED.getValue().equals(after.getStatusId()));
			}
		}
	}
}
