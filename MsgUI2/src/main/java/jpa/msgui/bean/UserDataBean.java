package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import jpa.model.EmailAddress;
import jpa.model.SenderData;
import jpa.model.UserData;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.common.UserDataService;
import jpa.util.EmailAddrUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@javax.inject.Named("userData")
@javax.enterprise.context.SessionScoped
public class UserDataBean implements java.io.Serializable {
	private static final long serialVersionUID = 2276036390316734499L;
	static final Logger logger = LogManager.getLogger(UserDataBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient UserDataService userDao = null;
	private transient EmailAddressService emailAddrDao = null;
	private transient SenderDataService senderDao = null;
	
	private transient DataModel<UserData> users = null;
	private UserData user = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private transient UIInput userIdInput = null;
	private String userEmailAddr = null;
	private String userSenderId = null;
	
	private String testResult = null;
	private String actionFailure = null;

	private static String TO_EDIT = "userAccountEdit.xhtml";
	private static String TO_FAILED = null;
	private static String TO_SAVED = "manageUserAccounts.xhtml";
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;

	public DataModel<UserData> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (users == null) {
			List<UserData> userList = getUserDataService().getAll();
			users = new ListDataModel<UserData>(userList);
		}
		return users;
	}
	
	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}

	public String refresh() {
		users = null;
		return "";
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
	
	public EmailAddressService getEmailAddressService() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getWebAppContext().getBean(EmailAddressService.class);
		}
		return emailAddrDao;
	}
	public SenderDataService getSenderDataService() {
		if (senderDao == null) {
			senderDao = SpringUtil.getWebAppContext().getBean(SenderDataService.class);
		}
		return senderDao;
	}
	
	public void viewUserListener(AjaxBehaviorEvent event) {
		viewUser();
	}

	public String viewUser() {
		if (isDebugEnabled)
			logger.debug("viewUser() - Entering...");
		if (users == null) {
			logger.warn("viewUser() - User List is null.");
			return TO_FAILED;
		}
		if (!users.isRowAvailable()) {
			logger.warn("viewUser() - User Row not available.");
			return TO_FAILED;
		}
		reset();
		this.user = (UserData) users.getRowData();
		logger.info("viewUser() - User to be edited: " + user.getUserId());
		user.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (user.getEmailAddress()!=null) {
			setUserEmailAddr(user.getEmailAddress().getAddress());
		}
		if (user.getSenderData()!=null) {
			setUserSenderId(user.getSenderData().getSenderId());
		}
		if (isDebugEnabled)
			logger.debug("viewUser() - UserData to be passed to jsp: " + user);
		
		return TO_EDIT;
	}
	
	public void saveUserListener(AjaxBehaviorEvent event) {
		saveUser();	
	}
	
	public String saveUser() {
		if (isDebugEnabled)
			logger.debug("saveUser() - Entering...");
		if (user == null) {
			logger.warn("saveUser() - UserData is null.");
			return TO_FAILED;
		}
		reset();
		if (StringUtils.isNotBlank(getUserEmailAddr())) {
			if (!EmailAddrUtil.isRemoteEmailAddress(getUserEmailAddr())) {
				testResult = "invalidEmailAddress";
				return null;
			}
			if (user.getEmailAddress()!=null) {
				if (EmailAddrUtil.compareEmailAddrs(user.getEmailAddress().getAddress(), getUserEmailAddr())!=0) {
					EmailAddress newAddr = getEmailAddressService().findSertAddress(getUserEmailAddr());
					user.setEmailAddress(newAddr);
				}
			}
			else {
				EmailAddress newAddr = getEmailAddressService().findSertAddress(getUserEmailAddr());
				user.setEmailAddress(newAddr);
			}
		}
		else {
			if (user.getEmailAddress()!=null) {
				user.setEmailAddress(null);
			}
		}
		if (StringUtils.isNotBlank(getUserSenderId())) {
			if (user.getSenderData()!=null) {
				if (!getUserSenderId().equals(user.getSenderData().getSenderId())) {
					SenderData sender = getSenderDataService().getBySenderId(getUserSenderId());
					user.setSenderData(sender);
				}
			}
		}
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			user.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getUserDataService().update(user);
			logger.info("in saveUser() - Rows Updated: " + 1);
		}
		else {
			getUserDataService().insert(user);
			getUserList().add(user);
			logger.info("saveUser() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}
	
	public void deleteUsersListener(AjaxBehaviorEvent event) {
		deleteUsers();
	}
	
	public String deleteUsers() {
		if (isDebugEnabled)
			logger.debug("deleteUsers() - Entering...");
		if (users == null) {
			logger.warn("deleteUsers() - User List is null.");
			return TO_FAILED;
		}
		reset();
		List<UserData> smtpList = getUserList();
		for (int i=0; i<smtpList.size(); i++) {
			UserData vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getUserDataService().deleteByUserId(vo.getUserId());
				if (rowsDeleted > 0) {
					logger.info("deleteUsers() - User deleted: " + vo.getUserId());
				}
				smtpList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public void copyUserListener(AjaxBehaviorEvent event) {
		copyUser();
	}
	
	public String copyUser() {
		if (isDebugEnabled)
			logger.debug("copyUser() - Entering...");
		if (users == null) {
			logger.warn("copyUser() - User List is null.");
			return TO_FAILED;
		}
		reset();
		List<UserData> smtpList = getUserList();
		for (int i=0; i<smtpList.size(); i++) {
			UserData vo = smtpList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.user = new UserData();
				try {
					vo.copyPropertiesTo(this.user);
					user.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
					user.setHits(0);
					user.setLastVisitTime(null);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				user.setUserId(null);
				user.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return null;
	}
	
	public void addUserListener(AjaxBehaviorEvent event) {
		addUser();
	}
	
	public String addUser() {
		if (isDebugEnabled)
			logger.debug("addUser() - Entering...");
		reset();
		this.user = new UserData();
		user.setMarkedForEdition(true);
		editMode = false;
		beanMode = BeanMode.insert;
		return TO_EDIT;
	}
	
	public void cancelEditListener(AjaxBehaviorEvent event) {
		cancelEdit();
	}
	
	public String cancelEdit() {
		refresh();
		beanMode = BeanMode.list;
		return TO_CANCELED;
	}
	
	public boolean getAnyUsersMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyUsersMarkedForDeletion() - Entering...");
		if (users == null) {
			logger.warn("getAnyUsersMarkedForDeletion() - User List is null.");
			return false;
		}
		List<UserData> smtpList = getUserList();
		for (Iterator<UserData> it=smtpList.iterator(); it.hasNext();) {
			UserData vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * validate primary key
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String userId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - userId: " + userId);

		UserData vo = getUserDataService().getByUserId(userId);
		if (editMode == false && vo != null) {
			// user already exist
	        FacesMessage message =jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "userAlreadyExist", new String[] {userId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == true && vo == null) {
			// user does not exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "userDoesNotExist", new String[] {userId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	/**
	 * actionListener
	 * @param e
	 */
	public void actionFired(ActionEvent e) {
		logger.info("actionFired(ActionEvent) - " + e.getComponent().getId());
	}
	
	/**
	 * valueChangeEventListener
	 * @param e
	 */
	public void fieldValueChanged(ValueChangeEvent e) {
		if (isDebugEnabled) {
			logger.debug("fieldValueChanged(ValueChangeEvent) - " + e.getComponent().getId()
					+ ": " + e.getOldValue() + " -> " + e.getNewValue());
		}
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		userIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<UserData> getUserList() {
		if (users == null) {
			return new ArrayList<UserData>();
		}
		else {
			return (List<UserData>)users.getWrappedData();
		}
	}
	
	public UserData getUser() {
		return user;
	}

	public void setUser(UserData user) {
		this.user = user;
	}

	public boolean isEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public String getBeanMode() {
		return beanMode == null ? "" : beanMode.name();
	}

	public void setBeanMode(String beanMode) {
		try {
			this.beanMode = BeanMode.valueOf(beanMode);
		}
		catch (Exception e) {}
	}

	public String getUserEmailAddr() {
		return userEmailAddr;
	}

	public void setUserEmailAddr(String userEmailAddr) {
		this.userEmailAddr = userEmailAddr;
	}

	public String getUserSenderId() {
		return userSenderId;
	}

	public void setUserSenderId(String userSenderId) {
		this.userSenderId = userSenderId;
	}

	public UIInput getUserIdInput() {
		return userIdInput;
	}

	public void setUserIdInput(UIInput userIdInput) {
		this.userIdInput = userIdInput;
	}

	public String getTestResult() {
		return testResult;
	}

	public void setTestResult(String testResult) {
		this.testResult = testResult;
	}

	public String getActionFailure() {
		return actionFailure;
	}

	public void setActionFailure(String actionFailure) {
		this.actionFailure = actionFailure;
	}
}
