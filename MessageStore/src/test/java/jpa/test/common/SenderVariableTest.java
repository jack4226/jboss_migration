package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.model.SenderVariable;
import jpa.model.SenderVariablePK;
import jpa.service.common.SenderVariableService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class SenderVariableTest extends BoTestBase {
	static Logger logger = Logger.getLogger(SenderVariableTest.class);
	
	final String testVariableName = "CurrentDate";
	final String testSenderId = Constants.DEFAULT_SENDER_ID;
	
	@BeforeClass
	public static void SenderVariablePrepare() {
	}

	@Autowired
	SenderVariableService service;

	@Test
	public void senderVariableService() {
		SenderData cd0 = new SenderData();
		cd0.setSenderId(testSenderId);
		SenderVariablePK pk0 = new SenderVariablePK(cd0, testVariableName, new Timestamp(System.currentTimeMillis()));
		SenderVariable var1 = service.getByBestMatch(pk0);
		assertNotNull(var1);
		System.out.println("SenderVariable: " + PrintUtil.prettyPrint(var1));

		SenderVariablePK pk1 = var1.getSenderVariablePK();
		SenderVariable var2 = service.getByPrimaryKey(pk1);
		assertTrue(var1.equals(var2));

		List<SenderVariable> list1 = service.getByVariableName(pk1.getVariableName());
		assertFalse(list1.isEmpty());
		
		List<SenderVariable> list2 = service.getCurrentBySenderId(testSenderId);
		assertFalse(list2.isEmpty());

		// test insert
		Timestamp newTms = new Timestamp(System.currentTimeMillis()+1);
		SenderVariable var3 = createNewInstance(var2);
		SenderVariablePK pk2 = var2.getSenderVariablePK();
		SenderVariablePK pk3 = new SenderVariablePK(pk2.getSenderData(), pk2.getVariableName(), newTms);
		var3.setSenderVariablePK(pk3);
		service.insert(var3);
		assertNotNull(service.getByPrimaryKey(pk3));
		// end of test insert
		
		service.delete(var3);
		assertNull(service.getByPrimaryKey(pk3));

		// test deleteByVariableName
		SenderVariable var4 = createNewInstance(var2);
		SenderVariablePK pk4 = new SenderVariablePK(pk2.getSenderData(), pk2.getVariableName()+"_v4", pk2.getStartTime());
		var4.setSenderVariablePK(pk4);
		service.insert(var4);
		assertTrue(1==service.deleteByVariableName(pk4.getVariableName()));
		assertNull(service.getByPrimaryKey(pk4));

		// test deleteByPrimaryKey
		SenderVariable var5 = createNewInstance(var2);
		SenderVariablePK pk5 = new SenderVariablePK(pk2.getSenderData(), pk2.getVariableName()+"_v5", pk2.getStartTime());
		var5.setSenderVariablePK(pk5);
		service.insert(var5);
		assertTrue(1==service.deleteByPrimaryKey(pk5));
		assertNull(service.getByPrimaryKey(pk5));

		// test getCurrentBySenderId
		List<SenderVariable> list3 = service.getCurrentBySenderId(pk2.getSenderData().getSenderId());
		for (SenderVariable rec : list3) {
			logger.info(PrintUtil.prettyPrint(rec));
		}

		// test update
		SenderVariable var6 = createNewInstance(var2);
		SenderVariablePK pk6 = new SenderVariablePK(pk2.getSenderData(), pk2.getVariableName()+"_v6", pk2.getStartTime());
		var6.setSenderVariablePK(pk6);
		service.insert(var6);
		assertNotNull(service.getByPrimaryKey(pk6));
		var6.setVariableValue("new test value");
		service.update(var6);
		SenderVariable var_updt = service.getByRowId(var6.getRowId());
		assertTrue("new test value".equals(var_updt.getVariableValue()));
		// end of test update
		
		service.delete(var6);
		assertNull(service.getByRowId(var6.getRowId()));
		
		SenderVariable var7 = createNewInstance(var2);
		SenderVariablePK pk7 = new SenderVariablePK(pk2.getSenderData(), pk2.getVariableName()+"_v7", pk2.getStartTime());
		var7.setSenderVariablePK(pk7);
		service.insert(var7);
		assertNotNull(service.getByPrimaryKey(pk7));
		assertTrue(0==service.deleteBySenderId("NotExist"));
	}

	private SenderVariable createNewInstance(SenderVariable orig) {
		SenderVariable dest = new SenderVariable();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
