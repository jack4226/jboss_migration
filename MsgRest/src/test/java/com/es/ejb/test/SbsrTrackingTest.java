package com.es.ejb.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.subscription.SubscriptionLocal;
import com.es.ejb.tracking.SbsrTrackingLocal;

public class SbsrTrackingTest {
	protected final static Logger logger = LogManager.getLogger(SbsrTrackingTest.class);
	
	private static EJBContainer ejbContainer;
	
	private static SbsrTrackingLocal sbsrTracking;
	private static SubscriptionLocal subscript;
	
	@BeforeClass
	public static void startTheContainer() throws NamingException {
		Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
		ejbContainer = EJBContainer.createEJBContainer(properties);
		
		lookupABean();
	}

	private static void lookupABean() throws NamingException {
		Object object1 = ejbContainer.getContext().lookup("java:global/MsgRest/SbsrTracking");
		assertNotNull(object1);
		assert(object1 instanceof SbsrTrackingLocal);
		sbsrTracking = (SbsrTrackingLocal) object1;
		
		Object object2 = ejbContainer.getContext().lookup("java:global/MsgRest/Subscription");
		assertNotNull(object2);
		assert(object2 instanceof SubscriptionLocal);
		subscript = (SubscriptionLocal) object2;
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testSbsrTracking() {
		try {
			int rows = sbsrTracking.updateClickCount(-1);
			assert(rows==0);
		}
		catch (EJBException e) {
			logger.error("EJBException caught: " + e.getMessage());
			fail();
		}
		
		assertTrue(sbsrTracking.getByMailingListId("").isEmpty());
		assertTrue(sbsrTracking.getByBroadcastMessageRowId(-1).isEmpty());
		
		List<BroadcastMessage> bcstMsgs = sbsrTracking.getByMailingListId("SMPLLST1");
		assertFalse(bcstMsgs.isEmpty());
		
		int idx1 = new Random().nextInt(bcstMsgs.size());
		BroadcastMessage bm = bcstMsgs.get(idx1);
		
		List<BroadcastTracking> bcstTrks = sbsrTracking.getByBroadcastMessageRowId(bm.getRowId());
		assertFalse(bcstTrks.isEmpty());
		
		int idx2 = new Random().nextInt(bcstTrks.size());
		BroadcastTracking bt = bcstTrks.get(idx2);
		
		assert(1<=sbsrTracking.updateClickCount(bt.getRowId()));
		assert(1<=sbsrTracking.updateOpenCount(bt.getRowId()));
		assert(1<=sbsrTracking.updateSentCount(bt.getRowId()));
		
		jpa.model.Subscription sub = subscript.getByUniqueKey(bt.getEmailAddress().getRowId(), bt.getBroadcastMessage().getMailingList().getListId());
		if (sub != null) {
			assert(1<=sbsrTracking.updateClickCount(bt.getEmailAddress().getRowId(), bt.getBroadcastMessage().getMailingList().getListId()));
			assert(1<=sbsrTracking.updateOpenCount(bt.getEmailAddress().getRowId(), bt.getBroadcastMessage().getMailingList().getListId()));
		}
	}
	
}
