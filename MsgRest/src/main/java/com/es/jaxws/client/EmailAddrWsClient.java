package com.es.jaxws.client;

import static org.junit.Assert.assertNotNull;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

import com.es.ejb.emailaddr.EmailAddrWs;
import com.es.ejb.ws.vo.EmailAddrVo;
import com.es.tomee.util.TomeeCtxUtil;

import jpa.util.PrintUtil;

public class EmailAddrWsClient {
	protected final static Logger logger = Logger.getLogger(EmailAddrWsClient.class);
	
	public static void main(String[] args) {
		testEmailAddrWs();
	}
	
	static void testEmailAddrWs() {
		int port = TomeeCtxUtil.findHttpPort(new int[] {8181, 8080});
		try {
			Service service = Service.create(new URL("http://localhost:" + port + "/MsgRest/webservices/EmailAddr?wsdl"),
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
		}
	}
}
