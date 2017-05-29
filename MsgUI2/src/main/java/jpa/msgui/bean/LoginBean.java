package jpa.msgui.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import jpa.constant.Constants;
import jpa.model.UserData;
import jpa.msgui.filter.SessionTimeoutFilter;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.common.UserDataService;
import jpa.util.SenderUtil;

import org.apache.log4j.Logger;

@ManagedBean(name="login")
//@javax.inject.Named("login")
@RequestScoped
public class LoginBean implements java.io.Serializable {
	private static final long serialVersionUID = -5547672142490601294L;
	static final Logger logger = Logger.getLogger(LoginBean.class);
	private String userId = null;
	private String password = null;
	private String message = null;
	
	@ManagedProperty(value="#{param.source}")
	private String source = null; // login or timeout
	
	@ManagedProperty("#{facesContext}")
	private FacesContext context;
	
	private transient UserDataService userDao = null;
	
	public String login() {
		logger.info("login() - UserId: " +  userId);
		message = null;
		UserData vo = getUserDataService().getForLogin(userId, password);
		if (vo == null) {
			message = "Unknown UserId and/or invalid password!";
			return null;
		}
		if (isUserLoggedin()) {
			logout();
		}
		vo.setPassword(null); // for security
		setSessionUserData(vo);
		logger.info("login() - user logged in: " + userId);
		if (Constants.ADMIN_ROLE.equals(vo.getRole())) {
			return Constants.ADMIN_ROLE;
		}
		else {
			return Constants.USER_ROLE;
		}
	}
	
    public String logout() {
    	getHttpSession().invalidate();
    	// invalidate() will trigger SessionExpiredListener.sessionDestroyed()
		// method to perform clean up.
		return "login";
	}
    
    public String changePassword() {
    	return null;
    }
    
    // Getters
    public UserData getSessionUserData() {
        return (UserData) getHttpSession().getAttribute(SessionTimeoutFilter.USER_DATA_ID);
    }

    public void setSessionUserData(UserData userVo) {
		getHttpSession().setAttribute(SessionTimeoutFilter.USER_DATA_ID, userVo);
    }
    
    public HttpSession getHttpSession() {
    	ExternalContext ctx = context.getExternalContext(); //FacesContext.getCurrentInstance().getExternalContext();
    	return ((HttpSession) ctx.getSession(true));
    }
    
    // Checkers
    public boolean isUserLoggedin() {
    	return (getSessionUserData() != null);
    }

    public boolean getIsAdmin() {
    	if (getSessionUserData() == null) {
    		return false;
    	}
    	else {
    		return getSessionUserData().getIsAdmin();
    	}
    }
    
    public boolean isCurrentPageMainPage() {
    	String viewId = FacesUtil.getCurrentViewId();
    	return ("/main.xhtml".equals(viewId) || "/main.faces".equals(viewId));
    }
    
	public boolean getIsProductKeyValid() {
		boolean isValid = SenderUtil.isProductKeyValid();
		return isValid;
	}
	
	public boolean getIsTrialPeriodExpired() {
		return SenderUtil.isTrialPeriodEnded();
	}

    public String getMainPage() {
    	return getHttpSession().getServletContext().getContextPath() + "/main.xhtml";
    }
    
	public FacesContext getContext() {
		return context;
	}

	public void setContext(FacesContext context) {
		this.context = context;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public UserDataService getUserDataService() {
		if (userDao == null) {
			userDao = SpringUtil.getWebAppContext().getBean(UserDataService.class);
		}
		return userDao;
	}

	public void setUserDataService(UserDataService userDao) {
		this.userDao = userDao;
	}
}
