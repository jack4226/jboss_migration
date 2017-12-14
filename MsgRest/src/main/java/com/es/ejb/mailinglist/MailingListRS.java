package com.es.ejb.mailinglist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMultipart;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import jpa.util.BeanCopyUtil;
import jpa.util.PrintUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.apache.log4j.Logger;

import com.es.ejb.ws.vo.MailingListVo;
import com.es.jaxrs.common.ErrorResponse;
import com.es.tomee.util.BeanReflUtil;
import com.es.tomee.util.JaxrsUtil;

import jpa.service.maillist.MailingListService;
import jpa.spring.util.SpringUtil;
import jpa.tomee.util.TomeeCtxUtil;

@Path("/msgapi/mailinglist")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class MailingListRS {
	static final Logger logger = Logger.getLogger(MailingListRS.class);
	
	final String LF = System.getProperty("line.separator", "\n");

	@javax.ejb.EJB
	private MailingListLocal maillist;
	
	private MailingListService mlistService;
	
	public MailingListRS() {
		BeanCopyUtil.registerBeanUtilsConverters();
	}
	
	MailingListLocal getMailingListLocal() {

		if (maillist == null) {
			try {
				javax.naming.Context context = TomeeCtxUtil.getLocalContext();
				maillist = (MailingListLocal) context.lookup("MailingListLocal");
			}
			catch (NamingException e) {
		    	mlistService = SpringUtil.getAppContext().getBean(MailingListService.class);
			}
		}
		return maillist;
	}

	@Path("/list")
	@GET
	public Response getAllMailingLists() {
		logger.info("Entering getAllMailingLists() method...");

		getMailingListLocal();
		List<jpa.model.MailingList> list = null;
		if (maillist != null) {
			list = maillist.getActiveLists();
		}
		else {
			list = mlistService.getAll(true);
		}
		logger.info("Number of lists: " + list.size());
		List<MailingListVo> volist = new ArrayList<MailingListVo>();
		for (jpa.model.MailingList ml : list) {
			MailingListVo vo = mailingListModelToVo(ml);
			volist.add(vo);
		}
		GenericEntity<List<MailingListVo>> entity = new GenericEntity<List<MailingListVo>>(volist) {};
		return Response.ok(entity).build();
	}
	
	/*
	 * Demonstrates getting path and query parameters from UriInfo.
	 * 
	 * path parameter for {keytype} = listId or address
	 * query parameter: value={listId or listAddress}
	 */
	@Path("/getBy/{keytype}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getByListIdOrAddress(@Context UriInfo uriInfo, @Context Request req, @Context HttpHeaders hh) {
		logger.info("Entering getByListIdOrAddress() method...");
		java.net.URI uri = uriInfo.getRequestUri();
		logger.info("URI: " + PrintUtil.prettyPrint(uri));
		
		// print out HTTP headers
		JaxrsUtil.printOutHttpHeaders(hh);

		// verify the request method
		if (req.getMethod().equals("GET")) {
			Response.ResponseBuilder rb = req.evaluatePreconditions();
			if (rb != null) {
                throw new WebApplicationException(rb.build());
            }
		}
		
		// retrieving path and query parameters
		MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String keyType = null;
		for (Iterator<String> it = pathParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Path Key/Values: " + key + " => " + pathParams.get(key));
			if (StringUtils.equals("keytype", key)) {
				keyType = StringUtils.join(pathParams.get(key), ",");
			}
		}
		String keyValue = null;
		for (Iterator<String> it = queryParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Query Key/Values: " + key + " => " + queryParams.get(key));
			if (StringUtils.equals("value", key)) {
				keyValue = queryParams.getFirst(key);
			}
		}
		if (StringUtils.isNoneBlank(keyType) && StringUtils.isNotBlank(keyValue)) {
			ResponseBuilder rb = new ResponseBuilderImpl();
			getMailingListLocal();
			jpa.model.MailingList ml;
			if (StringUtils.containsIgnoreCase(keyType, "id")) {
				if (maillist != null) {
					ml = maillist.getByListId(keyValue);
				}
				else {
					ml = mlistService.getByListId(keyValue);
				}
			}
			else {
				if (maillist != null) {
					ml = maillist.getByListAddress(keyValue);
				}
				else {
					ml = mlistService.getByListAddress(keyValue);
				}
			}
			if (ml != null) {
				rb.status(Status.OK);
				MailingListVo vo = mailingListModelToVo(ml);
				rb.entity(vo);
			}
			else {
				ErrorResponse er = new ErrorResponse();
				er.setHttpStatus(404);
				er.setErrorCode(104);
				er.setErrorMessage("Mailing List not found");
				rb.status(404); //Status.NOT_FOUND);
				rb.entity(er);
			}
			return rb.build();
		}
		else {
			return Response.noContent().build();
		}
	}
	
	/*
	 * Demonstrates update with XML pay-load, for example:
	 * 
		<MailingListVo>
		<rowId>1</rowId>
		<updtTime>2015-05-01T13:53:54-04:00</updtTime>
		<updtUserId>MsgMaint</updtUserId>
		<listId>SMPLLST1</listId>
		<listEmailAddr>demolist1@localhost</listEmailAddr>
		<displayName>Sample List 1</displayName>
		<description>Sample Mailing List 1</description>
		<isBuiltin>false</isBuiltin>
		<isSendText>true</isSendText>
		<createTime>2015-05-01T13:53:54-04:00</createTime>
		<listMasterEmailAddr>sitemaster@127.0.0.1</listMasterEmailAddr>
		</MailingListVo>
	 *
	 */
	@Path("/update/{listId}")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public Response updateList(@Context Request req, @PathParam("listId") String listId, MailingListVo vo) {
		logger.info("Entering updateList() method...  list to update: " + listId);
		logger.info("MailingList to upadte:" + PrintUtil.prettyPrint(vo));
		if (!req.getMethod().equals("POST")) {
			Response.noContent().build();
		}
		Response.ResponseBuilder rb = req.evaluatePreconditions();
		if (rb != null) {
			rb.build();
        }

		try {
			getMailingListLocal();
			jpa.model.MailingList ml;
			if (maillist != null) {
				ml = maillist.getByListId(listId);
			}
			else {
				ml = mlistService.getByListId(listId);
			}
			vo.setListId(listId); // make sure listId is not changed
			try {
				BeanUtils.copyProperties(ml, vo);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to copy properties", e);
			}
			//getMailingListLocal().update(ml);
			if (maillist != null) {
				maillist.update(ml);
			}
			else {
				mlistService.update(ml);
			}
			logger.info("MailingList updated, listId = " + ml.getListId());
			return Response.ok("Success").build();
		}
		catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			return Response.serverError().entity("Exception caught").build();
		}
	}

	@Path("/updateform")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED) //"application/x-www-form-urlencoded"
	public Response updateForm(MultivaluedMap<String, String> formParams, @FormParam("listId") String listId) {
		logger.info("Entering updateForm() method...");
		for (Iterator<String> it = formParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Form Key/Values: " + key + " => " + formParams.get(key));
		}
		try {
			getMailingListLocal();
			jpa.model.MailingList ml;
			if (maillist != null) {
				ml = maillist.getByListId(listId);
			}
			else {
				ml = mlistService.getByListId(listId);
			}
			if (ml == null) {
				return Response.noContent().build();
			}
			BeanReflUtil.copyProperties(ml, formParams);
			//getMailingListLocal().update(ml);
			if (maillist != null) {
				maillist.update(ml);
			}
			else {
				mlistService.update(ml);
			}
			logger.info("MailingList updated, listId = " + ml.getListId());
			return Response.ok("Success").build();
		}
		catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			return Response.serverError().entity("Exception caught").build();
		}
	}

	@Path("/uploadpart")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("multipart/mixed")
	public Map<String, Object> saveMultipart(MultipartBody multipartBody, @Context HttpHeaders hh) {
		logger.info("Entering saveMultipart() method..."); 
		JaxrsUtil.printOutHttpHeaders(hh);
		
		Map<String, Object> map = new LinkedHashMap<>();
		
		Attachment root = multipartBody.getRootAttachment();
		if (root != null) {
			logger.info("Root attachment content type/id: " + root.getContentType() + " / " + root.getContentId());
			JaxrsUtil.printOutMultivaluedMap(root.getHeaders());
			boolean isTextContent = StringUtils.contains(root.getContentType().toString(), "text");
			try {
				byte[] content = JaxrsUtil.getBytesFromDataHandler(root.getDataHandler());
				if (content != null && content.length > 0) {
					map.put(root.getContentType().toString(), content);
				}
				if (isTextContent) {
					logger.info("     Content: " + LF + new String(content));
				}
				else {
					logger.info("     Content name: " + root.getDataHandler().getName());
				}
			}
			catch (IOException e) {
				throw new WebApplicationException(e);
			}
		}

		for (Attachment attch : multipartBody.getAllAttachments()) {
			logger.info("Attachment content type/id: " + attch.getContentType() + " / " + attch.getContentId());
			JaxrsUtil.printOutMultivaluedMap(attch.getHeaders());
			String contentType = attch.getContentType().toString();
			boolean isTextContent = StringUtils.contains(contentType, "text");
			try {
				byte[] content = JaxrsUtil.getBytesFromDataHandler(attch.getDataHandler());
				if (content != null && content.length > 0) { // content is empty content Id is "root"
					map.put(contentType, content);
				}
				if (isTextContent) {
					logger.info("     Content: " + LF + new String(content));
				}
				else {
					logger.info("     Content name: " + attch.getDataHandler().getName());
				}
			}
			catch (IOException e) {
				throw new WebApplicationException(e);
			}
		}
		
		return map;
	}
	
	@Path("/uploadpart2")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("multipart/mixed")
	public Map<String, Object> saveMultipart2(MultipartBody multipartBody, @Context HttpHeaders hh,
			@Multipart(value="listId", required=false) String listId,
			@Multipart(value="fileUpload", required=false, type="text/*") String file) {
		// The attachment whose Id equals "fileUpload" can only be text content type.
		logger.info("Entering saveMultipart() method..., listId = " + listId); 
		JaxrsUtil.printOutHttpHeaders(hh);
		
		Map<String, Object> map = new LinkedHashMap<>();
		
		Attachment root = multipartBody.getRootAttachment();
		if (root != null) {
			logger.info("Root attachment content type/id: " + root.getContentType() + " / " + root.getContentId());
			JaxrsUtil.printOutMultivaluedMap(root.getHeaders());
			boolean isTextContent = StringUtils.contains(root.getContentType().toString(), "text");
			try {
				byte[] content = JaxrsUtil.getBytesFromDataHandler(root.getDataHandler());
				if (content != null && content.length > 0) {
					map.put(root.getContentType().toString(), content);
				}
				if (isTextContent) {
					logger.info("     Content: " + LF + new String(content));
				}
				else {
					logger.info("     Content name: " + root.getDataHandler().getName());
				}
			}
			catch (IOException e) {
				throw new WebApplicationException(e);
			}
		}

		for (Attachment attch : multipartBody.getAllAttachments()) {
			logger.info("Attachment content type/id: " + attch.getContentType() + " / " + attch.getContentId());
			JaxrsUtil.printOutMultivaluedMap(attch.getHeaders());
			String contentType = attch.getContentType().toString();
			boolean isTextContent = StringUtils.contains(contentType, "text");
			try {
				byte[] content = JaxrsUtil.getBytesFromDataHandler(attch.getDataHandler());
				if (content != null && content.length > 0) {
					map.put(contentType, content);
				}
				if (isTextContent) {
					logger.info("     Content: " + LF + new String(content));
				}
				else {
					logger.info("     Content name: " + attch.getDataHandler().getName());
				}
			}
			catch (IOException e) {
				throw new WebApplicationException(e);
			}
		}
		if (file != null) { // @Multipart annotation does not work with java.io.File type parameter
			logger.info("Attachment text file: " + LF + file);
		}
		
		return map;
	}


	@Path("/uploadpart3")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("multipart/mixed;type=text/xml")
	public Map<String, Object> saveMultipart3(@Context HttpHeaders hh,
			@Multipart(value = "root", required = true, type = "text/xml") String root,
			@Multipart(value = "fileUpload", required = false, type = "text/plain") String file) {

		logger.info("Entering saveMultipart() method..."); 
		JaxrsUtil.printOutHttpHeaders(hh);
		
		Map<String, Object> map = new LinkedHashMap<>();
		
		map.put("text/xml", root);
		logger.info("Root Content: " + LF + root);
		if (StringUtils.isNotBlank(file)) {
			map.put("text/plain", file);
			logger.info("fileUpload Content: " + LF + file);
		}
		
		return map;
	}

	@Path("/uploadfile")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("multipart/mixed")
	public MultipartBody saveAttachments(List<Attachment> attachments, @Context HttpHeaders hh) {
		logger.info("Entering  saveAttachments() method...");
		JaxrsUtil.printOutHttpHeaders(hh);
		List<Attachment> atts = new LinkedList<Attachment>();
		for (Attachment attch : attachments) {
			logger.info("Attachment content type/id: " + attch.getContentType() + " / " + attch.getContentId());
			String contentType = attch.getContentType().toString();
			try {
				byte[] content = JaxrsUtil.getBytesFromDataHandler(attch.getDataHandler());
				if (content != null && content.length > 0) {
					atts.add(new Attachment(attch.getContentId(), contentType, content));
				}
			}
			catch (IOException e) {
				throw new WebApplicationException(e);
			}
		}
		return new MultipartBody(atts, true);
	}

	@Path("/uploadfile2")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("multipart/mixed;type=text/xml")
	public MultipartBody saveAttachments2(List<Attachment> attachments, @Context HttpHeaders hh,
			@Multipart(value="fileUpload", required=false, type="text/xml") String file) {
		logger.info("Entering  saveAttachments() method..., fileUpload = " + LF + file);
		JaxrsUtil.printOutHttpHeaders(hh);
		List<Attachment> atts = new LinkedList<Attachment>();
		for (Attachment attch : attachments) {
			logger.info("Attachment content type/id: " + attch.getContentType() + " / " + attch.getContentId());
			String contentType = attch.getContentType().toString();
			boolean isTextContent = StringUtils.contains(contentType, "text");
			try {
				byte[] content = JaxrsUtil.getBytesFromDataHandler(attch.getDataHandler());
				if (content != null && content.length > 0) {
					atts.add(new Attachment(attch.getContentId(), contentType, content));
				}
				if (isTextContent) {
					logger.info("     Content: " + LF + new String(content));
				}
				else {
					logger.info("     Content name: " + attch.getDataHandler().getName());
				}
			}
			catch (IOException e) {
				throw new WebApplicationException(e);
			}
		}
		return new MultipartBody(atts, true);
	}

	@Path("/uploadmime")
	@POST
	@Consumes("multipart/related")
	public String saveMimeMessage(MimeMultipart mimeMultipartData) {
		// TODO implement this
		return null;
	}

	MailingListVo mailingListModelToVo(jpa.model.MailingList ml) {
		MailingListVo vo = new MailingListVo();
		try {
			BeanUtils.copyProperties(vo, ml);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to copy properties", e);
		}
		String listaddr = ml.getAcctUserName() + "@" + ml.getSenderData().getDomainName();
		vo.setListEmailAddr(listaddr);
		return vo;
	}
}
