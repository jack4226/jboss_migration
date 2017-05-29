package com.es.ejb.emailaddr;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.es.ejb.ws.vo.EmailAddrVo;

@WebService (targetNamespace = "http://com.es.ws.emailaddr/wsdl")
public interface EmailAddrWs {

	@WebMethod
	@WebResult(name="EmailAddress")
	public EmailAddrVo getOrAddAddress(@WebParam(name="emailAddr") String address);
	
	@WebMethod
	public int delete(@WebParam(name="emailAddr") String address);
}
