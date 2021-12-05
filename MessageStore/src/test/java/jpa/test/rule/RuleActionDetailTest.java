package jpa.test.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.model.rule.RuleActionDetail;
import jpa.model.rule.RuleDataType;
import jpa.service.rule.RuleActionDetailService;
import jpa.service.rule.RuleDataTypeService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class RuleActionDetailTest extends BoTestBase {
	static Logger logger = LogManager.getLogger(RuleActionDetailTest.class);
	
	final String testActionId = "testAction";
	final String testServiceName = "testService";
	
	@BeforeClass
	public static void ActionDetailPrepare() {
	}

	@Autowired
	RuleActionDetailService service;
	@Autowired
	RuleDataTypeService typeService;

	@Test
	public void actionDetailService1() {
		RuleDataType typ1 = null;
		List<RuleDataType> lst1 = typeService.getAll();
		if (!lst1.isEmpty()) {
			typ1 = lst1.get(0);
		}
		
		// test insert
		RuleActionDetail var1 = new RuleActionDetail();
		var1.setActionId(testActionId);
		var1.setServiceName(testServiceName);
		var1.setRuleDataType(typ1);
		var1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(var1);
		
		RuleActionDetail var2 = service.getByActionId(testActionId);
		assertNotNull(var2);
		logger.info("RuleActionDetail: " + PrintUtil.prettyPrint(var2));

		List<RuleActionDetail> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		
		List<String> listweb = service.getActionIdList();
		assertTrue(listweb.size()>0);
		
		// test insert
		RuleActionDetail var3 = createNewInstance(list1.get(0));
		var3.setActionId(var3.getActionId()+"_v2");
		var3.setRuleDataType(typ1);
		service.insert(var3);
		assertNotNull(service.getByActionId(var3.getActionId()));
		// end of test insert
		// test update
		var3.setUpdtUserId("jpa test");
		service.update(var3);
		RuleActionDetail var5 = service.getByActionId(var3.getActionId());
		assertTrue("jpa test".equals(var5.getUpdtUserId()));
		
		service.delete(var3);

		Optional<RuleActionDetail> deleted = service.getByRowId(var5.getRowId());
		if (deleted.isPresent()) {
			fail();
		}
		
		// test delete
		RuleActionDetail var4 = createNewInstance(var2);
		var4.setActionId(var2.getActionId() + "_v4");
		service.insert(var4);
		assertTrue(1==service.deleteByActionId(var4.getActionId()));
	}
	
	private RuleActionDetail createNewInstance(RuleActionDetail orig) {
		RuleActionDetail dest = new RuleActionDetail();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
