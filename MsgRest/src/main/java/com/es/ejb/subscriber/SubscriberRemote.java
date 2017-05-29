package com.es.ejb.subscriber;
import java.util.List;

import javax.ejb.Remote;

import jpa.model.SubscriberData;
import jpa.model.Subscription;

@Remote
public interface SubscriberRemote {
	public List<SubscriberData> getAllSubscribers();
	public SubscriberData getSubscriberById(String subrId);
	public SubscriberData getSubscriberByEmailAddress(String emailAddr);

	public Subscription subscribe(String emailAddr, String listId);
	public Subscription unSubscribe(String emailAddr, String listId);

	public Subscription addToList(String sbsrEmailAddr, String listEmailAddr);
	public Subscription removeFromList(String sbsrEmailAddr, String listEmailAddr);

	public void insertSubscriber(SubscriberData vo);
	public void updateSubscriber(SubscriberData vo);
	public void deleteSubscriber(SubscriberData vo);
	
	public Subscription optInRequest(String emailAddr, String listId);
	public Subscription optInConfirm(String emailAddr, String listId);
}
