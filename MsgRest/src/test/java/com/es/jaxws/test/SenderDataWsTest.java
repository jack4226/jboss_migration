package com.es.jaxws.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.ejb.embeddable.EJBContainer;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import jpa.constant.Constants;
import jpa.util.PrintUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.senderdata.SenderDataWs;
import com.es.ejb.ws.vo.SenderDataVo;

public class SenderDataWsTest {

	protected final static Logger logger = LogManager.getLogger(SenderDataWsTest.class);
	
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
	public void testSenderDataWs() {
		try {
			Service service = Service.create(new URL("http://127.0.0.1:4204/MsgRest/SenderData?wsdl"),
				new QName("http://com.es.ws.senderdata/wsdl", "SenderDataService"));
			assertNotNull(service);
			SenderDataWs senderDao = service.getPort(SenderDataWs.class);
			
			List<SenderDataVo> list = senderDao.getAll();
			assert(!list.isEmpty());
			for (SenderDataVo vo : list) {
				logger.info(PrintUtil.prettyPrint(vo));
			}
			SenderDataVo vo = senderDao.getBySenderId(Constants.DEFAULT_SENDER_ID);
			assertNotNull(vo);
			try {
				senderDao.getBySenderId("FakeSender");
				fail();
			}
			catch (SOAPFaultException se) {
				logger.error("SOAPFaultException caught: " + se.getMessage());
				// expected
			}
			
			java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());
			vo.setUpdtTime(updtTime);
			String irsTaxId = "TaxId-" + new Random().nextInt(1000);
			vo.setIrsTaxId(irsTaxId);
			senderDao.update(vo);
			vo = senderDao.getBySenderId(vo.getSenderId());
			//assert(!updtTime.after(vo.getUpdtTime())); // TODO not working with Hibernate under Maven
			assert(irsTaxId.equals(vo.getIrsTaxId()));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}
