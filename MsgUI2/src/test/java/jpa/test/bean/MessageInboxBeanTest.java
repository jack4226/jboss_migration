package jpa.test.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.SerializationUtils;

import jpa.constant.MsgDirectionCode;
import jpa.constant.RuleCriteria;
import jpa.data.preload.FolderEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.model.msg.MessageInbox;
import jpa.msgui.bean.MessageInboxBean;
import jpa.msgui.bean.MsgSessionBean;
import jpa.msgui.bean.SimpleMailTrackingMenu;
import jpa.msgui.util.ClassCrawler;
import jpa.msgui.util.StaticCodes;
import jpa.msgui.vo.PagingVo;
import jpa.msgui.vo.PagingVo.PageAction;
import jpa.msgui.vo.SearchFieldsVo;
import jpa.service.common.EmailAddressService;
import jpa.service.common.EntityManagerService;
import jpa.service.common.SessionUploadService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgin.MessageInboxBo;
import jpa.service.msgout.MessageBeanBo;
import jpa.service.rule.RuleLogicService;
import jpa.util.PrintUtil;

public class MessageInboxBeanTest {
	static final Logger logger = Logger.getLogger(MessageInboxBeanTest.class);
	
	static AbstractApplicationContext applContext = null;
	
	static MessageInboxService mi_svc = null;
	static EmailAddressService ea_svc = null;
	static RuleLogicService rl_svc = null;
	static SessionUploadService su_svc = null;
	static EntityManagerService em_svc = null;
	static MessageInboxBo mi_bo = null;
	static MessageBeanBo mb_bo = null;
	
	static int pageSize = 5;
	
	@BeforeClass
	public static void setupSpring() {
		//PrintClassPath.print();
		applContext = jpa.spring.util.SpringUtil.getAppContext();
		mi_svc = applContext.getBean(MessageInboxService.class);
		ea_svc = applContext.getBean(EmailAddressService.class);
		rl_svc = applContext.getBean(RuleLogicService.class);
		su_svc = applContext.getBean(SessionUploadService.class);
		em_svc = applContext.getBean(EntityManagerService.class);
		mi_bo = applContext.getBean(MessageInboxBo.class);
		mb_bo = applContext.getBean(MessageBeanBo.class);
	}

	
	@Test
	public void testSerialization() {
		MessageInboxBean mib1 = Mockito.mock(MessageInboxBean.class);
		SerializationUtils.serialize(mib1);
		
		MessageInboxBean mib2 = new MessageInboxBean();
		SerializationUtils.serialize(mib2);
		
		StaticCodes sc = new StaticCodes();
		SerializationUtils.serialize(sc);
		
		Map<Field, Set<String>> badFields = ClassCrawler.initiateCrawling(MessageInboxBean.class);
		assertFalse(badFields.isEmpty());
	}

	@Test
	public void testSessionBean() {
		MsgSessionBean sb = Mockito.mock(MsgSessionBean.class);
		
		MessageInboxBean eab = new MessageInboxBean();
		eab.setSessionBean(sb);
		
		Mockito.when(sb.getSessionParam(Mockito.anyString())).thenReturn("value");
		
		assertEquals("value", eab.getSessionParam("any"));
		
		Mockito.verify(sb, Mockito.times(1)).getSessionParam("any");
	}

	@Test
	//@org.junit.Ignore
	public void testPagination() {
		MsgSessionBean sb = Mockito.mock(MsgSessionBean.class);
		
		MessageInboxBean mib = new MessageInboxBean();
		
		mib.getPagingVo().setPageSize(pageSize);
		
		mib.setSessionBean(sb);
		mib.setMessageInboxService(mi_svc);
		mib.setEmailAddressService(ea_svc);
		mib.setRuleLogicService(rl_svc);
		mib.setSessionUploadService(su_svc);
		mib.setEntityManagerService(em_svc);
		mib.setMessageInboxBo(mi_bo);
		mib.setMessageBeanBo(mb_bo);
		
		assertEquals(0, mib.getPagingVo().getPageNumber());
		assertEquals(PageAction.CURRENT, mib.getPagingVo().getPageAction());
		
		Mockito.when(sb.getSessionParam("frompage")).thenReturn("main");
		
		SimpleMailTrackingMenu mailTracking = Mockito.mock(SimpleMailTrackingMenu.class);
		PagingVo menuPagingVo = new PagingVo();
		menuPagingVo.setPageSize(pageSize);
		Mockito.when(mailTracking.getPagingVo()).thenReturn(menuPagingVo);
		Mockito.when(sb.getSessionParam("mailTracking")).thenReturn(mailTracking);
		
		Mockito.when(mailTracking.getSearchFieldsVo()).thenReturn(new SearchFieldsVo(menuPagingVo));
		
		mib.getPagingVo().setOrderBy(PagingVo.Column.rowId, false);
		
		// fetch first page
		DataModel<MessageInbox> dm1 = mib.getAll();
		assertTrue(dm1.getRowCount() > 0);
		assertEquals(dm1.getClass(), ListDataModel.class);
		
		int idx = 0;
		for (Iterator<MessageInbox> it=dm1.iterator(); it.hasNext();) {
			MessageInbox mi = it.next();
			int level = idx == 0 ? 4 : 1;
			logger.info("MessageInbox[" + (idx++) + "]: " + PrintUtil.prettyPrint(mi, level));
		}
		assertTrue(idx <= pageSize);
		
		mib.getPagingVo().setPageAction(PageAction.NEXT);
		DataModel<MessageInbox> dm2 = mib.getAll();
		assertTrue(dm2.getRowCount() >= 0);
		
		if (dm2.getRowCount() > 0) {
			logger.info("Found second page !!!");
			@SuppressWarnings("unchecked")
			List<MessageInbox> list1 = (List<MessageInbox>) dm1.getWrappedData();
			@SuppressWarnings("unchecked")
			List<MessageInbox> list2 = (List<MessageInbox>)dm2.getWrappedData();
			org.junit.Assert.assertNotEquals(list1.get(0).getRowId(), list2.get(0).getRowId());
			
			mib.getPagingVo().setPageAction(PageAction.PREVIOUS);
			DataModel<MessageInbox> dm3 = mib.getAll();
			@SuppressWarnings("unchecked")
			List<MessageInbox> list3 = (List<MessageInbox>)dm3.getWrappedData();
			assertMessageInboxesSame(list1.get(0), list3.get(0));
		}
	}
	
