package com.es.jaxrs.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.es.ejb.ws.vo.MailingListVo;
import com.es.tomee.util.JaxrsUtil;
import jpa.tomee.util.TomeeCtxUtil;

import jpa.util.FileUtil;
import jpa.util.PrintUtil;
import jpa.xml.util.XmlHelper;

public class MailingListClient {
	protected final static Logger logger = Logger.getLogger(MailingListClient.class);
	
	public static void main(String[] args) {
		int port = TomeeCtxUtil.findHttpPort(new int[] {8181, 8080});
		String uriStr = "http://localhost:" + port;
		try {
			testMultipart(uriStr, "uploadpart");
			testMultipart(uriStr, "uploadfile");
			testGetAllListAsXml(uriStr);
			testGetByListId(uriStr);
			testGetByAddressAndUpdate(uriStr);
			testUpdateWithForm(uriStr);
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}

	static void testGetAllListAsXml(String uriStr) throws IOException {
		List<Object> providers = new ArrayList<>();
		JAXBElementProvider<?> JAXB = new JAXBElementProvider<>();
		providers.add(JAXB);
		JSONProvider<?> JSON = new JSONProvider<>();
		providers.add(JSON);
		providers.clear(); // XXX providers are not needed
		
		WebClient client = WebClient.create(uriStr, providers).path("/MsgRest/msgapi/mailinglist/list");
		
		/*
		 * Received following error when tried to run client.get(List.class)
		 * org.apache.cxf.jaxrs.client.ClientWebApplicationException: .No message body reader has been found for class : interface java.util.List, ContentType : application/xml.
		 */
		//client.get(List.class);
		
		Response rsp = client.get();
		logger.info("Status: " + rsp.getStatus() + ", Class: " + rsp.getEntity().getClass().getName());
		InputStream is = (InputStream) rsp.getEntity();
		logger.info("Size of InputStream: " + is.available());
		String message = IOUtils.toString(is, "UTF-8");
		try {
			logger.info("Mailing List: " + XmlHelper.printXml(message));
		} catch (SAXException | ParserConfigurationException | TransformerException e) {
			logger.error("Failed to print as XML", e);
		}
	}

	static void testGetByListId(String uriStr) {
		WebClient client = WebClient.create(uriStr).path("/MsgRest/msgapi/mailinglist/getBy/listId");
		client.query("value", "SMPLLST1");
		MailingListVo vo = client.get(MailingListVo.class);
		assertNotNull(vo);
		logger.info("Mailing List: (get by listId)" + PrintUtil.prettyPrint(vo));
	}
	
	static void testGetByAddressAndUpdate(String uriStr) {
		// get mailing list from list email address
		WebClient client = WebClient.create(uriStr).path("/MsgRest/msgapi/mailinglist/getBy/address");
		client.query("value", "demolist2@localhost");
		MailingListVo vo = client.get(MailingListVo.class);
		assertNotNull(vo);
		logger.info("Mailing List: (before update)" + PrintUtil.prettyPrint(vo));
		// update the mailing list
		client = WebClient.create(uriStr).path("/MsgRest/msgapi/mailinglist/update/" + vo.getListId());
		int suffix = new Random().nextInt(1000) + 1000;
		vo.setDescription("Sample mailing list 2 - " + suffix);
		Response rsp = client.post(vo);
		logger.info("HTTP Status from update with VO object: " + rsp.getStatus());
		assertEquals(200, rsp.getStatus());
		// fetch updated mailing list and verify result.
		client = WebClient.create(uriStr).path("/MsgRest/msgapi/mailinglist/getBy/address");
		client.query("value", "demolist2@localhost");
		vo = client.get(MailingListVo.class);
		assertNotNull(vo);
		logger.info("Mailing List: (after update)" + PrintUtil.prettyPrint(vo));
		assertTrue(StringUtils.endsWith(vo.getDescription(), "" + suffix));
	}
	
	static void testUpdateWithForm(String uriStr) {
		WebClient client = WebClient.create(uriStr).path("/MsgRest/msgapi/mailinglist/updateform");
		Form form = new Form();
		form.set("listId", "SMPLLST2");
		int suffix = new Random().nextInt(1000) + 1000;
		form.set("description", "Sample mailing list 2 - " + suffix);
		client.type(MediaType.APPLICATION_FORM_URLENCODED);
		// Send the form object along with the post call
		Response rsp = client.post(form);
		logger.info("HTTP Status from update with Form object:" + rsp.getStatus());
		assertEquals(200, rsp.getStatus());
		// fetch updated mailing list and verify result.
		client = WebClient.create(uriStr).path("/MsgRest/msgapi/mailinglist/getBy/listId");
		client.query("value", "SMPLLST2");
		MailingListVo vo = client.get(MailingListVo.class);
		assertNotNull(vo);
		logger.info("Mailing List: (after update with form)" + PrintUtil.prettyPrint(vo));
		assertTrue(StringUtils.endsWith(vo.getDescription(), "" + suffix));
	}
	
	static void testMultipart(String uriStr, String part) throws IOException {
		WebClient client = WebClient.create(uriStr).path("/MsgRest/msgapi/mailinglist/" + part);
		client.type("multipart/form-data").accept("multipart/mixed");
		List<Attachment> atts = new LinkedList<Attachment>();
		byte[] txtfile = FileUtil.loadFromFile("META-INF", "openejb.xml");
		atts.add(new Attachment("root", "text/xml", txtfile));
		if (StringUtils.contains(part, "part")) {
			byte[] txtfile2 = FileUtil.loadFromFile("META-INF", "ejb-jar.xml");
			atts.add(new Attachment("fileUpload", "text/xml", txtfile2));
		}
		Collection<?> attlist = client.postAndGetCollection(atts, Attachment.class);
		for (Object obj : attlist) {
			if (!(obj instanceof Attachment)) {
				logger.warn("Not an Attachment, skip.");
				continue;
			}
			Attachment att = (Attachment)obj;
			byte[] content = JaxrsUtil.getBytesFromDataHandler(att.getDataHandler());
			boolean isTextContent = StringUtils.contains(att.getDataHandler().getContentType().toString(), "text");
			logger.info("Content type: " + att.getContentType() + ", id: " + att.getContentId());
			if (isTextContent) {
				logger.info("     Content: " + new String(content));
			}
		}
	}

}
