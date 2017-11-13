package com.es.ejb.subscriber;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

@EnableServices(value = "jaxrs")
@RunWith(ApplicationComposer.class)
public class SubscriptionRSTest {
	static final Logger logger = Logger.getLogger(SubscriptionRSTest.class);

	@Module
	public SingletonBean app() {
	    return (SingletonBean) new SingletonBean(SubscriptionRS.class).localBean();
	}

    @Test
    public void get() throws IOException {
		final String message = WebClient.create("http://localhost:4204")
				.path("/SubscriptionRSTest/msgapi/subscription/getSubscriber")
				.query("emailAddr", "jsmith@test.com")
				.get(String.class);
        logger.info("Message: " + message);
        assertTrue(message.indexOf("Smith") > 0);
    }

}
