package com.es.ejb.subscriber;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import com.es.ejb.ws.vo.SubscriberDataVo;
import com.es.ejb.ws.vo.SubscriptionVo;

@WebService (targetNamespace = "http://com.es.ws.subscriber/wsdl")
public interface SubscriberWs {

	@WebMethod
	@WebResult(name="Subscription")
	public SubscriptionVo addEmailToList(@WebParam(name="emailAddr") String emailAddr, @WebParam(name="listId") String listId);
	
	@WebMethod
	@WebResult(name="Subscription")
	public SubscriptionVo removeEmailFromList(@WebParam(name="emailAddr") String emailAddr, @WebParam(name="listId") String listId);
	
	@WebMethod
	@WebResult(name="SubscriberData")
	public SubscriberDataVo getSubscriberData(@WebParam(name="emailAddr") String emailAddr);
	
	@WebMethod
	@WebResult(name="isSuccess")
	public Boolean addSubscriber(@WebParam(name="subscriberData") SubscriberDataVo vo);
	
	@WebMethod
	@WebResult(name="isSuccess")
	public Boolean addAndSubscribe(@WebParam(name="subscriberData") SubscriberDataVo vo, @WebParam(name="listId") String listId);
}
