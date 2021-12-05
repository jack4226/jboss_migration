package com.es.jaxws.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import jpa.util.ExceptionUtil;
import jpa.util.PrintUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.idtokens.IdTokensWs;
import com.es.ejb.ws.vo.IdTokensVo;

public class IdTokensWsTest {

	protected final static Logger logger = LogManager.getLogger(IdTokensWsTest.class);
	
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
	public void testIdTokensWs() {
		try {
			Service service = Service.create(new URL("http://127.0.0.1:4204/MsgRest/IdTokens?wsdl"),
				new QName("http://com.es.ws.idtokens/wsdl", "IdTokensService"));
			assertNotNull(service);
			IdTokensWs idtkn = service.getPort(IdTokensWs.class);
			List<IdTokensVo> volist = idtkn.getAll();
			assert(!volist.isEmpty());
			for (IdTokensVo vo : volist) {
				logger.info(PrintUtil.prettyPrint(vo));
			}
			IdTokensVo vo = idtkn.getBySenderId(volist.get(volist.size()-1).getSenderId());
			assertNotNull(vo);
			
			java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());
			vo.setUpdtTime(updtTime);
			String desc = vo.getDescription();
			if (StringUtils.lastIndexOf(desc, "-") > 0) {
				desc = desc.substring(0, StringUtils.lastIndexOf(desc, "-"));
			} else {
				desc += "-V2";
			}
			vo.setDescription(desc);
			idtkn.update(vo);
			vo = idtkn.getBySenderId(vo.getSenderId());
			//assert(updtTime.equals(vo.getUpdtTime())); // TODO not working with Hibernate under Maven
			assert (desc.equals(vo.getDescription()));
			
			try {
				idtkn.getBySenderId("FakeSender");
				fail();
			}
			catch (SOAPFaultException e) {
				String error = ExceptionUtil.findNestedStackTrace(e, "javax.persistence.NoResultException");
				if (error != null) {
					logger.error("NoResultException caught: " + error);
				}
				else {
					logger.error("Exception caught", e);
				}
			}
 		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
		
		try {
			TimeUnit.SECONDS.sleep(0);
		}
		catch (InterruptedException e) {
			logger.error("InterruptedException caught: " + e.getMessage());
		}
	}

}
