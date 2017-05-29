package com.es.ejb.client;

import java.util.List;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.NamingException;

import jpa.model.SubscriberData;
import jpa.util.PrintUtil;

import org.apache.log4j.Logger;

import com.es.ejb.subscriber.SubscriberRemote;
import com.es.tomee.util.TomeeCtxUtil;

public class SubscriberClient {
	static Logger logger = Logger.getLogger(SubscriberClient.class);
	
	public static void main(String[] args) {
		try {
			SubscriberClient client = new SubscriberClient();
			client.testSubscriber();
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
	
	void testSubscriber() {
		SubscriberRemote subr = null;
		Context ctx = null;
		try {
			ctx = TomeeCtxUtil.getRemoteContext();
			TomeeCtxUtil.listContext(ctx, "");
			subr = (SubscriberRemote) ctx.lookup("ejb/Subscriber");
		}
		catch (NamingException e) {
			logger.error("NamingException", e);
			return;
		}

		// test EJB remote access
		logger.info("SubscriberRemote instance: " + subr);
		
		try {
			subr.getSubscriberByEmailAddress("");
		}
		catch (EJBException e) {
			logger.error("EJBException" + e.getMessage());
		}
		
		List<SubscriberData> subrlist = subr.getAllSubscribers();
		for (SubscriberData data : subrlist) {
			logger.info(PrintUtil.prettyPrint(data, 1));
		}
	}
}
