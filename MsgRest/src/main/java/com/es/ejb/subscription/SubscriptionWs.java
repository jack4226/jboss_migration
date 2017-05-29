package com.es.ejb.subscription;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;

import jpa.model.Subscription;

@WebService (targetNamespace = "http://com.es.ws.subscription/wsdl")
public interface SubscriptionWs {

	@WebMethod
	@WebResult(name="subsciprionList", partName="subscription")
	public List<Subscription> getByListId(String listId);
	
	@WebMethod
	@WebResult(name="subsciprionList", partName="subscription")
	public List<Subscription> getByAddress(String address);
	
	@WebMethod
	@WebResult(name="subsciprion")
	public Subscription getByUniqueKey(int emailAddrRowId, String listId);
	
	@WebMethod
	@WebResult(name="subsciprion")
	public Subscription getByAddressAndListId(String address, String listId);
	
	@WebMethod
	@WebResult(name="subsciprion")
	public Subscription subscribe(String address, String listId);
	
	@WebMethod
	@WebResult(name="subsciprion")
	public Subscription unsubscribe(String address, String listId);

}
