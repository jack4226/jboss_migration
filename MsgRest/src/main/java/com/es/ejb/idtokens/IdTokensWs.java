package com.es.ejb.idtokens;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.es.ejb.ws.vo.IdTokensVo;

@WebService (targetNamespace = "http://com.es.ws.idtokens/wsdl")
public interface IdTokensWs {

	@WebMethod
	@WebResult(name="IdTokens")
	public IdTokensVo getBySenderId(@WebParam(name="senderId") String senderId);
	
	@WebMethod
	@WebResult(name="IdTokensList", partName="IdTokens")
	public List<IdTokensVo> getAll();
	
	@WebMethod
	public void update(@WebParam(name="idTokens") IdTokensVo vo);
}
