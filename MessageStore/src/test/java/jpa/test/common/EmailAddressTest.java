package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import jpa.constant.Constants;
import jpa.constant.RuleCriteria;
import jpa.constant.StatusId;
import jpa.model.EmailAddress;
import jpa.model.SubscriberData;
import jpa.msgui.vo.PagingVo;
import jpa.msgui.vo.PagingVo.PageAction;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriberDataService;
import jpa.spring.util.BoTestBase;
import jpa.util.BeanCopyUtil;
import jpa.util.EmailAddrUtil;
import jpa.util.PrintUtil;

public class EmailAddressTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(EmailAddressTest.class);

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void EmailAddrPrepare() {
		logger.info("########## EmailAddressTest");
	}

	@Autowired
	EmailAddressService service;

	@Autowired
	SubscriberDataService subrService;

	private String testEmailAddr1 = "jpatest1@localhost";
	private String testEmailAddr2 = "jpatest2@localhost";

	@Test
	public void testInsertEmailAddress() {
		// test insert
		EmailAddress rcd1 = new EmailAddress();
		rcd1.setAddress(testEmailAddr1);
		rcd1.setOrigAddress(testEmailAddr1);
		rcd1.setStatusId(StatusId.ACTIVE.getValue());
		rcd1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd1);
		assertTrue(rcd1.getRowId() > 0);
		EmailAddress rowInserted = service.getByRowId(rcd1.getRowId());
		logger.info("EmailAddress inserted : " + PrintUtil.prettyPrint(rowInserted));
		assertTrue(1==service.updateLastRcptTime(rcd1.getRowId()));
		assertTrue(1==service.deleteByRowId(rcd1.getRowId()));
		assertNull(service.getByRowId(rcd1.getRowId()));
	}

	@Test
	public void testGetListByPagingVo() {
		// test paging for UI application
		int rowId = service.getRowIdForPreview();
		assertTrue(rowId>0);
		PagingVo vo = new PagingVo();
		vo.setPageSize(5);
		vo.setStatusId(StatusId.ACTIVE.getValue());
		vo.setSearchCriteria(PagingVo.Column.bounceCount, new PagingVo.Criteria(RuleCriteria.GE, 0, PagingVo.Column.bounceCount.getDataType()));
		PagingVo.Criteria addrCriteria = new PagingVo.Criteria(RuleCriteria.REG_EX, "test.com tesstuser", PagingVo.MatchBy.AnyWords);
		vo.getSearchBy().setCriteria(PagingVo.Column.origAddress, addrCriteria);
		vo.getOrderBy().setOrderBy(PagingVo.Column.address, Boolean.TRUE);
		logger.info("####################################################");
		List<EmailAddress> listpg1 = service.getAddrListByPagingVo(vo);
		assertTrue(listpg1.size()>0);
		EmailAddress addrObj = listpg1.get(0);
		logger.info("getAddrListByPagingVo() : " + PrintUtil.prettyPrint(addrObj));
		assertNotNull(addrObj.getSentCount());
		assertNotNull(addrObj.getOpenCount());
		assertNotNull(addrObj.getClickCount());
		int count = service.getEmailAddressCount(vo);
		assertTrue(count>=listpg1.size());
		SerializationUtils.serialize(listpg1.get(0));
		
		vo.setPageAction(PageAction.NEXT);
		List<EmailAddress> listpg2 = service.getAddrListByPagingVo(vo);
		assertTrue(listpg2.size()>=0);
	}

	@Test
	public void testGetPageByPagingVo() {
		PagingVo vo = new PagingVo();
		vo.setStatusId(StatusId.ACTIVE.getValue());
		vo.setSearchCriteria(PagingVo.Column.bounceCount, new PagingVo.Criteria(RuleCriteria.GE, 0, PagingVo.Column.bounceCount.getDataType()));
		PagingVo.Criteria addrCriteria = new PagingVo.Criteria(RuleCriteria.REG_EX, "test.com tesstuser", PagingVo.MatchBy.AnyWords);
		vo.getSearchBy().setCriteria(PagingVo.Column.origAddress, addrCriteria);
		vo.getOrderBy().setOrderBy(PagingVo.Column.updtTime, Boolean.FALSE);
		logger.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		Page<EmailAddress> page = service.getPageByPagingVo(vo);
		assertTrue(page.getNumberOfElements() >= 2);
		for (EmailAddress addr : page.getContent()) {
			logger.info("getPageByPagingVo():" + PrintUtil.prettyPrint(addr, 1));
			break;
		}
		assertFalse(page.getContent().get(0).getUpdtTime().before(page.getContent().get(1).getUpdtTime()));
	}

	@Test
	public void testEmailAddressService() {
		assertNotNull(entityManager);
		// test get by address pattern
		List<EmailAddress> lst1 = service.getByAddressPattern("@localhost|@test.com$");
		assertFalse(lst1.isEmpty());
		List<EmailAddress> lst2 = service.getByAddressPattern("@test.com$");
		assertFalse(lst2.isEmpty());
		assertTrue(lst1.size()>lst2.size());
		EmailAddress rcd2 = lst2.get(0);
		assertNotNull(rcd2);
		boolean isAcceptHtml = rcd2.isAcceptHtml();
		assertTrue(1==service.updateAcceptHtml(rcd2.getRowId(), !isAcceptHtml));
		rcd2 = service.getByRowId(rcd2.getRowId());
		assertFalse(isAcceptHtml == rcd2.isAcceptHtml());
		
		lst2 = service.getByAddressDomain(EmailAddrUtil.getEmailDomainName(rcd2.getAddress()));
		assertFalse(lst2.isEmpty());
		lst2 = service.getByAddressUser(EmailAddrUtil.getEmailUserName(rcd2.getAddress()));
		assertFalse(lst2.isEmpty());
		
		EmailAddress obj = service.getByAddressWithCounts(lst2.get(0).getAddress());
		logger.info("getByAddressWithCounts:" + PrintUtil.prettyPrint(obj));
		assertNotNull(obj.getClickCount());
		assertNotNull(obj.getOpenCount());
		
		assertNull(service.getByAddressWithCounts("fake_addr@does.not.exist"));
		
		// test update
		rcd2.setUpdtUserId("JpaTest");
		service.update(rcd2);
		EmailAddress rcd3 = service.getByRowId(rcd2.getRowId());
		assertTrue("JpaTest".equals(rcd2.getUpdtUserId()));

		List<SubscriberData> subr_lst = subrService.getAll();
		assertFalse(subr_lst.isEmpty());
		
		// test insert with subscriber data
		EmailAddress rcd4 = new EmailAddress();
		BeanCopyUtil.registerBeanUtilsConverters();
		try {
			BeanUtils.copyProperties(rcd4, rcd3);
		}
		catch (Exception e) {
			logger.error("Exception caught during bean copy", e);
			fail();
		}
		rcd4.setAddress(testEmailAddr2);
		rcd4.setOrigAddress(testEmailAddr2);
		rcd4.setSubscriberData(subr_lst.get(0));
		/*
		 * Must setSubscriptions to null to avoid this Hibernate error:
		 * "Found shared references to a collection: jpa.model.EmailAddress.subscriptionList"
		 */
		rcd4.setSubscriptions(null);
		service.insert(rcd4);
		assertTrue(rcd4.getRowId() > 0);
		
		// test update bounce count
		int bounceCount = rcd4.getBounceCount();
		for (int i=0; i<Constants.BOUNCE_SUSPEND_THRESHOLD; i++) {
			service.updateBounceCount(rcd4);
		}
		
		EmailAddress rcd5 = service.getByAddress(testEmailAddr2);
		logger.info("testUpdateBounceCount:" + PrintUtil.prettyPrint(rcd5,2));
		assertNotNull(rcd5.getSubscriberData()); // TODO investigate with EclipseLink
		assertTrue(rcd5.getBounceCount()==(bounceCount+Constants.BOUNCE_SUSPEND_THRESHOLD));
		assertTrue(StatusId.SUSPENDED.getValue().equals(rcd5.getStatusId()));
		
		// test delete
		service.delete(rcd4);
		EmailAddress deleted = service.getByRowId(rcd4.getRowId());
		if (deleted != null) {
			fail();
		}
	}

	@Test
	public void testFindSert() {
		EmailAddress rcd6 = service.findSertAddress("jpatest3@localhost");
		assertNotNull(rcd6);
		assertTrue(rcd6.getRowId()>0);
		logger.info("testFindSert-1" + PrintUtil.prettyPrint(rcd6));
		assertTrue(1==service.updateLastSentTime(rcd6.getRowId()));
		assertTrue(0==service.updateLastSentTime(0));
		assertTrue(1==service.updateStatus(rcd6.getAddress(), StatusId.SUSPENDED));
		assertTrue(0==service.updateStatus("invalid-address@invalid.com", StatusId.SUSPENDED));
		
		EmailAddress rcd7 = service.findSertAddress("jpatest5@localhost");
		assertNotNull(rcd7);
		assertTrue(rcd7.getRowId()>0);
		logger.info("testFindSert-2" + PrintUtil.prettyPrint(rcd7));

		assertTrue(1==service.deleteByAddress(rcd6.getAddress()));
		assertTrue(1==service.deleteByRowId(rcd7.getRowId()));
		assertNull(service.getByAddress(rcd6.getAddress()));
	}

}
