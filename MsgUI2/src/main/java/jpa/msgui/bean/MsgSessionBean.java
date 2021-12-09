package jpa.msgui.bean;

import java.util.Enumeration;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jpa.msgui.util.FacesUtil;

@javax.inject.Named("sessionBean")
@javax.enterprise.context.SessionScoped //ViewScoped
public class MsgSessionBean implements java.io.Serializable {
	private static final long serialVersionUID = -5665449341441493983L;
	static final Logger logger = LogManager.getLogger(MsgSessionBean.class);

	@javax.inject.Inject // <-- WORKS
    protected HttpSession httpSession;
	
	/*
	 * One case where this would still fail is when you explicitly perform a forward within the same request using 
	 * RequestDispatcher#forward() or ExternalContext#dispatch(). You will then face java.lang.IllegalStateException
	 * at com.sun.faces.context.FacesContextImpl.assertNotReleased.
	 */
	@javax.enterprise.inject.Produces
	@javax.enterprise.context.RequestScoped
	public FacesContext getFacesContext(){
	    return FacesContext.getCurrentInstance();
	}

	/*
	 * XXX  Experimental - There is no way to get HTTP request object from HttpSession!
	 */
	public HttpSession getHttpSession() {
		if (httpSession != null) {
			httpSession.getServletContext();
			Enumeration<String> enu = httpSession.getAttributeNames();
			while (enu.hasMoreElements()) {
				String attrName = enu.nextElement();
				logger.info("attribute name = " + attrName);
			}
		}
		else {
			logger.error("HttpSession Injection Failed!!!");
		}
		return httpSession;
	}

	public void setHttpSession(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	public Object getSessionParam(String paramKey) {
        Object obj = FacesUtil.getSessionMapValue(paramKey);
        //Object obj = getFacesContext().getExternalContext().getSessionMap().get(paramKey);
        return obj;
    }
	
	public String getRequestParam(String paramKey) {
		getHttpSession();
        String value = FacesUtil.getRequestParameter(paramKey);
        //String value =  getFacesContext().getExternalContext().getRequestParameterMap().get(paramKey);
        return value;
    }
	
	public Object getManagedBean(String beanName) {
		Object obj = FacesUtil.getManagedBean(beanName);
		return obj;
	}
	
	public void setSessionParam(String paramKey, String paramValue) {
		FacesUtil.setSessionMapValue(paramKey, paramValue);
	}
}
