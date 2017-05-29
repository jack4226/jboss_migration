package jpa.msgui.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jpa.constant.Constants;
import jpa.model.UserData;
import jpa.util.StringUtil;

import org.apache.log4j.Logger;

/**
 * The Session Timeout filter.
 */
@WebFilter(filterName="SessionTimeoutFilter", urlPatterns="/*")
public class SessionTimeoutFilter implements Filter {
	static final Logger logger = Logger.getLogger(SessionTimeoutFilter.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	private String timeoutPage = "/login.xhtml";
	private String loginPage = "/login.xhtml";
	private String noPermissionPage = "/noPermission.faces";
	/** The unique ID to set and get the UserData from the HttpSession. */
	public static final String USER_DATA_ID = "SessionTimeoutFilter.UserData";

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) {
		// Nothing to do here.
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		String pathInfo = StringUtil
				.trim(httpRequest.getRequestURI(), httpRequest.getContextPath());
		// Check PathInfo
		if (isPageSessioned(httpRequest, pathInfo) == false) {
			chain.doFilter(request, response);
			return;
		}
		// Get UserData from HttpSession
		HttpSession httpSession = httpRequest.getSession();
		UserData userVo = (UserData) httpSession.getAttribute(USER_DATA_ID);
		if (userVo == null || isSessionInvalid(httpRequest)) {
			// No UserData found in HttpSession
			String timeoutUrl = httpRequest.getContextPath() + timeoutPage + "?source=timeout";
			logger.info("doFilter() - session is invalid! redirecting to: " + timeoutUrl);
			httpResponse.sendRedirect(timeoutUrl);
			//FacesContext.getCurrentInstance().getExternalContext().redirect(timeoutUrl);
			return;
		}
		if (!isPagePermitted(userVo, pathInfo)) {
			String noPermissionUrl = httpRequest.getContextPath() + noPermissionPage;
			httpResponse.sendRedirect(noPermissionUrl);
			return;
		}
		// Add hit count and update UserData
		userVo.addHit();
		// Continue filtering
		try { // we've been getting random IndexOutOfBoundsException
			// did not work
			chain.doFilter(request, response);
		}
		catch (IndexOutOfBoundsException e) {
			logger.error("IndexOutOfBoundsException caught", e);
		}
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// Apparently there's nothing to destroy?
	}
	
	/**
	 * Session shouldn't be checked for certain pages. For example for timeout
	 * page. Since we're redirecting to timeout page from this filter, if we
	 * don't disable session control for it, filter will again redirect to it
	 * and this will be result with an infinite loop.
	 */
	private boolean isPageSessioned(HttpServletRequest httpRequest, String pathInfo) {
		boolean pageSessioned = false;
		if (pathInfo.startsWith("/includes") || pathInfo.startsWith("/images")
				|| pathInfo.startsWith("/htmls")) {
			// This is not necessary, but it might be useful if you want to skip
			// some include files. If those include files are loaded, continue 
			// the filter chain and abort this filter, because it is usually not
			// necessary to lookup for any UserData then. Or, if the url-pattern 
			// in the web.xml is specific enough, this if-block can be removed.
			pageSessioned = false;
		}
		if (pathInfo.matches("^(?:/[a-zA-Z]{5,8})?/[a-zA-Z]\\w{0,250}\\.(?:faces|xhtml|jsf).*")) {
			// file name matches "/*.faces" or "/*.xhtml" or "/*.jsf"
			if (!pathInfo.startsWith(loginPage) && !pathInfo.startsWith(timeoutPage)
					&& !pathInfo.startsWith(noPermissionPage)
					&& !pathInfo.startsWith("/publicsite")
					&& !pathInfo.startsWith("/javax.faces.resource")) {
				// not login page and timeout page
				pageSessioned = true;
			}
		}
		if (isDebugEnabled) {
			logger.debug("isPageSessioned() - pathInfo: " + pathInfo + ", " + pageSessioned);
		}
		return pageSessioned;
	}
	
	boolean isPagePermitted(UserData userVo, String pathInfo) {
		//logger.info("isPagePermitted() - pathInfo: " + pathInfo);
		if (userVo == null) {
			return false;
		}
		if (pathInfo.startsWith("/admin/") && !Constants.ADMIN_ROLE.equals(userVo.getRole())) {
			return false;
		}
		return true;
	}
	
	private boolean isSessionInvalid(HttpServletRequest httpRequest) {
		boolean sessionInValid = (httpRequest.getRequestedSessionId() != null)
				&& !httpRequest.isRequestedSessionIdValid();
		return sessionInValid;
	}
}
