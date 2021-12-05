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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.service.common.SubscriptionService;
import jpa.service.maillist.MailingListService;
import jpa.service.task.UnsubscribeFromList;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

@org.springframework.test.annotation.Commit
public class UnsubscribeFromListTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = LogManager.getLogger(UnsubscribeFromListTest.class);
	
	@Resource
	private UnsubscribeFromList task;
	@Resource
	private MailingListService listService;
	@Resource
	private SubscriptionService subService;

	@BeforeClass
	public static void UnsubscribeFromListPrepare() {
	}

	private int rowId;
	private List<MailingList> lists = null;
	private static String fromaddr = "user" + StringUtils.leftPad("" + new Random().nextInt(100), 2, "0") + "@localhost";
	
	@Before
	@Rollback(value=false)
	public void prepare() {
		MessageBean mBean = new MessageBean();
		lists = listService.getAll(true);
		String toaddr = lists.get(0).getListEmailAddr();
		try {
			mBean.setFrom(InternetAddress.parse(fromaddr, false));
			mBean.setTo(InternetAddress.parse(toaddr, false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("unsubscribe");
		mBean.setValue(new Date()+ " Test body message.");
		mBean.setMailboxUser("testUser");

		MessageContext ctx = new MessageContext(mBean);
		task.process(ctx);
		assertFalse(ctx.getRowIds().isEmpty());
		rowId = ctx.getRowIds().get(0);
	}
	
	@Test
	public void testUnsubscribeFromList() throws Exception {
		logger.info("in testUnsubscribeFromList() method...");
		// verify results
		Optional<Subscription> sub = subService.getByRowId(rowId);
		assertTrue(sub.isPresent());
		logger.info("Subscription record: " + PrintUtil.prettyPrint(sub, 2));
		assertTrue(fromaddr.equals(sub.get().getEmailAddress().getAddress()));
		//assertFalse(sub.isSubscribed());
		assertTrue(lists.get(0).getListId().equals(sub.get().getMailingList().getListId()));
	}
}
