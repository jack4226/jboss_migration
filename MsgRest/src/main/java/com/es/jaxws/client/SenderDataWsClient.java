package com.es.jaxws.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import jpa.constant.Constants;
import jpa.util.PrintUtil;

import org.apache.log4j.Logger;

import com.es.ejb.senderdata.SenderDataWs;
import com.es.ejb.ws.vo.SenderDataVo;
import jpa.tomee.util.TomeeCtxUtil;

public class SenderDataWsClient {
	protected final static Logger logger = Logger.getLogger(SenderDataWsClient.class);
	
	public static void main(String[] args) {
		testSenderDataWs();
	}
	
	static void testSenderDataWs() {
		int port = TomeeCtxUtil.findHttpPort(new int[] {8181, 8080});
		try {
			Service service = Service.create(new URL("http://localhost:" + port + "/MsgRest/webservices/SenderData?wsdl"),
				new QName("http://com.es.ws.senderdata/wsdl", "SenderDataService"));
			assertNotNull(service);
			SenderDataWs senderDao = service.getPort(SenderDataWs.class);
			
			try {
				senderDao.getBySenderId("FakeSender");
				fail();
			}
			catch (SOAPFaultException se) {
				// expected
				logger.error("SOAPFaultException caught as expected", se);
			}
			
			List<SenderDataVo> list = senderDao.getAll();
			assert(!list.isEmpty());
			for (SenderDataVo vo : list) {
				logger.info(PrintUtil.prettyPrint(vo));
			}
			SenderDataVo vo = senderDao.getBySenderId(Constants.DEFAULT_SENDER_ID);
			assertNotNull(vo);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
}
