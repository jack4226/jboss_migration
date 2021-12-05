package jpa.test.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.constant.RuleCategory;
import jpa.constant.RuleType;
import jpa.data.preload.RuleNameEnum;
import jpa.model.rule.RuleLogic;
import jpa.service.rule.RuleLogicService;
import jpa.service.rule.RuleLogicWithCountService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class RuleLogicTest extends BoTestBase {
	Logger logger = LogManager.getLogger(RuleLogicTest.class);

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void RuleLogicPrepare() {
	}

	@Autowired
	RuleLogicService service;
	
	@Autowired
	RuleLogicWithCountService serviceWithCount;
	
	@Test
	//@org.junit.Ignore
	public void ruleLogicService1() {
		assertNotNull(entityManager);
		// test insert
		RuleLogic obj1 = new RuleLogic();
		obj1.setRuleName("testrule1");
		obj1.setEvalSequence(0);
		obj1.setRuleType(RuleType.ALL.getValue());
		obj1.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
		obj1.setMailType(Constants.SMTP_MAIL);
		obj1.setRuleCategory(RuleCategory.PRE_RULE.getValue());
		obj1.setSubrule(false);
		obj1.setBuiltinRule(false);
		obj1.setDescription("simply get rid of the messages from the mailbox.");
		service.insert(obj1);
		
		RuleLogic objs2 = service.getByRuleName("testrule1");
		assertNotNull(objs2);
		logger.info(PrintUtil.prettyPrint(objs2));
		
		List<String> listweb1 = service.getBuiltinRuleNames4Web();
		assertTrue(listweb1.size()>0);
		List<String> listweb2 = service.getCustomRuleNames4Web();
		assertTrue(listweb2.size()>0);
		
		// test update
		RuleLogic obj2 = objs2;
		obj2.setUpdtUserId("JpaTest");
		service.update(obj2);
		RuleLogic obj3 = service.getByRowId(obj2.getRowId());
		assertTrue("JpaTest".equals(obj3.getUpdtUserId()));
		
		RuleLogic obj3b = service.getByRowId(99999999);
		assertNull(obj3b);
		
		RuleLogic badrule = service.getByRuleName("bad test rule name");
		if (badrule != null) fail();

		// test insert
		RuleLogic obj4 = new RuleLogic();
		try {
			BeanUtils.copyProperties(obj4, obj3);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		obj4.setRuleName(obj3.getRuleName()+"_v2");
		service.insert(obj4);
		
		RuleLogic objs4 = service.getByRuleName(obj4.getRuleName());
		assertNotNull(objs4);
		assertTrue(obj3.getRowId()!=objs4.getRowId());
		
		assertTrue(1<service.getNextEvalSequence());
		List<RuleLogic> lst1 = service.getActiveRules();
		for (RuleLogic objs : lst1) {
			logger.info(PrintUtil.prettyPrint(objs,1));
		}
		logger.info("Number of active rules: " + lst1.size());
		RuleLogic logic = service.getByRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		logger.info(PrintUtil.prettyPrint(logic));
		
		boolean hasSubrules = service.getHasSubrules(RuleNameEnum.HARD_BOUNCE.getValue());
		assertTrue(hasSubrules);
		
		List<RuleLogic> lst2 = service.getSubrules(false);
		assertFalse(lst2.isEmpty());
		List<RuleLogic> lst3 = service.getSubrules(true);
		assertTrue(lst2.size()>lst3.size());

		// test delete
		assertTrue(1==service.deleteByRuleName(obj3.getRuleName()));
	}
	
	@Test
	public void testRuleLogicCountService() {
		// test insert
		RuleLogic obj1 = new RuleLogic();
		obj1.setRuleName("testrule1");
		obj1.setEvalSequence(0);
		obj1.setRuleType(RuleType.ALL.getValue());
		obj1.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
		obj1.setMailType(Constants.SMTP_MAIL);
		obj1.setRuleCategory(RuleCategory.PRE_RULE.getValue());
		obj1.setSubrule(false);
		obj1.setBuiltinRule(false);
		obj1.setDescription("simply get rid of the messages from the mailbox.");
		serviceWithCount.insert(obj1);

		logger.info("Calling getByRuleName()...");
		Object[] objs2 = serviceWithCount.getByRuleName("testrule1");
		assertNotNull(objs2);
		assertTrue(objs2.length > 0);
		for (int i=0; i<objs2.length; i++) {
			Object obj = objs2[i];
			logger.info("Object[" + i + "]: "  + PrintUtil.prettyPrint(obj));
		}
		
		logger.info("Calling getByActiveRules()...");
		List<Object[]> objs3 = serviceWithCount.getByActiveRules();
		assertTrue(objs3!=null && objs3.size()>0);
		int idx1 = 0;
		for (Object[] objs : objs3) {
			logger.info("ActiveRule[" + idx1 + "]: ##############################################");
			assertTrue(objs!=null && objs.length>0);
			for (int i=0; i<objs.length; i++) {
				Object obj = objs[i];
				logger.info("Object[" + i + "]: "  + PrintUtil.prettyPrint(obj));
			}
			idx1++;
		}
		
		logger.info("Calling getAll(builtin)...");
		List<Object[]> objs4 = serviceWithCount.getAll(true);
		assertTrue(objs4!=null && objs4.size()>0);
		int idx2 = 0;
		for (Object[] objs : objs4) {
			logger.info("BuiltinRule[" + idx2 + "]: ++++++++++++++++++++++++++++++++++++++++++++++");
			assertTrue(objs!=null && objs.length>0);
			for (int i=0; i<objs.length; i++) {
				Object obj = objs[i];
				logger.info("Object[" + i + "]: "  + PrintUtil.prettyPrint(obj));
			}
			idx2++;
		}

	}
}
