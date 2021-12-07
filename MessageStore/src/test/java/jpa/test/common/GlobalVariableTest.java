package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.model.GlobalVariable;
import jpa.model.GlobalVariablePK;
import jpa.service.common.GlobalVariableService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class GlobalVariableTest extends BoTestBase {
	static Logger logger = LogManager.getLogger(GlobalVariableTest.class);
	
	final String testVariableName = "CurrentDate";
	
	@BeforeClass
	public static void GlobalVariablePrepare() {
	}

	@Autowired
	GlobalVariableService service;

	@Test
	public void globalVariableService1() {
		GlobalVariablePK pk0 = new GlobalVariablePK(testVariableName,new java.sql.Timestamp(System.currentTimeMillis()));
		GlobalVariable var1 = service.getByBestMatch(pk0);
		assertNotNull(var1);
		logger.info("GlobalVariable: " + PrintUtil.prettyPrint(var1));

		GlobalVariablePK pk1 = var1.getGlobalVariablePK();
		GlobalVariable var2 = service.getByPrimaryKey(pk1);
		assertTrue(var1.equals(var2));
		
		GlobalVariablePK pk11 = new GlobalVariablePK();
		pk11.setVariableName(pk1.getVariableName());
		pk11.setStartTime(null);
		assertNull(service.getByPrimaryKey(pk11));

		List<GlobalVariable> list1 = service.getCurrent();
		assertFalse(list1.isEmpty());
		
		List<GlobalVariable> list2 = service.getByVariableName(pk1.getVariableName());
		assertFalse(list2.isEmpty());
		
		// test insert
		Timestamp newTms = new Timestamp(System.currentTimeMillis());
		GlobalVariable var3 = createNewInstance(var2);
		GlobalVariablePK pk2 = var2.getGlobalVariablePK();
		GlobalVariablePK pk3 = new GlobalVariablePK(pk2.getVariableName(), newTms);
		var3.setGlobalVariablePK(pk3);
		service.insert(var3);
		assertNotNull(service.getByPrimaryKey(pk3));
		// end of test insert
		
		service.delete(var3);
		assertNull(service.getByPrimaryKey(pk3));
		
		// test getByStatusid
		List<GlobalVariable> list3 = service.getByStatusId(var3.getStatusId());
		assertFalse(list3.isEmpty());
		for (GlobalVariable rec : list3) {
			logger.info(PrintUtil.prettyPrint(rec));
		}
		
		// test deleteByVariableName
		GlobalVariable var4 = createNewInstance(var2);
		GlobalVariablePK pk4 = new GlobalVariablePK(pk2.getVariableName()+"_v4",pk2.getStartTime());
		var4.setGlobalVariablePK(pk4);
		service.insert(var4);
		assertTrue(1==service.deleteByVariableName(pk4.getVariableName()));
		assertNull(service.getByPrimaryKey(pk4));

		// test deleteByPrimaryKey
		GlobalVariable var5 = createNewInstance(var2);
		GlobalVariablePK pk5 = new GlobalVariablePK(pk2.getVariableName()+"_v5",pk2.getStartTime());
		var5.setGlobalVariablePK(pk5);
		service.insert(var5);
		assertTrue(1==service.deleteByPrimaryKey(pk5));
		assertNull(service.getByPrimaryKey(pk5));

		// test update
		GlobalVariable var6 = createNewInstance(var2);
		GlobalVariablePK pk6 = new GlobalVariablePK(pk2.getVariableName()+"_v6",pk2.getStartTime());
		var6.setGlobalVariablePK(pk6);
		service.insert(var6);
		assertNotNull(service.getByPrimaryKey(pk6));
		var6.setVariableValue("new test value");
		service.update(var6);
		Optional<GlobalVariable> var_updt = service.getByRowId(var6.getRowId());
		assertTrue(var_updt.isPresent());
		assertTrue("new test value".equals(var_updt.get().getVariableValue()));
		// end of test update
		
		service.delete(var6);
		assertTrue(service.getByRowId(var6.getRowId()).isEmpty());
	}
	
	private GlobalVariable createNewInstance(GlobalVariable orig) {
		GlobalVariable dest = new GlobalVariable();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
