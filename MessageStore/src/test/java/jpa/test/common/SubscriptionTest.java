package jpa.test.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.constant.RuleCriteria;
import jpa.constant.StatusId;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.common.EntityManagerService;
import jpa.service.common.SubscriptionService;
import jpa.service.maillist.MailingListService;
import jpa.spring.util.BoTestBase;
import jpa.util.BeanCopyUtil;
import jpa.util.PrintUtil;

public class SubscriptionTest extends BoTestBase {

	@BeforeClass
	public static void SubscriptionPrepare() {
	}

	@Autowired
	SubscriptionService service;
	
	@Autowired
	EmailAddressService eaService;
	@Autowired
	MailingListService mlService;
	@Autowired
	EntityManagerService emService;

	private EmailAddress emailAddr1 = null;
	private EmailAddress emailAddr2 = null;
	private EmailAddress emailAddr3 = null;
	
	@Before
	public void prepare() {
		String testEmailAddr1 = "jpatest1@localhost";
		emailAddr1 = new EmailAddress();
		emailAddr1.setAddress(testEmailAddr1);
		emailAddr1.setOrigAddress(testEmailAddr1);
		emailAddr1.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr1);
		
		String testEmailAddr2 = "jpatest2@localhost";
		emailAddr2 = new EmailAddress();
		emailAddr2.setAddress(testEmailAddr2);
		emailAddr2.setOrigAddress(testEmailAddr2);
		emailAddr2.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr2.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr2);

		String testEmailAddr3 = "jpatest3@localhost";
		emailAddr3 = new EmailAddress();
		emailAddr3.setAddress(testEmailAddr3);
		emailAddr3.setOrigAddress(testEmailAddr3);
		emailAddr3.setStatusId(StatusId.ACTIVE.getValue());
		emailAddr3.setUpdtUserId(Constants.DEFAULT_USER_ID);
		eaService.insert(emailAddr3);
	}
	
	@Test
	public void SubscriptionService1() {
		List<MailingList> list = mlService.getAll(false);
		assertFalse(list.isEmpty());

		List<Subscription> subs = service.getByListId(list.get(0).getListId());
		if (!subs.isEmpty()) {
			Subscription rcd7 = subs.get(0);
			java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());
			int openCount = rcd7.getOpenCount();
			int clickCount = rcd7.getClickCount();
			rcd7.setOpenCount(openCount+5);
			rcd7.setSentCount(rcd7.getSentCount()+1);
			rcd7.setClickCount(clickCount+10);
			rcd7.setLastClickTime(updtTime);
			rcd7.setLastOpenTime(updtTime);
			rcd7.setLastSentTime(updtTime);
			service.update(rcd7);
			String address = rcd7.getEmailAddress().getAddress();
			String listId = rcd7.getMailingList().getListId();
			Subscription rcd8 = service.getByAddressAndListId(address, listId);
			logger.info("RCD8: " + PrintUtil.prettyPrint(rcd8,1));
			assertEquals(rcd8.getOpenCount(), openCount+5);
			assertEquals(rcd8.getClickCount(), clickCount+10);
			
			Subscription sub = service.getByUniqueKey(rcd7.getEmailAddress().getRowId(), rcd7.getMailingList().getListId());
			assertNotNull(sub);
			assertNull(service.getByUniqueKey(rcd7.getEmailAddress().getRowId(), "DoesNotExist"));
		}
	}
	
	@Test
	public void SubscriptionService2() {
		List<MailingList> list = mlService.getAll(false);
		assertFalse(list.isEmpty());

		// test insert
		Subscription rcd1 = new Subscription();
		rcd1.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
		rcd1.setMailingList(list.get(0));
		rcd1.setEmailAddress(emailAddr1);
		rcd1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd1);
		
		Subscription rcd2 = service.getByAddressAndListId(emailAddr1.getAddress(), list.get(0).getListId());
		assertNotNull(rcd2);
		
		// test update
		rcd2.setUpdtUserId("JpaTest");
		service.update(rcd2);

		Optional<Subscription> rcd3 = service.getByRowId(rcd2.getRowId());
		assertTrue(rcd3.isPresent());
		assertTrue("JpaTest".equals(rcd3.get().getUpdtUserId()));
		// end of test update		
		
		emService.detach(rcd2); /* added to work around following Derby error (not sure if it is still happening):
			"cannot be updated because it has changed or been deleted since it was last read." */

		assertTrue(1<=service.updateClickCount(emailAddr1.getRowId(), list.get(0).getListId()));
		assertTrue(1<=service.updateOpenCount(emailAddr1.getRowId(), list.get(0).getListId()));
		
		// test paging for UI application
		PagingVo vo = new PagingVo();
		vo.setStatusId(StatusId.ACTIVE.getValue());
		vo.getSearchBy().setCriteria(PagingVo.Column.statusId, new PagingVo.Criteria(RuleCriteria.EQUALS, StatusId.ACTIVE.getValue()));
		vo.getSearchBy().setCriteria(PagingVo.Column.origAddress, new PagingVo.Criteria(RuleCriteria.CONTAINS, "test.com"));
		List<Subscription> listpg = service.getSubscriptionsWithPaging(list.get(0).getListId(), vo);
		assertTrue(listpg.size()>0);
		long count = service.getSubscriptionCount(list.get(0).getListId(), vo);
		assertTrue(count==listpg.size());
		
		Subscription rcd6 = new Subscription();
		rcd6.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
		rcd6.setMailingList(list.get(0));
		rcd6.setEmailAddress(emailAddr3);
		rcd6.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd6);

		assertFalse(service.getByAddress(emailAddr3.getAddress()).isEmpty());

		assertTrue(1<=service.getByListIdSubscribersOnly(list.get(0).getListId()).size());
		assertTrue(1<=service.getByListIdProsperctsOnly(list.get(0).getListId()).size());
		
		//assertTrue(1==service.deleteByPrimaryKey(emailAddr3.getRowId(), list.get(0).getRowId()));
		assertTrue(1==service.deleteByAddress(emailAddr3.getAddress()));

		assertTrue(1==service.updateSentCount(rcd3.get().getRowId(), 1));
	}
	
	@Test
	public void SubscriptionService3() {
		List<MailingList> list = mlService.getAll(false);
		assertFalse(list.isEmpty());

		// test insert
		Subscription rcd1 = new Subscription();
		rcd1.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
		rcd1.setMailingList(list.get(0));
		rcd1.setEmailAddress(emailAddr1);
		rcd1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd1);

		Subscription rcd3 = service.getByAddressAndListId(emailAddr1.getAddress(), list.get(0).getListId());
		assertNotNull(rcd3);
		
		// test insert 2
		Subscription rcd4 = new Subscription();
		BeanCopyUtil.registerBeanUtilsConverters();
		try {
			BeanUtils.copyProperties(rcd4, rcd3);
			rcd4.setEmailAddress(emailAddr2);
			if (list.size()>1) {
				rcd4.setMailingList(list.get(1));
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		rcd4.setOpenCount(rcd3.getOpenCount()+1);
		service.insert(rcd4);
		
		Subscription rcd5 = service.getByAddressAndListId(emailAddr2.getAddress(), rcd4.getMailingList().getListId());
		assertNotNull(rcd5);
		assertTrue(rcd5.getRowId()!=rcd3.getRowId());
		assertFalse(rcd3.getOpenCount()==rcd5.getOpenCount());
		assertFalse(rcd3.getEmailAddress().equals(rcd5.getEmailAddress()));
		// end of test insert
		
		// test delete
		service.delete(rcd3);
		assertNull(service.getByRowId(rcd3.getRowId()));
		int rowsDeleted = service.deleteByRowId(rcd3.getRowId());
		assertTrue(0==rowsDeleted);

		logger.info(PrintUtil.prettyPrint(rcd5,1));
		rowsDeleted = service.deleteByAddressAndListId(emailAddr2.getAddress(), rcd5.getMailingList().getListId());
		assertTrue(1==rowsDeleted);

		// test subscription
		Subscription sub1 = service.subscribe("jpasubtest1@localhost", list.get(0).getListId());
		logger.info(PrintUtil.prettyPrint(sub1,1));
		sub1 = service.getByAddressAndListId("jpasubtest1@localhost", list.get(0).getListId());
		assertNotNull(sub1);
		assertTrue(sub1.isSubscribed());
	
		Subscription sub2 = service.unsubscribe("jpasubtest1@localhost", list.get(0).getListId());
		logger.info(PrintUtil.prettyPrint(sub2,1));
		sub2 = service.getByAddressAndListId("jpasubtest1@localhost", list.get(0).getListId());
		assertFalse(sub2.isSubscribed());
		
		assertTrue(2<=list.size());
		assertTrue(0<service.deleteByListId(list.get(1).getListId()));
		
		assertTrue(0==service.deleteByUniqueKey(emailAddr2.getRowId(), list.get(1).getListId()));
	}
}
