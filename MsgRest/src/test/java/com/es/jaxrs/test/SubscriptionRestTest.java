package com.es.jaxrs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.es.ejb.subscriber.SubscriptionRS;

import jpa.model.SubscriberData;
import jpa.model.Subscription;
import jpa.util.PrintUtil;

@EnableServices(value = "jaxrs")
@RunWith(ApplicationComposer.class)
public class SubscriptionRestTest {
	static final Logger logger = Logger.getLogger(SubscriptionRestTest.class);
	
	private String sbsrEmail = "jsmith@test.com";

	@Module
	public SingletonBean app() {
	    return (SingletonBean) new SingletonBean(SubscriptionRS.class).localBean();
	}
    
    @Test
    //@org.junit.Ignore
    public void testGetSubscriberAsObject() throws IOException {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/getSubscriber").query("emailAddr", sbsrEmail);
		logger.info("Status: " + client.get().getStatus());
		SubscriberData sbsrData = client.get(SubscriberData.class);
		logger.info("Subscriber: " + PrintUtil.prettyPrint(sbsrData));
		assertEquals("Joe", sbsrData.getFirstName());
        assertEquals("Smith", sbsrData.getLastName());
    }

    @Test
    //@org.junit.Ignore
    public void testGetSubscriberAsStream() throws IOException {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/getSubscriber").query("emailAddr", sbsrEmail)
				.accept(MediaType.APPLICATION_XML);
		final Response rsp = client.get();
		logger.info("Status: " + rsp.getStatus() + ", Class: " + rsp.getEntity().getClass().getName());
		InputStream is = (InputStream) rsp.getEntity();
		logger.info("Size of InputStream: " + is.available());
		String message = IOUtils.toString(is, "UTF-8");
		logger.info("Subscriber XML: " + message);
		assertTrue(message.startsWith("<") && message.endsWith(">"));
		assertTrue(message.indexOf("Smith") > 0);
    }
    
    @Test
    //@org.junit.Ignore
    public void testGetSubscriberByPath() throws IOException {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/subscriber/" + sbsrEmail);
		logger.info("Status: " + client.get().getStatus());
		SubscriberData sbsrData = client.get(SubscriberData.class);
		logger.info("Subscriber: " + PrintUtil.prettyPrint(sbsrData));
		assertEquals("Joe", sbsrData.getFirstName());
        assertEquals("Smith", sbsrData.getLastName());
    }
    
    @Test
    //@org.junit.Ignore
    public void testSubscribeGetAsStream() throws IOException {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/subscribe/" + sbsrEmail)
				.accept(MediaType.APPLICATION_XML);
		final Response rsp = client.put("SMPLLST1");
		logger.info("Status: " + rsp.getStatus());
		InputStream is = (InputStream) rsp.getEntity();
		logger.info("Size of InputStream: " + is.available());
		String message = IOUtils.toString(is, "UTF-8");
		logger.info("Subscription (subscribed): " + message);
		assertTrue(message.startsWith("<") && message.endsWith(">"));
		assertTrue(message.contains(sbsrEmail));
		assertTrue(message.contains("<isSubscribed>true</isSubscribed>"));
    }
    
    @Test
    //@org.junit.Ignore
    public void testUnsubscribeGetAsObject() throws IOException {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/unsubscribe/" + sbsrEmail);
		final Subscription sub = client.put("SMPLLST1", Subscription.class);
		logger.info("Subscription (unsubscribed): " + PrintUtil.prettyPrint(sub));
		assertEquals(false, sub.isSubscribed());
    }
    
    @Test
    //@org.junit.Ignore
	public void testAddEmailToList() throws IOException {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/addtolist/" + "newsbsr@test.com");
		Subscription sub = client.put("demolist1@localhost", Subscription.class);
		logger.info("Subscription (added to list): " + PrintUtil.prettyPrint(sub));
		assertEquals(sub.isSubscribed(), true);
	}

    @Test
    //@org.junit.Ignore
	public void testRemoveEmailFromList() throws IOException {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/removefromlist/" + "newsbsr@test.com");
		Subscription sub = client.put("demolist1@localhost", Subscription.class);
		logger.info("Subscription (removed from list): " + PrintUtil.prettyPrint(sub));
		assertEquals(sub.isSubscribed(), false);
	}

    @Test
	public void testUpdateSubscriber() {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/update/" + sbsrEmail);
		Form form = new Form();
		int suffix = new Random().nextInt(10000) + 10000;
		form.param("msgFooter", "Have a nice day. - " + suffix);
		form.param("msgHeader", "Joe's Message Header - " + suffix);
		client.type(MediaType.APPLICATION_FORM_URLENCODED);
		// Send the form object along with the post call
		Response rsp = client.post(form);
		logger.info("HTTP Status from update with Form object:" + rsp.getStatus());
		assertEquals(200, rsp.getStatus());
		
		// fetch updated subscriber and verify result.
		client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/getSubscriber").query("emailAddr", sbsrEmail);
		SubscriberData sbsr = client.get(SubscriberData.class);
		logger.info("Subscriber (after update with form): " + PrintUtil.prettyPrint(sbsr));
		assertTrue(StringUtils.endsWith(sbsr.getMsgHeader(), "" + suffix));
		assertTrue(StringUtils.endsWith(sbsr.getMsgFooter(), "" + suffix));
	}

    private List<Object> getProviders() {
    	// build provider list
    	List<Object> providers = new ArrayList<>();
    	return providers;
    }
}
