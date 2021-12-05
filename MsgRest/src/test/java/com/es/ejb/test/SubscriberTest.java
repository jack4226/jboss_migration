package com.es.ejb.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import jpa.model.SubscriberData;
import jpa.util.ExceptionUtil;
import jpa.util.PrintUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.subscriber.Subscriber;
import com.es.ejb.subscriber.SubscriberLocal;
import com.es.ejb.subscriber.SubscriberRemote;

public class SubscriberTest {
	protected final static Logger logger = LogManager.getLogger(SubscriberTest.class);
	
	private static EJBContainer ejbContainer;
	
	private SubscriberLocal subscriber;
	 
	@BeforeClass
	public static void startTheContainer() {
		Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
		ejbContainer = EJBContainer.createEJBContainer(properties);
	}

	@Before
	public void lookupABean() throws NamingException {
		Object object = ejbContainer.getContext().lookup("java:global/MsgRest/Subscriber"); //"java:global/MsgRest/Subscriber!com.es.ejb.subscriber.Subscriber");

		assert(object instanceof Subscriber);

		subscriber = (SubscriberLocal) object;
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testSubscriber() {
		try {
			assertNull(subscriber.getSubscriberById(""));
		}
		catch (EJBException e) {
			logger.error("EJBException caught: " + e.getMessage());
			//assertNotNull(e.getCause());
			//assert(e.getCause() instanceof NoResultException);
			fail();
		}
		
		List<SubscriberData> subrList = subscriber.getAllSubscribers();
		assert(!subrList.isEmpty());
		
		SubscriberData subr1 = subscriber.getSubscriberById(subrList.get(0).getSubscriberId());
		assertNotNull(subr1);
		
		SubscriberData subr2 = subscriber.getSubscriberByEmailAddress(subrList.get(0).getEmailAddress().getAddress());
		assertNotNull(subr2);
		
		logger.info(PrintUtil.prettyPrint(subr2, 1));
	}
	
	@Test
	public void testSubscriberRemote() {
		try {
			SubscriberRemote rmt = (SubscriberRemote) ejbContainer.getContext().lookup(
					"java:global/MsgRest/Subscriber!com.es.ejb.subscriber.SubscriberRemote");
			List<SubscriberData> subrList = rmt.getAllSubscribers();
			assert(!subrList.isEmpty());
		}
		catch (NamingException e) {
			logger.error("NamingException caught: " + e.getMessage());
			fail();
		}
	}

	@Test
	public void testSubscriberLocal() {
		try {
			SubscriberLocal subr = (SubscriberLocal) ejbContainer.getContext().lookup(
					"java:global/MsgRest/Subscriber!com.es.ejb.subscriber.SubscriberLocal");
			List<SubscriberData> subrList = subr.getAllSubscribers();
			assert(!subrList.isEmpty());
			
			try {
				subr.subscribe("", "");
				fail();
			}
			catch (EJBException e) {
				logger.error("EJBException caught: " + e.getMessage());
				assert(ExceptionUtil.findRootCause(e) instanceof IllegalArgumentException);
			}
		}
		catch (NamingException e) {
			logger.error("NamingException caught: " + e.getMessage());
			fail();
		}
	}


}
