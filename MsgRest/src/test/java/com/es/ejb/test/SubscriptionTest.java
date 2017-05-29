package com.es.ejb.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Properties;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.subscription.SubscriptionLocal;

public class SubscriptionTest {
	protected final static Logger logger = Logger.getLogger(SubscriptionTest.class);
	
	private static EJBContainer ejbContainer;
	
	private static SubscriptionLocal subscript;
	
	@BeforeClass
	public static void startTheContainer() throws NamingException {
		Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
		ejbContainer = EJBContainer.createEJBContainer(properties);
		
		lookupABean();
	}

	private static void lookupABean() throws NamingException {
		Object object = ejbContainer.getContext().lookup("java:global/MsgRest/Subscription");
		assertNotNull(object);
		assert(object instanceof SubscriptionLocal);
		subscript = (SubscriptionLocal) object;
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testSubscription() {
		assertTrue(subscript.getByAddress("").isEmpty());
		assertTrue(subscript.getByListId("").isEmpty());
		
		List<jpa.model.Subscription> subs = subscript.getByListId("SMPLLST1");
		assertFalse(subs.isEmpty());
		
		jpa.model.Subscription sub1 = subs.get(0);
		
		jpa.model.Subscription sub2 = subscript.getByRowId(sub1.getRowId());
		assertNotNull(sub2);
		
		assertSubscriptionsSame(sub1, sub2);
		
		jpa.model.Subscription sub3 = subscript.getByUniqueKey(sub2.getEmailAddress().getRowId(), sub2.getMailingList().getListId());
		
		assertSubscriptionsSame(sub1, sub3);
		
		jpa.model.Subscription sub4 = subscript.getByAddressAndListId(sub2.getEmailAddress().getAddress(), sub2.getMailingList().getListId());
		
		assertSubscriptionsSame(sub1, sub4);
		
		assert(1<=subscript.updateClickCount(sub2.getEmailAddress().getRowId(), sub2.getMailingList().getListId()));
		assert(1<=subscript.updateOpenCount(sub2.getEmailAddress().getRowId(), sub2.getMailingList().getListId()));
	}
	
	private void assertSubscriptionsSame(jpa.model.Subscription sub1, jpa.model.Subscription sub2) {
		assertEquals(sub1.getRowId(), sub2.getRowId());
		assertEquals(sub1.getEmailAddrShort(), sub2.getEmailAddrShort());
		assertEquals(sub1.getAcceptHtmlDesc(), sub2.getAcceptHtmlDesc());
		assertEquals(sub1.getSubscriberName(), sub2.getSubscriberName());
		assertEquals(sub1.getOpenCount(), sub2.getOpenCount());
		assertEquals(sub1.getClickCount(), sub2.getClickCount());
		assertEquals(sub1.getSentCount(), sub2.getSentCount());
	}
	
}
