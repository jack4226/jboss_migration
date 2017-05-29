package jpa.msgui.listener;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import jpa.model.UserData;
import jpa.msgui.filter.SessionTimeoutFilter;
import jpa.msgui.util.SpringUtil;
import jpa.service.common.SessionUploadService;
import jpa.service.common.UserDataService;

import org.apache.log4j.Logger;

/**
 * When a user session times out, the sessionDestroyed() method will be invoked.
 * This method will make necessary cleanups (logging out user, updating database
 * and audit logs, etc...). After this method, we will be in a clean and stable
 * state.
 */
@WebListener
public class SessionExpiredListener implements HttpSessionListener {
	static final Logger logger = Logger.getLogger(SessionExpiredListener.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	public SessionExpiredListener() {
	}

	public void sessionCreated(HttpSessionEvent event) {
		if (isDebugEnabled) {
			logger.debug("sessionCreated() - " + event.getSession().getId());
		}
	}

	public void sessionDestroyed(HttpSessionEvent event) {
		// get the session to be destroyed...
		HttpSession session = event.getSession();
		if (isDebugEnabled) {
			logger.debug("sessionDestroyed() - " + session.getId() + " Logging out user...");
		}
		/*
		 * nobody can reach user data after this point because session is
		 * invalidated already. So, get the user data from session and save
		 * its logout information before losing it. User's redirection to the
		 * timeout page will be handled by the SessionTimeoutFilter.
		 */
		try {
			sessionExpired(session);
		}
		catch (Exception e) {
			logger.error("error while logging out at session destroyed", e);
		}
	}

	/**
	 * Gets the logged in user data from userVo and makes necessary logout
	 * operations.
	 */
	public static void sessionExpired(HttpSession httpSession) {
		// update users table with "hits" and "last access time"
		UserData userVo = (UserData) httpSession.getAttribute(SessionTimeoutFilter.USER_DATA_ID);
		if (userVo != null) {
			getUserDataservice(httpSession).update4Web(userVo);
			logger.info("sessionExpired() - UserData table - rows updated: " + 1);
		}
		else {
			logger.warn("sessionExpired() - UserData is null, user info not updated.");
		}
		
		//jsessionid=1A706557C46AB7A909464548A0622EEF
		String sessionId = httpSession.getId(); // get SessionId
		logger.info("sessionExpired() - sessionId: " + sessionId);
		// clean up user session tables
		if (sessionId != null) {
			int rowsDeleted = getSessionUploadService(httpSession).deleteBySessionId(sessionId);
			logger.info("sessionExpired() - rows deleted from SessionUpload: " + rowsDeleted);
		}
		else {
			logger.warn("sessionExpired() - SessionId is null, SessionUpload not cleaned.");
		}
	}

	private static UserDataService getUserDataservice(HttpSession httpSession) {
		ServletContext ctx = httpSession.getServletContext();
		return (UserDataService) SpringUtil.getWebAppContext(ctx).getBean("userDataService");
	}

	private static SessionUploadService getSessionUploadService(HttpSession httpSession) {
		ServletContext ctx = httpSession.getServletContext();
		return (SessionUploadService) SpringUtil.getWebAppContext(ctx).getBean("sessionUploadService");
	}
}
