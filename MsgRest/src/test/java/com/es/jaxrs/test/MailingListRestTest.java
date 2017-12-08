package com.es.jaxrs.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.es.ejb.mailinglist.MailingListRS;
import com.es.ejb.ws.vo.MailingListVo;
import com.es.jaxrs.common.ListReader;

import jpa.util.PrintUtil;

@EnableServices(value = "jaxrs")
@RunWith(ApplicationComposer.class)
public class MailingListRestTest {
	static final Logger logger = Logger.getLogger(MailingListRestTest.class);
	
	@Module
	@Classes(MailingListRS.class)
	public WebApp app() {
		return new WebApp().contextRoot("MailingListRestTest");
	}

	@Test
	public void testGetMailingListsAsString() {
		final String message = WebClient.create("http://localhost:4204")
				.path("/MailingListRestTest/msgapi/mailinglist/list")
				.accept(MediaType.APPLICATION_JSON)
				.get(String.class);
        logger.info("Message: " + message);
        assertTrue(message.indexOf("MailingListVo") > 0);
	}
	
	@Test
	public void testGetMailingListsAsList() {
		@SuppressWarnings("unchecked")
		final List<MailingListVo> list = WebClient.create("http://localhost:4204", getProviders())
				.path("/MailingListRestTest/msgapi/mailinglist/list")
				.accept(MediaType.APPLICATION_JSON)
				.get(List.class);
        logger.info("MailingListVo list size: " + list.size());
        assertFalse(list.isEmpty());
        for (MailingListVo vo : list) {
        	logger.info("MailingListVo" + PrintUtil.prettyPrintRecursive(vo));
        	assertTrue(StringUtils.isNotBlank(vo.getListId()));
        	assertTrue(StringUtils.isNotBlank(vo.getListEmailAddr()));
        }
	}

    private <T> List<Object> getProviders() {
    	// build provider list
    	List<Object> providers = new ArrayList<>();
    	ListReader<T> lstRdr = new ListReader<>();
    	providers.add(lstRdr);
    	return providers;
    }

}
