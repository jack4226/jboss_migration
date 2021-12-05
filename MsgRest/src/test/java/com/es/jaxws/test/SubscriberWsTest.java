package com.es.jaxws.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.subscriber.SubscriberWs;
import com.es.ejb.ws.vo.SubscriptionVo;

public class SubscriberWsTest {
	protected final static Logger logger = LogManager.getLogger(SubscriberWsTest.class);
	
	private static EJBContainer ejbContainer;
	
	@BeforeClass
	public static void startTheContainer() {
		Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        properties.setProperty("httpejbd.print", "true");
        properties.setProperty("httpejbd.indent.xml", "true");
		ejbContainer = EJBContainer.createEJBContainer(properties);
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testSubscriberWs() {
		try {
			Service service = Service.create(new URL("http://127.0.0.1:4204/MsgRest/Subscriber?wsdl"),
				new QName("http://com.es.ws.subscriber/wsdl", "SubscriberService"));
			assertNotNull(service);
			SubscriberWs sbsr = service.getPort(SubscriberWs.class);
			
			SubscriptionVo sub = sbsr.addEmailToList("test@test.com", "SMPLLST1");
			assertNotNull(sub);
			assertTrue(sub.isSubscribed());
			
			sub = sbsr.removeEmailFromList("test@test.com", "SMPLLST1");
			assertNotNull(sub);
			assertFalse(sub.isSubscribed());
			
			assertNotNull(sbsr.getSubscriberData("jsmith@test.com"));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
		
		try {
			TimeUnit.SECONDS.sleep(2);
		}
		catch (InterruptedException e) {
			logger.error("InterruptedException caught: " + e.getMessage());
		}
	}
}
