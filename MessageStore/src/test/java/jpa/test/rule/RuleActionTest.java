package jpa.test.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.data.preload.RuleNameEnum;
import jpa.model.SenderData;
import jpa.model.rule.RuleAction;
import jpa.model.rule.RuleActionDetail;
import jpa.model.rule.RuleActionPK;
import jpa.model.rule.RuleDataType;
import jpa.model.rule.RuleLogic;
import jpa.service.common.SenderDataService;
import jpa.service.rule.RuleActionDetailService;
import jpa.service.rule.RuleActionService;
import jpa.service.rule.RuleDataTypeService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.BoTestBase;
import jpa.util.JpaUtil;
import jpa.util.PrintUtil;

public class RuleActionTest extends BoTestBase {
	static Logger logger = LogManager.getLogger(RuleActionTest.class);
	
	final String testActionId = "testAction";
	final String testFieldValues = "test Field Values";
	
	@BeforeClass
	public static void ActionDetailPrepare() {
	}

	@Autowired
	RuleActionService service;
	@Autowired
	RuleLogicService logicService;
	@Autowired
	SenderDataService senderService;
	@Autowired
	RuleActionDetailService detailService;
	@Autowired
	RuleDataTypeService typeService;

	RuleActionDetail dtl1 = null;
	
	@Before
	public void prepare() {
		String testServiceName = "testService";
		RuleDataType typ1 = null;
		List<RuleDataType> lst1 = typeService.getAll();
		if (!lst1.isEmpty()) {
			typ1 = lst1.get(0);
		}
		
		// test insert
		dtl1 = new RuleActionDetail();
		dtl1.setActionId(testActionId);
		dtl1.setServiceName(testServiceName);
		dtl1.setRuleDataType(typ1);
		dtl1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		detailService.insert(dtl1);
	}
	@After
	public void teardown() {
		if (dtl1!=null) {
			detailService.delete(dtl1);
		}
	}
	@Test
	public void ruleActionService() {
		List<RuleLogic> lst1 = logicService.getAll(true);
		assertFalse(lst1.isEmpty());
		RuleLogic rlg1 = lst1.get(0);
		
		List<SenderData> lst2 = senderService.getAll();
		assertFalse(lst2.isEmpty());
		SenderData clt1 = lst2.get(0);
		
		List<RuleActionDetail> lst3 = detailService.getAll();
		if (!lst3.isEmpty()) {
			dtl1 = lst3.get(0);
		}
		
		List<RuleAction> actions = service.getByBestMatch(RuleNameEnum.GENERIC.getValue(), null, null);
		assertTrue(3==actions.size() || 4==actions.size());
		
		List<RuleAction> actlist = service.getByRuleName(RuleNameEnum.HARD_BOUNCE.getValue());
		assertTrue(actlist.size()==4);
		
		boolean hasActions = service.getHasActions(RuleNameEnum.HARD_BOUNCE.getValue());
		assertTrue(hasActions);
		
		if (JpaUtil.isHibernate()) { // XXX NOT WORKING with EclipseLink
			List<RuleAction> actlistv0 = service.getByRuleName_v0(RuleNameEnum.HARD_BOUNCE.getValue());
			assertTrue(actlistv0.size()>=4);
		}
		
		// test insert
		RuleAction var1 = new RuleAction();
		RuleActionPK pk1 = new RuleActionPK();
		pk1.setActionSequence(0);
		pk1.setSenderData(clt1);
		pk1.setRuleLogic(rlg1);
		pk1.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
		var1.setRuleActionPK(pk1);
		var1.setRuleActionDetail(dtl1);
		var1.setFieldValues(testFieldValues);
		var1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(var1);
		
		List<RuleAction> lst4 = service.getByRuleName(rlg1.getRuleName());
		assertFalse(lst4.isEmpty());
		RuleAction var2 = service.getByRowId(lst4.get(0).getRowId());
		RuleActionPK pk2 = var2.getRuleActionPK();
		RuleAction var3 = service.getByPrimaryKey(pk2);
		assertNotNull(var3);
		logger.info("RuleAction: " + PrintUtil.prettyPrint(var3,1));
		
		RuleActionPK pk22 = new RuleActionPK();
		pk22.setRuleLogic(rlg1);
		pk22.setActionSequence(999);
		RuleAction notExist = service.getByPrimaryKey(pk22);
		assertNull(notExist);

		List<RuleAction> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		
		List<RuleAction> list2 = service.getByBestMatch(rlg1.getRuleName(), null, clt1.getSenderId());
		assertFalse(list2.isEmpty());
		RuleAction var4 = service.getMostCurrent(rlg1.getRuleName(), 0, clt1.getSenderId());
		logger.info("RuleAction 4: " + PrintUtil.prettyPrint(var4,2));
		
		assertNull(service.getMostCurrent(rlg1.getRuleName(), 999, clt1.getSenderId()));
		
		service.delete(var3);
		assertNull(service.getByRowId(var3.getRowId()));
		// test delete
		RuleAction var5 = createNewInstance(var2);
		if (lst1.size()>1) {
			rlg1 = lst1.get(lst1.size()-1);
		}
		RuleActionPK pk5 = var5.getRuleActionPK();
		pk5.setRuleLogic(rlg1);
		pk5.setActionSequence(2);
		service.insert(var5);
		var5 = service.getByPrimaryKey(pk5);
		int random = 2; //new java.util.Random().nextInt(3);
		if (random==0) {
			assertTrue(1==service.deleteByRowId(var5.getRowId()));
		}
		else if (random==1) {
			assertTrue(1<=service.deleteByRuleName(pk5.getRuleLogic().getRuleName()));
		}
		else {
			assertTrue(1==service.deleteByPrimaryKey(pk5));
			assertTrue(0==service.deleteByPrimaryKey(pk5));
		}
	}
	
	private RuleAction createNewInstance(RuleAction orig) {
		RuleAction dest = new RuleAction();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
