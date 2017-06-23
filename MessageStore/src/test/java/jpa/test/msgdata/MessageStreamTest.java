package jpa.test.msgdata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageInbox;
import jpa.model.msg.MessageStream;
import jpa.model.rule.RuleLogic;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgdata.MessageStreamService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;
import jpa.util.TestUtil;

public class MessageStreamTest extends BoTestBase {

	@BeforeClass
	public static void MessageStreamPrepare() {
	}

	@Autowired
	MessageStreamService service;
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
	
	private MessageStream adr1;
	private MessageStream adr2;

	@Test
	public void messageAddressService() throws IOException {
		insertMsgStreams();
		MessageStream adr11 = service.getByRowId(adr1.getRowId());
		
		logger.info(PrintUtil.prettyPrint(adr11,2));
		
		MessageStream adr12 = service.getByMsgInboxId(inbox1.getRowId());
		assertTrue(adr11.equals(adr12));
		
		assertTrue(service.getByFromAddress(inbox1.getFromAddress().getAddress()).size()>0);
		
		// test update
		adr2.setUpdtUserId("jpa test");
		service.update(adr2);
		MessageStream adr22 = service.getByRowId(adr2.getRowId());
		assertTrue("jpa test".equals(adr22.getUpdtUserId()));
		
		// test delete
		service.delete(adr11);
		assertNull(service.getByRowId(adr11.getRowId()));

		
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		assertNull(service.getByRowId(adr2.getRowId()));
		
		insertMsgStreams();
		assertNotNull(service.getLastRecord());
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
		assertNull(service.getByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertMsgStreams() throws IOException {
		// test insert
		adr1 = new MessageStream();
		adr1.setMessageInbox(inbox1);
		adr1.setMsgSubject("test jpa subject");
		adr1.setFromAddrRowId(from.getRowId());
		adr1.setToAddrRowId(to.getRowId());
		adr1.setMsgStream(TestUtil.loadFromSamples("BouncedMail_1.txt"));;
		service.insert(adr1);
		
		MessageInbox inbox2 = inboxService.getPrevoiusRecord(inbox1);
		adr2 = service.getByMsgInboxId(inbox2.getRowId());
		if (adr2 == null) {
			adr2 = new MessageStream();
			adr2.setMessageInbox(inbox2);
			adr2.setMsgSubject("jpa test");
			adr2.setFromAddrRowId(from.getRowId());
			adr2.setMsgStream(TestUtil.loadFromSamples("BouncedMail_1.txt"));
			service.insert(adr2);
		}
	}
}
