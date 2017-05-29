package com.es.ejb.subscription;

import java.util.List;

import javax.ejb.Remote;

import jpa.model.Subscription;

@Remote
public interface SubscriptionRemote {

	public List<Subscription> getByListId(String listId);
	
	public List<Subscription> getByAddress(String address);
	
	public Subscription getByUniqueKey(int emailAddrRowId, String listId);
	
	public Subscription getByAddressAndListId(String address, String listId);
	
	public Subscription subscribe(String address, String listId);
	
	public Subscription unsubscribe(String address, String listId);
}
