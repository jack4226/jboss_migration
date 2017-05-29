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
import jpa.constant.EmailAddrType;
import jpa.constant.MsgDirectionCode;
import jpa.data.preload.FolderEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.model.EmailAddress;
import jpa.model.SenderData;
import jpa.model.msg.MessageAddress;
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageAddressService;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class MessageAddressTest extends BoTestBase {

	@BeforeClass
	public static void MessageAddressPrepare() {
	}

	@Autowired
	MessageAddressService service;
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
	
	private MessageAddress adr1;
	private MessageAddress adr2;

	@Test
	public void messageAddressService() {
		insertAddr1AndAddr2();
		MessageAddress adr11 = service.getByRowId(adr1.getRowId());
		
		System.out.println(PrintUtil.prettyPrint(adr11,2));
		
		MessageAddress adr12 = service.getByPrimaryKey(inbox1.getRowId(), EmailAddrType.FROM_ADDR.getValue(), from.getAddress());
		assertTrue(adr11.equals(adr12));
		
		// test update
		adr2.setUpdtUserId("jpa test");
		service.update(adr2);
		MessageAddress adr22 = service.getByRowId(adr2.getRowId());
		assertTrue("jpa test".equals(adr22.getUpdtUserId()));
		
		// test delete
		service.delete(adr11);
		assertNull(service.getByRowId(adr11.getRowId()));
		
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		assertNull(service.getByRowId(adr2.getRowId()));
		
		insertAddr1AndAddr2();
		assertTrue(1==service.deleteByPrimaryKey(inbox1.getRowId(), EmailAddrType.FROM_ADDR.getValue(), from.getAddress()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
	}
	
	private void insertAddr1AndAddr2() {
		// test insert
		adr1 = new MessageAddress();
		adr1.setMessageInbox(inbox1);
		adr1.setAddressType(EmailAddrType.FROM_ADDR.getValue());
		adr1.setEmailAddrRowId(from.getRowId());
		service.insert(adr1);
		
		adr2 = new MessageAddress();
		adr2.setMessageInbox(inbox1);
		adr2.setAddressType(EmailAddrType.TO_ADDR.getValue());
		adr2.setEmailAddrRowId(to.getRowId());
		service.insert(adr2);
		assertTrue(service.getByMsgInboxId(inbox1.getRowId()).size()==2);		
	}
}
