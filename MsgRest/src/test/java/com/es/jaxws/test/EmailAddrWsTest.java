package com.es.jaxws.test;

import static org.junit.Assert.assertNotNull;
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

import com.es.ejb.emailaddr.EmailAddrWs;
import com.es.ejb.ws.vo.EmailAddrVo;

import jpa.util.PrintUtil;

public class EmailAddrWsTest {
	protected final static Logger logger = LogManager.getLogger(EmailAddrWsTest.class);

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
	public void testEmailAddrWs() {
		try {
			Service service = Service.create(new URL("http://127.0.0.1:4204/MsgRest/EmailAddr?wsdl"),
				new QName("http://com.es.ws.emailaddr/wsdl", "EmailAddrService"));
			assertNotNull(service);
			EmailAddrWs addr = service.getPort(EmailAddrWs.class);
			EmailAddrVo vo = addr.getOrAddAddress("test@test.com");
			assertNotNull(vo);
			logger.info(PrintUtil.prettyPrint(vo));
			vo = addr.getOrAddAddress("emailaddr@soapws.test");
			assertNotNull(vo);
			int rows = addr.delete(vo.getAddress());
			assert(rows > 0);
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
