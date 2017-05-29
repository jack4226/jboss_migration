package jpa.msgui.util;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringUtil implements java.io.Serializable {
	private static final long serialVersionUID = 6759672751571503132L;
	static WebApplicationContext webContext = null;
	
	/**
	 * get WebApplicationContext, calling from a JSF managed bean.
	 * 
	 * @return a WebApplicationContext reference
	 */
	public static WebApplicationContext getWebAppContext() {
		if (webContext == null) {
			FacesContext facesCtx = FacesContext.getCurrentInstance();
			ServletContext sctx = (ServletContext) facesCtx.getExternalContext().getContext();
			webContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sctx);
		}
		return webContext;
	}
	
	/**
	 * get WebApplicationContext, calling from a Servlet
	 * 
	 * @param sctx -
	 *            a servlet context
	 * @return a WebApplicationContext reference
	 */
	public static WebApplicationContext getWebAppContext(ServletContext sctx) {
		WebApplicationContext webContext = WebApplicationContextUtils
				.getRequiredWebApplicationContext(sctx);
		return webContext;
	}
}