	@Test
	public void testSearch() {
		MsgSessionBean sb = Mockito.mock(MsgSessionBean.class);
		
		MessageInboxBean mib = new MessageInboxBean();
		
		mib.getPagingVo().setPageSize(pageSize);
		
		mib.setSessionBean(sb);
		mib.setMessageInboxService(mi_svc);
		mib.setEmailAddressService(ea_svc);
		mib.setRuleLogicService(rl_svc);
		mib.setSessionUploadService(su_svc);
		mib.setEntityManagerService(em_svc);
		mib.setMessageInboxBo(mi_bo);
		mib.setMessageBeanBo(mb_bo);
		
		assertEquals(0, mib.getPagingVo().getPageNumber());
		assertEquals(PageAction.CURRENT, mib.getPagingVo().getPageAction());
		
		Mockito.when(sb.getSessionParam("frompage")).thenReturn("main");
		
		SimpleMailTrackingMenu mailTracking = Mockito.mock(SimpleMailTrackingMenu.class);
		PagingVo menuPagingVo = new PagingVo();
		menuPagingVo.setPageSize(pageSize);
		Mockito.when(mailTracking.getPagingVo()).thenReturn(menuPagingVo);
		Mockito.when(sb.getSessionParam("mailTracking")).thenReturn(mailTracking);
		
		SearchFieldsVo menuSrchVo = new SearchFieldsVo(menuPagingVo);
		Mockito.when(mailTracking.getSearchFieldsVo()).thenReturn(menuSrchVo);
		
		SearchFieldsVo beanSrchVo = mib.getSearchFieldVo();
		assertEquals(mib.getPagingVo(), beanSrchVo.getPagingVo());

		RuleNameEnum testRule = RuleNameEnum.GENERIC;
		menuSrchVo.setRuleName(testRule.getValue());
		menuSrchVo.setIsRead(false);
		menuSrchVo.setIsFlagged(false);
		
		MsgDirectionCode msgDir = MsgDirectionCode.SENT;
		menuSrchVo.getPagingVo().setSearchCriteria(PagingVo.Column.msgDirection, new PagingVo.Criteria(RuleCriteria.EQUALS, msgDir.getValue()));
		if (MsgDirectionCode.SENT.equals(msgDir)) {
			menuSrchVo.setFolderType(FolderEnum.Sent);
		}
		
		// fetch search result
		DataModel<MessageInbox> dm1 = mib.getAll();
		assertTrue(dm1.getRowCount() > 0);
		assertEquals(dm1.getClass(), ListDataModel.class);
		
		int idx = 0;
		for (Iterator<MessageInbox> it=dm1.iterator(); it.hasNext();) {
			MessageInbox mi = it.next();
			//logger.info("MessageInbox[" + (idx++) + "]: " + PrintUtil.prettyPrint(mi, 2));
			assertEquals(testRule.getValue(), mi.getRuleLogic().getRuleName());
			assertEquals(0, mi.getReadCount());
			assertEquals(false, mi.isFlagged());
			assertEquals(msgDir.getValue(), mi.getMsgDirection());
		}
		assertTrue(idx <= pageSize);
		assertTrue(menuSrchVo.equalsLevel1(beanSrchVo));
	}
	
	void assertMessageInboxesSame(MessageInbox mi1, MessageInbox mi2) {
		assertEquals(mi1.getRowId(), mi2.getRowId());
		assertEquals(mi1.getMsgSubject(), mi2.getMsgSubject());
		assertEquals(mi1.getMsgBody(), mi2.getMsgBody());
		assertEquals(mi1.getMsgBodySize(), mi2.getMsgBodySize());
		assertEquals(mi1.getMsgContentType(), mi2.getMsgContentType());
		assertEquals(mi1.getStatusId(), mi2.getStatusId());
		assertEquals(mi1.getCarrierCode(), mi2.getCarrierCode());
	}

}
