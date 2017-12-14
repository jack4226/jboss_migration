package com.es.jaxrs.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
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
import com.es.tomee.util.JaxrsUtil;

import jpa.util.FileUtil;
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
	@org.junit.Ignore
	public void testGetMailingListsAsString() {
		final String message = WebClient.create("http://localhost:4204")
				.path("/MailingListRestTest/msgapi/mailinglist/list")
				.accept(MediaType.APPLICATION_JSON)
				.get(String.class);
        logger.info("Message: " + message);
        assertTrue(message.startsWith("{") && message.endsWith("}"));
        assertTrue(message.indexOf("MailingListVo") > 0);
	}
	
	@Test
	@org.junit.Ignore
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

	@Test
	@org.junit.Ignore
	public void testGetByListId() {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/MailingListRestTest/msgapi/mailinglist/getBy/listId");
		client.query("value", "SMPLLST1");
		MailingListVo vo = client.get(MailingListVo.class);
		assertNotNull(vo);
		logger.info("Mailing List: (get by listId)" + PrintUtil.prettyPrint(vo));
		assertEquals("SMPLLST1", vo.getListId());
	}
	
	@Test
	@org.junit.Ignore
	public void testGetByAddress() {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/MailingListRestTest/msgapi/mailinglist/getBy/address");
		client.query("value", "demolist2@localhost");
		MailingListVo vo = client.get(MailingListVo.class);
		assertNotNull(vo);
		logger.info("Mailing List: (get by address)" + PrintUtil.prettyPrint(vo));
		assertEquals("demolist2@localhost", vo.getListEmailAddr());
	}
	
	@Test
	@org.junit.Ignore
	public void testUpdateWithObject() {
		// get mailing list from list email address
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/MailingListRestTest/msgapi/mailinglist/getBy/address");
		client.query("value", "demolist2@localhost");
		MailingListVo vo = client.get(MailingListVo.class);
		assertNotNull(vo);
		logger.info("Mailing List: (before update)" + PrintUtil.prettyPrint(vo));

		// update the mailing list
		client = WebClient.create("http://localhost:4204", getProviders())
				.path("/MailingListRestTest/msgapi/mailinglist/update/" + vo.getListId());
		int suffix = new Random().nextInt(10000) + 10000;
		vo.setDescription("Sample mailing list 2 - " + suffix);
		Response rsp = client.post(vo);
		logger.info("HTTP Status from update with VO object: " + rsp.getStatus());
		assertEquals(200, rsp.getStatus());

		// fetch updated mailing list and verify result.
		client = WebClient.create("http://localhost:4204", getProviders())
				.path("/MailingListRestTest/msgapi/mailinglist/getBy/address");
		client.query("value", "demolist2@localhost");
		vo = client.get(MailingListVo.class);
		assertNotNull(vo);
		logger.info("Mailing List: (after update)" + PrintUtil.prettyPrint(vo));
		assertTrue(StringUtils.endsWith(vo.getDescription(), "" + suffix));
	}

	@Test
	@org.junit.Ignore
	public void testUpdateWithForm() {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/MailingListRestTest/msgapi/mailinglist/updateform");
		Form form = new Form();
		form.set("listId", "SMPLLST2");
		int suffix = new Random().nextInt(10000) + 10000;
		form.set("description", "Sample mailing list 2 - " + suffix);
		client.type(MediaType.APPLICATION_FORM_URLENCODED);
		// Send the form object along with the post call
		Response rsp = client.post(form);
		logger.info("HTTP Status from update with Form object:" + rsp.getStatus());
		assertEquals(200, rsp.getStatus());

		// fetch updated mailing list and verify result.
		client = WebClient.create("http://localhost:4204", getProviders())
				.path("/MailingListRestTest/msgapi/mailinglist/getBy/listId");
		client.query("value", "SMPLLST2");
		MailingListVo vo = client.get(MailingListVo.class);
		assertNotNull(vo);
		logger.info("Mailing List: (after update with form)" + PrintUtil.prettyPrint(vo));
		assertTrue(StringUtils.endsWith(vo.getDescription(), "" + suffix));
	}

	@Test
	public void testUploadPartAndFile() throws IOException {
		testUpload("uploadpart", 2, 1);
		try {
			testUpload("uploadpart", 2, 2);
			fail();
		}
		catch (javax.ws.rs.WebApplicationException e) {
			// media type mismatch.
		}
		
		testUpload("uploadpart2", 2, 1);
		testUpload("uploadpart2", 1, 2);
		
		testUpload("uploadfile", 2, 1);
		try {
			testUpload("uploadfile", 2, 2);
			fail();
		}
		catch (javax.ws.rs.WebApplicationException e) {
			// media type mismatch.
		}
	}

	private void testUpload(String part, int expectedCount, int iteration) throws IOException {
		WebClient client = WebClient.create("http://localhost:4204", getProviders())
				.path("/MailingListRestTest/msgapi/mailinglist/" + part);
		client.type("multipart/form-data").accept("multipart/mixed");
		List<Attachment> atts = new LinkedList<Attachment>();
		byte[] txtfile1 = FileUtil.loadFromFile("META-INF", "openejb.xml");
		byte[] txtfile2 = FileUtil.loadFromFile("META-INF", "MANIFEST.MF");
		byte[] txtfile3 = FileUtil.loadFromFile("META-INF", "ejb-jar.xml");
		byte[] pdffile = FileUtil.loadFromFile("META-INF", "ErrorCodes.pdf");
		assertNotNull(txtfile1);
		atts.add(new Attachment("root", "text/xml", txtfile1));
		if (StringUtils.equals(part, "uploadpart")) {
			if (iteration == 1) {
				atts.add(new Attachment("ErrorCode.pdf", "application/octet-stream", pdffile));
			} else if (iteration == 2) {
				// atts.add(new Attachment("textfile", "text/plain", txtfile1));
				atts.add(new Attachment("textfile", "application/octet-stream", txtfile2));
			}
		}
		else if (StringUtils.equals(part, "uploadpart2")) {
			if (iteration == 1) {
				atts.add(new Attachment("textfile", "text/plain", txtfile2));
			}
			else if (iteration == 2) {
				atts.add(new Attachment("textfile_nomatch", "text/plain", txtfile2));
			}
		}
		else if (StringUtils.equals(part, "uploadfile")) {
			if (iteration == 1) {
				atts.add(new Attachment("ErrorCode.pdf", "application/octet-stream", pdffile));
			}
			else if (iteration == 2) {
				atts.add(new Attachment("textfile", "application/octet-stream", txtfile3));
			}
		}
		// invoke restful service
		Collection<?> attlist = client.postAndGetCollection(atts, Attachment.class);
		assertNotNull(attlist);
		assertFalse(attlist.isEmpty());
		int count = 0;
		for (Object obj : attlist) {
			if (!(obj instanceof Attachment)) {
				logger.warn("Not an Attachment, skip.");
				continue;
			}
			count ++;
			Attachment att = (Attachment) obj;
			byte[] content = JaxrsUtil.getBytesFromDataHandler(att.getDataHandler());
			assertNotNull(content);
			assertTrue(content.length > 0);
			boolean isTextContent = StringUtils.contains(att.getDataHandler().getContentType(), "text");
			logger.info("Content type: " + att.getContentType() + ", Content Id: " + att.getContentId());
			if (isTextContent) {
				logger.info("Content: " + new String(content));
			}
			else {
				logger.info("Content length: " + content.length);
			}
		}
		assertEquals(expectedCount, count);
	}

    private <T> List<Object> getProviders() {
    	// build provider list
    	List<Object> providers = new ArrayList<>();
    	ListReader<T> lstRdr = new ListReader<>();
    	providers.add(lstRdr);
    	return providers;
    }

}
