package jpa.test.msgdata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Optional;

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
import jpa.model.SenderData;
import jpa.model.msg.MessageActionLog;
import jpa.model.msg.MessageActionLogPK;
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageActionLogService;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class MessageActionLogTest extends BoTestBase {

	@BeforeClass
	public static void MessageActionLogPrepare() {
	}

	@Autowired
	MessageActionLogService service;
	@Autowired
	MessageInboxService inboxService;
	@Autowired
	EmailAddressService addrService;
	@Autowired
	SenderDataService senderService;
	@Autowired
	RuleLogicService logicService;
	@Autowired
	MessageFolderService folderService;

	private MessageInbox inbox1;
	private EmailAddress from;
	private EmailAddress to;

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
	}
	
	private MessageActionLog log1;
	private MessageActionLog log2;

	@Test
	public void messageActionLogService() {
		insertActionLogs();
		Optional<MessageActionLog> log11 = service.getByRowId(log1.getRowId());
		assertTrue(log11.isPresent());
		
		logger.info(PrintUtil.prettyPrint(log11.get(),3));
		
		MessageActionLog log12 = service.getByPrimaryKey(log11.get().getMessageActionLogPK());
		assertTrue(log11.get().equals(log12));
		
		MessageActionLogPK notExist = new MessageActionLogPK();
		notExist.setMessageInbox(log11.get().getMessageActionLogPK().getMessageInbox());
		notExist.setLeadMessageRowId(99999);
		assertNull(service.getByPrimaryKey(notExist));
		
		// test update
		log2.setUpdtUserId("jpa test");
		service.update(log2);
		Optional<MessageActionLog> adr22 = service.getByRowId(log2.getRowId());
		assertTrue(adr22.isPresent());
		assertTrue("jpa test".equals(adr22.get().getUpdtUserId()));
		
		assertTrue(1==service.getByLeadMsgId(inbox1.getRowId()).size());
		
		// test delete
		service.delete(log11.get());
		assertFalse(service.getByRowId(log11.get().getRowId()).isPresent());

		
		assertTrue(1==service.deleteByRowId(log2.getRowId()));
		assertTrue(0==service.deleteByLeadMsgId(log2.getMessageActionLogPK().getLeadMessageRowId()));
		
		insertActionLogs();
		assertTrue(1==service.deleteByPrimaryKey(log1.getMessageActionLogPK()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertActionLogs() {
		// test insert
		log1 = new MessageActionLog();
		MessageActionLogPK pk1 = new MessageActionLogPK(inbox1, inbox1.getLeadMessageRowId());
		log1.setMessageActionLogPK(pk1);
		log1.setActionService(RuleNameEnum.SEND_MAIL.name());
		log1.setParameters("sent");
		service.insert(log1);

		log2 = new MessageActionLog();
		MessageInbox inbox2 = inboxService.getPrevoiusRecord(inbox1);
		MessageActionLogPK pk2 = new MessageActionLogPK(inbox1,inbox2.getRowId());
		log2.setMessageActionLogPK(pk2);
		log2.setActionService(RuleNameEnum.CSR_REPLY.name());
		log2.setParameters("rowid=122");
		service.insert(log2);
		
		assertTrue(2==service.getByMsgInboxId(inbox1.getRowId()).size());
	}
}
