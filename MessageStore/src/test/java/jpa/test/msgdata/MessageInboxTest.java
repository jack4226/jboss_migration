package jpa.test.msgdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.constant.MsgStatusCode;
import jpa.constant.RuleCriteria;
import jpa.constant.XHeaderName;
import jpa.data.preload.FolderEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.SenderData;
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageHeader;
import jpa.model.msg.MessageHeaderPK;
import jpa.model.msg.MessageInbox;
import jpa.model.msg.MessageUnsubComment;
import jpa.model.rule.RuleLogic;
import jpa.msgui.vo.PagingVo;
import jpa.msgui.vo.SearchFieldsVo;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.maillist.MailingListService;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageHeaderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

@org.springframework.test.annotation.Commit
public class MessageInboxTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(MessageInboxTest.class);

	@BeforeClass
	public static void MessageInboxPrepare() {
	}

	@Autowired
	private MessageInboxService service;
	@Autowired
	private EmailAddressService addrService;
	@Autowired
	private SenderDataService senderService;
	@Autowired
	private RuleLogicService logicService;
	@Autowired
	private MessageHeaderService headerService;
	@Autowired
	private MailingListService listService;
	@Autowired
	private MessageFolderService folderService;

	private static String testFromAddr = "test@test.com";
	private static EmailAddress from_addr;
	private static String testToAddr;
	private static EmailAddress to_addr;
	private static Integer msg1RowId;
	
	@Before
	public void insertAndUpdate() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		String suffix = StringUtils.leftPad(new Random().nextInt(10000) + "", 4, '0');
		
		MessageInbox in = new MessageInbox();
		
		in.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		in.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
		in.setMsgSubject("Test Subject - " + suffix);
		in.setMsgPriority("2 (Normal)");
		in.setReceivedTime(updtTime);
		
		from_addr = addrService.findSertAddress(testFromAddr);
		in.setFromAddress(from_addr);
		in.setReplytoAddress(null);

		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		testToAddr = sender.getReturnPathLeft() + "@" + sender.getDomainName();
		to_addr = addrService.findSertAddress(testToAddr);
		in.setToAddress(to_addr);
		in.setSenderData(sender);
		in.setSubscriberData(null);
		in.setPurgeDate(null);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.GENERIC.getValue());
		in.setRuleLogic(logic);
		in.setMsgContentType("multipart/mixed");
		in.setBodyContentType("text/plain");
		in.setMsgBody("Test Message Body - " + suffix);
		
		MessageHeader hdr1 = new MessageHeader();
		MessageHeaderPK pk1 = new MessageHeaderPK(in,1);
		hdr1.setMessageHeaderPK(pk1);
		hdr1.setHeaderName(XHeaderName.MAILER.value());
		hdr1.setHeaderValue("Mailserder");
		in.getMessageHeaderList().add(hdr1);

		List<MailingList> mlists=listService.getAll(true);
		MailingList mlist=mlists.get(0);
		
		MessageUnsubComment cmt1 = new MessageUnsubComment();
		cmt1.setMessageInbox(in);
		cmt1.setComments("jpa test unsub comment 1 - " + suffix);
		cmt1.setEmailAddr(from_addr);
		cmt1.setMailingList(mlist);
		in.setMessageUnsubComment(cmt1);

		MessageFolder folder = folderService.getOneByFolderName(FolderEnum.Inbox.name());
		in.setMessageFolder(folder);
		service.insert(in);
		
		MessageInbox msg1 = service.getByRowId(in.getRowId());
		assertNotNull(msg1);
		assertNotNull(msg1.getLeadMessageRowId());
		assertNotNull(msg1.getRuleLogic());
		int readcount = msg1.getReadCount();
		msg1.setReadCount(msg1.getReadCount()+1);
		service.updateReadCount(msg1);
		logger.info("MessageInbox inserted: " + PrintUtil.prettyPrint(msg1,2));
		msg1RowId = msg1.getRowId();
		msg1 = service.getByRowId(msg1.getRowId());
		assertTrue(msg1.getReadCount()>readcount);
	}
	
	@After
	public void wrapup() {
		//
	}
	
	@Test
	public void testMessageInboxService() {
		assertNotNull(msg1RowId);
		MessageInbox msg1 = service.getByRowId(msg1RowId);
		assertNotNull(msg1);
		assertTrue(testFromAddr.equals(msg1.getFromAddress().getAddress()));
		assertNotNull(msg1.getRuleLogic());
		
		msg1.setReadCount(msg1.getReadCount() + 1);
		service.updateReadCount(msg1);
		
		msg1.setFlagged(!msg1.isFlagged());
		service.updateIsFlagged(msg1);
		
		assertNotNull(msg1.getMessageFolder());
		assertNotNull(msg1.getLeadMessageRowId());
		assertTrue(0 < service.closeMessagesByLeadMsgId(msg1));
		
		msg1.setStatusId(MsgStatusCode.OPENED.getValue());
		assertTrue(0 < service.moveMessageToFolderByStatus(msg1));
		
		String ruleNameBefore = RuleNameEnum.GENERIC.getValue();
		if (msg1.getRuleLogic() != null) {
			ruleNameBefore = msg1.getRuleLogic().getRuleName();
		}
		String ruleNameAfter = RuleNameEnum.HARD_BOUNCE.getValue();
		if (RuleNameEnum.HARD_BOUNCE.getValue().equals(ruleNameBefore)) {
			ruleNameAfter = RuleNameEnum.SOFT_BOUNCE.getValue();
		}
		
		RuleLogic rlAfter = logicService.getByRuleName(ruleNameAfter);
		assertNotNull(rlAfter);
		msg1.setRuleLogic(rlAfter);
		service.update(msg1);
		
		assertEquals(1, service.updateRuleName(msg1.getRowId(), ruleNameAfter));
		
		assertEquals(ruleNameAfter, msg1.getRuleLogic().getRuleName());
		
		RuleLogic rlBefore = logicService.getByRuleName(ruleNameBefore);
		assertNotNull(rlBefore);
		msg1.setRuleLogic(rlBefore);
		//service.update(msg1); // threw Optimistic Locking Failure Exception
		
		assertEquals(1, service.updateRuleName(msg1.getRowId(), ruleNameBefore));
		
		List<MessageInbox> lst1 = service.getByFromAddress(from_addr.getAddress());
		assertFalse(lst1.isEmpty());
		List<MessageInbox> lst2 = service.getByToAddress(to_addr.getAddress());
		assertFalse(lst2.isEmpty());

		List<MessageInbox> lst3 = service.getByLeadMsgId(msg1.getLeadMessageRowId());
		assertFalse(lst3.isEmpty());
		List<MessageInbox> lstAll = new ArrayList<>();
		lstAll.addAll(lst1);
		lstAll.addAll(lst2);
		lstAll.addAll(lst3);
		Integer msgIdWithRefMsg = null;
		for (MessageInbox mi : lstAll) {
			Integer refMsgId = mi.getReferringMessageRowId();
			if (refMsgId != null) {
				logger.info("Retrieving Referring Messages by ReferringMessageRowId: " + refMsgId);
				List<MessageInbox> lst4 = service.getByReferringMsgId(refMsgId);
				assertFalse(lst4.isEmpty());
				MessageInbox refMsg = service.getByRowId(refMsgId);
				if (refMsg != null) {
					msgIdWithRefMsg = mi.getRowId();
					break;
				}
			}
		}
		
		if (msgIdWithRefMsg != null) {
			MessageInbox msgWithRefMsgBasic = service.getByRowId(msgIdWithRefMsg);
			logger.info("Message with basic data:" + PrintUtil.prettyPrint(msgWithRefMsgBasic, 2));
			assertNull(msgWithRefMsgBasic.getReferringMessage());
			MessageInbox msgWithRefMsgAllData = service.getAllDataByPrimaryKey(msgIdWithRefMsg);
			logger.info("Message with all data:" + PrintUtil.prettyPrint(msgWithRefMsgAllData, 2));
			assertNotNull(msgWithRefMsgAllData.getReferringMessage());
		}
		
		for (MessageInbox inbox : lst3) {
			if (inbox.getRowId() == msg1.getRowId()) {
				assertTrue(inbox.getReadCount()==msg1.getReadCount());
			}
		}
		
		List<MessageHeader> headers = headerService.getByMsgInboxId(msg1.getRowId());
		assertFalse(headers.isEmpty());
		
		MessageInbox msg2 = service.getLastRecord();
		assertNotNull(msg2);
		logger.info("The Last Message:" + PrintUtil.prettyPrint(msg2,2));
		
		assertNull(service.getNextRecord(msg2));

		MessageInbox msg3  =service.getPrevoiusRecord(msg2);
		if (msg3 == null) {
			assertTrue("MessageInbox table is empty", true);
			fail();
		}

		logger.info(PrintUtil.prettyPrint(msg3,1));
		
		assertFalse(msg1.equals(msg3));

		MessageInbox msg4  = service.getNextRecord(msg3);
		assertTrue(msg2.equals(msg4));
		
		MessageInbox msg5  = service.getFirstRecord();
		assertNotNull(msg5);
		assertNull(service.getPrevoiusRecord(msg5));
		
		// test delete
		assertTrue(1==service.deleteByRowId(msg2.getRowId()));
		assertNull(service.getByRowId(msg2.getRowId()));
	}
	
	@Test
	public void testGettersForWeb() {
		assertNotNull(msg1RowId);
		SearchFieldsVo vo = new SearchFieldsVo(new PagingVo());
		// test UI methods
		logger.info("=================================================");
		vo.getPagingVo().setOrderBy(PagingVo.Column.receivedTime, false);
		List<MessageInbox> listweb0 = service.getListForWeb(vo);
		assertTrue(listweb0.size() > 0);
		Set<Integer> keys = new HashSet<>();
		for (MessageInbox mi : listweb0) {
			keys.add(mi.getRowId());
		}
		assertEquals(listweb0.size(), keys.size());
		logger.info("-------------------------------------------------");
		String msg1MsgBody;
		{
			MessageInbox msg1 = service.getByRowId(msg1RowId);
			assertNotNull(msg1);
			vo.setIsRead(null);
			vo.setIsFlagged(false);
			if (msg1.getRuleLogic() != null) {
				vo.setRuleName(msg1.getRuleLogic().getRuleName());
			}
			vo.getPagingVo().setSearchCriteria(PagingVo.Column.msgSubject, new PagingVo.Criteria(RuleCriteria.REG_EX, msg1.getMsgSubject(), PagingVo.MatchBy.AnyWords));
			vo.getPagingVo().setSearchCriteria(PagingVo.Column.msgBody, new PagingVo.Criteria(RuleCriteria.REG_EX, msg1.getMsgBody(), PagingVo.MatchBy.AllWords));
			msg1MsgBody = msg1.getMsgBody();
		}
		List<MessageInbox> listweb1 = service.getListForWeb(vo);
		assertTrue(listweb1.size() > 0);
		for (int i=0; i<listweb1.size(); i++) {
			MessageInbox mi = listweb1.get(i);
			if (i < 5 || i == (listweb1.size() - 1)) {
				if (i == 0) {
					logger.info("Message Inbox [" + i + "]: " + PrintUtil.prettyPrint(mi, 2));
				}
				logger.info("Fetched message From Address[" + i + "]: " + PrintUtil.prettyPrint(mi.getFromAddress()));
			}
		}
		
		MessageInbox msg2 = listweb1.get(new Random().nextInt(listweb1.size()));
		if (msg2.getReadCount() == 0) {
			vo.setIsRead(false);
		}
		else {
			vo.setIsRead(true);
		}
		
		logger.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		vo.getPagingVo().setSearchValue(PagingVo.Column.toAddrId, msg2.getToAddress().getRowId());
		vo.getPagingVo().setSearchValue(PagingVo.Column.fromAddr, msg2.getFromAddress().getAddress());
		vo.getPagingVo().setOrderBy(PagingVo.Column.updtTime, false);
		List<MessageInbox> listweb2 = service.getListForWeb(vo);
		assertTrue(listweb2.size() > 0);

		FolderEnum folder = vo.getFolderType();
		vo.setFolderType(null);
		vo.getPagingVo().setSearchValue(PagingVo.Column.fromAddrId, msg2.getFromAddress().getRowId());
		vo.getPagingVo().setSearchValue(PagingVo.Column.toAddrId, null);
		vo.getPagingVo().setSearchValue(PagingVo.Column.fromAddr, null);
		vo.getPagingVo().setSearchValue(PagingVo.Column.toAddr, msg2.getToAddress().getAddress());
		List<MessageInbox> listweb3 = service.getListForWeb(vo);
		assertTrue(listweb3.size() > 0);

		logger.info("#################################################");
		vo.setFolderType(folder);
		vo.getPagingVo().setSearchValue(PagingVo.Column.fromAddrId, null);
		vo.getPagingVo().getSearchCriteria(PagingVo.Column.msgSubject).setMatchBy(PagingVo.MatchBy.BeginWith);
		vo.getPagingVo().getSearchCriteria(PagingVo.Column.msgBody).setMatchBy(PagingVo.MatchBy.ExactPhrase);
		List<MessageInbox> listweb4 = service.getListForWeb(vo);
		assertTrue(listweb4.size() <= listweb1.size());
		
		vo.setIsRead(null);
		vo.getPagingVo().getSearchCriteria(PagingVo.Column.msgSubject).setValue(null);
		vo.getPagingVo().getSearchCriteria(PagingVo.Column.msgBody).setValue(msg1MsgBody);
		vo.getPagingVo().getSearchCriteria(PagingVo.Column.msgBody).setMatchBy(PagingVo.MatchBy.AnyWords);
		long count = service.getRowCountForWeb(vo);
		assertTrue(count >= listweb1.size());
		count = service.getRowCountForWeb(vo);
		int allUnreadCount = service.getAllUnreadCount();
		assertTrue(0 < service.updateStatusIdByLeadMsgId(listweb1.get(0)));
		int receivedUnreadCount = service.getReceivedUnreadCount();
		assertTrue(0 <= receivedUnreadCount);
		int sentUnreadCount = service.getSentUnreadCount();
		if (allUnreadCount > receivedUnreadCount) {
			assertTrue(0 < sentUnreadCount);
		}
		assertTrue((receivedUnreadCount + sentUnreadCount) == allUnreadCount);
		logger.info("Received unread count = " + receivedUnreadCount + ", Sent unread count = " + sentUnreadCount);
		logger.info("All unread count = " + service.getAllUnreadCount());
	}
	
	@Test
	public void testGetRecentByDays() {
		List<MessageInbox> lst5 = service.getRecentByDays(10);
		assertFalse(lst5.isEmpty());
		long currTms = System.currentTimeMillis();
		for (MessageInbox mi : lst5) {
			Timestamp tms = mi.getReceivedTime();
			long days = TimeUnit.MILLISECONDS.toDays(currTms - tms.getTime());
			assertTrue(10>=days);
		}
	}
	
	@Test
	public void testGetByNotExistsDeliveryStatus() {
		PagingVo vo = new PagingVo();
		vo.setPageSize(2);
		List<MessageInbox> list1 = service.getByNotExistsDeliveryStatus(vo);
		assertTrue(list1.size() >= 0);
		if (list1.size() > 0) {
			logger.info("Message without delivery status: " + PrintUtil.prettyPrint(list1.get(0), 2));
			vo.setPageAction(PagingVo.PageAction.NEXT);
			List<MessageInbox> list2 = service.getByNotExistsDeliveryStatus(vo);
			assertTrue(list2.size() >= 0);
		}
	}
	
}
