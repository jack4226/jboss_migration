package jpa.msgui.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@javax.inject.Named("gettingStarted")
@javax.enterprise.context.SessionScoped
public class GettingStarted implements java.io.Serializable {
	private static final long serialVersionUID = -7733276722871469541L;
	static final Logger logger = LogManager.getLogger(GettingStarted.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Inject
    protected MsgSessionBean sessionBean;

	//@javax.inject.Inject // XXX tomee failed to start
	@javax.faces.annotation.ManagedProperty("#{param.titleKey}") // XXX Did not work
	private String titleKey;
	
	//@javax.inject.Inject  // XXX tomee failed to start
	@javax.faces.annotation.ManagedProperty(value = "#{jpa.msgui.messages}") // XXX Did not work
	private transient ResourceBundle bundle;
	
	private transient DataModel<?> functionKeys = null;
	private String functionKey = null;
	private String jspPageLink = null;
	
	/* values must be defined in resource bundle - messages.properties */
	private String[] menuTooltips = { "configureMailboxes", "configureSmtpServers",
			"configureSiteProfiles", "customizeBuiltinRules", "configureCustomRules",
			"maintainActionDetails", "configureMailingLists", "configureEmailVariables",
			"configureEmailTemplates", "manageUserAccounts" };

	private String[] navigationKeys = { "mailbox.list", "smtpserver.list", "siteprofile.list",
			"builtinrule.list", "msgrule.list", "actiondetail.list", "mailinglist.list",
			"emailvariable.list", "emailtemplate.list", "useraccount.list" };

	private String[] functionRequired = { "yes", "yes", "yes", "no", "no", "no", "no", "no", "no",
			"no" };

	/* XXX JSTL import attribute url does not accept any expressions. So this is not used.  */
	private String[] jspPageLinks = { "configureMailboxes.jsp", "configureSmtpServers.jsp",
			"configureSiteProfiles.jsp", "customizeBuiltinRules.jsp", "configureCustomRules.jsp",
			"msgActionDetailList.jsp", "configureMailingLists.jsp", "configureEmailVariables.jsp",
			"configureEmailTemplates.jsp", "manageUserAccounts.jsp" };

	// PROPERTY: titleKey
	public void setTitleKey(String titleKey) {
		this.titleKey = titleKey;
	}

	public String getTitleKey() {
		if (bundle == null) {
			logger.error("Resource Bundle Injection Failed!!!");
		}
		// XXX Another way to get message from ResourceBundle
		/*
		javax.faces.application.FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
				"jpa.msgui.messages", "gettingStartedHeaderText", new String[] {});
		return message.getDetail();
		 */
		titleKey = sessionBean.getRequestParam("titleKey");
		if (titleKey == null) {
			titleKey = (String) sessionBean.getSessionParam("titleKey");
		}
		String fromPage = sessionBean.getRequestParam("frompage");
		if (fromPage == null) {
			fromPage = (String) sessionBean.getSessionParam("frompage");
		}
		if (!StringUtils.equals(fromPage, "main") || titleKey == null) {
			if (titleKey == null) {
				logger.warn("Failed to retrieve titleKey from request or session params, use default!!!");
			}
			titleKey = "gettingStartedHeaderText";
		}
		return titleKey;
	}

	// PROPERTY: functionKeys
	public DataModel<?> getFunctionKeys() {
		if (functionKeys == null) {
			List<String> functionList = new LinkedList<String>();
			for (int i = 0; i < menuTooltips.length; i++) {
				functionList.add(menuTooltips[i]);
			}
			functionKeys = new ListDataModel<Object>();
			functionKeys.setWrappedData(functionList);
		}
		return functionKeys;
	}

	public String selectFunction() {
		this.functionKey = (String) getFunctionKeys().getRowData();
		int i;
		for (i = 0; i < menuTooltips.length; i++) {
			if (menuTooltips[i].equals(this.functionKey)) {
				jspPageLink = jspPageLinks[i];
				break;
			}
		}
		logger.info("selectFunction() - functionKey selected: " + functionKey + ", value: " + navigationKeys[i]);
		return null; // navigationKeys[i];
	}

	public String getFunctionKey() {
		if (functionKey == null) {
			functionKey = menuTooltips[0];
		}
		return functionKey;
	}

	public void setFunctionKey(String function) {
		this.functionKey = function;
	}

	public String[] getFunctionRequired() {
		return functionRequired;
	}

	public void setFunctionRequired(String[] functionRequired) {
		this.functionRequired = functionRequired;
	}

	public String getJspPageLink() {
		if (jspPageLink == null) {
			jspPageLink = jspPageLinks[0];
		}
		return jspPageLink;
	}

	public void setJspPageLink(String jspPageLink) {
		this.jspPageLink = jspPageLink;
	}
}
