package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.constant.EmailVariableType;
import jpa.data.preload.EmailVariableEnum;
import jpa.model.EmailVariable;
import jpa.model.SubscriberData;
import jpa.service.common.EmailVariableService;
import jpa.service.common.SubscriberDataService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class EmailVariableTest extends BoTestBase {
	static Logger logger = LogManager.getLogger(EmailVariableTest.class);
	
	final String testVariableName = "jpa test variable name";
	
	@BeforeClass
	public static void EmailVariablePrepare() {
	}

	@Autowired
	EmailVariableService service;
	
	@Autowired
	SubscriberDataService sbsrDataService;

	@Test
	public void globalVariableService1() {
		// test insert
		EmailVariable var1 = new EmailVariable();
		var1.setVariableName(testVariableName);
		var1.setVariableType(EmailVariableType.Custom.getValue());
		var1.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(var1);
		
		EmailVariable var2 = service.getByVariableName(testVariableName);
		assertNotNull(var2);
		logger.info("EmailVariable: " + PrintUtil.prettyPrint(var2));

		List<EmailVariable> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		
		List<EmailVariable> list2 = service.getAllCustomVariables();
		assertFalse(list2.isEmpty());
		
		List<SubscriberData> sbsrs = sbsrDataService.getAll();
		assertFalse(sbsrs.isEmpty());		
		for (EmailVariableEnum evEnum : EmailVariableEnum.values()) {
			if ("Subscriber_Data".equals(evEnum.getTableName()) && StringUtils.isNotBlank(evEnum.getColumnName())) {
				String queryStr = evEnum.getVariableQuery();
				assertNotNull(service.getByQuery(queryStr, sbsrs.get(0).getEmailAddress().getRowId()));
				assertNull(service.getByQuery(queryStr, 9999999));
			}
		}
		
		// test insert
		EmailVariable var3 = createNewInstance(list2.get(0));
		var3.setVariableName(var3.getVariableName()+"_v2");
		service.insert(var3);
		assertNotNull(service.getByVariableName(var3.getVariableName()));
		// end of test insert
		
		service.delete(var3);
		assertNull(service.getByVariableName(var3.getVariableName()));
		
		// test deleteByVariableName
		EmailVariable var4 = createNewInstance(var2);
		var4.setVariableName(var2.getVariableName() + "_v4");
		service.insert(var4);
		assertTrue(1==service.deleteByVariableName(var4.getVariableName()));
		assertNull(service.getByVariableName(var4.getVariableName()));

		// test deleteByPrimaryKey
		EmailVariable var5 = createNewInstance(var2);
		var5.setVariableName(var2.getVariableName() + "_v5");
		service.insert(var5);
		var5 = service.getByVariableName(var5.getVariableName());
		service.delete(var5);
		assertNull(service.getByVariableName(var5.getVariableName()));
		
		// test update
		EmailVariable var6 = createNewInstance(var2);
		var6.setVariableName(var2.getVariableName() + "_v6");
		service.insert(var6);
		assertNotNull(service.getByVariableName(var6.getVariableName()));
		var6.setDefaultValue("new test value");
		service.update(var6);
		EmailVariable var_updt = service.getByVariableName(var6.getVariableName());
		assertTrue("new test value".equals(var_updt.getDefaultValue()));
		// end of test update
		
		service.delete(var6);
		assertTrue(service.getByRowId(var6.getRowId()).isEmpty());
	}
	
	private EmailVariable createNewInstance(EmailVariable orig) {
		EmailVariable dest = new EmailVariable();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
