package jpa.test.msgdata;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

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
import jpa.model.msg.MessageDeliveryStatus;
import jpa.model.msg.MessageDeliveryStatusPK;
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageDeliveryStatusService;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class MessageDeliveryStatusTest extends BoTestBase {

	@BeforeClass
	public static void MessageDeliveryStatusPrepare() {
	}

	@Autowired
	MessageDeliveryStatusService service;
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
	
	private MessageDeliveryStatus log1;
	private MessageDeliveryStatus log2;

	@Test
	public void messageDeliveryStatusService() {
		insertDeliveryStatuss();
		MessageDeliveryStatus log11 = service.getByRowId(log1.getRowId());
		
		System.out.println(PrintUtil.prettyPrint(log11,3));
		
		MessageDeliveryStatus log12 = service.getByPrimaryKey(log11.getMessageDeliveryStatusPK());
		assertTrue(log11.equals(log12));
		
		// test update
		log2.setUpdtUserId("jpa test");
		service.update(log2);
		MessageDeliveryStatus adr22 = service.getByRowId(log2.getRowId());
		assertTrue("jpa test".equals(adr22.getUpdtUserId()));
		
		assertTrue(2==service.getByMsgInboxId(inbox1.getRowId()).size());
		
		// test delete
		service.delete(log11);
		assertNull(service.getByRowId(log11.getRowId()));
		
		assertTrue(1==service.deleteByRowId(log2.getRowId()));
		assertNull(service.getByRowId(log2.getRowId()));
		assertNull(service.getByPrimaryKey(log2.getMessageDeliveryStatusPK()));
		
		insertDeliveryStatuss();
		assertTrue(1==service.deleteByPrimaryKey(log1.getMessageDeliveryStatusPK()));
		assertTrue(0==service.deleteByRowId(log1.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertDeliveryStatuss() {
		// test insert
		log1 = new MessageDeliveryStatus();
		MessageDeliveryStatusPK pk1 = new MessageDeliveryStatusPK(inbox1, from.getRowId());
		log1.setMessageDeliveryStatusPK(pk1);
		log1.setFinalRecipientAddress(from.getAddress());
		log1.setDeliveryStatus("jpa test delivery status");
		service.insert(log1);

		log2 = new MessageDeliveryStatus();
		MessageDeliveryStatusPK pk2 = new MessageDeliveryStatusPK(inbox1,to.getRowId());
		log2.setMessageDeliveryStatusPK(pk2);
		log2.setFinalRecipientAddress(to.getAddress());
		log2.setDsnStatus("jpa test DSN status");
		log2.setDsnReason("jpa test DSN reason");
		log2.setDsnText("jpa test DSN text");
		service.insert(log2);
		
		service.insert(log2);
		
		assertTrue(2==service.getByMsgInboxId(inbox1.getRowId()).size());
	}
}
