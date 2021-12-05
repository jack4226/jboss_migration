package jpa.msgui.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import jpa.constant.Constants;
import jpa.model.MailingList;
import jpa.model.SenderData;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.common.SenderDataService;
import jpa.service.common.SubscriptionService;
import jpa.service.maillist.MailingListService;
import jpa.util.EmailAddrUtil;
import jpa.util.SenderUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@ManagedBean(name="mailingList")
@javax.faces.bean.ViewScoped
public class MailingListBean implements java.io.Serializable {
	private static final long serialVersionUID = 3726339874453826497L;
	static final Logger logger = Logger.getLogger(MailingListBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient MailingListService mailingListDao = null;
	private transient SubscriptionService subscriptionDao = null;
	
	private transient DataModel<MailingList> mailingLists = null;
	private MailingList mailingList = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private transient UIInput listIdInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private static String TO_EDIT = "mailingListEdit.xhtml";
	private static String TO_SELF = null;
	private static String TO_SAVED = "configureMailingLists.xhtml";
	private static String TO_FAILED = null;
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;

	public DataModel<MailingList> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (mailingLists == null) {
			List<MailingList> mailingListList = null;
			if (!SenderUtil.isProductKeyValid() && SenderUtil.isTrialPeriodEnded()) {
				mailingListList = getMailingListService().getAll(false);
			}
			else {
				mailingListList = getMailingListService().getAll(false);
			}
			mailingLists = new ListDataModel<MailingList>(mailingListList);
		}
		return mailingLists;
	}

	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}
	
	public String refresh() {
		mailingLists = null;
		return TO_SELF;
	}
	
	public MailingListService getMailingListService() {
		if (mailingListDao == null) {
			mailingListDao = SpringUtil.getWebAppContext().getBean(MailingListService.class);
		}
		return mailingListDao;
	}

	public void setMailingListService(MailingListService mailingListDao) {
		this.mailingListDao = mailingListDao;
	}
	
	public SubscriptionService getSubscriptionService() {
		if (subscriptionDao == null) {
			subscriptionDao = SpringUtil.getWebAppContext().getBean(SubscriptionService.class);
		}
		return subscriptionDao;
	}

	/*
	 * Use String signature for rowId to support JSF script.
	 */
	public String findListIdByRowId(String rowId) {
		Optional<MailingList> mlist = getMailingListService().getByRowId(Integer.parseInt(rowId));
		if (mlist.isPresent()) {
			return mlist.get().getListId();
		}
		else {
			return "";
		}
	}
	
	public void viewMailingListListener(AjaxBehaviorEvent event) {
		viewMailingList();
	}

	public String viewMailingList() {
		if (isDebugEnabled)
			logger.debug("viewMailingList() - Entering...");
		if (mailingLists == null) {
			logger.warn("viewMailingList() - MailingList List is null.");
			return "mailinglist.failed";
		}
		if (!mailingLists.isRowAvailable()) {
			logger.warn("viewMailingList() - MailingList Row not available.");
			return "mailinglist.failed";
		}
		reset();
		this.mailingList = (MailingList) mailingLists.getRowData();
		logger.info("viewMailingList() - MailingList to be edited: " + mailingList.getListId());
		mailingList.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled)
			logger.debug("viewMailingList() - MailingList to be passed to jsp: " + mailingList);
		
		return TO_EDIT;
	}
	
	public void saveMailingListListener(AjaxBehaviorEvent event) {
		saveMailingList();
	}
	
	public String saveMailingList() {
		if (isDebugEnabled)
			logger.debug("saveMailingList() - Entering...");
		if (mailingList == null) {
			logger.warn("saveMailingList() - MailingList is null.");
			return "mailinglist.failed";
		}
		reset();
		if (!EmailAddrUtil.isRemoteEmailAddress(mailingList.getListEmailAddr())) {
			testResult = "invalidEmailAddress";
			return TO_FAILED;
		}
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			mailingList.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getMailingListService().update(mailingList);
			logger.info("saveMailingList() - Rows Updated: " + 1);
		}
		else { // a new list
			getMailingListService().insert(mailingList);
			getMailingListList().add(mailingList);
			logger.info("saveMailingList() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}

	public void deleteMailingListsListener(AjaxBehaviorEvent event) {
		deleteMailingLists();
	}

	public String deleteMailingLists() {
		if (isDebugEnabled)
			logger.debug("deleteMailingLists() - Entering...");
		if (mailingLists == null) {
			logger.warn("deleteMailingLists() - MailingList List is null.");
			return "mailinglist.failed";
		}
		reset();
		List<MailingList> mailList = getMailingListList();
		for (int i=0; i<mailList.size(); i++) {
			MailingList vo = mailList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMailingListService().deleteByListId(vo.getListId());
				if (rowsDeleted > 0) {
					logger.info("deleteMailingLists() - MailingList deleted: " + vo.getListId());
				}
				mailList.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public void copyMailingListListener(AjaxBehaviorEvent event) {
		copyMailingList();
	}
	
	public String copyMailingList() {
		if (isDebugEnabled)
			logger.debug("copyMailingList() - Entering...");
		if (mailingLists == null) {
			logger.warn("copyMailingList() - MailingList List is null.");
			return TO_FAILED;
		}
		reset();
		List<MailingList> mailList = getMailingListList();
		for (int i=0; i<mailList.size(); i++) {
			MailingList vo = mailList.get(i);
			if (vo.isMarkedForDeletion()) { // copy from 
				this.mailingList = new MailingList();
				try {
					vo.copyPropertiesTo(this.mailingList);
					mailingList.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
					if (mailingList.getSenderData() == null) {
						mailingList.setSenderData(vo.getSenderData());
					}
					mailingList.setSubscriptions(null);
					mailingList.setBroadcastMessages(null);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				mailingList.setListId(null);
				mailingList.setAcctUserName(null);
				mailingList.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return TO_SELF;
	}
	
	public void addMailingListListener(AjaxBehaviorEvent event) {
		addMailingList();
	}
	
	public String addMailingList() {
		if (isDebugEnabled)
			logger.debug("addMailingList() - Entering...");
		reset();
		SenderDataService senderService = SpringUtil.getWebAppContext().getBean(SenderDataService.class);
		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		this.mailingList = new MailingList();
		mailingList.setSenderData(sender);
		mailingList.setMarkedForEdition(true);
		editMode = false;
		beanMode = BeanMode.insert;
		return TO_EDIT;
	}
	
	// TODO
	public void viewSubscriptionsListener(AjaxBehaviorEvent event) {
		
		beanMode = BeanMode.viewList;
	}
	
	public void cancelEditListener(AjaxBehaviorEvent event) {
		cancelEdit();
	}
	
	public String cancelEdit() {
		refresh();
		beanMode = BeanMode.list;
		return TO_CANCELED;
	}
	
	public void uploadFilesListener(AjaxBehaviorEvent event) {
		uploadFiles();
	}
	
	// TODO use xhtml for file upload
	public String uploadFiles() {
		String listId = mailingList.getListId();
		String pageUrl = "/upload/emailAddrAttachFile.jsp?frompage=uploademails&listid="+listId;
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext ectx = context.getExternalContext();
		try {
			ectx.redirect(ectx.encodeResourceURL(ectx.getRequestContextPath() + pageUrl));
		}
		catch (IOException e) {
			logger.error("uploadFiles() - IOException caught", e);
			throw new FacesException("Cannot redirect to " + pageUrl + " due to IO exception.", e);
		}
		return TO_SELF;
	}
	
	public boolean getAnyListsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyListsMarkedForDeletion() - Entering...");
		if (mailingLists == null) {
			logger.warn("getAnyListsMarkedForDeletion() - MailingList List is null.");
			return false;
		}
		List<MailingList> mailList = getMailingListList();
		for (Iterator<MailingList> it=mailList.iterator(); it.hasNext();) {
			MailingList vo = it.next();
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
		String listId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - listId: " + listId);
		MailingList vo = getMailingListService().getByListId(listId);
		if (vo != null) {
			if (editMode == true && mailingList != null
					&& vo.getRowId() != mailingList.getRowId()) {
				// mailingList does not exist
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
		        		"jpa.msgui.messages", "mailingListAlreadyExist", new String[] {listId});
						//"jpa.msgui.messages", "mailingListDoesNotExist", null);
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
			else if (editMode == false) {
				// mailingList already exist
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "mailingListAlreadyExist", new String[] {listId});
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
	}
	
	public void validateEmailAddress(FacesContext context, UIComponent component, Object value) {
		String emailAddr = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailAddress() - addr: " + emailAddr);
		if (StringUtils.isNotBlank(emailAddr)) {
			if (!EmailAddrUtil.isRemoteOrLocalEmailAddress(emailAddr)) {
				// invalid email address
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "invalidEmailAddress", new String[] {emailAddr});
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
		}
	}
	
	public void validateAccountUserName(FacesContext context, UIComponent component, Object value) {
		String acctUserName = (String) value;
		if (isDebugEnabled)
			logger.debug("validateAccountUserName() - account user name: " + acctUserName);
		if (StringUtils.isNotBlank(acctUserName)) {
			if (!acctUserName.matches("^(?i)([a-z0-9\\.\\_\\%\\+\\-])+$")) {
				// invalid email address
		        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
						"jpa.msgui.messages", "invalidAccountUserName", new String[] {acctUserName});
				message.setSeverity(FacesMessage.SEVERITY_WARN);
				throw new ValidatorException(message);
			}
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
		listIdInput = null;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<MailingList> getMailingListList() {
		if (mailingLists == null) {
			return new ArrayList<MailingList>();
		}
		else {
			return (List<MailingList>)mailingLists.getWrappedData();
		}
	}
	
	public MailingList getMailingList() {
		return mailingList;
	}

	public void setMailingList(MailingList mailingList) {
		this.mailingList = mailingList;
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

	public UIInput getListIdInput() {
		return listIdInput;
	}

	public void setListIdInput(UIInput listIdInput) {
		this.listIdInput = listIdInput;
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
