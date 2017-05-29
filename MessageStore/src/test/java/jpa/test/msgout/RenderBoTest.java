package jpa.test.msgout;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.VariableType;
import jpa.data.preload.GlobalVariableEnum;
import jpa.data.preload.SenderVariableEnum;
import jpa.model.msg.MessageRendered;
import jpa.service.maillist.RenderRequest;
import jpa.service.maillist.RenderResponse;
import jpa.service.msgdata.MessageRenderedService;
import jpa.service.msgout.MsgOutboxBo;
import jpa.spring.util.BoTestBase;
import jpa.variable.RenderVariableVo;

@org.springframework.test.annotation.Commit
public class RenderBoTest extends BoTestBase {

	@BeforeClass
	public static void RenderBoPrepare() {
	}

	@Autowired
	jpa.service.maillist.RenderBo service;
	@Autowired
	MsgOutboxBo outboxBo;
	@Autowired
	MessageRenderedService renderedService;

	@Test
	public void renderBoService() {
		MessageRendered mr = renderedService.getFirstRecord();
		
		RenderRequest req = outboxBo.getRenderRequestByPK(mr.getRowId());
		assertTrue(StringUtils.isNotBlank(req.getMsgSourceId()));
		assertTrue(StringUtils.isNotBlank(req.getSenderId()));
		assertTrue(Constants.DEFAULT_SENDER_ID.equals(req.getSenderId()));
		assertFalse(req.getVariableOverrides().isEmpty());
		assertTrue(req.getVariableOverrides().containsKey(GlobalVariableEnum.CurrentDate.name()));
		//assertTrue(req.getVariableOverrides().containsKey(SenderVariableEnum.SenderId.name()));
		RenderVariableVo vo = new RenderVariableVo(
				EmailAddrType.TO_ADDR.getValue(),
				"testto@test.com",
				VariableType.ADDRESS);
		req.getVariableOverrides().put(vo.getVariableName(), vo);
		
		try {
			RenderResponse rsp = service.getRenderedEmail(req);
			assertTrue(req.getMsgSourceId().equals(rsp.getMessageSource().getMsgSourceId()));
			assertTrue(req.getSenderId().equals(rsp.getSenderId()));
			assertNotNull(rsp.getMessageBean());
			assertFalse(rsp.getVariableFinal().isEmpty());
			assertTrue(rsp.getVariableErrors().isEmpty());
			assertTrue(rsp.getVariableFinal().size()>req.getVariableOverrides().size());
			for (SenderVariableEnum var : SenderVariableEnum.values()) {
				assertTrue(rsp.getVariableFinal().containsKey(var.name()));
			}
			for (GlobalVariableEnum var : GlobalVariableEnum.values()) {
				assertTrue(rsp.getVariableFinal().containsKey(var.name()));
			}
			assertTrue(rsp.getVariableFinal().containsKey(EmailAddrType.FROM_ADDR.getValue()));
			assertNotNull(rsp.getMessageBean().getFromAsString());
			assertTrue(rsp.getMessageBean().getFromAsString().equals(rsp.getVariableFinal().get(EmailAddrType.FROM_ADDR.getValue()).getVariableValue()));
			assertTrue(rsp.getMessageBean().getToAsString().equals(rsp.getVariableFinal().get(EmailAddrType.TO_ADDR.getValue()).getVariableValue()));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
