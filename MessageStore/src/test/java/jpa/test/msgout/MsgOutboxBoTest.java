package jpa.test.msgout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.message.MessageBean;
import jpa.model.msg.MessageRendered;
import jpa.model.msg.RenderVariable;
import jpa.service.maillist.RenderBo;
import jpa.service.maillist.RenderRequest;
import jpa.service.maillist.RenderResponse;
import jpa.service.msgdata.MessageRenderedService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;
import jpa.variable.ErrorVariableVo;
import jpa.variable.RenderVariableVo;
import jpa.variable.Renderer;

@org.springframework.test.annotation.Commit
public class MsgOutboxBoTest extends BoTestBase {

	@BeforeClass
	public static void MsgOutboxBoPrepare() {
	}

	@Autowired
	jpa.service.msgout.MsgOutboxBo service;
	@Autowired
	RenderBo renderBo;
	@Autowired
	MessageRenderedService renderedService;

	@Test
	public void msgOutboxBoService() {
		MessageRendered mr = renderedService.getFirstRecord();
		logger.info("MessageRendered:\n" + ToStringBuilder.reflectionToString(mr, ToStringStyle.MULTI_LINE_STYLE));
		mr = renderedService.getAllDataByPrimaryKey(mr.getRowId());
		
		try {
			MessageBean bean = service.getMessageByPK(mr.getRowId());
			logger.info("MessageRendered:\n" + PrintUtil.prettyPrint(mr, 1));
			logger.info("MessageBean retrieved:\n" + bean);
			assertNotNull(bean.getRenderId());
			assertNotNull(mr.getRowId());
			assertEquals(mr.getRowId(), bean.getRenderId());
			if (StringUtils.isNotBlank(bean.getSenderId())) {
				assertTrue(bean.getSenderId().equals(mr.getSenderData().getSenderId()));
			}
			assertTrue(bean.getFromAsString().equals(mr.getMessageSource().getFromAddress().getAddress()));
			assertTrue(bean.getCarrierCode().getValue().equals(mr.getMessageSource().getCarrierCode()));
			logger.info("subject: " + bean.getSubject());
			logger.info("body: " + bean.getBody());
			Renderer renderer = Renderer.getInstance();
			String bodyTmptl = mr.getMessageTemplate().getBodyTemplate();
			String subjTmptl = mr.getMessageTemplate().getSubjectTemplate();
			List<RenderVariable> varbles = mr.getRenderVariableList();
			Map<String, RenderVariableVo> map = RenderBo.renderVariablesToMap(varbles);
			Map<String, ErrorVariableVo> errors = new HashMap<String, ErrorVariableVo>();
			assertTrue(bean.getBody().equals(renderer.render(bodyTmptl, map, errors)));
			assertTrue(bean.getSubject().equals(renderer.render(subjTmptl, map, errors)));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		RenderRequest req = service.getRenderRequestByPK(mr.getRowId());
		assertNotNull(req);
		assertTrue(req.getMsgSourceId().equals(mr.getMessageSource().getMsgSourceId()));
		assertTrue(req.getSenderId().equals(mr.getSenderData().getSenderId()));
		assertTrue(req.getVariableOverrides().size()>=mr.getRenderVariableList().size());
		try {
			RenderResponse rsp = renderBo.getRenderedEmail(req);
			int renderId = service.saveRenderData(rsp);
			MessageRendered mr2 = renderedService.getByPrimaryKey(renderId);
			assertTrue(mr2.getSenderData().getRowId().equals(mr.getSenderData().getRowId()));
			assertTrue(mr2.getMessageSource().getRowId()==mr.getMessageSource().getRowId());
			assertTrue(mr2.getMessageTemplate().getRowId()==mr.getMessageTemplate().getRowId());
			assertTrue(mr2.getRenderAttachmentList().size()==mr.getRenderAttachmentList().size());
			assertTrue(mr2.getRenderVariableList().size()>=mr.getRenderVariableList().size());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
