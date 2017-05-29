package jpa.test.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.SerializationUtils;

import jpa.constant.Constants;
import jpa.model.EmailAddress;
import jpa.msgui.bean.EmailAddressBean;
import jpa.msgui.bean.MsgSessionBean;
import jpa.msgui.util.ClassCrawler;
import jpa.msgui.vo.PagingVo.PageAction;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.MailingListService;
import jpa.util.PrintUtil;

public class EmailAddressBeanTest {
	static final Logger logger = Logger.getLogger(EmailAddressBeanTest.class);
	
	static AbstractApplicationContext applContext = null;
	static EmailAddressService ea_svc = null;
	static MailingListService ml_svc = null;
	
	@BeforeClass
	public static void setupSpring() {
		//PrintClassPath.print();
		applContext = jpa.spring.util.SpringUtil.getAppContext();
		ea_svc = applContext.getBean(EmailAddressService.class);
		ml_svc = applContext.getBean(MailingListService.class);
	}
	
	@Test
	public void testSerialization() {
		EmailAddressBean eab1 = Mockito.mock(EmailAddressBean.class);
		SerializationUtils.serialize(eab1);
		
		EmailAddressBean eab2 = new EmailAddressBean();
		SerializationUtils.serialize(eab2);
		
		Map<Field, Set<String>> badFields = ClassCrawler.initiateCrawling(EmailAddressBean.class);
		assertFalse(badFields.isEmpty());
	}

	@Test
	public void testSessionBean() {
		MsgSessionBean sb = Mockito.mock(MsgSessionBean.class);
		
		EmailAddressBean eab = new EmailAddressBean();

		eab.setSessionBean(sb);
		
		Mockito.when(sb.getSessionParam(Mockito.anyString())).thenReturn("value");
		
		assertEquals("value", eab.getSessionParam("any"));
		
		Mockito.verify(sb, Mockito.times(1)).getSessionParam("any");
	}
	
	@Test
	//@Ignore
	public void testPagination() {
		MsgSessionBean sb = Mockito.mock(MsgSessionBean.class);
		
		EmailAddressBean eab = new EmailAddressBean();
		eab.setSessionBean(sb);
		eab.setEmailAddressService(ea_svc);
		eab.setMailingListService(ml_svc);
		
		assertEquals(0, eab.getPagingVo().getPageNumber());
		assertEquals(PageAction.CURRENT, eab.getPagingVo().getPageAction());
		
		Mockito.when(sb.getSessionParam(Mockito.anyString())).thenReturn("main");
		
		// fetch first page
		DataModel<EmailAddress> dm1 = eab.getEmailAddrs();
		eab.getPagingVo().setRowCount(eab.getRowCount());
		
		assertTrue(dm1.getRowCount() > 0);
		assertEquals(dm1.getClass(), ListDataModel.class);
		ListDataModel<EmailAddress> ldm1 = (ListDataModel<EmailAddress>) dm1;
		@SuppressWarnings("unchecked")
		List<EmailAddress> eaList1 = (List<EmailAddress>) dm1.getWrappedData();
		assertTrue(ldm1.getRowCount() >= eaList1.size());
		assertFalse(eaList1.isEmpty());
		assertFalse(eab.getAnyEmailAddrsMarkedForDeletion());
		for (int i=0; i<eaList1.size(); i++) {
			EmailAddress ea = eaList1.get(i);
			if (i == 0 || i == (eaList1.size() - 1)) {
				ea.setMarkedForDeletion(true);
				EmailAddress ea_all = ea_svc.getAllDataByAddress(ea.getAddress());
				logger.info("EmailAddress: " + PrintUtil.prettyPrint(ea_all));
			}
		}
		assertTrue(eab.getAnyEmailAddrsMarkedForDeletion());
		
		// disable pagingVo reset
		Mockito.when(sb.getSessionParam(Mockito.anyString())).thenReturn("nav");
		
		EmailAddress ea1 = dm1.getRowData();
		assertNotNull(ea1);
		assertNotNull(ea1.getRowId());
		assertTrue(ea1.getRowId() > 0);
		String addr1 = eab.findAddressByRowId(ea1.getRowId().toString());
		assertTrue(StringUtils.isNotBlank(addr1));
		assertEquals(addr1, ea1.getAddress());
		
		// test paging
		if ( eab.getPagingVo().getRowCount() > eab.getPagingVo().getPageSize()) {
			assertTrue(eab.getLastPageRow() == eab.getPagingVo().getPageSize());
			
			eab.pageNext((AjaxBehaviorEvent)null);
			assertEquals(1, eab.getPagingVo().getPageNumber());
			assertEquals(PageAction.NEXT, eab.getPagingVo().getPageAction());
			
			DataModel<EmailAddress> dm2 = eab.getEmailAddrs();
			
			assertTrue(dm2.getRowCount() > 0);
			@SuppressWarnings("unchecked")
			List<EmailAddress> eaList2 = (List<EmailAddress>) dm2.getWrappedData();
			assertNotEquals(eaList1, eaList2);
			assertNotEquals(eaList1.get(0), eaList2.get(0));
			assertTrue(eab.getLastPageRow() > eab.getPagingVo().getPageSize());
			
			eab.pagePrevious((AjaxBehaviorEvent)null);
			assertEquals(0, eab.getPagingVo().getPageNumber());
			assertEquals(PageAction.PREVIOUS, eab.getPagingVo().getPageAction());
			
			DataModel<EmailAddress> dm3 = eab.getEmailAddrs();
			
			assertTrue(dm3.getRowCount() > 0);
			@SuppressWarnings("unchecked")
			List<EmailAddress> eaList3 = (List<EmailAddress>) dm3.getWrappedData();
			assertEquals(eaList1.size(), eaList3.size());
			for (int i=0; i<eaList1.size(); i++) {
				assertEmailAddressesSame(eaList1.get(i), eaList3.get(i));
			}
		}
		else {
			logger.warn("Number of email addresses is not less than a page !!!");
			assertTrue(eab.getLastPageRow() > eab.getPagingVo().getRowCount());
		}
		
		// test saveEmailAddr()
		EmailAddress email_addr = eab.getEmailAddr();
		eab.setEditMode(true);
		int bounceCountBefore = email_addr.getBounceCount();
		email_addr.setBounceCount(bounceCountBefore + 1);
		email_addr.setLastBounceTime(new java.sql.Timestamp(System.currentTimeMillis()));
		eab.saveEmailAddr();
		assertNotEquals(bounceCountBefore, eab.getEmailAddr().getBounceCount());

	}

