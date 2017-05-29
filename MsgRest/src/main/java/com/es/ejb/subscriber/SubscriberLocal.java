package com.es.ejb.subscriber;
import java.util.List;

import javax.ejb.Local;

import jpa.model.SubscriberData;
import jpa.model.Subscription;

import com.es.ejb.ws.vo.SubscriptionVo;

@Local
public interface SubscriberLocal {
	public List<SubscriberData> getAllSubscribers();
	public SubscriberData getSubscriberById(String subrId);
	public SubscriberData getSubscriberByEmailAddress(String emailAddr);

	public List<SubscriptionVo> getSubscribedList(String emailAddr);
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
