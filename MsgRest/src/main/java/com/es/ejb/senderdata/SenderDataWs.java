package com.es.ejb.senderdata;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.es.ejb.ws.vo.SenderDataVo;

@WebService (targetNamespace = "http://com.es.ws.senderdata/wsdl")
public interface SenderDataWs {

	@WebMethod
	public SenderDataVo getBySenderId(String senderId);
	
	@WebMethod
	public List<SenderDataVo> getAll();
	
	@WebMethod
	public void update(SenderDataVo vo);
}
