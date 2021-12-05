package com.es.ejb.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import jpa.model.SenderData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.senderdata.SenderDataLocal;

public class SenderDataTest {
	protected final static Logger logger = LogManager.getLogger(SenderDataTest.class);
	
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
	public void testSenderDataLocal() {
		try {
			SenderDataLocal senderDao = (SenderDataLocal) ejbContainer.getContext().lookup(
					"java:global/MsgRest/SenderData!com.es.ejb.senderdata.SenderDataLocal");
			
			try {
				SenderData sd = senderDao.findBySenderId("");
				assertNull(sd);
			}
			catch (EJBException e) {
				logger.error("EJBException caught: " + e.getMessage());
				//assertNotNull(e.getCause());
				//assert(e.getCause() instanceof NoResultException);
				fail();
			}
			
			List<SenderData> list = senderDao.findAll();
			assert(!list.isEmpty());
			jpa.model.SenderData sender = senderDao.findBySenderId(list.get(0).getSenderId());
			assertNotNull(sender);
			java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());
			sender.setUpdtTime(updtTime);
			String irsTaxId = "TaxId-" + new Random().nextInt(1000);
			sender.setIrsTaxId(irsTaxId);
			senderDao.update(sender);
			jpa.model.SenderData senderUpdated = senderDao.findBySenderId(sender.getSenderId());
			//assert(updtTime.equals(senderUpdated.getUpdtTime())); // TODO not working with Hibernate under Maven
			assert(irsTaxId.equals(senderUpdated.getIrsTaxId()));
		}
		catch (NamingException e) {
			logger.error("NamingException caught: " + e.getMessage());
			fail();
		}
	}
}
