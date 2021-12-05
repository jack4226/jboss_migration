package jpa.test.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.StatusId;
import jpa.model.rule.RuleDataType;
import jpa.model.rule.RuleDataValue;
import jpa.model.rule.RuleDataValuePK;
import jpa.service.rule.RuleDataTypeService;
import jpa.service.rule.RuleDataValueService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class RuleDataValueTest extends BoTestBase {
	static Logger logger = LogManager.getLogger(RuleDataValueTest.class);
	
	final String testDataType1 = "CarbonCopyAddress1";
	final String testDataType2 = "CarbonCopyAddress2";
	final String testDataValue = EmailAddrType.TO_ADDR.getValue();
	
	@BeforeClass
	public static void ActionPropertyPrepare() {
	}

	@Autowired
	RuleDataTypeService typeService;
	@Autowired
	RuleDataValueService valueService;

	RuleDataType typ1= null;
	RuleDataType typ2= null;
	
	@BeforeTransaction
	public void prepare() {
		// test insert
		typ1 = typeService.getByDataType(testDataType1);
		if (typ1 == null) {
			typ1 = new RuleDataType();
			typ1.setDataType(testDataType1);
			typ1.setStatusId(StatusId.ACTIVE.getValue());
			typ1.setUpdtUserId(Constants.DEFAULT_USER_ID);
			typeService.insert(typ1);
		}

		typ2 = typeService.getByDataType(testDataType2);
		if (typ2 == null) {
			typ2 = new RuleDataType();
			typ2.setDataType(testDataType2);
			typ2.setStatusId(StatusId.ACTIVE.getValue());
			typ2.setUpdtUserId(Constants.DEFAULT_USER_ID);
			typeService.insert(typ2);
		}
	}
	
	@AfterTransaction
	public void tearDown() {
		if (typ1!=null) {
			typeService.deleteByDataType(typ1.getDataType());
		}
		if (typ2!=null) {
			typeService.deleteByRowId(typ2.getRowId());
		}
	}

	@Test
	public void ruleDataValueService() {
		// test insert
		RuleDataValue var1 = new RuleDataValue();
		RuleDataValuePK pk1 = new RuleDataValuePK(typ1,testDataValue);
		var1.setRuleDataValuePK(pk1);
		var1.setStatusId(StatusId.ACTIVE.getValue());
		var1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		valueService.insert(var1);
		
		RuleDataValue var2 = valueService.getByPrimaryKey(pk1);
		assertNotNull(var2);
		logger.info("RuleDataValue: " + PrintUtil.prettyPrint(var2));

		List<RuleDataValue> list1 = valueService.getAll();
		assertFalse(list1.isEmpty());
		
		List<RuleDataValue> list2 = valueService.getByDataType(testDataType1);
		assertFalse(list2.isEmpty());
		
		assertTrue(valueService.getByRowId(list2.get(0).getRowId()).isPresent());
		
		// test insert
		RuleDataValue var3 = createNewInstance(list2.get(0));
		RuleDataValuePK pk2 = list2.get(0).getRuleDataValuePK();
		RuleDataValuePK pk3 = new RuleDataValuePK(pk2.getRuleDataType(),pk2.getDataValue()+"_v3");
		var3.setRuleDataValuePK(pk3);
		var3.setStatusId(StatusId.ACTIVE.getValue());
		valueService.insert(var3);
		assertNotNull(valueService.getByPrimaryKey(pk3));
		// end of test insert

		// test update
		var3.setUpdtUserId("jpa test");
		valueService.update(var3);
		RuleDataValue var4 = valueService.getByPrimaryKey(pk3);
		assertTrue("jpa test".equals(var4.getUpdtUserId()));
		
		valueService.delete(var3);
		RuleDataValue deleted = valueService.getByPrimaryKey(pk3);
		if (deleted != null) {
			fail();
		}
		assertTrue(valueService.getByRowId(var3.getRowId()).isEmpty());
		
		// test delete
		RuleDataValue var5 = createNewInstance(var2);
		RuleDataValuePK pk5 = new RuleDataValuePK(pk2.getRuleDataType(),pk2.getDataValue()+"_v5");
		var5.setRuleDataValuePK(pk5);
		var5.setStatusId(StatusId.ACTIVE.getValue());
		valueService.insert(var5);
		var5 = valueService.getByPrimaryKey(pk5);
		assertTrue(1==valueService.deleteByRowId(var5.getRowId()));
		valueService.deleteByPrimaryKey(pk5);
		assertTrue(1==valueService.deleteByDataType(pk5.getRuleDataType().getDataType()));
	}
	
	private RuleDataValue createNewInstance(RuleDataValue orig) {
		RuleDataValue dest = new RuleDataValue();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
