package jpa.test.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.RuleCriteria;
import jpa.constant.RuleDataName;
import jpa.data.preload.RuleNameEnum;
import jpa.model.rule.RuleElement;
import jpa.model.rule.RuleElementPK;
import jpa.model.rule.RuleLogic;
import jpa.service.rule.RuleElementService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.BoTestBase;
import jpa.util.BeanCopyUtil;
import jpa.util.PrintUtil;

public class RuleElementTest extends BoTestBase {

	@Autowired
	private EntityManager entityManager;

	@BeforeClass
	public static void RuleElementPrepare() {
	}

	@Autowired
	RuleElementService service;
	@Autowired
	RuleLogicService logicService;
	
	@Test
	public void ruleElementService1() {
		assertNotNull(entityManager);
		RuleLogic logic = logicService.getByRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		assertNotNull(logic.getRuleElements());
		assertFalse(logic.getRuleElements().isEmpty());

		// test insert
		int size = logic.getRuleElements().size();
		RuleElement obj1 = new RuleElement();
		BeanCopyUtil.registerBeanUtilsConverters();
		try {
			BeanUtils.copyProperties(obj1, logic.getRuleElements().get(size-1));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		RuleElementPK pk1 = new RuleElementPK();
		pk1.setRuleLogic(logic);
		pk1.setElementSequence(logic.getRuleElements().get(size-1).getRuleElementPK().getElementSequence()+1);
		obj1.setRuleElementPK(pk1);
		obj1.setDataName(RuleDataName.BODY.getValue());
		obj1.setCriteria(RuleCriteria.CONTAINS.getValue());
		obj1.setTargetText("Mail delivery failed.");
		service.insert(obj1);
		
		assertFalse(service.getAll().isEmpty());

		List<RuleElement> lst1 = service.getByRuleName(logic.getRuleName());
		assertFalse(lst1.isEmpty());
		RuleElement elem = lst1.get(0);
		logger.info(PrintUtil.prettyPrint(elem));
		
		// test update
		RuleElement obj2 = logic.getRuleElements().get(size-1);
		obj2.setUpdtUserId("JpaTest");
		service.update(obj2);
		RuleElement obj3 = service.getByRowId(obj2.getRowId());
		assertTrue("JpaTest".equals(obj3.getUpdtUserId()));
		
		// test insert
		RuleElement obj4 = new RuleElement();
		try {
			BeanUtils.copyProperties(obj4, obj3);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		RuleElementPK pk2 = new RuleElementPK();
		pk2.setRuleLogic(obj3.getRuleElementPK().getRuleLogic());
		pk2.setElementSequence(obj3.getRuleElementPK().getElementSequence()+2);
		obj4.setRuleElementPK(pk2);
		service.insert(obj4);
		
		RuleElement objs4 = service.getByPrimaryKey(obj4.getRuleElementPK());
		assertNotNull(objs4);
		assertTrue(obj3.getRowId()!=objs4.getRowId());
		service.delete(objs4);
		RuleElement deleted = service.getByPrimaryKey(obj4.getRuleElementPK());
		if (deleted != null) {
			fail();
		}
		// test delete
		int random = new Random().nextInt(3);
		if (random == 0) {
			assertTrue(1==service.deleteByRowId(elem.getRowId()));
		}
		else if (random == 1) {
			assertTrue(1==service.deleteByPrimaryKey(obj3.getRuleElementPK()));
		}
		else {
			assertTrue(1<service.deleteByRuleName(obj3.getRuleElementPK().getRuleLogic().getRuleName()));
		}
	}
}
