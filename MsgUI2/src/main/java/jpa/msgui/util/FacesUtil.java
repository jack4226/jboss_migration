package jpa.msgui.util;

import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.event.ActionEvent;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jpa.model.UserData;
import jpa.msgui.filter.SessionTimeoutFilter;

public class FacesUtil implements java.io.Serializable {
	private static final long serialVersionUID = -2074968051875861770L;

	/**
	 * redirect to a new jsp file. For example: 
	 * <code>
	 * 	FacesUtil.redirect("faces/some_page.jsp");
	 * </code>
	 * @param address
	 * @throws IOException
	 */
	public static void redirect(String address) throws IOException {
	/*
	JSF navigation has 2 modes:
	1) Forward
	2) Redirect
	
	The forward solution is not good. Mostly because:
	The URL is not updated on the browser, causing compatibility problems with
	service	providers. No book-marking available. No possibility to send URL to
	someone by email and all sort of side effect problems.

	The redirect solution is also problematic, since redirecting to a new page is a
	new request. This causes the data that is put in the request map to disappear.

	This is the reason why we should give up using JSF navigation mechanism and
	use our own "redirect" and query string parameters to navigate between pages. 
	*/
		FacesContext.getCurrentInstance().getExternalContext().redirect(address);
	}
	
	/**
	 * Accessing the FacesContext inside HttpServlet or Filter.
	 * 
	 * Servlets and Filters outside a FacesServlet can't access FacesContext
	 * directly. When you want to retrieve a FacesContext instance outside a
	 * FacesServlet, the FacesContext.getCurrentInstance() method returns null.
	 * In this case you need to create the FacesContext yourself.
	 */
	public static FacesContext getFacesContext(HttpServletRequest request, HttpServletResponse response) {
		// Get current FacesContext
		FacesContext facesContext = FacesContext.getCurrentInstance();
		// Check the returns
		if (facesContext == null) {
			// Get a Life-cycle factory
			LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
					.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
			Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
			// Get a FacesContext factory
			FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder
					.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
			// Create a new FacesContext
			facesContext = contextFactory.getFacesContext(request.getSession().getServletContext(),
					request, response, lifecycle);
			// Create a new View
			UIViewRoot view = facesContext.getApplication().getViewHandler().createView(
					facesContext, "");
			facesContext.setViewRoot(view);
			// Set current FacesContext instance
			FacesContextWrapper.setCurrentInstance(facesContext);
		}
		return facesContext;
	}
	
	// Wrap the protected FacesContext.setCurrentInstance() in a inner class.
	private static abstract class FacesContextWrapper extends FacesContext {
		protected static void setCurrentInstance(FacesContext facesContext) {
			FacesContext.setCurrentInstance(facesContext);
		}
	}

