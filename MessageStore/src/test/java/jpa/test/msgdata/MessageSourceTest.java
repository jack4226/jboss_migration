package jpa.test.msgdata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.constant.VariableType;
import jpa.model.EmailAddress;
import jpa.model.SenderData;
import jpa.model.msg.MessageSource;
import jpa.model.msg.TemplateData;
import jpa.model.msg.TemplateDataPK;
import jpa.model.msg.TemplateVariable;
import jpa.model.msg.TemplateVariablePK;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageSourceService;
import jpa.service.msgdata.TemplateDataService;
import jpa.service.msgdata.TemplateVariableService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class MessageSourceTest extends BoTestBase {

	@BeforeClass
	public static void MessageSourcePrepare() {
	}

	@Autowired
	MessageSourceService service;
	@Autowired
	EmailAddressService addrService;
	@Autowired
	TemplateDataService templateService;
	@Autowired
	TemplateVariableService variableService;
	@Autowired
	SenderDataService senderService;

	private TemplateData tmp1;
	private TemplateVariable var1;
	
	@Before
	public void prepare() {
		Timestamp tms = new Timestamp(System.currentTimeMillis());
		String testTemplateId = "jpa test template id";
		String testSenderId = Constants.DEFAULT_SENDER_ID;
		SenderData cd0 = senderService.getBySenderId(testSenderId);
		TemplateDataPK tpk0 = new TemplateDataPK(cd0, testTemplateId, tms);
		tmp1 = new TemplateData();
		tmp1.setTemplateDataPK(tpk0);
		tmp1.setContentType("text/plain");
		tmp1.setBodyTemplate("jpa test template value");
		tmp1.setSubjectTemplate("jpa test subject");
		templateService.insert(tmp1);

		String testVariableId = "jpa test variable id";
		String testVariableName = "jpa test variable name";
		TemplateVariablePK vpk0 = new TemplateVariablePK(cd0, testVariableId, testVariableName, tms);
		var1 = new TemplateVariable();
		var1.setTemplateVariablePK(vpk0);
		var1.setVariableType(VariableType.TEXT.getValue());
		var1.setVariableValue("jpa test variable value");
		variableService.insert(var1);
	}
	
	private String testMsgSourceId = "test jpa msgsource id";
	
	@Test
	public void messageSourceService() {
		EmailAddress adr1 = addrService.findSertAddress("jsmith@test.com");
		MessageSource src1 = new MessageSource();
		src1.setMsgSourceId(testMsgSourceId);
		src1.setTemplateData(tmp1);
		src1.getTemplateVariableList().add(var1);
		src1.setFromAddress(adr1);
		service.insert(src1);
		
		List<MessageSource> list = service.getAll();
		assertFalse(list.isEmpty());
		
		MessageSource tkn0 = service.getByMsgSourceId(testMsgSourceId);
		assertNotNull(tkn0);
		assertFalse(tkn0.getTemplateVariableList().isEmpty());
		logger.info(PrintUtil.prettyPrint(tkn0,2));
		
		List<MessageSource> lst2 = service.getByFromAddress(adr1.getAddress());
		assertFalse(lst2.isEmpty());

		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		MessageSource tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert
		MessageSource tkn2 = new MessageSource();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setMsgSourceId(tkn1.getMsgSourceId()+"_v2");
		// to prevent "found shared references to a collection" error from Hibernate
		tkn2.setTemplateVariableList(null);
		service.insert(tkn2);
		
		MessageSource tkn3 = service.getByMsgSourceId(tkn2.getMsgSourceId());
		assertTrue(tkn3.getRowId()!=tkn1.getRowId());
		// end of test insert
		
		// test select with no result
		service.delete(tkn3);
		assertNull(service.getByMsgSourceId(tkn2.getMsgSourceId()));

		
		assertTrue(0==service.deleteByMsgSourceId(tkn3.getMsgSourceId()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
}
