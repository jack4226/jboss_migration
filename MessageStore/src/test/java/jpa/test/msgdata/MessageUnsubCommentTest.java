package jpa.test.msgdata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.FolderEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.SenderData;
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageInbox;
import jpa.model.msg.MessageUnsubComment;
import jpa.model.rule.RuleLogic;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.maillist.MailingListService;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgdata.MessageUnsubCommentService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class MessageUnsubCommentTest extends BoTestBase {

	@BeforeClass
	public static void MessageUnsubCommentPrepare() {
	}

	@Autowired
	private MessageUnsubCommentService service;
	@Autowired
	private MessageInboxService inboxService;
	@Autowired
	private EmailAddressService addrService;
	@Autowired
	private SenderDataService senderService;
	@Autowired
	private RuleLogicService logicService;
	@Autowired
	private MailingListService listService;
	@Autowired
	private MessageFolderService folderService;

	private static MessageInbox inbox1;
	private static EmailAddress from;
	private static EmailAddress to;
	private static MailingList mlist;

	@Before
	public void prepare() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		inbox1 = new MessageInbox();
		
		inbox1.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		inbox1.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
		inbox1.setMsgSubject("Test Subject");
		inbox1.setMsgPriority("2 (Normal)");
		inbox1.setReceivedTime(updtTime);
		
		from = addrService.findSertAddress("test@test.com");
		inbox1.setFromAddress(from);
		inbox1.setReplytoAddress(null);

		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		String to_addr = sender.getReturnPathLeft() + "@" + sender.getDomainName();
		to = addrService.findSertAddress(to_addr);
		inbox1.setToAddress(to);
		inbox1.setSenderData(sender);
		inbox1.setSubscriberData(null);
		inbox1.setPurgeDate(null);
		inbox1.setUpdtTime(updtTime);
		inbox1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.GENERIC.getValue());
		inbox1.setRuleLogic(logic);
		inbox1.setMsgContentType("multipart/mixed");
		inbox1.setBodyContentType("text/plain");
		inbox1.setMsgBody("Test Message Body");
		
		MessageFolder folder = folderService.getOneByFolderName(FolderEnum.Inbox.name());
		inbox1.setMessageFolder(folder);
		
		inboxService.insert(inbox1);
		
		List<MailingList> mlists=listService.getAll(true);
		assertFalse(mlists.isEmpty());
		mlist=mlists.get(0);
	}
	
	private static MessageUnsubComment cmt1;
	private static MessageUnsubComment cmt2;

	@Test
	public void messageUnsubCommentService() throws IOException {
		insertUnsubComments();
		MessageUnsubComment cmt11 = service.getByRowId(cmt1.getRowId());
		
		System.out.println(PrintUtil.prettyPrint(cmt11,2));
		
		MessageUnsubComment cmt12 = service.getByMsgInboxId(inbox1.getRowId());
		assertTrue(cmt11.equals(cmt12));
		
		List<MessageUnsubComment> unsubList = service.getByFromAddress(from.getAddress());
		assertTrue(2<=unsubList.size());
		
		// test update
		cmt2.setUpdtUserId("jpa test");
		service.update(cmt2);
		MessageUnsubComment adr22 = service.getByRowId(cmt2.getRowId());
		assertTrue("jpa test".equals(adr22.getUpdtUserId()));
		
		// test delete
		service.delete(cmt11);
		inbox1.setMessageUnsubComment(null);
		inboxService.update(inbox1);
		assertNull(service.getByRowId(cmt11.getRowId()));

		
		assertTrue(1==service.deleteByRowId(cmt2.getRowId()));
		
		insertUnsubComments();
		assertTrue(1<=service.deleteByRowId(cmt2.getRowId()));
		assertNull(service.getByRowId(cmt2.getRowId()));
		List<MessageUnsubComment> reducedList = service.getByFromAddress(cmt2.getEmailAddr().getAddress());
		assertTrue(0<=reducedList.size());
		assertNull(service.getByMsgInboxId(cmt2.getMessageInbox().getRowId()));

		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertUnsubComments() throws IOException {
		// test insert
		cmt1 = new MessageUnsubComment();
		cmt1.setMessageInbox(inbox1);
		cmt1.setComments("jpa test unsub comment 1");
		cmt1.setEmailAddr(from);
		cmt1.setMailingList(mlist);
		service.insert(cmt1);
		inbox1.setMessageUnsubComment(cmt1);
		inboxService.update(inbox1);
		
		MessageInbox inbox2 = inboxService.getPrevoiusRecord(inbox1);
		assertFalse(inbox1.getRowId().equals(inbox2.getRowId()));
		if (inbox2.getMessageUnsubComment() != null) {
			cmt2 = inbox2.getMessageUnsubComment();
		}
		else {
			cmt2 = new MessageUnsubComment();
		}
		cmt2.setMessageInbox(inbox2);
		cmt2.setComments("jpa test unsub comment 2");
		cmt2.setEmailAddr(from);
		cmt2.setMailingList(mlist);
		if (inbox2.getMessageUnsubComment() != null) {
			service.update(cmt2);
		}
		else {
			service.insert(cmt2);
		}
		inbox2.setMessageUnsubComment(cmt2);
		inboxService.update(inbox2);
	}
}
