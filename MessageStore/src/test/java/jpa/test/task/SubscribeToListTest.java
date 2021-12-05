package jpa.test.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.service.common.SubscriptionService;
import jpa.service.maillist.MailingListService;
import jpa.service.task.SubscribeToList;
import jpa.spring.util.BoTestBase;

@org.springframework.test.annotation.Commit
public class SubscribeToListTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = LogManager.getLogger(SubscribeToListTest.class);
	
	@Resource
	private SubscribeToList task;
	@Resource
	private MailingListService listService;
	@Resource
	private SubscriptionService subService;

	@BeforeClass
	public static void SubscribeToListPrepare() {
	}

	@Test
	public void testSubscribeToList() throws Exception {
		MessageBean mBean = new MessageBean();
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		String fromaddr = "user" + digits + "@localhost";
		//String fromaddr = "event.alert@localhost";
		List<MailingList> lists = listService.getAll(true);
		String toaddr = lists.get(0).getListEmailAddr();
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("subscribe");
		mBean.setValue(new Date()+ " Test body message.");
		mBean.setMailboxUser("testUser");

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		
		// verify results
		assertFalse(ctx.getRowIds().isEmpty());
		Optional<Subscription> sub = subService.getByRowId(ctx.getRowIds().get(0));
		assertTrue(sub.isPresent());
		assertTrue(fromaddr.equals(sub.get().getEmailAddress().getAddress()));
		assertTrue(sub.get().isSubscribed());
		assertTrue(lists.get(0).getListId().equals(sub.get().getMailingList().getListId()));
	}
}
