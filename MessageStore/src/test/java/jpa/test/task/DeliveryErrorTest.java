package jpa.test.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import jpa.constant.MsgDirectionCode;
import jpa.constant.MsgStatusCode;
import jpa.message.MessageBean;
import jpa.message.MessageBodyBuilder;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.msg.MessageDeliveryStatus;
import jpa.model.msg.MessageInbox;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.msgdata.MessageDeliveryStatusService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgout.MessageBeanBo;
import jpa.service.task.DeliveryError;
import jpa.spring.util.BoTestBase;

@org.springframework.test.annotation.Commit
public class DeliveryErrorTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = LogManager.getLogger(DeliveryErrorTest.class);
	
	@Resource
	private DeliveryError task;
	@Resource
	private EmailAddressService emailService;
	@Resource
	private MessageInboxService inboxService;
	@Resource
	private MessageDeliveryStatusService dlvrService;
	@Resource
	private MessageBeanBo msgBeanBo;

	@BeforeClass
	public static void DeliveryErrorPrepare() {
	}

	private boolean isUpdatingSameRecord = true;
	private int rowId;
	private MessageBean inboxMsgBean = null;
	private MessageInbox inbox = null;
	private String finalRcpt = "event.alert@localhost";
	private String id_xhdr = null;
	
	@Before
	@Rollback(value=false)
	public void prepare() {
		PagingVo vo = new PagingVo();
		vo.setOrderBy(PagingVo.Column.updtTime, false);
		vo.setPageSize(5);
		// First try to get a message without delivery status
		List<MessageInbox> list1 = inboxService.getByNotExistsDeliveryStatus(vo);
		if (list1.size() > 0) {
			inbox = list1.get(0);
		}
		if (inbox == null) {
			// did not find a message without delivery status, get last record
			inbox = inboxService.getLastRecord();
		}
		assertNotNull(inbox);
		//inbox = inboxService.getAllDataByPrimaryKey(inbox.getRowId());
		logger.info("Message Inbox: " + jpa.util.PrintUtil.prettyPrint(inbox, 2));
		
		// create a MessaageBean from the message record
		inboxMsgBean = msgBeanBo.createMessageBean(inbox);
		assertNotNull(inboxMsgBean.getMsgId());
		logger.info("Message Bean: " +inboxMsgBean.toString());
		
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		// retrieve Email_Id from header
		id_xhdr = parser.parseHeaders(inboxMsgBean.getHeaders());
		if (StringUtils.isBlank(id_xhdr)) {
			// Email_Id not found, add one with value of row id
			MessageBodyBuilder.addEmailIdToHeader(inboxMsgBean);
			id_xhdr = parser.parseHeaders(inboxMsgBean.getHeaders());
		}
		assertTrue(StringUtils.isNotBlank(id_xhdr));
		if (inboxMsgBean.getMsgRefId() == null) {
			inboxMsgBean.setMsgRefId(Integer.parseInt(id_xhdr));
		}
		
		MessageInbox minbox = inboxService.getByRowId(inboxMsgBean.getMsgRefId());
		if (minbox == null) { // referring message does not exist
			inboxMsgBean.setMsgRefId(inbox.getRowId());
		}

		if (!id_xhdr.equals(inboxMsgBean.getMsgRefId() + "")) {
			id_xhdr = inboxMsgBean.getMsgRefId().toString();
		}
		if (!inboxMsgBean.getMsgRefId().equals(inbox.getRowId())) {
			// update delivery status to referring record.
			isUpdatingSameRecord = false;
		}
		inboxMsgBean.setMsgId(null);
		inboxMsgBean.setFinalRcpt(finalRcpt);
		inboxMsgBean.setDsnAction("failed");
		inboxMsgBean.setDsnStatus("5.1.1");
		inboxMsgBean.setDiagnosticCode("smtp; 554 delivery error: dd This user doesn't have a yahoo.com account (unknown.useraddress@yahoo.com) [0] - mta522.mail.mud.yahoo.com");
		inboxMsgBean.setDsnDlvrStat("The delivery of following message failed due to:" + LF +
				" 511 5.1.1 Invalid Destination Mailbox Address." + LF +
				"Invalid Addresses..., TO addr: unknown.useraddress@nc.rr.com");

		MessageContext ctx = new MessageContext(inboxMsgBean);
		task.process(ctx);
		assertTrue(ctx.getRowIds().size() == 1);
		rowId = ctx.getRowIds().get(0); // the message updated with delivery status
		assertTrue(rowId > 0);
		logger.info("After prepare() - delivery status added to message inbox rowId = " + rowId + ", id_xhdr = " + id_xhdr);
	}

	@Test
	public void testDeliveryError() throws Exception {
		// verify results
		assertEquals(inboxMsgBean.getMsgRefId(), Integer.valueOf(rowId));
		
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String id_inbox = parser.parseMsg(inboxMsgBean.getBody());
		if (StringUtils.isBlank(id_inbox)) {
			id_inbox = parser.parseHeaders(inboxMsgBean.getHeaders());
		}
		assertTrue(StringUtils.isNotBlank(id_inbox));
		
		MessageInbox refInbox = inboxService.getAllDataByPrimaryKey(rowId);
		assertNotNull(refInbox);
		logger.info("Message RefInbox: " + jpa.util.PrintUtil.prettyPrint(refInbox, 2));
		
//		String id_refinbox = parser.parseMsg(refInbox.getMsgBody());
//		if (StringUtils.isBlank(id_refinbox)) {
//			id_refinbox = parser.parseHeaders(MsgHeaderUtil.messageHeaderList2MsgHeaderList(refInbox.getMessageHeaderList()));
//		}
//		if (StringUtils.isBlank(id_refinbox)) { // just for safety
//			id_refinbox = refInbox.getRowId() + "";
//		}
//		assertTrue(StringUtils.isNotBlank(id_refinbox));
//		
//		if (isUpdatingSameRecord == false) {
// 			assertEquals(id_refinbox, inboxMsgBean.getMsgRefId() + "");
//			if (inbox.getReferringMessageRowId() != null) {
//				assertEquals(id_refinbox, inbox.getReferringMessageRowId() + "");
//			}
//			else {
//				assertEquals(id_refinbox, inbox.getLeadMessageRowId()+"");
//			}
//		}
//		else {
//			assertEquals(id_inbox, id_refinbox);
//		}
		
		if (MsgDirectionCode.SENT.getValue().equals(inbox.getMsgDirection())) {
			if (isUpdatingSameRecord) {
				assertEquals(id_xhdr, inbox.getRowId() + "");
			}
			else {
				assertEquals(id_xhdr, refInbox.getRowId() + "");
			}
		}
		else if (MsgDirectionCode.RECEIVED.getValue().equals(inbox.getMsgDirection())) {
			assertTrue(id_xhdr.equals(id_inbox) || id_xhdr.equals(inboxMsgBean.getMsgRefId()+""));
		}
		
		assertTrue(MsgStatusCode.DELIVERY_FAILED.getValue().equals(refInbox.getStatusId()));
		assertFalse(refInbox.getMessageDeliveryStatusList().isEmpty());
		
		boolean finalRcptFound = false;
		for (MessageDeliveryStatus status : refInbox.getMessageDeliveryStatusList()) {
			if (finalRcpt.equals(status.getFinalRecipientAddress())) {
				assertTrue(inboxMsgBean.getDsnDlvrStat().equals(status.getDeliveryStatus()));
				assertTrue(inboxMsgBean.getDiagnosticCode().equals(status.getDsnReason()));
				assertTrue(inboxMsgBean.getDsnStatus().equals(status.getDsnStatus()));
				assertTrue(inboxMsgBean.getDiagnosticCode().equals(status.getDsnReason()));
				finalRcptFound = true;
			}
		}
		assertTrue(finalRcptFound);
	}
}
