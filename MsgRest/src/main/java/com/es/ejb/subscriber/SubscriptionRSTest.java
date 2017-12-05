package com.es.ejb.subscriber;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.es.ejb.ws.vo.SubscriptionVo;
import com.es.jaxrs.common.ListReader;

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
    public void getSubscriberAsObject() throws IOException {
		final SubscriberData sbsrData = WebClient.create("http://localhost:4204")
				.path("/SubscriptionRSTest/msgapi/subscription/getSubscriber")
				.query("emailAddr", "jsmith@test.com").accept(MediaType.APPLICATION_JSON)
				.get(SubscriberData.class);
		logger.info("SubscriberData: " + PrintUtil.prettyPrint(sbsrData));
		assertEquals("Joe", sbsrData.getFirstName());
        assertEquals("Smith", sbsrData.getLastName());
    }

    @Test
    public void getSubscriberListAsJson() throws IOException {
		final String message = WebClient.create("http://localhost:4204")
				.path("/SubscriptionRSTest/msgapi/subscription/subscribedlist")
				.query("emailAddr", "jsmith@test.com").accept(MediaType.APPLICATION_JSON)
				.get(String.class);
        logger.info("Message: " + message);
        assertTrue(message.indexOf("jsmith@test.com") > 0);
    }

    @Test
    public void getSubscriberListAsXml() throws IOException {
		final String message = WebClient.create("http://localhost:4204")
				.path("/SubscriptionRSTest/msgapi/subscription/subscribedlist")
				.query("emailAddr", "jsmith@test.com").accept(MediaType.APPLICATION_XML)
				.get(String.class);
        logger.info("Message: " + message);
        assertTrue(message.indexOf("jsmith@test.com") > 0);
    }

    @Test
    public void getSubscriberListAddress() throws IOException {
		@SuppressWarnings("unchecked")
		final List<SubscriptionVo> list = (List<SubscriptionVo>) WebClient.create("http://localhost:4204", getProviders())
				.path("/SubscriptionRSTest/msgapi/subscription/subscribedlist")
				.query("emailAddr", "jsmith@test.com").accept(MediaType.APPLICATION_JSON)
				.get(List.class);
		assertNotNull(list);
		assertFalse(list.isEmpty());
		for (SubscriptionVo vo : list) {
			logger.info("Message: " + PrintUtil.prettyPrintRecursive(vo));
			assertEquals("jsmith@test.com", vo.getAddress());
		}
    }

    private List<Object> getProviders() {
    	// build provider list
    	List<Object> providers = new ArrayList<>();
    	//SubscriberDataReader reader = new SubscriberDataReader();
    	//providers.add(reader);
    	ListReader<SubscriptionVo> lstRdr = new ListReader<>();
    	providers.add(lstRdr);
    	return providers;
    }

}
