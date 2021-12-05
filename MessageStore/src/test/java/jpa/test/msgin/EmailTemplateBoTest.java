package jpa.test.msgin;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.exception.DataValidationException;
import jpa.model.SenderData;
import jpa.model.msg.TemplateData;
import jpa.model.msg.TemplateDataPK;
import jpa.service.common.SenderDataService;
import jpa.service.maillist.EmailTemplateBo;
import jpa.service.maillist.RenderBo;
import jpa.service.maillist.TemplateRenderVo;
import jpa.service.msgdata.TemplateDataService;
import jpa.spring.util.BoTestBase;
import jpa.variable.RenderUtil;

@org.springframework.test.annotation.Commit
public class EmailTemplateBoTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(EmailTemplateBoTest.class);

	static final String LF = System.getProperty("line.separator", "\n");
	
	@BeforeClass
	public static void EmailTemplatePrepare() {
	}

	@Autowired
	EmailTemplateBo service;
	@Autowired
	private RenderBo renderBo;
	@Autowired
	private SenderDataService senderService;
	@Autowired
	private TemplateDataService templateDataDao;

	@Test
	public void testEmailTemplateBo() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			String text = renderBo.renderTemplateById("testTemplate", null, null);
			logger.info(text);
			assertTrue(text.indexOf(sdf.format(new java.util.Date()))>0);
			SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
			TemplateDataPK pk = new TemplateDataPK(sender, "testTemplate", null);
			TemplateData bodyVo = templateDataDao.getByBestMatch(pk);
			if (bodyVo == null) {
				throw new DataValidationException("BodyTemplate not found for testTemplate");
			}
			else {
				logger.debug("Template to render:" + LF + bodyVo.getBodyTemplate());
				assertTrue(bodyVo.getBodyTemplate().indexOf("${CurrentDate}")>0);
			}
			List<String> variables = RenderUtil.retrieveVariableNames(bodyVo.getBodyTemplate());
			logger.info("Variables: " + variables);
			assertTrue(variables.contains("CurrentDate"));
			
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("BroadcastMsgId","3");
			TemplateRenderVo renderVo = service.renderEmailTemplate("jsmith@test.com",vars, "SampleNewsletter1");
			logger.info(renderVo);
			assertTrue(renderVo.getBody().indexOf("${CurrentDate}")<0);
			assertTrue(renderVo.getBody().indexOf(sdf.format(new java.util.Date()))>0);
			assertTrue(renderVo.getBody().indexOf("BroadcastMsgId: 3")>0);
			
			int sbsrid = 1;
			String userProfileUrl = service.renderEmailVariable("UserProfileURL", Integer.valueOf(sbsrid));
			logger.info(userProfileUrl);
			assertTrue(userProfileUrl.indexOf("="+sbsrid)>0);
			
			String checkText = "Dear ${SubscriberAddress}," + LF + LF + 
			"This is a sample text newsletter message for a traditional mailing list." + LF +
			"With a traditional mailing list, people who want to subscribe to the list " + LF +
			"must send an email from their account to the mailing list address with " + LF +
			"\"subscribe\" in the email subject." + LF + LF + 
			"Unsubscribing from a traditional mailing list is just as easy; simply send " + LF +
			"an email to the mailing list address with \"unsubscribe\" in subject." + LF + LF +
			"Date sent: ${CurrentDate}" + LF + LF +
			"BroadcastMsgId: ${BroadcastMsgId}, ListId: ${MailingListId}" + LF + LF +
			"Contact Email: ${ContactEmailAddress}" + LF + LF +
			"To see our promotions, copy and paste the following link in your browser:" + LF +
			"${WebSiteUrl}/SamplePromoPage.jsp?msgid=${BroadcastMsgId}&listid=${MailingListId}&sbsrid=${SubscriberAddressId}" + LF +
			"${FooterWithUnsubAddr}";
			try {
				service.checkVariableLoop(checkText);
			}
			catch (DataValidationException e) {
				logger.error("DataValidationException caught", e);
				fail();
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
}
