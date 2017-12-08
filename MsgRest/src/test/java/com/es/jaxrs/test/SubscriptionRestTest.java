package com.es.jaxrs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.es.ejb.subscriber.SubscriptionRS;

import jpa.model.SubscriberData;
import jpa.util.PrintUtil;

@EnableServices(value = "jaxrs")
@RunWith(ApplicationComposer.class)
public class SubscriptionRestTest {
	static final Logger logger = Logger.getLogger(SubscriptionRestTest.class);
	
	private String sbsrEmail = "jsmith@test.com";

	@Module
	@Classes(SubscriptionRS.class)
	public WebApp app() {
		return new WebApp().contextRoot("SubscriptionRestTest");
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
    public void testGetSubscriberAsStream() throws IOException {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/getSubscriber").query("emailAddr", sbsrEmail);
		final Response rsp = client.get();
		logger.info("Status: " + rsp.getStatus() + ", Class: " + rsp.getEntity().getClass().getName());
		InputStream is = (InputStream) rsp.getEntity();
		logger.info("Size of InputStream: " + is.available());
		String message = IOUtils.toString(is, "UTF-8");
		logger.info("Subscriber: " + message);
		assertTrue(message.indexOf("Smith") > 0);
    }
    
    @Test
    public void testGetSubscriberByPath() throws IOException {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRestTest/msgapi/subscription/subscriber/" + sbsrEmail);
		final Response rsp = client.get();
		logger.info("Status: " + rsp.getStatus() + ", Class: " + rsp.getEntity().getClass().getName());
		InputStream is = (InputStream) rsp.getEntity();
		logger.info("Size of InputStream: " + is.available());
		String message = IOUtils.toString(is, "UTF-8");
		logger.info("Subscriber: " + message);
		assertTrue(message.indexOf("Smith") > 0);
    }
    
    private List<Object> getProviders() {
    	// build provider list
    	List<Object> providers = new ArrayList<>();
    	//SubscriberDataReader reader = new SubscriberDataReader();
    	//providers.add(reader);
    	return providers;
    }
}
