package jpa.test.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import jpa.constant.Constants;
import jpa.constant.VariableType;
import jpa.data.preload.EmailTemplateEnum;
import jpa.data.preload.EmailVariableEnum;
import jpa.data.preload.MailingListEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.MsgHeader;
import jpa.message.util.EmailIdParser;
import jpa.model.msg.MessageHeader;
import jpa.model.msg.MessageInbox;
import jpa.service.common.EmailAddressService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.task.BroadcastToList;
import jpa.spring.util.BoTestBase;
import jpa.variable.ErrorVariableVo;
import jpa.variable.RenderVariableVo;
import jpa.variable.Renderer;

@org.springframework.test.annotation.Commit
public class BroadcastToListTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = LogManager.getLogger(BroadcastToListTest.class);
	
	@Resource
	private BroadcastToList task;
	@Resource
	private EmailAddressService emailService;
	@Resource
	private MessageInboxService inboxService;

	@BeforeClass
	public static void BroadcastPrepare() {
	}

	@Test
	public void testBroadcastToList() throws Exception {
		EmailTemplateEnum testNewsLetter = EmailTemplateEnum.SampleNewsletter2;
		MessageBean mBean = new MessageBean();
		mBean.setSubject(testNewsLetter.getSubject());
		mBean.setValue(testNewsLetter.getBodyText());
		mBean.setMailboxUser("testUser");
		mBean.setRuleName(RuleNameEnum.BROADCAST.getValue());
		MessageInbox _minbox = inboxService.getLastRecord();
		mBean.setMsgId(_minbox.getRowId());
		mBean.setSenderId(Constants.DEFAULT_SENDER_ID);

		MessageContext ctx = new MessageContext(mBean);
		ctx.setTaskArguments(MailingListEnum.SMPLLST1.name());
		task.process(ctx);
		
		// now verify results
		assertFalse(ctx.getRowIds().isEmpty());
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		for (Integer rowid : ctx.getRowIds()) {
			MessageInbox minbox = inboxService.getAllDataByPrimaryKey(rowid);
			String emailId_body = parser.parseMsg(minbox.getMsgBody());
			mBean.getHeaders().clear();
			for (MessageHeader mhdr : minbox.getMessageHeaderList()) {
				MsgHeader hdr = new MsgHeader();
				hdr.setName(mhdr.getHeaderName());
				hdr.setValue(mhdr.getHeaderValue());
				mBean.getHeaders().add(hdr);
			}
			String emailId_xhdr = parser.parseHeaders(mBean.getHeaders());
			logger.info("Email_Id from body: " + emailId_body + ", from XHdr: " + emailId_xhdr);
			assertNotNull(emailId_xhdr);
			if (emailId_body != null) { // in case of text message
				assertTrue(emailId_body.equals(emailId_xhdr));
			}
			assertTrue(emailId_xhdr.equals(minbox.getRowId()+""));
			
			String to_sent = minbox.getToAddress().getAddress();
			RenderVariableVo vo = new RenderVariableVo(
					EmailVariableEnum.SubscriberAddress.name(),
					to_sent,
					VariableType.ADDRESS);
			Map<String, RenderVariableVo> vars = new HashMap<String, RenderVariableVo>();
			vars.put(vo.getVariableName(), vo);
			String subj = Renderer.getInstance().render(
					testNewsLetter.getSubject(), vars,
					new HashMap<String, ErrorVariableVo>());
			logger.info("Subject rendered: " + subj);
			logger.info("Subject msginbox: " + minbox.getMsgSubject());
			assertTrue(subj.equals(minbox.getMsgSubject()));
			
			if (EmailTemplateEnum.SampleNewsletter2.equals(testNewsLetter)) {
				assertTrue(minbox.getMsgBody().indexOf(to_sent)>0);
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			assertTrue(minbox.getMsgBody().indexOf(sdf.format(new java.util.Date()))>0);
		}
	}
}