	@Test
	public void testInsertDeleteEmailAddr() {
		MsgSessionBean sb = Mockito.mock(MsgSessionBean.class);
		
		EmailAddressBean eab = new EmailAddressBean();
		eab.setSessionBean(sb);
		eab.setEmailAddressService(ea_svc);
		eab.setMailingListService(ml_svc);
		
		assertEquals(0, eab.getPagingVo().getPageNumber());
		assertEquals(PageAction.CURRENT, eab.getPagingVo().getPageAction());
		
		Mockito.when(sb.getSessionParam(Mockito.anyString())).thenReturn("main");
		
		// fetch first page
		DataModel<EmailAddress> dm1 = eab.getEmailAddrs();
		
		assertTrue(dm1.getRowCount() > 0);
		assertEquals(dm1.getClass(), ListDataModel.class);
		ListDataModel<EmailAddress> ldm1 = (ListDataModel<EmailAddress>) dm1;
		@SuppressWarnings("unchecked")
		List<EmailAddress> eaList1 = (List<EmailAddress>) dm1.getWrappedData();
		assertTrue(ldm1.getRowCount() >= eaList1.size());
		assertFalse(eaList1.isEmpty());
		
		Mockito.when(sb.getSessionParam(Mockito.anyString())).thenReturn("nav");
		
		// test viewEmailAddr()
		assertEquals("emailAddressEdit.xhtml", eab.viewEmailAddr());
		assertTrue(eab.getEmailAddr().isMarkedForEdition());
		String addr_str = eab.getEmailAddr().getAddress();
		eab.getEmailAddrs().setRowIndex(eab.getEmailAddrs().getRowIndex() + 1);
		eab.viewEmailAddr();
		assertNotEquals(addr_str, eab.getEmailAddr().getAddress());
		
		// test insert
		eab.addEmailAddr();
		EmailAddress email_addr = eab.getEmailAddr();
		if (eab.isEditMode() == false) { // insert
			email_addr.setAddress("emailaddrbean@insert.test");
			email_addr.setStatusChangeTime(new java.sql.Timestamp(System.currentTimeMillis()));
			email_addr.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
		}
		int bounceCountBefore = email_addr.getBounceCount();
		email_addr.setBounceCount(bounceCountBefore + 1);
		email_addr.setLastBounceTime(new java.sql.Timestamp(System.currentTimeMillis()));
		eab.saveEmailAddr();
		assertNotEquals(bounceCountBefore, eab.getEmailAddr().getBounceCount());
		
		// test delete
		long rowCountBefore = eab.getPagingVo().getRowCount();
		for (EmailAddress ea : eaList1) {
			ea.setMarkedForDeletion(false);
		}
		email_addr.setMarkedForDeletion(true);
		eab.deleteEmailAddrs(null);
		assertTrue(rowCountBefore > eab.getPagingVo().getRowCount());
	}
	
	static void assertEmailAddressesSame(EmailAddress ea1, EmailAddress ea2) {
		assertNotNull(ea1);
		assertNotNull(ea2);
		assertEquals(ea1.getAddress(), ea2.getAddress());
		assertEquals(ea1.getBounceCount(), ea2.getBounceCount());
		assertEquals(ea1.getClickCount(), ea2.getClickCount());
		assertEquals(ea1.getCurrAddress(), ea2.getCurrAddress());
		assertEquals(ea1.getLastBounceTime(), ea2.getLastBounceTime());
		assertEquals(ea1.getLastRcptTime(), ea2.getLastRcptTime());
		assertEquals(ea1.getLastSentTime(), ea2.getLastSentTime());
		assertEquals(ea1.getOpenCount(), ea2.getOpenCount());
		assertEquals(ea1.getOrigAddress(), ea2.getOrigAddress());
		assertEquals(ea1.getRowId(), ea2.getRowId());
		assertEquals(ea1.getRuleName(), ea2.getRuleName());
		assertEquals(ea1.getSentCount(), ea2.getSentCount());
		assertEquals(ea1.getStatusChangeTime(), ea2.getStatusChangeTime());
		assertEquals(ea1.getStatusChangeUserId(), ea2.getStatusChangeUserId());
		assertEquals(ea1.getStatusId(), ea2.getStatusId());
		assertEquals(ea1.getUpdtTime(), ea2.getUpdtTime());
		assertEquals(ea1.getUpdtUserId(), ea2.getUpdtUserId());
	}
}
