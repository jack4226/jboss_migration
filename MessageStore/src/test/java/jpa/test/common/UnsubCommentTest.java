package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.UnsubComment;
import jpa.service.common.EmailAddressService;
import jpa.service.common.UnsubCommentService;
import jpa.service.maillist.MailingListService;
import jpa.spring.util.BoTestBase;
import jpa.util.BeanCopyUtil;

public class UnsubCommentTest extends BoTestBase {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void UnsubCommentPrepare() {
	}

	@Autowired
	UnsubCommentService service;
	
	@Autowired
	EmailAddressService emailService;
	@Autowired
	MailingListService mlistService;

	private String testUnsubComment1 = "jpa test comment 1";
	private String testUnsubComment2 = "jpa test comment 2";
	
	EmailAddress emailAddr = null;
	MailingList mlist = null;
	@Before
	public void prepare() {
		emailAddr = emailService.findSertAddress("jpatest1@localhost");
		List<MailingList> list = mlistService.getAll(false);
		if (!list.isEmpty()) {
			mlist = list.get(0);
		}
	}
	
	@Test
	public void testUnsubCommentService() {
		assertNotNull(entityManager);
		// test insert
		UnsubComment rcd1 = new UnsubComment();
		rcd1.setEmailAddress(emailAddr);
		rcd1.setMailingList(mlist);
		rcd1.setComments(testUnsubComment1);
		rcd1.setStatusId(StatusId.ACTIVE.getValue());
		rcd1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(rcd1);
		
		List<UnsubComment> lst1 = service.getByAddress(emailAddr.getAddress());
		assertFalse(lst1.isEmpty());
		UnsubComment rcd2 = lst1.get(0);
		assertNotNull(rcd2);
		
		List<UnsubComment> lst2 = service.getByAddressAndListId(emailAddr.getAddress(), lst1.get(0).getMailingList().getListId());
		assertFalse(lst2.isEmpty());
		
		assertTrue(lst2.size()<=service.getByMailingListId(lst1.get(0).getMailingList().getListId()).size());
		
		// test update
		rcd2.setUpdtUserId("JpaTest");
		service.update(rcd2);
		UnsubComment rcd3 = service.getByRowId(rcd2.getRowId());
		assertTrue("JpaTest".equals(rcd2.getUpdtUserId()));
		
		UnsubComment rcd4 = new UnsubComment();
		BeanCopyUtil.registerBeanUtilsConverters();
		try {
			BeanUtils.copyProperties(rcd4, rcd3);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		rcd4.setComments(testUnsubComment2);
		service.insert(rcd4);
		
		List<UnsubComment> lst3 = service.getByAddress(rcd4.getEmailAddress().getAddress());
		assertTrue(lst3.size()==2);
		
		// test delete
		service.delete(rcd4);
		assertTrue(1<=service.deleteByAddress(rcd2.getEmailAddress().getAddress()));
		assertTrue(0<=service.deleteByRowId(rcd3.getRowId()));
	}
}
