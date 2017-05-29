package com.es.ejb.test;

import static org.junit.Assert.*;

import java.util.Properties;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import jpa.model.EmailAddress;

import org.apache.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.emailaddr.EmailAddrRemote;

public class EmailAddrTest {
	protected final static Logger logger = Logger.getLogger(EmailAddrTest.class);

	private static EJBContainer ejbContainer;
	
	@BeforeClass
	public static void startTheContainer() {
		Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
		ejbContainer = EJBContainer.createEJBContainer(properties);
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testEmailAddrRemote() {
		try {
			EmailAddrRemote rmt = (EmailAddrRemote) ejbContainer.getContext().lookup(
					"java:global/MsgRest/EmailAddr!com.es.ejb.emailaddr.EmailAddrRemote");
			
			EmailAddress addr = rmt.findSertAddress("emailaddr@remote.test");
			assertNotNull(addr);
			int rows = rmt.delete(addr.getAddress());
			assert(rows>0);
		}
		catch (NamingException e) {
			logger.error("NamingException caught: " + e.getMessage());
			fail();
		}
	}
}
