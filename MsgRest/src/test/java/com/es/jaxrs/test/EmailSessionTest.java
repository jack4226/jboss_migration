package com.es.jaxrs.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.es.jaxrs.service.EmailSession;
import com.es.jaxrs.service.EmailSession.EmailDto;

@EnableServices(value = "jaxrs")
@RunWith(ApplicationComposer.class)
public class EmailSessionTest {

    @Module
	public SingletonBean app() {
	    return (SingletonBean) new SingletonBean(EmailSession.class).localBean();
	}

    @Test
    public void post() throws IOException {
    	EmailDto dto = new EmailDto();
    	dto.message = "Hello Tomitribe";
    	WebClient client = WebClient.create("http://localhost:4204").path("/EmailSessionTest/email/");
    	client.type(MediaType.APPLICATION_XML);
		final String message = client.post(dto, String.class);
        //assertEquals("Failed to send message: Unknown SMTP host: your.mailserver.host", message);
        assertEquals("Sent", message);
    }
}
