package com.es.ejb.subscriber;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import jpa.model.SubscriberData;
import jpa.model.Subscription;
import jpa.service.common.SubscriberDataService;
import jpa.service.common.SubscriptionService;
import jpa.spring.util.SpringUtil;
import jpa.util.BeanCopyUtil;
import jpa.util.ExceptionUtil;
import jpa.util.PrintUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;
import org.apache.log4j.Logger;

import com.es.ejb.ws.vo.SubscriptionVo;
import com.es.jaxrs.common.ErrorResponse;
import com.es.tomee.util.BeanReflUtil;
import jpa.tomee.util.TomeeCtxUtil;

@Path("/msgapi/subscription")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class SubscriptionRS {
	static final Logger logger = Logger.getLogger(SubscriptionRS.class);

	@javax.ejb.EJB
	private SubscriberLocal subscriber;
	
	private SubscriberDataService subscriberDao;
	private SubscriptionService subscriptionDao;
	
	public SubscriptionRS() {
		BeanCopyUtil.registerBeanUtilsConverters();
	}
	
	SubscriberLocal getSubscriberLocal() {
		if (subscriber == null) {
			try {
				javax.naming.Context context = TomeeCtxUtil.getLocalContext();
				subscriber = (SubscriberLocal) context.lookup("SubscriberLocal");
			}
			catch (NamingException e) {
				// fall back services used by unit testing
				subscriberDao = SpringUtil.getAppContext().getBean(SubscriberDataService.class);
				subscriptionDao = SpringUtil.getAppContext().getBean(SubscriptionService.class);
			}
		}
		return subscriber;
	}
	
	@Path("/getSubscriber")
	@GET
	public Response getSubscriberAsXmlOrJson(@QueryParam("emailAddr") String emailAddr) {
		logger.info("Entering getSubscriberAsXmlOrJson() method..."); 
		ResponseBuilder rb = new ResponseBuilderImpl();
		try {
			getSubscriberLocal();
			SubscriberData sd = null;
			if (subscriber != null) {
				sd = subscriber.getSubscriberByEmailAddress(emailAddr);
			}
			else {
				sd = subscriberDao.getByEmailAddress(emailAddr);
			}
			if (sd != null) {
				rb.status(Status.OK);
				rb.entity(sd);
			}
			else {
				ErrorResponse er = new ErrorResponse();
				er.setHttpStatus(404);
				er.setErrorCode(102);
				er.setErrorMessage("Subscriber not found");
				rb.status(404); //Status.NOT_FOUND);
				rb.entity(er);
			}
			return rb.build();
		}
		catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			rb.status(500); //Status.INTERNAL_SERVER_ERROR);
			rb.entity("Exception caught: " + e.getMessage());
			return rb.build();
		}
	}

	@Path("/subscribe/{emailAddr}")
    @PUT
	public Response subscribe(@PathParam("emailAddr") String emailAddr, String listId) {
		logger.info("Entering subscriber() method..."); 
		ResponseBuilder rb = new ResponseBuilderImpl();
		try {
			getSubscriberLocal();
			Subscription sub = null;
			if (subscriber != null) {
				sub = subscriber.subscribe(emailAddr, listId);
			}
			else {
				sub = subscriptionDao.subscribe(emailAddr, listId);
			}
			rb.status(Status.OK);
			rb.entity(sub);
		}
		catch (Exception e) {
			Exception cause = ExceptionUtil.findRootCause(e);
			if (cause instanceof IllegalArgumentException) {
				logger.error("IllegalArgumentException caught: " + e.getMessage());
				ErrorResponse er = new ErrorResponse();
				er.setHttpStatus(404);
				er.setErrorCode(122);
				er.setErrorMessage(cause.getMessage());
				rb.status(404);
				rb.entity(er);
			}
			else {
				logger.error("Unchecked exception caught", e);
				return Response.serverError().build();
			}
		}
		return rb.build();
	}

	@Path("/unsubscribe/{emailAddr}")
	@PUT
	public Response unSubscribe(@PathParam("emailAddr") String emailAddr, String listId) {
		logger.info("Entering unSubscriber() method..."); 
		ResponseBuilder rb = new ResponseBuilderImpl();
		try {
			Subscription sub = getSubscriberLocal().unSubscribe(emailAddr, listId);
			rb.status(Status.OK);
			rb.entity(sub);
		}
		catch (Exception e) {
			Exception cause = ExceptionUtil.findRootCause(e);
			if (cause instanceof IllegalArgumentException) {
				logger.error("IllegalArgumentException caught: " + e.getMessage());
				ErrorResponse er = new ErrorResponse();
				er.setHttpStatus(404);
				er.setErrorCode(122);
				er.setErrorMessage(cause.getMessage());
				rb.status(404);
				rb.entity(er);
			}
			else {
				logger.error("Unchecked exception caught", e);
				return Response.serverError().build();
			}
		}
		return rb.build();
	}

	@Path("/addtolist/{sbsrEmailAddr}")
    @PUT
	public Subscription addToList(@PathParam("sbsrEmailAddr") String sbsrEmailAddr, String listEmailAddr) {
		logger.info("Entering addToList() method..."); 
		try {
			Subscription sub = getSubscriberLocal().addToList(sbsrEmailAddr, listEmailAddr);
			return sub;
		}
		catch (Exception e) {
			Exception cause = ExceptionUtil.findRootCause(e);
			if (cause instanceof IllegalArgumentException) {
				throw new WebApplicationException(Response.ok(cause.getMessage()).build());
			}
			throw new WebApplicationException(Response.serverError().build());
		}
	}

	@Path("/removefromlist/{sbsrEmailAddr}")
	@PUT
	public Subscription removeFromList(@PathParam("sbsrEmailAddr") String sbsrEmailAddr, String listEmailAddr) {
		logger.info("Entering removeFromList() method..."); 
		try {
			Subscription sub = getSubscriberLocal().removeFromList(sbsrEmailAddr, listEmailAddr);
			return sub;
		}
		catch (Exception e) {
			Exception cause = ExceptionUtil.findRootCause(e);
			if (cause instanceof IllegalArgumentException) {
				throw new WebApplicationException(Response.ok(cause.getMessage()).build());
			}
			throw new WebApplicationException(Response.serverError().build());
		}
	}
	

	@Path("/subscribedlist")
	@GET
	public Response getSubscriberByEmailAddress(@QueryParam("emailAddr") String emailAddr, @Context HttpHeaders hh) {
		logger.info("Entering getSubscriberByEmailAddress() method..."); 
		try {
			List<SubscriptionVo> sublist = getSubscriberLocal().getSubscribedList(emailAddr);
			if (!sublist.isEmpty()) {
				logger.info(PrintUtil.prettyPrint(sublist.get(0), 1));
				GenericEntity<List<SubscriptionVo>> entity = new GenericEntity<List<SubscriptionVo>>(sublist) {};
				return Response.ok(entity).build();
			}
			else {
				boolean acceptJson = false;
				MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
				if (headerParams.containsKey("Accept")) {
					if (StringUtils.containsIgnoreCase(headerParams.getFirst("Accept"), "/json")) {
						acceptJson = true;
					}
				}
				String rsp;
				if (acceptJson) {
					rsp = "{\"subscriptions\": \"not found\"}";
				}
				else {
					rsp = "<subscriptions>Subscriber not found</subscriptions>";
				}
				return Response.ok(rsp).build();
			}
		}
		catch (Exception e) {
			logger.error("NamingException caught: " + e.getMessage());
			// produce HTTP 500 Internal Server Error
			return Response.serverError().entity("NamingException caught").build();
		}
	}
	
	@Path("/subscriber/{emailAddr}")
	@GET
	@Produces("application/xml")
	public SubscriberData getSubscriberByEmailAddressAsXml(@PathParam("emailAddr") String emailAddr) {
		logger.info("Entering getSubscriberByEmailAddressAsXml() method..."); 
		try {
			getSubscriberLocal();
			SubscriberData sd = null;
			if (subscriber != null) {
				sd = subscriber.getSubscriberByEmailAddress(emailAddr);
			}
			else {
				sd = subscriberDao.getByEmailAddress(emailAddr);
			}
			if (sd != null) {
				logger.info(PrintUtil.prettyPrint(sd,1));
				return sd;
			}
			else {
				ResponseBuilder rb = new ResponseBuilderImpl();
				ErrorResponse er = new ErrorResponse();
				er.setHttpStatus(404);
				er.setErrorCode(102);
				er.setErrorMessage("Subscriber not found");
				rb.status(404);
				rb.entity(er);
				throw new WebApplicationException(rb.build());
			}
		}
		catch (Exception e) {
			throw new WebApplicationException(Response.serverError().entity("NamingException caught").build());
		}
	}

	@Path("/update/{emailAddr}")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response update(@Context UriInfo ui, @Context HttpHeaders hh, MultivaluedMap<String, String> formParams,
			@PathParam("emailAddr") String emailAddr) {
		logger.info("Entering update() method..."); 
		// print out UriInfo
		MultivaluedMap<String, String> pathParams = ui.getPathParameters();
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		for (Iterator<String> it = pathParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Path Key: " + key + ", Values: " + pathParams.get(key));
		}
		for (Iterator<String> it = queryParams.keySet().iterator(); it
				.hasNext();) {
			String key = it.next();
			logger.info("Query Key: " + key + ", Values: " + queryParams.get(key));
		}
		
		// print out headers and cookies
		MultivaluedMap<String, String> headerParams = hh.getRequestHeaders();
		for (Iterator<String> it = headerParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Header Key: " + key + ", Values: " + headerParams.get(key));
		}
		Map<String, Cookie> cookieParams = hh.getCookies();
		for (Iterator<String> it = cookieParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Cookie Key: " + key + ", Values: " + cookieParams.get(key));
		}

		// print out Form Parameters
		for (Iterator<String> it = formParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			logger.info("Form Key: " + key + ", Values: " + formParams.get(key));
		}
		try {
			SubscriberData sbsrdata = getSubscriberLocal().getSubscriberByEmailAddress(emailAddr);
			if (sbsrdata != null) {
				BeanReflUtil.copyProperties(sbsrdata, formParams);
				getSubscriberLocal().updateSubscriber(sbsrdata);
				return Response.ok("Success").build();
			}
			else {
				ResponseBuilder rb = new ResponseBuilderImpl();
				rb.status(404);
				rb.entity("Subscriber not found");
				return rb.build();
			}
		}
		catch (Exception e) {
			logger.error("NamingException caught: " + e.getMessage());
			return Response.serverError().entity("NamingException caught").build();
		}
	}
}
