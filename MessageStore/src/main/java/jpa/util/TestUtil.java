package jpa.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import jpa.constant.EmailAddrType;
import jpa.data.preload.RuleNameEnum;
import jpa.message.BodypartBean;
import jpa.message.BodypartUtil;
import jpa.message.MessageBean;
import jpa.message.MessageNode;
import jpa.message.MsgHeader;
import jpa.model.EmailAddress;
import jpa.model.msg.MessageAddress;
import jpa.model.msg.MessageDeliveryStatus;
import jpa.model.msg.MessageInbox;
import jpa.model.msg.MessageRfcField;
import jpa.service.common.EmailAddressService;
import jpa.service.msgdata.MessageInboxService;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestUtil {
	static final Logger logger = LogManager.getLogger(TestUtil.class);
	
	public static boolean isRunningInJunitTest() {
		StackTraceElement[] traces = Thread.currentThread().getStackTrace();
		for (int i = traces.length - 1; i > 0; i--) {
			StackTraceElement trace = traces[i];
			if (StringUtils.startsWith(trace.getClassName(), "org.junit.runners")) {
				// org.junit.runners.ParentRunner.run(ParentRunner.java:363)
				return true;
			}
			else if (StringUtils.contains(trace.getClassName(), "junit.runner")) {
				// org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:192)
				return true;
			}
		}
		return false;
	}
	
	public static byte[] loadFromSamples(String fileName) {
		return FileUtil.loadFromFile("samples/", fileName);
	}

	public static MessageInbox verifyBouncedMail_1(int inboxRowId, MessageInboxService inboxService, EmailAddressService emailService) {
		MessageInbox inbox = inboxService.getAllDataByPrimaryKey(inboxRowId);
		assertTrue("support.hotline@jbatch.com".equals(inbox.getToAddress().getAddress()));
		assertTrue("postmaster@synnex.com.au".equals(inbox.getFromAddress().getAddress()));
		assertTrue("Delivery Status Notification (Failure)".equals(inbox.getMsgSubject()));
		for (MessageAddress addr : inbox.getMessageAddressList()) {
			if (EmailAddrType.FINAL_RCPT_ADDR.getValue().equals(addr.getAddressType())) {
				EmailAddress emailAddr = emailService.getByRowId(addr.getEmailAddrRowId());
				assertTrue("jackwnn@synnex.com.au".equals(emailAddr.getAddress()));
			}
		}
		assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(inbox.getRuleLogic().getRuleName()));
		assertFalse(inbox.getMessageRfcFieldList().isEmpty());
		for (MessageRfcField rfc : inbox.getMessageRfcFieldList()) {
			logger.info("RFC Type: " + rfc.getMessageRfcFieldPK().getRfcType());
			logger.info(PrintUtil.prettyPrint(rfc,2));
			if (rfc.getMessageRfcFieldPK().getRfcType().indexOf("multipart/report")>=0) {
				EmailAddress finalRcpt = emailService.getByRowId(rfc.getFinalRcptAddrRowId());
				assertTrue("jackwnn@synnex.com.au".equals(finalRcpt.getAddress()));
			}
			else if (rfc.getMessageRfcFieldPK().getRfcType().indexOf("message/rfc822")>=0) {
				assertTrue(rfc.getDsnRfc822().indexOf("Return-Path: jackwng@gmail.com")>0);
				assertTrue(rfc.getDsnText().indexOf("Dear jackwnn@synnex.com.au")>0);
			}
		}
		return inbox;
	}

	public static MessageInbox verifyBouncedMail_2(int inboxRowId, MessageInboxService inboxService, EmailAddressService emailService) {
		MessageInbox inbox = inboxService.getAllDataByPrimaryKey(inboxRowId);
		assertTrue("JWang@nc.rr.com".equalsIgnoreCase(inbox.getToAddress().getAddress()));
		assertTrue("postmaster@mail.rr.com".equals(inbox.getFromAddress().getAddress()));
		assertTrue("Mail System Error - Returned Mail".equals(inbox.getMsgSubject()));
		for (MessageAddress addr : inbox.getMessageAddressList()) {
			if (EmailAddrType.FINAL_RCPT_ADDR.getValue().equals(addr.getAddressType())) {
				EmailAddress emailAddr = emailService.getByRowId(addr.getEmailAddrRowId());
				assertTrue("unknown.useraddress@aim.com".equals(emailAddr.getAddress()));
			}
		}
		assertTrue(RuleNameEnum.HARD_BOUNCE.getValue().equals(inbox.getRuleLogic().getRuleName()));
		assertFalse(inbox.getMessageRfcFieldList().isEmpty());
		for (MessageRfcField rfc : inbox.getMessageRfcFieldList()) {
			logger.info("RFC Type: " + rfc.getMessageRfcFieldPK().getRfcType());
			logger.info(PrintUtil.prettyPrint(rfc,2));
			if (rfc.getMessageRfcFieldPK().getRfcType().indexOf("multipart/report")>=0) {
				EmailAddress finalRcpt = emailService.getByRowId(rfc.getFinalRcptAddrRowId());
				assertTrue("unknown.useraddress@aim.com".equals(finalRcpt.getAddress()));
			}
			else if (rfc.getMessageRfcFieldPK().getRfcType().indexOf("message/rfc822")>=0) {
				assertTrue(rfc.getDsnRfc822().indexOf("Return-Path: <bounce-10.00012567.0-unknown.useraddress=aim.com@jackwng.dyndns.org>")>0);
				assertTrue(rfc.getDsnText().indexOf("System Email Id: 10.00012567.0")>=0);
				assertTrue(rfc.getDsnText().indexOf("From: Jack Wang <df153@aim.com>")>0);
				assertTrue("Re:test to itself".equals(rfc.getOriginalMsgSubject()));
			}
		}
		return inbox;
	}
	
	public static MessageInbox verifyDeliveryStatus4BounceMail_2(int inboxRowId, MessageInboxService inboxService) {
		MessageInbox inbox = inboxService.getAllDataByPrimaryKey(inboxRowId);
		assertFalse(inbox.getMessageDeliveryStatusList().isEmpty());
		boolean verified = false;
		for (MessageDeliveryStatus status : inbox.getMessageDeliveryStatusList()) {
			if ("<unknown.useraddress@aim.com>".equals(status.getFinalRecipientAddress())) {
				assertTrue(status.getDeliveryStatus().indexOf("Final-Recipient: RFC822; <unknown.useraddress@aim.com>")>0);
				assertTrue(status.getDsnReason().indexOf("smtp; 550 MAILBOX NOT FOUND")>=0);
				assertTrue("5.1.1".equals(status.getDsnStatus()));
				//assertTrue("failed".equals(status.getDsnReason()));
				assertTrue("<unknown.useraddress@aim.com>".equals(status.getFinalRecipientAddress()));
				verified = true;
				break;
			}
		}
		assertTrue(verified);
		return inbox;
	}
	
	public static void verifyMessageBean4BounceMail_1(MessageBean msgBean) {
		BodypartBean bodyBean1 = BodypartUtil.retrieveDlvrStatus(msgBean, 0);
		if (bodyBean1!=null) {
			logger.info(PrintUtil.prettyPrint(bodyBean1));
			assertTrue("message/delivery-status".equals(bodyBean1.getContentType()));
			String dlvrStatus = new String(bodyBean1.getValue());
			assertTrue(dlvrStatus.indexOf("Reporting-MTA: dns;MELMX.synnex.com.au")>=0);
			assertTrue(dlvrStatus.indexOf("Final-Recipient: rfc822;jackwnn@synnex.com.au")>0);
			assertTrue(dlvrStatus.indexOf("Status: 5.1.1")>0);
		}
		
		BodypartBean bodyBean2 = BodypartUtil.retrieveMessageRfc822(msgBean, 0);
		logger.info(PrintUtil.prettyPrint(bodyBean2));
		assertTrue("message/rfc822".equals(bodyBean2.getContentType()));
		assertTrue(bodyBean2.getNodes().size()==1);
		BodypartBean bodyBean2_1 = bodyBean2.getNodes().get(0);
		logger.info(PrintUtil.prettyPrint(bodyBean2_1));
		assertTrue(bodyBean2_1.getContentType().startsWith("text/html"));
		String rfc822 = new String(bodyBean2_1.getValue());
		assertTrue(rfc822.indexOf("Dear jackwnn@synnex.com.au")>0);
		assertTrue(rfc822.indexOf("Online Pharmacy Products!")>0);
		int hdrcnt = 0;
		for (MsgHeader hdr : bodyBean2_1.getHeaders()) {
			if ("Return-Path".equals(hdr.getName())) {
				assertTrue("jackwng@gmail.com".equals(hdr.getValue()));
				hdrcnt++;
			}
			else if ("Message-Id".equals(hdr.getName())) {
				assertTrue("<03907644185382.773588432734.799319-7043@cimail571.msn.com>".equals(hdr.getValue()));
				hdrcnt++;
			}
			else if ("To".equals(hdr.getName())) {
				assertTrue("<jackwnn@synnex.com.au>".equals(hdr.getValue()));
				hdrcnt++;
			}
			else if ("Subject".equals(hdr.getName())) {
				assertTrue("May 74% OFF".equals(hdr.getValue()));
				hdrcnt++;
			}
		}
		assertTrue(hdrcnt==4);

		List<BodypartBean> bodyBeans = BodypartUtil.retrieveReportText(msgBean, 0);
		assertTrue(bodyBeans.size()==1);
		BodypartBean bb = bodyBeans.get(0);
		logger.info(PrintUtil.prettyPrint(bb));
		String reportText = new String(bb.getValue());
		assertTrue(reportText.indexOf("Delivery to the following recipients failed.")>0);
		assertTrue(reportText.indexOf("jackwnn@synnex.com.au")>0);
		
		BodypartBean bodyBean3 = BodypartUtil.retrieveMDNReceipt(msgBean, 0);
		assertNull(bodyBean3);
		
		BodypartBean bodyBean4 = BodypartUtil.retrieveRfc822Headers(msgBean, 0);
		assertNull(bodyBean4);
		
		BodypartBean bodyBean5 = BodypartUtil.retrieveRfc822Text(msgBean, 0);
		assertNotNull(bodyBean5);
		assertNotNull(bodyBean5.getValue());
		String rfc822Text = new String(bodyBean5.getValue());
		assertTrue(rfc822Text.indexOf("Delivery to the following recipients failed.")>0);
		assertTrue(rfc822Text.indexOf("jackwnn@synnex.com.au")>0);
		
		List<BodypartBean> bodyBeans2 = BodypartUtil.retrieveAlternatives(msgBean);
		assertFalse(bodyBeans2.isEmpty());
		assertTrue(msgBean.equals(bodyBeans2.get(0)));
	}
	
	public static void verifyParsedMessageBean4BounceMail_1(MessageBean msgBean) {
		logger.info(PrintUtil.prettyPrint(msgBean));
		assertTrue("jackwnn@synnex.com.au".equals(msgBean.getFinalRcpt()));
		assertTrue("failed".equals(msgBean.getDsnAction()));

		assertNotNull(msgBean.getDsnDlvrStat());
		assertTrue(msgBean.getDsnDlvrStat().indexOf("Received-From-MTA: dns;asp-6.reflexion.net")>0);
		assertTrue(msgBean.getDsnDlvrStat().indexOf("Final-Recipient: rfc822;jackwnn@synnex.com.au")>0);
		assertTrue(msgBean.getDsnDlvrStat().indexOf("Status: 5.1.1")>0);
		
		assertNotNull(msgBean.getDsnRfc822());
		assertTrue(msgBean.getDsnRfc822().indexOf("from asp-6.reflexion.net ([205.237.99.181]) by MELMX.synnex.com.au")>=0);
		assertTrue(msgBean.getDsnRfc822().indexOf("Received: by asp-6.reflexion.net")>0);
		assertTrue(msgBean.getDsnRfc822().indexOf("Received: (qmail 22418 invoked from network); 13 May 2008 22:47:48 -0000")>0);
		//assertTrue(msgBean.getDsnRfc822().indexOf("From: Viagra � Official Site <jackwnn@synnex.com.au>")>=0);
		assertTrue(msgBean.getDsnRfc822().indexOf("Official Site <jackwnn@synnex.com.au>")>=0);
		assertTrue(msgBean.getDsnRfc822().indexOf("X-Rfx-Unknown-Address: Address <jackwnn@synnex.com.au> is not protected by Reflexion.")>0);
		assertTrue(msgBean.getDsnRfc822().indexOf("Date: 14 May 2008 08:50:31 +1000")>0);
		
		assertTrue("5.1.1".equals(msgBean.getDsnStatus()));
		assertNotNull(msgBean.getDsnText());
		assertTrue(msgBean.getDsnText().indexOf("content=\"text/html; charset=iso-8859-1\"")>0);
		assertTrue(msgBean.getDsnText().indexOf("Dear jackwnn@synnex.com.au")>0);
		assertTrue(msgBean.getDsnText().indexOf("Coupon No. 194")>0);
		
		List<MsgHeader> dlvrd_to = msgBean.getHeader("Delivered-To");
		assertFalse(dlvrd_to.isEmpty());
		assertTrue("support.hotline@jbatch.com".equals(dlvrd_to.get(0).getValue()));
		
		List<MsgHeader> received = msgBean.getHeader("Received");
		assertTrue(received.size()==3);
		
		List<MsgHeader> subject = msgBean.getHeader("Subject");
		assertTrue("Delivery Status Notification (Failure)".equals(subject.get(0).getValue()));
		
		assertTrue("multipart/report".equals(msgBean.getMimeType()));
		
		assertTrue("May 74% OFF".equals(msgBean.getOrigSubject()));
		assertTrue("Delivery Status Notification (Failure)".equals(msgBean.getSubject()));
		assertTrue("support.hotline@jbatch.com".equals(msgBean.getToAsString()));
		
		MessageNode report = msgBean.getReport();
		assertNotNull(report);
		//logger.info("Report: " + StringUtil.prettyPrint(report));
		MessageBean msgBean2 = (MessageBean) report.getBodypartNode();
		assertTrue(msgBean2.getBody().indexOf("Delivery to the following recipients failed.")>0);
		assertTrue(msgBean2.getBody().indexOf("jackwnn@synnex.com.au")>0);
		assertTrue(msgBean2.getBody().indexOf("Content-Type: message/delivery-status")>0);
		assertTrue(msgBean2.getBody().indexOf("Reporting-MTA: dns;MELMX.synnex.com.au")>0);
		assertTrue(msgBean2.getBody().indexOf("Received-From-MTA: dns;asp-6.reflexion.net")>0);
		assertTrue(msgBean2.getBody().indexOf("Final-Recipient: rfc822;jackwnn@synnex.com.au")>0);
		assertTrue(msgBean2.getBody().indexOf("Content-Type: message/rfc822")>0);
	}
}
