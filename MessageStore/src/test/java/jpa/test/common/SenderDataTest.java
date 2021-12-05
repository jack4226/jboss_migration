package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.model.SenderVariable;
import jpa.model.SubscriberData;
import jpa.model.UserData;
import jpa.model.rule.RuleAction;
import jpa.service.common.SenderDataService;
import jpa.spring.util.BoTestBase;

//@org.springframework.test.annotation.Commit
public class SenderDataTest extends BoTestBase {

	@BeforeClass
	public static void SenderDataPrepare() {
	}

	@Autowired
	SenderDataService service;

	@Test
	public void senderDataService() {
		List<SenderData> list = service.getAll();
		assertFalse(list.isEmpty());
		
		SenderData tkn0 = service.getBySenderId(Constants.DEFAULT_SENDER_ID);
		assertNotNull(tkn0);
		
		assertTrue(tkn0.getSystemId().equals(service.getSystemId()));
		assertTrue(tkn0.getSystemKey().equals(service.getSystemKey()));
		assertNotNull(service.getByDomainName(tkn0.getDomainName()));

		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		Optional<SenderData> tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue(tkn1.isPresent());
		assertTrue("JpaTest".equals(tkn1.get().getUpdtUserId()));
		// end of test update
		
		// test insert
		SenderData tkn2 = new SenderData();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setSenderId(Constants.DEFAULT_SENDER_ID + "_2");
		tkn2.setSenderVariables(new ArrayList<SenderVariable>());
		tkn2.setSubscribers(new ArrayList<SubscriberData>());
		tkn2.setUserDatas(new ArrayList<UserData>());
		tkn2.setRuleActions(new ArrayList<RuleAction>());
		service.insert(tkn2);
		
		SenderData tkn3 = service.getBySenderId(tkn2.getSenderId());
		assertTrue(tkn3.getRowId()!=tkn1.get().getRowId());
		// end of test insert
		
		// test select with No Result
		service.delete(tkn3);
		assertNull(service.getBySenderId(tkn2.getSenderId()));

		
		assertTrue(0==service.deleteBySenderId(tkn3.getSenderId()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
}
