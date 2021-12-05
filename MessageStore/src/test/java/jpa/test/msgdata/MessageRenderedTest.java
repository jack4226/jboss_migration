package jpa.test.msgdata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.model.msg.MessageRendered;
import jpa.model.msg.MessageSource;
import jpa.model.msg.RenderVariable;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageRenderedService;
import jpa.service.msgdata.MessageSourceService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class MessageRenderedTest extends BoTestBase {

	@BeforeClass
	public static void MessageRenderedPrepare() {
	}

	@Autowired
	MessageRenderedService service;
	@Autowired
	SenderDataService senderService;
	@Autowired
	MessageSourceService sourceService;

	@Test
	public void messageRenderedService() {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		
		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		List<MessageSource> srcs = sourceService.getAll();
		assertFalse(srcs.isEmpty());
		MessageSource src1 = srcs.get(0);

		MessageRendered in1 = new MessageRendered();
		in1.setMessageSource(src1);
		in1.setMessageTemplate(src1.getTemplateData());
		in1.setStartTime(updtTime);
		in1.setSenderData(sender);
		in1.setSubscriberData(null);
		in1.setPurgeAfter(null);
		service.insert(in1);
		
		Optional<MessageRendered> msg1 = service.getByPrimaryKey(in1.getRowId());
		assertTrue(msg1.isPresent());
		logger.info("Message #1: " + PrintUtil.prettyPrint(msg1.get(),2));
		
		MessageRendered  first = service.getFirstRecord();
		assertNotNull(first);
		assertNull(service.getPrevoiusRecord(first));
		
		MessageRendered in2 = new MessageRendered();
		try {
			BeanUtils.copyProperties(in2, msg1.get());
			in2.setRenderAttachmentList(null);
			in2.setRenderVariableList(new ArrayList<RenderVariable>());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		service.insert(in2);
		
		MessageRendered msg2 = service.getLastRecord();
		logger.info("Message #2: " + PrintUtil.prettyPrint(msg2,2));
		Optional<MessageRendered> msg22 = service.getAllDataByPrimaryKey(msg2.getRowId());
		assertTrue(msg22.isPresent());
		logger.info("Message #2 (All Data): " + PrintUtil.prettyPrint(msg22.get(),2));
		
		assertNull(service.getNextRecord(msg2));
		
		MessageRendered msg3  =service.getPrevoiusRecord(msg2);
		if (msg3 != null) {
			logger.info("Message #3: " + PrintUtil.prettyPrint(msg3,2));
			
			assertTrue(msg1.get().equals(msg3));
	
			MessageRendered msg4  =service.getNextRecord(msg3);
			assertTrue(msg2.equals(msg4));
		}
		else {
			assertTrue("MessageRendered table is empty", true);
		}
	}
}
