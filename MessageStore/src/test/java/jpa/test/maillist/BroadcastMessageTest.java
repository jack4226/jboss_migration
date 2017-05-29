package jpa.test.maillist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import jpa.constant.Constants;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.StatusId;
import jpa.model.BroadcastMessage;
import jpa.msgui.vo.PagingVo;
import jpa.msgui.vo.PagingVo.PageAction;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.BroadcastMessageService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class BroadcastMessageTest extends BoTestBase {
	static Logger logger = Logger.getLogger(BroadcastMessageTest.class);

	@BeforeClass
	public static void BroadcastMessagePrepare() {
	}

	@Autowired
	BroadcastMessageService service;
	
	@Autowired
	EmailAddressService eaService;

	@Test
	public void testBroadcastMessageService() {
		
		List<BroadcastMessage> bdlist = service.getAll();
		assertFalse(bdlist.isEmpty());
		
		BroadcastMessage bd1 = bdlist.get(0);
		
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		bd1.setUpdtTime(ts);
		bd1.setClickCount(bd1.getClickCount()+1);
		bd1.setOpenCount(bd1.getOpenCount()+1);
		bd1.setSentCount(bd1.getSentCount()+1);
		bd1.setComplaintCount(bd1.getComplaintCount()+1);
		bd1.setUnsubscribeCount(bd1.getUnsubscribeCount()+1);
		bd1.setReferralCount(bd1.getReferralCount()+1);
		bd1.setLastClickTime(ts);
		bd1.setLastOpenTime(ts);
		bd1.setMsgSubject("Test Broadcast message # 1");
		bd1.setMsgBody("Test Broadcast message body here.\n" + bd1.getMsgBody());
		service.update(bd1);
		
		assertTrue(1<=service.updateReferalCount(bd1.getRowId()));
		assertTrue(1<=service.updateSentCount(bd1.getRowId(), 2));
		assertTrue(1<=service.updateOpenCount(bd1.getRowId()));
		assertTrue(1<=service.updateClickCount(bd1.getRowId()));
		assertTrue(1<=service.updateUnsubscribeCount(bd1.getRowId()));
		
		BroadcastMessage bd2 = service.getByRowId(bd1.getRowId());
		assertTrue(ts.equals(bd2.getUpdtTime()));
		System.out.println(PrintUtil.prettyPrint(bd2, 2));
		
		List<BroadcastMessage> bdlist2 = service.getByMailingListId(bd1.getMailingList().getListId());
		assertFalse(bdlist2.isEmpty());
		
		List<BroadcastMessage> bdlist3 = service.getByEmailTemplateId(bd1.getEmailTemplate().getTemplateId());
		assertFalse(bdlist3.isEmpty());
		
		bdlist3 = service.getByListIdAndTemplateId(bd1.getMailingList().getListId(), bd1.getEmailTemplate().getTemplateId());
		assertFalse(bdlist3.isEmpty());
		
		BroadcastMessage bd3 = new BroadcastMessage();
		bd3.setMailingList(bd1.getMailingList());
		bd3.setEmailTemplate(bd1.getEmailTemplate());
		bd3.setDeliveryType(MailingListDeliveryType.ALL_ON_LIST.getValue());
		bd3.setStatusId(StatusId.ACTIVE.getValue());
		bd3.setUpdtUserId(Constants.DEFAULT_USER_ID);
		bd3.setStartTime(ts);
		bd3.setUpdtTime(ts);
		service.insert(bd3);
		
		assertTrue(bd3.getRowId()>0  && bd3.getRowId()!=bd1.getRowId());
		service.delete(bd3);
		assert(0==service.deleteByRowId(bd3.getRowId()));
		assertNull(service.getByRowId(bd3.getRowId()));
	}

	@Test
	public void testBroadcastMessageForWeb() {
		List<BroadcastMessage> bdlist = service.getAll();
		assertFalse(bdlist.isEmpty());
		
		BroadcastMessage bd1 = bdlist.get(0);
		bd1.setSentCount(bd1.getSentCount()+1);
		service.update(bd1);
		
		int count = service.getMessageCountForWeb();
		assertTrue(count>=1);
		
		PagingVo vo = new PagingVo();
		int pageSize = count > vo.getPageSize() ? vo.getPageSize() : (count > 2 ? count - 2 : count);
		vo.setPageSize(pageSize);
		
		vo.getOrderBy().setOrderBy(PagingVo.Column.rowId, false);
		Page<BroadcastMessage> pageList1 = service.getMessageListForWeb(vo);
		assertFalse(pageList1.getContent().isEmpty());
		assertEquals(pageSize, pageList1.getNumberOfElements());
		
		assertTrue(count >= pageList1.getNumberOfElements());
		logger.info("Page 1: " + PrintUtil.prettyPrint(pageList1));
		
		if (count > pageSize) {
			vo.setPageAction(PageAction.NEXT);
			Page<BroadcastMessage> pageList2 = service.getMessageListForWeb(vo);
			assertTrue(pageList2.getNumberOfElements() > 0);
			org.junit.Assert.assertNotEquals(pageList1.getContent().get(0), pageList2.getContent().get(0));
			logger.info("Page 1 - Element 1: " + PrintUtil.prettyPrint(pageList1.getContent().get(0), 1));
			logger.info("Page 2 - Element 1: " + PrintUtil.prettyPrint(pageList2.getContent().get(0), 1));
			
			vo.setPageAction(PageAction.PREVIOUS);
			Page<BroadcastMessage> pageList3 = service.getMessageListForWeb(vo);
			assertEquals(pageList1.getNumberOfElements(), pageList3.getNumberOfElements());
			for (int i=0; i<pageList1.getNumberOfElements(); i++) {
				assertEquals(pageList1.getContent().get(i), pageList3.getContent().get(i));
			}
		}
	}
}
