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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.model.SenderVariable;
import jpa.model.SubscriberData;
import jpa.model.UserData;
import jpa.model.msg.TemplateData;
import jpa.model.msg.TemplateDataPK;
import jpa.model.rule.RuleAction;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.TemplateDataService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

//@org.springframework.test.annotation.Commit
public class TemplateDataTest extends BoTestBase {
	static Logger logger = LogManager.getLogger(TemplateDataTest.class);
	
	final String testTemplateId = "jpa test template id";
	final String testSenderId = Constants.DEFAULT_SENDER_ID;
	
	@BeforeClass
	public static void TemplateDataPrepare() {
	}

	@Autowired
	TemplateDataService service;
	@Autowired
	SenderDataService senderService;

	@Test
	public void templateDataService() {
		SenderData sender = senderService.getBySenderId(testSenderId);
		
		SenderData cd0 = new SenderData();
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
		TemplateData rcd1 = new TemplateData();
		rcd1.setTemplateDataPK(pk0);
		rcd1.setContentType("text/plain");
		rcd1.setBodyTemplate("jpa test template value");
		rcd1.setSubjectTemplate("jpa test subject");
		service.insert(rcd1);
		
		TemplateData var1 = service.getByBestMatch(pk0);
		assertNotNull(var1);
		logger.info("TemplateData: " + PrintUtil.prettyPrint(var1,2));

		TemplateDataPK pk1 = var1.getTemplateDataPK();
		TemplateData var2 = service.getByPrimaryKey(pk1);
		assertTrue(var1.equals(var2));

		List<TemplateData> list1 = service.getByTemplateId(pk1.getTemplateId());
		assertFalse(list1.isEmpty());
		
		List<TemplateData> list2 = service.getCurrentBySenderId(testSenderId);
		assertFalse(list2.isEmpty());

		// test insert
		Timestamp newTms = new Timestamp(System.currentTimeMillis()+1000);
		TemplateData var3 = createNewInstance(var2);
		TemplateDataPK pk2 = var2.getTemplateDataPK();
		TemplateDataPK pk3 = new TemplateDataPK(pk2.getSenderData(), pk2.getTemplateId(), newTms);
		var3.setTemplateDataPK(pk3);
		service.insert(var3);
		assertNotNull(service.getByPrimaryKey(pk3));
		// end of test insert
		
		service.delete(var3);
		assertNull(service.getByPrimaryKey(pk3));


		// test deleteByTemplateId
		TemplateData var4 = createNewInstance(var2);
		TemplateDataPK pk4 = new TemplateDataPK(pk2.getSenderData(), pk2.getTemplateId()+"_v4", pk2.getStartTime());
		var4.setTemplateDataPK(pk4);
		service.insert(var4);
		assertTrue(1==service.deleteByTemplateId(pk4.getTemplateId()));
		assertNull(service.getByPrimaryKey(pk4));

		// test deleteByPrimaryKey
		TemplateData var5 = createNewInstance(var2);
		TemplateDataPK pk5 = new TemplateDataPK(pk2.getSenderData(), pk2.getTemplateId()+"_v5", pk2.getStartTime());
		var5.setTemplateDataPK(pk5);
		service.insert(var5);
		assertTrue(1==service.deleteByPrimaryKey(pk5));
		assertNull(service.getByPrimaryKey(pk5));

		// test deleteBySenderId
		TemplateData var6 = createNewInstance(var2);
		TemplateDataPK pk6 = new TemplateDataPK(pk2.getSenderData(), pk2.getTemplateId()+"_v5", pk2.getStartTime());
		var6.setTemplateDataPK(pk6);
		service.insert(var6);
		assertTrue(1<=service.deleteBySenderId(pk6.getSenderData().getSenderId()));
		assertNull(service.getByPrimaryKey(pk6));

		// test getCurrentBySenderId
		List<TemplateData> list3 = service.getCurrentBySenderId(pk2.getSenderData().getSenderId());
		for (TemplateData rec : list3) {
			logger.info(PrintUtil.prettyPrint(rec,2));
		}

		// test update
		TemplateData var9 = createNewInstance(var2);
		TemplateDataPK pk9 = new TemplateDataPK(pk2.getSenderData(), pk2.getTemplateId()+"_v6", pk2.getStartTime());
		var9.setTemplateDataPK(pk9);
		service.insert(var9);
		assertNotNull(service.getByPrimaryKey(pk9));
		var9.setBodyTemplate("new test value");
		service.update(var9);
		Optional<TemplateData> var_updt = service.getByRowId(var9.getRowId());
		assertTrue(var_updt.isPresent());
		assertTrue("new test value".equals(var_updt.get().getBodyTemplate()));
		// end of test update
		
		service.delete(var9);
		assertNull(service.getByRowId(var9.getRowId()));

	}

	private TemplateData createNewInstance(TemplateData orig) {
		TemplateData dest = new TemplateData();
		try {
			BeanUtils.copyProperties(dest, orig);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dest;
	}
}
