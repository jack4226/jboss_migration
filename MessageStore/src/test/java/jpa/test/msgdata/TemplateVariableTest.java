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
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.constant.VariableType;
import jpa.model.SenderData;
import jpa.model.SenderVariable;
import jpa.model.SubscriberData;
import jpa.model.UserData;
import jpa.model.msg.TemplateData;
import jpa.model.msg.TemplateDataPK;
import jpa.model.msg.TemplateVariable;
import jpa.model.msg.TemplateVariablePK;
import jpa.model.rule.RuleAction;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.TemplateDataService;
import jpa.service.msgdata.TemplateVariableService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

//@org.springframework.test.annotation.Commit
public class TemplateVariableTest extends BoTestBase {
	static Logger logger = Logger.getLogger(TemplateVariableTest.class);
	
	final String testTemplateId = "jpa test template id";
	final String testSenderId = Constants.DEFAULT_SENDER_ID;
	final String testVariableId = "jpa test variable id";
	final String testVariableName = "jpa test variable name";
	
	@BeforeClass
	public static void TemplateVariablePrepare() {
	}

	@Autowired
	TemplateVariableService service;
	@Autowired
	TemplateDataService templateService;
	@Autowired
	SenderDataService senderService;
	
	private TemplateData tmp0;
	private SenderData cd0;
	
	@Before
	public void prepare() {
		SenderData sender = senderService.getBySenderId(testSenderId);
		
		cd0 = new SenderData();
		try {
			BeanUtils.copyProperties(cd0, sender);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		cd0.setSenderId(Constants.DEFAULT_SENDER_ID + "_v2");
		cd0.setSenderVariables(new ArrayList<SenderVariable>());
		cd0.setSubscribers(new ArrayList<SubscriberData>());
		cd0.setUserDatas(new ArrayList<UserData>());
		cd0.setRuleActions(new ArrayList<RuleAction>());
		senderService.insert(cd0);

		TemplateDataPK pk0 = new TemplateDataPK(cd0, testTemplateId, new Timestamp(System.currentTimeMillis()));
		tmp0 = new TemplateData();
		tmp0.setTemplateDataPK(pk0);
		tmp0.setContentType("text/plain");
		tmp0.setBodyTemplate("jpa test template value");
		tmp0.setSubjectTemplate("jpa test subject");
		templateService.insert(tmp0);
	}

	@After
	public void teardown() {
		templateService.delete(tmp0);
	}

	@Test
	public void templateDataService() {
		Timestamp tms = new Timestamp(System.currentTimeMillis());
		
		TemplateVariablePK pk0 = new TemplateVariablePK(cd0, testVariableId, testVariableName, tms);
		TemplateVariable rcd1 = new TemplateVariable();
		rcd1.setTemplateVariablePK(pk0);
		rcd1.setVariableType(VariableType.TEXT.getValue());
		rcd1.setVariableValue("jpa test variable value");
		service.insert(rcd1);
		
		TemplateVariable var1 = service.getByBestMatch(pk0);
		assertNotNull(var1);
		logger.info("TemplateVariable: " + PrintUtil.prettyPrint(var1,2));

		TemplateVariablePK pk1 = var1.getTemplateVariablePK();
		TemplateVariable var2 = service.getByPrimaryKey(pk1);
		assertTrue(var1.equals(var2));

		List<TemplateVariable> list1 = service.getByVariableId(pk1.getVariableId());
		assertFalse(list1.isEmpty());
		
		List<TemplateVariable> list2 = service.getCurrentByVariableId(pk1.getVariableId());
		assertFalse(list2.isEmpty());
		assertTrue(list1.size()>=list2.size());
		
		List<TemplateVariable> list3 = service.getCurrentBySenderId(testSenderId);
		assertFalse(list3.isEmpty());
		for (TemplateVariable rec : list3) {
			logger.info(PrintUtil.prettyPrint(rec,2));
		}

		// test insert
		Timestamp newTms = new Timestamp(System.currentTimeMillis()+1000);
		TemplateVariable var3 = createNewInstance(var2);
		TemplateVariablePK pk2 = var2.getTemplateVariablePK();
		TemplateVariablePK pk3 = new TemplateVariablePK(pk2.getSenderData(), pk2.getVariableId(), pk2.getVariableName(), newTms);
		var3.setTemplateVariablePK(pk3);
		service.insert(var3);
		assertNotNull(service.getByPrimaryKey(pk3));
		// end of test insert
		
		service.delete(var3);
		assertNull(service.getByPrimaryKey(pk3));


		// test deleteByVariableName
		TemplateVariable var4 = createNewInstance(var2);
		TemplateVariablePK pk4 = new TemplateVariablePK(pk2.getSenderData(), pk2.getVariableId(), pk2.getVariableName()+"_v4", pk2.getStartTime());
		var4.setTemplateVariablePK(pk4);
		service.insert(var4);
		assertTrue(1==service.deleteByVariableName(pk4.getVariableName()));
		assertNull(service.getByPrimaryKey(pk4));

		// test deleteByPrimaryKey
		TemplateVariable var5 = createNewInstance(var2);
		TemplateVariablePK pk5 = new TemplateVariablePK(pk2.getSenderData(), pk2.getVariableId(), pk2.getVariableName()+"_v5", pk2.getStartTime());
		var5.setTemplateVariablePK(pk5);
		service.insert(var5);
		assertTrue(1==service.deleteByPrimaryKey(pk5));
		assertNull(service.getByPrimaryKey(pk5));
		
		// test deleteVariableId
		TemplateVariable var6 = createNewInstance(var2);
		TemplateVariablePK pk6 = new TemplateVariablePK(pk2.getSenderData(), pk2.getVariableId(), pk2.getVariableName()+"_v6", pk2.getStartTime());
		var6.setTemplateVariablePK(pk6);
		service.insert(var6);
		int rowsDeleted = service.deleteByVariableId(pk6.getVariableId());
		assertTrue(1<=rowsDeleted);
		assertNull(service.getByPrimaryKey(pk6));
		
		// test deleteSenderId
		TemplateVariable var7 = createNewInstance(var2);
		TemplateVariablePK pk7 = new TemplateVariablePK(pk2.getSenderData(), pk2.getVariableId(), pk2.getVariableName()+"_v7", pk2.getStartTime());
		var7.setTemplateVariablePK(pk7);
		service.insert(var7);
		rowsDeleted = service.deleteBySenderId(pk7.getSenderData().getSenderId());
		assertTrue(1<=rowsDeleted);
		assertNull(service.getByPrimaryKey(pk6));
		
		// test update
		TemplateVariable var9 = createNewInstance(var2);
		TemplateVariablePK pk9 = new TemplateVariablePK(pk2.getSenderData(), pk2.getVariableId(), pk2.getVariableName()+"_v6", pk2.getStartTime());
		var9.setTemplateVariablePK(pk9);
		service.insert(var9);
		assertNotNull(service.getByPrimaryKey(pk9));
		var9.setVariableValue("new test value");
		service.update(var9);
		Optional<TemplateVariable> var_updt = service.getByRowId(var9.getRowId());
		assertTrue(var_updt.isPresent());
		assertTrue("new test value".equals(var_updt.get().getVariableValue()));
		// end of test update
		
		service.delete(var9);
		assertNull(service.getByRowId(var9.getRowId()));
	}

	private TemplateVariable createNewInstance(TemplateVariable orig) {
		TemplateVariable dest = new TemplateVariable();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
