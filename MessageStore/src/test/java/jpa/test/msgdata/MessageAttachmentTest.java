package jpa.test.msgdata;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Optional;

import javax.mail.Part;

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
import jpa.model.msg.MessageAttachment;
import jpa.model.msg.MessageAttachmentPK;
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageInbox;
import jpa.model.rule.RuleLogic;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageAttachmentService;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;
import jpa.util.TestUtil;

public class MessageAttachmentTest extends BoTestBase {

	@BeforeClass
	public static void MessageAttachmentPrepare() {
	}

	@Autowired
	MessageAttachmentService service;
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
	
	private MessageAttachment atc1;
	private MessageAttachment atc2;
	private MessageAttachment atc3;

	@Test
	public void messageAttachmentService() {
		insertMessageAttachments();
		Optional<MessageAttachment> atc11 = service.getByRowId(atc1.getRowId());
		assertTrue(atc11.isPresent());
		
		logger.info(PrintUtil.prettyPrint(atc11.get(),3));
		
		MessageAttachment atc12 = service.getByPrimaryKey(atc11.get().getMessageAttachmentPK());
		assertTrue(atc11.get().equals(atc12));
		
		// test update
		atc2.setUpdtUserId("jpa test");
		service.update(atc2);
		Optional<MessageAttachment> hdr22 = service.getByRowId(atc2.getRowId());
		assertTrue(hdr22.isPresent());
		assertTrue("jpa test".equals(hdr22.get().getUpdtUserId()));
		
		// test delete
		service.delete(atc11.get());
		assertNull(service.getByRowId(atc11.get().getRowId()));

		
		assertTrue(1==service.deleteByRowId(atc2.getRowId()));
		assertNull(service.getByRowId(atc2.getRowId()));
		assertTrue(1==service.deleteByMsgInboxId(inbox1.getRowId()));
		
		insertMessageAttachments();
		assertTrue(1==service.deleteByPrimaryKey(atc1.getMessageAttachmentPK()));
		assertNull(service.getByPrimaryKey(atc1.getMessageAttachmentPK()));
		
		assertTrue(2==service.deleteByMsgInboxId(inbox1.getRowId()));
		assertTrue(0==service.getByMsgInboxId(inbox1.getRows()).size());
	}
	
	private void insertMessageAttachments() {
		// test insert
		atc1 = new MessageAttachment();
		MessageAttachmentPK pk1 = new MessageAttachmentPK(inbox1,1,1);
		atc1.setMessageAttachmentPK(pk1);
		atc1.setAttachmentDisp(Part.ATTACHMENT);
		atc1.setAttachmentName("test.txt");
		atc1.setAttachmentType("text/plain; name=\"test.txt\"");
		atc1.setAttachmentValue("Test blob content goes here.".getBytes());
		service.insert(atc1);
		
		atc2 = new MessageAttachment();
		MessageAttachmentPK pk2 = new MessageAttachmentPK(inbox1,1,2);
		atc2.setMessageAttachmentPK(pk2);
		atc2.setAttachmentDisp(Part.INLINE);
		atc2.setAttachmentName("one.gif");
		atc2.setAttachmentType("image/gif; name=one.gif");
		atc2.setAttachmentValue(TestUtil.loadFromSamples("one.gif"));
		service.insert(atc2);
		
		atc3 = new MessageAttachment();
		MessageAttachmentPK pk3 = new MessageAttachmentPK(inbox1,1,3);
		atc3.setMessageAttachmentPK(pk3);
		atc3.setAttachmentDisp(Part.ATTACHMENT);
		atc3.setAttachmentName("jndi.bin");
		atc3.setAttachmentType("application/octet-stream; name=\"jndi.bin\"");
		atc3.setAttachmentValue(TestUtil.loadFromSamples("jndi.bin"));
		service.insert(atc3);
		
		assertTrue(service.getByMsgInboxId(inbox1.getRowId()).size()==3);		
	}
}
