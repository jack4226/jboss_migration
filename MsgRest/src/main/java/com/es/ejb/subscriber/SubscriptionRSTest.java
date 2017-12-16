package com.es.ejb.subscriber;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.log4j.Logger;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.es.ejb.ws.vo.SubscriptionVo;

import jpa.model.SubscriberData;
import jpa.util.PrintUtil;

@EnableServices(value = "jaxrs")
@RunWith(ApplicationComposer.class)
public class SubscriptionRSTest {
	static final Logger logger = Logger.getLogger(SubscriptionRSTest.class);

	@Module
	public SingletonBean app() {
	    return (SingletonBean) new SingletonBean(SubscriptionRS.class).localBean();
	}

    @Test
    //@org.junit.Ignore
    public void getSubscriberAsObject() throws IOException {
		final SubscriberData sbsrData = WebClient.create("http://localhost:4204")
				.path("/SubscriptionRSTest/msgapi/subscription/getSubscriber")
				.query("emailAddr", "jsmith@test.com")
				.get(SubscriberData.class);
		logger.info("SubscriberData: " + PrintUtil.prettyPrint(sbsrData));
		assertEquals("Joe", sbsrData.getFirstName());
        assertEquals("Smith", sbsrData.getLastName());
    }

    @Test
    //@org.junit.Ignore
    public void getSubscriberListAsJson() throws IOException {
		final String message = WebClient.create("http://localhost:4204")
				.path("/SubscriptionRSTest/msgapi/subscription/subscribedlist")
				.query("emailAddr", "jsmith@test.com").accept(MediaType.APPLICATION_JSON)
				.get(String.class);
        logger.info("Json Message: " + message);
        assertTrue(message.startsWith("[{") && message.endsWith("}]"));
        assertTrue(message.indexOf("jsmith@test.com") > 0);
    }

    @Test
    //@org.junit.Ignore
    public void getSubscriberListAsXml() throws IOException {
		final String message = WebClient.create("http://localhost:4204")
				.path("/SubscriptionRSTest/msgapi/subscription/subscribedlist")
				.query("emailAddr", "jsmith@test.com").accept(MediaType.APPLICATION_XML)
				.get(String.class);
        logger.info("Xml Message: " + message);
        assertTrue(message.startsWith("<?xml ") && message.endsWith(">"));
        assertTrue(message.indexOf("jsmith@test.com") > 0);
    }

    @Test
    //@org.junit.Ignore
    public void getSubscriberListAddress() throws IOException {
		@SuppressWarnings("unchecked")
		final List<SubscriptionVo> list = (List<SubscriptionVo>) WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRSTest/msgapi/subscription/subscribedlist")
				.query("emailAddr", "jsmith@test.com")
				.getCollection(SubscriptionVo.class);
		assertNotNull(list);
		assertFalse(list.isEmpty());
		for (SubscriptionVo vo : list) {
			logger.info("Object Message: " + PrintUtil.prettyPrintRecursive(vo));
			assertEquals("jsmith@test.com", vo.getAddress());
		}
    }

	private <T> List<Object> getProviders() {
    	// build provider list
    	List<Object> providers = new ArrayList<>();
    	JohnzonProvider<?> provider = new JohnzonProvider<>();
    	providers.add(provider);
    	return providers;
    }

}
