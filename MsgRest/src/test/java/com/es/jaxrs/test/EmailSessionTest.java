package com.es.jaxrs.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
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
    @Classes(EmailSession.class)
    public WebApp app() {
        return new WebApp().contextRoot("test");
    }

    @Test
    public void post() throws IOException {
    	EmailDto dto = new EmailDto();
    	dto.message = "Hello Tomitribe";
    	WebClient client = WebClient.create("http://localhost:4204").path("/test/email/");
		final String message = client.post(dto, String.class);
        //assertEquals("Failed to send message: Unknown SMTP host: your.mailserver.host", message);
        assertEquals("Sent", message);
    }
}
