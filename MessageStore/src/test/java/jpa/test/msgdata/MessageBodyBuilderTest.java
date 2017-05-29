package jpa.test.msgdata;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.message.MessageBean;
import jpa.message.MessageBodyBuilder;
import jpa.message.MsgHeader;
import jpa.message.util.EmailIdParser;
import jpa.spring.util.BoTestBase;

public class MessageBodyBuilderTest extends BoTestBase {

	@BeforeClass
	public static void MessageBodyBuilderPrepare() {
	}

	@Test
	public void testMessageBodyBuilder() {
		String LF = System.getProperty("line.separator", "\n");
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		int bodyId = 123456;
		int xheaderId = 345678;
		String emailIdStr = parser.createEmailId(bodyId);
		String emailIdXhdr = parser.createEmailId4XHdr(xheaderId);
		int msgId2 = 999999;

		// embed email_id for HTML email
		MessageBean msgBean = new MessageBean();
		msgBean.setContentType("text/html");
		msgBean.setSubject("Test Embedding Email_Id");
		msgBean.setBody("<HTML>This is the original message." + Constants.MSG_DELIMITER_BEGIN
				+ emailIdStr + Constants.MSG_DELIMITER_END + "</HTML>");
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		msgBean.setMsgId(Integer.valueOf(msgId2));
		msgBean.setBody(MessageBodyBuilder.getBodyWithEmailId(msgBean));
		System.out.println(">>>>>>>>>>>>>>>>HTML Message:" + LF + msgBean);

		String msgId = parser.parseMsg(msgBean.getBody());
		System.out.println("Email_Id from Body: " + msgId);
		assertTrue((""+bodyId).equals(msgId));

		// embed email_id for plain text email
		msgBean = new MessageBean();
		msgBean.setContentType("text/plain");
		msgBean.setSubject("Test Embedding Email_Id");
		msgBean.setBody("This is the original message.\n" + Constants.MSG_DELIMITER_BEGIN
				+ emailIdStr + Constants.MSG_DELIMITER_END);
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		msgBean.setMsgId(Integer.valueOf(msgId2));
		msgBean.setBody(MessageBodyBuilder.getBodyWithEmailId(msgBean));
		MsgHeader hdr = new MsgHeader();
		hdr.setName(parser.getEmailIdXHdrName());
		hdr.setValue(emailIdXhdr);
		List<MsgHeader> hdrs = new ArrayList<MsgHeader>();
		hdrs.add(hdr);
		msgBean.setHeaders(hdrs);
		System.out.println(">>>>>>>>>>>>>>>>TEXT Message:" + LF +msgBean);

		// parse email_id
		msgId = parser.parseMsg(msgBean.getBody());
		System.out.println("Email_Id from Body: " + msgId);
		assertTrue((""+bodyId).equals(msgId));
		msgId = parser.parseHeaders(msgBean.getHeaders());
		System.out.println("Email_Id from X-Header: " + msgId);
		assertTrue((""+xheaderId).equals(msgId));
		
		// embed email_id by MessageBodyBuilder
		msgBean.setEmBedEmailId(Boolean.TRUE);
		msgBean.setBody(MessageBodyBuilder.getBodyWithEmailId(msgBean));
		String emailId_Xhdr = parser.parseHeaders(msgBean.getHeaders());
		String emailId_Body = parser.parseMsg(msgBean.getBody());
		assertTrue(emailId_Xhdr.equals(emailId_Body));
		assertTrue(emailId_Body.equals(msgId2+""));

		msgBean.setEmBedEmailId(Boolean.TRUE);
		msgBean.setBody("This is test message with embedded Email_Id.");
		msgBean.setBody(MessageBodyBuilder.getBodyWithEmailId(msgBean));
		emailId_Xhdr = parser.parseHeaders(msgBean.getHeaders());
		emailId_Body = parser.parseMsg(msgBean.getBody());
		assertTrue(emailId_Xhdr.equals(emailId_Body));
		assertTrue(emailId_Body.equals(msgId2+""));
	}
}
