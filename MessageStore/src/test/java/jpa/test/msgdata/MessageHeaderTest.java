package jpa.test.msgdata;

import static org.junit.Assert.assertFalse;
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
import jpa.constant.XHeaderName;
import jpa.data.preload.FolderEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.model.EmailAddress;
import jpa.model.SenderData;
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageHeader;
import jpa.model.msg.MessageHeaderPK;
import jpa.model.msg.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageHeaderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class MessageHeaderTest extends BoTestBase {

	@BeforeClass
	public static void MessageHeaderPrepare() {
	}

	@Autowired
	MessageHeaderService service;
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
	
	private MessageHeader hdr1;
	private MessageHeader hdr2;
	private MessageHeader hdr3;

	@Test
	public void messageHeaderService() {
		insertMessageHeaders();
		Optional<MessageHeader> hdr11 = service.getByRowId(hdr1.getRowId());
		assertTrue(hdr11.isPresent());
		
		logger.info(PrintUtil.prettyPrint(hdr11.get(),2));
		
		MessageHeader hdr12 = service.getByPrimaryKey(hdr11.get().getMessageHeaderPK());
		assertTrue(hdr11.get().equals(hdr12));
		
		// test update
		hdr2.setUpdtUserId("jpa test");
		service.update(hdr2);
		Optional<MessageHeader> hdr22 = service.getByRowId(hdr2.getRowId());
		assertTrue(hdr22.isPresent());
		assertTrue("jpa test".equals(hdr22.get().getUpdtUserId()));
		
		// test delete
		/*
		 * Aggregated objects cannot be written/deleted/queried independently from their owners.
		 */
		service.delete(hdr11.get()); // TODO resolve above EclipseLink error
		service.deleteByRowId(hdr11.get().getRowId());
		assertFalse(service.getByRowId(hdr11.get().getRowId()).isPresent());

		
		assertTrue(1==service.deleteByRowId(hdr2.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
		assertTrue(0==service.getByMsgInboxId(inbox1.getRowId()).size());
		
		insertMessageHeaders();
		assertTrue(1==service.deleteByPrimaryKey(hdr1.getMessageHeaderPK()));
		assertTrue(2==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertMessageHeaders() {
		// test insert
		hdr1 = new MessageHeader();
		MessageHeaderPK pk1 = new MessageHeaderPK(inbox1,1);
		hdr1.setMessageHeaderPK(pk1);
		hdr1.setHeaderName(XHeaderName.MAILER.value());
		hdr1.setHeaderValue("Mailserder");
		service.insert(hdr1);
		
		hdr2 = new MessageHeader();
		MessageHeaderPK pk2 = new MessageHeaderPK(inbox1,2);
		hdr2.setMessageHeaderPK(pk2);
		hdr2.setHeaderName(XHeaderName.RETURN_PATH.value());
		hdr2.setHeaderValue("demolist1@localhost");
		service.insert(hdr2);
		
		hdr3 = new MessageHeader();
		MessageHeaderPK pk3 = new MessageHeaderPK(inbox1,3);
		hdr3.setMessageHeaderPK(pk3);
		hdr3.setHeaderName(XHeaderName.SENDER_ID.value());
		hdr3.setHeaderValue(Constants.DEFAULT_SENDER_ID);
		service.insert(hdr3);
		
		assertTrue(service.getByMsgInboxId(inbox1.getRowId()).size()==3);		
	}
}
