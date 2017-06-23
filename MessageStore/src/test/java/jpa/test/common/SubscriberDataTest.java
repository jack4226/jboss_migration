package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.msgui.vo.PagingSubscriberData;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.common.SubscriberDataService;
import jpa.spring.util.BoTestBase;
import jpa.util.BeanCopyUtil;
import jpa.util.PrintUtil;

public class SubscriberDataTest extends BoTestBase {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void SubscriberDataPrepare() {
	}

	@Autowired
	SubscriberDataService service;
	
	@Autowired
	SenderDataService cdService;
	@Autowired
	EmailAddressService emailService;

	@Test
	public void subscriberDataService() {
		assertNotNull(entityManager);
		
		assertNull(service.getBySubscriberId("sbsr-id-not-exist"));
		
		List<SubscriberData> list = service.getAll();
		assertFalse(list.isEmpty());
		
		SubscriberData rcd0 = service.getBySubscriberId(list.get(0).getSubscriberId());
		assertNotNull(rcd0);
		
		assertNotNull(service.getByEmailAddress(rcd0.getEmailAddress().getAddress()));
		
		// test paging for UI application
		PagingSubscriberData vo = new PagingSubscriberData(new PagingVo());
		vo.getPagingVo().setSearchValue(PagingVo.Column.senderId, rcd0.getSenderData().getSenderId());
		vo.getPagingVo().setSearchValue(PagingVo.Column.address, rcd0.getEmailAddress().getAddress());
		if (StringUtils.isNotBlank(rcd0.getSsnNumber())) {
			vo.getPagingVo().setSearchValue(PagingVo.Column.ssnNumber, rcd0.getSsnNumber());
		}
		if (StringUtils.isNotBlank(rcd0.getDayPhone())) {
			vo.getPagingVo().setSearchValue(PagingVo.Column.dayPhone, rcd0.getDayPhone());
		}
		if (StringUtils.isNotBlank(rcd0.getFirstName())) {
			vo.getPagingVo().setSearchValue(PagingVo.Column.firstName, rcd0.getFirstName());
		}
		if (StringUtils.isNotBlank(rcd0.getLastName())) {
			vo.getPagingVo().setSearchValue(PagingVo.Column.lastName, rcd0.getLastName());
		}
		List<SubscriberData> listPg = service.getSubscribersWithPaging(vo);
		assertTrue(listPg.size()>0);
		logger.info(PrintUtil.prettyPrint(listPg.get(0)));
		long count = service.getSubscriberCount(vo);
		assertTrue(count==listPg.size());

		// test update
		rcd0.setUpdtUserId("JpaTest");
		service.update(rcd0);
		SubscriberData rcd1 = service.getByRowId(rcd0.getRowId());
		assertTrue("JpaTest".equals(rcd1.getUpdtUserId()));
		
		// test insert
		SenderData cd2 = cdService.getBySenderId(rcd0.getSenderData().getSenderId());
		SubscriberData rcd2 = new SubscriberData();
		BeanCopyUtil.registerBeanUtilsConverters();
		try {
			// copy properties from rcd1 to rcd2
			BeanUtils.copyProperties(rcd2, rcd1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		rcd2.setSenderData(cd2);
		rcd2.setSubscriberId(rcd1.getSubscriberId()+"_2");
		rcd2.setEmailAddress(emailService.findSertAddress(rcd2.getSubscriberId()+"@localhost"));
		service.insert(rcd2);
		
		SubscriberData rcd3 = service.getBySubscriberId(rcd1.getSubscriberId()+"_2");
		assertNotNull(rcd3);
		assertTrue(rcd1.getRowId()!=rcd3.getRowId());
		
		assertTrue(1==service.deleteBySubscriberId(rcd3.getSubscriberId()));
		assertTrue(1==service.deleteByRowId(rcd1.getRowId()));
	}
}
