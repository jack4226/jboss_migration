package com.es.ejb.subscription;

import java.util.List;

import javax.ejb.Local;

import jpa.model.Subscription;

@Local
public interface SubscriptionLocal {

	public List<Subscription> getByListId(String listId);
	
	public List<Subscription> getByAddress(String address);
	
	public Subscription getByUniqueKey(int emailAddrRowId, String listId);
	
	public Subscription getByAddressAndListId(String address, String listId);
	
	public Subscription subscribe(String address, String listId);
	
	public Subscription unsubscribe(String address, String listId);
	
	// local interface only
	public Subscription getByRowId(int rowId);
	
	public int updateOpenCount(int emailAddrRowId, String listId);
	public int updateClickCount(int emailAddrRowId, String listId);
}
