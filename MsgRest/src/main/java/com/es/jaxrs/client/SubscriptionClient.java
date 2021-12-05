package com.es.jaxrs.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import jpa.tomee.util.TomeeCtxUtil;

import jpa.model.SubscriberData;
import jpa.model.Subscription;
import jpa.util.PrintUtil;
import jpa.xml.util.XmlHelper;

public class SubscriptionClient {
	static final Logger logger = LogManager.getLogger(SubscriptionClient.class);
	
	private static String sbsrEmail = "jsmith@test.com";
	private static String notExist = "addr_does_not_exist@abc.com";
	
	public static void main(String[] args) {
		int port = TomeeCtxUtil.findHttpPort(new int[] {8181, 8080});
		String httpAddr = "http://localhost:" + port;
		try {
			testGetSubscriberAsXml(httpAddr);
			testGetSubscriberAsObject(httpAddr);
			testUnsubscribeFromList(httpAddr);
			testSubscribeToList(httpAddr);
			testGetSubscribedList(httpAddr);
			testAddEmailToList(httpAddr);
			testRemoveEmailFromList(httpAddr);
			testUpdateSubscriber(httpAddr);
			testUpdateSubscriberNotExist(httpAddr);
			testGetSubscriberNotFound(httpAddr);
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}

	static void testGetSubscriberAsXml(String httpAddr) throws IOException {
		WebClient client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/subscriber/" + sbsrEmail);
		
		final Response rsp = client.get();
		logger.info("Status: " + rsp.getStatus() + ", Class: " + rsp.getEntity().getClass().getName());
		InputStream is = (InputStream) rsp.getEntity();
		logger.info("Size of InputStream: " + is.available());
		String message = IOUtils.toString(is, "UTF-8");
		try {
			logger.info("Subscriber: " + XmlHelper.printXml(message));
		} catch (SAXException | ParserConfigurationException | TransformerException e) {
			logger.error("Failed to print as XML", e);
		}
        assertTrue(StringUtils.contains(message, "614-234-5678"));
	}

	static void testGetSubscriberAsObject(String httpAddr) throws IOException {
		WebClient client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/getSubscriber").query("emailAddr", sbsrEmail);
		
		final Response rsp = client.get();
		logger.info("Response Status: " + rsp.getStatus());
		SubscriberData sbsr = client.get(SubscriberData.class);
		logger.info("Subscriber: " + PrintUtil.prettyPrint(sbsr));
		assertEquals(sbsr.getDayPhone(), "614-234-5678");
	}
	
	static void testGetSubscriberNotFound(String httpAddr) throws IOException {
		WebClient client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/getSubscriber").query("emailAddr", notExist);
		
		final Response rsp = client.get();
		logger.info("Response Status: " + rsp.getStatus());
		InputStream is = (InputStream) rsp.getEntity();
		logger.info("Size of InputStream: " + is.available());
		String message = IOUtils.toString(is, "UTF-8");
		try {
			logger.info("Subscriber: " + XmlHelper.printXml(message));
		} catch (SAXException | ParserConfigurationException | TransformerException e) {
			logger.error("Failed to print as XML", e);
		}
		assertEquals(rsp.getStatus(), 404);
	}

	static void testGetSubscribedList(String httpAddr) throws IOException {
		WebClient client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/subscribedlist").query("emailAddr", sbsrEmail);
		
		final Response rsp = client.get();
		logger.info("Status: " + rsp.getStatus() + ", Class: " + rsp.getEntity().getClass().getName());
		InputStream is = (InputStream) rsp.getEntity();
		logger.info("Size of InputStream: " + is.available());
		String message = IOUtils.toString(is, "UTF-8");
		try {
			logger.info("Subscribed List: " + XmlHelper.printXml(message));
		} catch (SAXException | ParserConfigurationException | TransformerException e) {
			logger.error("Failed to print as XML", e);
		}
		assertTrue(StringUtils.contains(message, sbsrEmail));
	}

	static void testUnsubscribeFromList(String httpAddr) throws IOException {
		WebClient client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/unsubscribe/" + sbsrEmail);
		
		Subscription sub = client.put("SMPLLST1", Subscription.class);
		logger.info("Subscription (unsubscribed): " + PrintUtil.prettyPrint(sub));
		assertEquals(sub.isSubscribed(), false);
	}

	static void testSubscribeToList(String httpAddr) throws IOException {
		WebClient client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/subscribe/" + sbsrEmail);
		
		Subscription sub = client.put("SMPLLST1", Subscription.class);
		logger.info("Subscription (subscribed): " + PrintUtil.prettyPrint(sub));
		assertEquals(sub.isSubscribed(), true);
	}

	static void testAddEmailToList(String httpAddr) throws IOException {
		WebClient client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/addtolist/" + "newsbsr@test.com");
		
		Subscription sub = client.put("demolist1@localhost", Subscription.class);
		logger.info("Subscription (added to list): " + PrintUtil.prettyPrint(sub));
		assertEquals(sub.isSubscribed(), true);
	}

	static void testRemoveEmailFromList(String httpAddr) throws IOException {
		WebClient client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/removefromlist/" + "newsbsr@test.com");
		
		Subscription sub = client.put("demolist1@localhost", Subscription.class);
		logger.info("Subscription (removed from list): " + PrintUtil.prettyPrint(sub));
		assertEquals(sub.isSubscribed(), false);
	}

	static void testUpdateSubscriber(String httpAddr) {
		WebClient client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/update/" + sbsrEmail);
		Form form = new Form();
		int suffix = new Random().nextInt(1000) + 1000;
		form.param("msgFooter", "Have a nice day. - " + suffix);
		form.param("msgHeader", "Joe's Message Header - " + suffix);
		client.type(MediaType.APPLICATION_FORM_URLENCODED);
		// Send the form object along with the post call
		Response rsp = client.post(form);
		logger.info("HTTP Status from update with Form object:" + rsp.getStatus());
		assertEquals(200, rsp.getStatus());
		// fetch updated subscriber and verify result.
		client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/getSubscriber").query("emailAddr", sbsrEmail);
		SubscriberData sbsr = client.get(SubscriberData.class);
		logger.info("Subscriber (after update with form): " + PrintUtil.prettyPrint(sbsr));
		assertTrue(StringUtils.endsWith(sbsr.getMsgHeader(), "" + suffix));
		assertTrue(StringUtils.endsWith(sbsr.getMsgFooter(), "" + suffix));
	}
	
	static void testUpdateSubscriberNotExist(String httpAddr) {
		WebClient client = WebClient.create(httpAddr).path("/MsgRest/msgapi/subscription/update/" + notExist);
		Form form = new Form();
		int suffix = new Random().nextInt(1000) + 1000;
		form.param("msgFooter", "Have a nice day. - " + suffix);
		client.type(MediaType.APPLICATION_FORM_URLENCODED);
		// Send the form object along with the post call
		Response rsp = client.post(form);
		logger.info("HTTP Status from update with non-existing email address: " + rsp.getStatus());
		assertEquals(404, rsp.getStatus());
	}
}
