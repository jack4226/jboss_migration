package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.data.preload.MailingListEnum;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.MailingListService;
import jpa.spring.util.BoTestBase;
import jpa.util.BeanCopyUtil;
import jpa.util.PrintUtil;

public class MailingListTest extends BoTestBase {

	@BeforeClass
	public static void MailingListPrepare() {
	}

	@Autowired
	MailingListService service;
	
	@Autowired
	EmailAddressService eaService;

	private EmailAddress emailAddr = null;
	private EmailAddress emailAddr2 = null;
	
	@BeforeTransaction
	public void prepare() {
		String testEmailAddr1 = "jpatest1@localhost";
		emailAddr = eaService.getByAddress(testEmailAddr1);
		if (emailAddr == null) {
			emailAddr = new EmailAddress();
			emailAddr.setAddress(testEmailAddr1);
			emailAddr.setOrigAddress(testEmailAddr1);
			emailAddr.setStatusId(StatusId.ACTIVE.getValue());
			emailAddr.setUpdtUserId(Constants.DEFAULT_USER_ID);
			eaService.insert(emailAddr);
		}
		
		String testEmailAddr2 = "jpatest2@localhost";
		emailAddr2 = eaService.getByAddress(testEmailAddr2);
		if (emailAddr2 == null) {
			emailAddr2 = new EmailAddress();
			emailAddr2.setAddress(testEmailAddr2);
			emailAddr2.setOrigAddress(testEmailAddr2);
			emailAddr2.setStatusId(StatusId.ACTIVE.getValue());
			emailAddr2.setUpdtUserId(Constants.DEFAULT_USER_ID);
			eaService.insert(emailAddr2);
		}
	}
	
	private String testListId1 = "TestList1";
	private String testListId2 = "TestList2";

	@Test
	public void testMailingListService() {
		List<MailingList> listall = service.getAll(false);
		assertFalse(listall.isEmpty());
		List<MailingList> list = service.getAll(true);
		assertFalse(list.isEmpty());
		assertTrue(list.size() < listall.size());
		
		MailingList ml01 = list.get(0);
		MailingList ml02 = service.getByListAddress(ml01.getAcctUserName() + "@" + ml01.getSenderData().getDomainName());
		assertTrue(ml01.equals(ml02));
		assertNull(service.getByListAddress(ml01.getAcctUserName() + "@doesnotexist"));

		List<MailingList> list2 = service.getByAddressWithCounts("jsmith@test.com");
		assertTrue(list2.size()>0);
		
		// test insert
		MailingList rcd1 = new MailingList();
		BeanCopyUtil.registerBeanUtilsConverters();
		try {
			BeanUtils.copyProperties(rcd1, list.get(0));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		rcd1.setListMasterEmailAddr("sitemaster@localhost");
		rcd1.setListId(testListId1);
		rcd1.setAcctUserName(testListId1);
		List<Subscription> subs = new ArrayList<Subscription>();
		// added next two lines to prevent this Hibernate error: 
		//	"Found shared references to a collection"
		rcd1.setSubscriptions(subs);
		rcd1.setBroadcastMessages(null);
		service.insert(rcd1);
		
		MailingList rcd2 = service.getByListId(testListId1);
		assertNotNull(rcd2);
		
		// test update
		rcd2.setUpdtUserId("JpaTest");
		service.update(rcd2);
		
		Optional<MailingList> rcd3 = service.getByRowId(rcd2.getRowId());
		assertTrue(rcd3.isPresent());
		assertTrue("JpaTest".equals(rcd3.get().getUpdtUserId()));
		// end of test update
		
		// test insert 2
		MailingList rcd4 = new MailingList();
		try {
			BeanUtils.copyProperties(rcd4, rcd3.get());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		rcd4.setListMasterEmailAddr("sitemaster2@localhost");
		rcd4.setListId(testListId2);
		rcd4.setAcctUserName(testListId2);
		rcd4.setSubscriptions(new ArrayList<Subscription>());
		service.insert(rcd4);
		
		MailingList rcd5 = service.getByListId(testListId2);
		assertTrue(rcd5.getRowId()!=rcd3.get().getRowId());
		assertFalse(rcd3.get().getListMasterEmailAddr().equals(rcd5.getListMasterEmailAddr()));
		// end of test insert
		
		MailingList mlst1 = service.getByListIdWithCounts(MailingListEnum.SMPLLST1.name());
		assertNotNull(mlst1);
		/*
		 * MySQL 	 : BigDecimal
		 * PostgreSQL: BigInteger
		 * Derby 	 : Integer
		 */
		logger.info(PrintUtil.prettyPrint(mlst1,1));
		logger.info("Mailing List Counts: " + mlst1.getSentCount() + "," + mlst1.getOpenCount() + "," + mlst1.getClickCount());
		
		// test delete
		service.delete(rcd3.get());
		assertFalse(service.getByRowId(rcd3.get().getRowId()).isPresent());

		logger.info(PrintUtil.prettyPrint(rcd5,1));
		int rowsDeleted = service.deleteByListId(testListId2);
		assertTrue(1==rowsDeleted);
		
		assertTrue(0==service.deleteBySenderId("DoesNotexist"));
	}
	
	@AfterTransaction
	public void cleanup() {
		eaService.deleteByAddress(emailAddr.getAddress());
		eaService.deleteByRowId(emailAddr2.getRowId());
	}
}
