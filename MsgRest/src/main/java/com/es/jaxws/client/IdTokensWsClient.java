package com.es.jaxws.client;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import jpa.constant.Constants;
import jpa.util.ExceptionUtil;
import jpa.util.PrintUtil;

import org.apache.log4j.Logger;

import com.es.ejb.idtokens.IdTokensWs;
import com.es.ejb.ws.vo.IdTokensVo;
import com.es.tomee.util.TomeeCtxUtil;

public class IdTokensWsClient {
	protected final static Logger logger = Logger.getLogger(IdTokensWsClient.class);
	
	public static void main(String[] args) {
		testIdTokensWs();
	}
	
	static void testIdTokensWs() {
		int port = TomeeCtxUtil.findHttpPort(new int[] {8181, 8080});
		try {
			Service service = Service.create(new URL("http://localhost:" + port + "/MsgRest/webservices/IdTokens?wsdl"),
				new QName("http://com.es.ws.idtokens/wsdl", "IdTokensService"));
			assertNotNull(service);
			IdTokensWs idtkn = service.getPort(IdTokensWs.class);
			
			try {
				idtkn.getBySenderId("FakeSender");
			}
			catch (SOAPFaultException e) {
				String error = ExceptionUtil.findNestedStackTrace(e, "javax.persistence.NoResultException");
				if (error != null) {
					logger.error("NoResultException caught: " + error);
				}
				else {
					logger.error("SOAPFaultException caught as expected", e);
				}
			}
			
			List<IdTokensVo> list = idtkn.getAll();
			assert(!list.isEmpty());
			for (IdTokensVo vo : list) {
				logger.info(PrintUtil.prettyPrint(vo));
			}
			IdTokensVo vo = idtkn.getBySenderId(Constants.DEFAULT_SENDER_ID);
			assertNotNull(vo);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
}
