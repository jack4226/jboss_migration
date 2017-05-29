package jpa.test.task;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.service.task.DropMessage;
import jpa.spring.util.BoTestBase;

@org.springframework.test.annotation.Commit
public class DropMessageTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(DropMessageTest.class);
	
	@Resource
	private DropMessage task;

	@BeforeClass
	public static void DropMessagePrepare() {
	}

	@Test
	public void testDropMessage() throws Exception {
		MessageBean mBean = new MessageBean();
		String fromaddr = "event.alert@localhost";
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

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		// verify results
		assertTrue(ctx.getRowIds().isEmpty());
	}
}