	public static void refreshCurrentJSFPage() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			String viewId = context.getViewRoot().getViewId();
			ViewHandler handler = context.getApplication().getViewHandler();
			UIViewRoot root = handler.createView(context, viewId);
			root.setViewId(viewId);
			context.setViewRoot(root);
		}
	}
	
	public static String getCurrentViewId() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			String viewId = context.getViewRoot().getViewId();
			return viewId;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getManagedBean(String beanName) {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			return (T) context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
		}
		return null;
	}
	
	/**
	 * get a Face's actionListener attribute. An attribute could be defined in following
	 * commandLink: <code>
	 *  <h:commandLink actionListener="#{myBean.action}">
	 *    <f:attribute name="attrname1" value="attrvalue1" />
	 *    ...
	 *	</h:commandLink>
	 * </code>
	 * 
	 * @param event -
	 *            Action event
	 * @param name -
	 *            attribute name
	 * @return attribute value
	 */
    public static String getActionAttribute(ActionEvent event, String name) {
    	if (event != null && event.getComponent() != null) {
    		return (String) event.getComponent().getAttributes().get(name);
    	}
    	return null;
    }
	
    /**
	 * get a HTTP request parameter value. An HTTP request parameter could be
	 * defined in following commandLink: <code>
	 * <h:commandLink action="#{myBean.action}">
	 *   <f:param name="paramname1" value="paramvalue1" />
	 *   ...
	 * </h:commandLink>
	 * </code>
	 * 
	 * @param name -
	 *            request parameter name
	 * @return request parameter value
	 */
    public static String getRequestParameter(String name) {
    	FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			return (String) context.getExternalContext().getRequestParameterMap().get(name);
		}
		return null;
	}

	/**
	 * get a HTTP request-scoped object.
	 * @param key -
	 *            object key
	 * @return object value
	 */
    public static Object getRequestMapValue(String key) {
    	FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			return context.getExternalContext().getRequestMap().get(key);
		}
		return null;
    }

    /**
	 * set a HTTP request-scoped object
	 * 
	 * @param key -
	 *            object key
	 * @param value -
	 *            object value
	 */
    public static void setRequestMapValue(String key, Object value) {
    	FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			context.getExternalContext().getRequestMap().put(key, value);
		}
    }

    /**
     * get a HTTP session-scoped object 
     * @param key - session key
     * @return session object
     */
    public static Object getSessionMapValue(String key) {
    	FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
    		return context.getExternalContext().getSessionMap().get(key);
    	}
    	else {
    		return null;
    	}
    }

    /**
     * set a HTTP session-scoped object
     * @param key - session key
     * @param value - session value
     */
    public static void setSessionMapValue(String key, Object value) {
    	FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			context.getExternalContext().getSessionMap().put(key, value);
		}
    }

    /**
     * get a HTTP application-scoped object
     * @param key - object key
     * @return object value
     */
    public static Object getApplicationMapValue(String key) { // broken, revisit
    	FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			ExternalContext ext_ctx = context.getExternalContext();
			java.util.Map<String, Object> appMap = ext_ctx.getApplicationMap();
			Object obj = appMap.get(key);
			return obj;
		}
		return null;
    }

    /**
     * set a HTTP application-scoped object
     * @param key - object key
     * @param value - object value
     */
    public static void setApplicationMapValue(String key, Object value) {
    	FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
			context.getExternalContext().getApplicationMap().put(key, value);
		}
    }
    
    /**
     * get a HTTP Servlet request
     * @return request value
     */ 
    public static HttpServletRequest getHttpServletRequest() {
    	FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
	    	Object req = context.getExternalContext().getRequest();
	    	if (req instanceof HttpServletRequest) {
	    		return (HttpServletRequest) req;
	    	}
		}
     	return null;
    }
    
    /**
     * get a Servlet request
     * @return request value
     */
    public static ServletRequest getServletRequest() {
    	FacesContext context = FacesContext.getCurrentInstance();
		if (context != null) {
	    	Object req = context.getExternalContext().getRequest();
	    	if (req instanceof ServletRequest) {
	    		return (ServletRequest) req;
	    	}
		}
    	return null;
    }
    
    /**
     * get a HTTP Servlet request Session Id
     * @return session id
     */
    public static String getSessionId() {
		String sessionId = null;
		HttpServletRequest httpRequest = getHttpServletRequest();
		if (httpRequest != null) {
			sessionId = httpRequest.getRequestedSessionId();
		}
		return sessionId;
    }

    /**
     * Check the request session id is valid
     * @return true if valid
     */
    public static boolean isSessionIdValid() {
    	boolean sessionIdValid = false;
		HttpServletRequest httpRequest = getHttpServletRequest();
		if (httpRequest != null) {
			sessionIdValid = httpRequest.isRequestedSessionIdValid();
		}
    	return sessionIdValid;
    }
    
    /**
     * get login UserVo from current HTTP session
     * @return a UserVo
     */
	public static UserData getLoginUserData() {
		UserData userVo = (UserData) getSessionMapValue(SessionTimeoutFilter.USER_DATA_ID);
		return userVo;
	}
	
    /**
     * get login user id from current HTTP session
     * @return a UserId
     */
	public static String getLoginUserId() {
		UserData userVo = getLoginUserData();
		if (userVo != null) {
			return userVo.getUserId();
		}
		else {
			return null;
		}
	}
	
    /**
     * get login user client id from current HTTP session
     * @return a ClientId
     */
	public static String getLoginUserSenderId() {
		UserData userVo = getLoginUserData();
		if (userVo != null) {
			return userVo.getSenderData().getSenderId();
		}
		else {
			return null;
		}
	}
}
