package jpa.test.msgdata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.constant.VariableType;
import jpa.model.SenderData;
import jpa.model.msg.MessageRendered;
import jpa.model.msg.MessageSource;
import jpa.model.msg.RenderVariable;
import jpa.model.msg.RenderVariablePK;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageRenderedService;
import jpa.service.msgdata.MessageSourceService;
import jpa.service.msgdata.RenderVariableService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class RenderVariableTest extends BoTestBase {

	@BeforeClass
	public static void RenderVariablePrepare() {
	}

	@Autowired
	RenderVariableService service;
	@Autowired
	MessageRenderedService renderedService;
	@Autowired
	SenderDataService senderService;
	@Autowired
	MessageSourceService sourceService;

	MessageRendered mrn1;
	MessageRendered mrn2;
	
	@Before
	public void prepare() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		List<MessageSource> srcs = sourceService.getAll();
		assertFalse(srcs.isEmpty());
		MessageSource src1 = srcs.get(0);

		mrn1 = new MessageRendered();
		mrn1.setMessageSource(src1);
		mrn1.setMessageTemplate(src1.getTemplateData());
		mrn1.setStartTime(updtTime);
		mrn1.setSenderData(sender);
		mrn1.setSubscriberData(null);
		mrn1.setPurgeAfter(null);
		renderedService.insert(mrn1);

		mrn2 = new MessageRendered();
		try {
			BeanUtils.copyProperties(mrn2, mrn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		renderedService.insert(mrn2);
	}
	
	@Test
	public void renderVariableService() {
		// test insert
		RenderVariable in1 = new RenderVariable();
		RenderVariablePK pk1 = new RenderVariablePK(mrn1,"jpa test variable 1");
		in1.setRenderVariablePK(pk1);
		in1.setVariableType(VariableType.TEXT.getValue());
		in1.setVariableValue("jpa test variable value 1");
		service.insert(in1);
		
		RenderVariable msg1 = service.getByPrimaryKey(in1.getRenderVariablePK());
		logger.info(PrintUtil.prettyPrint(msg1,2));
		
		RenderVariable in2 = new RenderVariable();
		try {
			BeanUtils.copyProperties(in2, msg1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		RenderVariablePK pk2 = new RenderVariablePK(mrn1,"jpa test variable 2");
		in2.setRenderVariablePK(pk2);
		service.insert(in2);
		
		RenderVariable in3 = new RenderVariable();
		try {
			BeanUtils.copyProperties(in3, msg1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		RenderVariablePK pk3 = new RenderVariablePK(mrn1,"jpa test variable 3");
		in3.setRenderVariablePK(pk3);
		service.insert(in3);

		Optional<RenderVariable> msg2  =service.getByRowId(in2.getRowId());
		assertTrue(msg2.isPresent());
		logger.info(PrintUtil.prettyPrint(msg2.get(),1));
		
		List<RenderVariable> lst1 = service.getByRenderId(mrn1.getRowId());
		assertTrue(3==lst1.size());
		RenderVariable rv1 = lst1.get(0);
		
		rv1.setUpdtUserId("jpa test");
		service.update(rv1);
		Optional<RenderVariable> rv2 = service.getByRowId(rv1.getRowId());
		assertTrue(rv2.isPresent());
		assertTrue("jpa test".equals(rv2.get().getUpdtUserId()));
		
		service.delete(in2);
		assertNull(service.getByPrimaryKey(in2.getRenderVariablePK()));

		assertTrue(1==service.deleteByPrimaryKey(in1.getRenderVariablePK()));
		assertNull(service.getByPrimaryKey(in1.getRenderVariablePK()));
		assertTrue(0==service.deleteByRowId(in1.getRowId()));
		assertTrue(1==service.deleteByRenderId(mrn1.getRowId()));
		assertTrue(0==service.getByRenderId(mrn1.getRowId()).size());
	}
}
