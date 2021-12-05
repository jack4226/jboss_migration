package jpa.test.rule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
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
import jpa.service.rule.RuleDataTypeService;
import jpa.service.rule.RuleDataValueService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class RuleDataTypeTest extends BoTestBase {
	static Logger logger = Logger.getLogger(RuleDataTypeTest.class);
	
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
			typeService.deleteByDataType(testDataType1);
		}
		if (typ2!=null) {
			typeService.deleteByRowId(typ2.getRowId());
		}
	}

	@Test
	public void ruleDataTypeService() {
		RuleDataType var2 = typeService.getByDataType(typ1.getDataType());
		assertNotNull(var2);
		logger.info("RuleDataType: " + PrintUtil.prettyPrint(var2));

		List<RuleDataType> list1 = typeService.getAll();
		assertFalse(list1.isEmpty());
		
		List<String> listweb = typeService.getDataTypeList();
		assertTrue(listweb.size()>0);
		
		// test insert
		RuleDataType var3 = createNewInstance(list1.get(0));
		var3.setDataType(var3.getDataType()+"_v2");
		typeService.insert(var3);
		assertNotNull(typeService.getByDataType(var3.getDataType()));
		// end of test insert
		// test update
		var3.setUpdtUserId("jpa test");
		typeService.update(var3);
		RuleDataType var5 = typeService.getByDataType(var3.getDataType());
		assertTrue("jpa test".equals(var5.getUpdtUserId()));
		
		typeService.delete(var3);
		assertTrue(typeService.getByRowId(var5.getRowId()).isPresent());
		// test delete
		RuleDataType var4 = createNewInstance(var2);
		var4.setDataType(var2.getDataType() + "_v4");
		typeService.insert(var4);
		assertTrue(1==typeService.deleteByDataType(var4.getDataType()));
	}

	private RuleDataType createNewInstance(RuleDataType orig) {
		RuleDataType dest = new RuleDataType();
		try {
			BeanUtils.copyProperties(dest, orig);
			/*
			 * XXX Added next line of code to resolves: 
			 * HibernateException: Found shared references to a collection: jpa.model.rule.RuleDataType.ruleDataValues
			 */
			dest.setRuleDataValues(new ArrayList<RuleDataValue>());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}

}
