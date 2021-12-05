package jpa.test.msgout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
		MessageRendered mr0 = renderedService.getFirstRecord();
		logger.info("MessageRendered:\n" + ToStringBuilder.reflectionToString(mr0, ToStringStyle.MULTI_LINE_STYLE));
		Optional<MessageRendered> mr = renderedService.getAllDataByPrimaryKey(mr0.getRowId());
		assertTrue(mr.isPresent());
		
		try {
			MessageBean bean = service.getMessageByPK(mr.get().getRowId());
			logger.info("MessageRendered:\n" + PrintUtil.prettyPrint(mr, 1));
			logger.info("MessageBean retrieved:\n" + bean);
			assertNotNull(bean.getRenderId());
			assertNotNull(mr.get().getRowId());
			assertEquals(mr.get().getRowId(), bean.getRenderId());
			if (StringUtils.isNotBlank(bean.getSenderId())) {
				assertTrue(bean.getSenderId().equals(mr.get().getSenderData().getSenderId()));
			}
			assertTrue(bean.getFromAsString().equals(mr.get().getMessageSource().getFromAddress().getAddress()));
			assertTrue(bean.getCarrierCode().getValue().equals(mr.get().getMessageSource().getCarrierCode()));
			logger.info("subject: " + bean.getSubject());
			logger.info("body: " + bean.getBody());
			Renderer renderer = Renderer.getInstance();
			String bodyTmptl = mr.get().getMessageTemplate().getBodyTemplate();
			String subjTmptl = mr.get().getMessageTemplate().getSubjectTemplate();
			List<RenderVariable> varbles = mr.get().getRenderVariableList();
			Map<String, RenderVariableVo> map = RenderBo.renderVariablesToMap(varbles);
			Map<String, ErrorVariableVo> errors = new HashMap<String, ErrorVariableVo>();
			assertTrue(bean.getBody().equals(renderer.render(bodyTmptl, map, errors)));
			assertTrue(bean.getSubject().equals(renderer.render(subjTmptl, map, errors)));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		RenderRequest req = service.getRenderRequestByPK(mr.get().getRowId());
		assertNotNull(req);
		assertTrue(req.getMsgSourceId().equals(mr.get().getMessageSource().getMsgSourceId()));
		assertTrue(req.getSenderId().equals(mr.get().getSenderData().getSenderId()));
		assertTrue(req.getVariableOverrides().size()>=mr.get().getRenderVariableList().size());
		try {
			RenderResponse rsp = renderBo.getRenderedEmail(req);
			int renderId = service.saveRenderData(rsp);
			Optional<MessageRendered> mr2 = renderedService.getByPrimaryKey(renderId);
			assertTrue(mr2.isPresent());
			assertTrue(mr2.get().getSenderData().getRowId().equals(mr.get().getSenderData().getRowId()));
			assertTrue(mr2.get().getMessageSource().getRowId()==mr.get().getMessageSource().getRowId());
			assertTrue(mr2.get().getMessageTemplate().getRowId()==mr.get().getMessageTemplate().getRowId());
			assertTrue(mr2.get().getRenderAttachmentList().size()==mr.get().getRenderAttachmentList().size());
			assertTrue(mr2.get().getRenderVariableList().size()>=mr.get().getRenderVariableList().size());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
