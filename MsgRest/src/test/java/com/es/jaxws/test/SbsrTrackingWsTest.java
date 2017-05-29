package com.es.jaxws.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.tracking.SbsrTrackingWs;

public class SbsrTrackingWsTest {
	protected final static Logger logger = Logger.getLogger(SbsrTrackingWsTest.class);
	
	private static EJBContainer ejbContainer;
	
	@BeforeClass
	public static void startTheContainer() {
		Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        //properties.setProperty("httpejbd.print", "true");
        //properties.setProperty("httpejbd.indent.xml", "true");
		ejbContainer = EJBContainer.createEJBContainer(properties);
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testSbsrTrackingWs() {
		try {
			Service service = Service.create(new URL("http://127.0.0.1:4204/MsgRest/SbsrTracking?wsdl"),
				new QName("http://com.es.ws.sbsrtracking/wsdl", "SbsrTrackingService"));
			assertNotNull(service);
			SbsrTrackingWs sbsrtrk = service.getPort(SbsrTrackingWs.class);
			
			assert(0==sbsrtrk.updateClickCount(-1));
			assert(0==sbsrtrk.updateOpenCount(-1));
			assert(0==sbsrtrk.updateMsgClickCount(0, ""));
			assert(0==sbsrtrk.updateMsgOpenCount(0, ""));
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
