package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import jpa.constant.Constants;
import jpa.constant.RuleCriteria;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.MailingListService;
import jpa.util.TestUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@ManagedBean(name="emailAddress")
@javax.faces.bean.ViewScoped
public class EmailAddressBean extends PaginationBean implements java.io.Serializable {
	private static final long serialVersionUID = -1230662734764912082L;
	static final Logger logger = Logger.getLogger(EmailAddressBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient EmailAddressService emailAddrDao = null;
	private transient MailingListService mailingListDao = null;
	
	private EmailAddress emailAddr = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;

	private transient DataModel<EmailAddress> emailAddrs = null;
	private transient UIInput emailAddrInput = null;
	
	private String searchString = null;
	
	private List<MailingList> mailingLists = null;
	private String testResult = null;
	private String actionFailure = null;

	private static String TO_SELF = null;
	private static String TO_FAILED = TO_SELF;
	private static String TO_LIST = "emailAddressList.xhtml";
	private static String TO_EDIT = "emailAddressEdit.xhtml";
	private static String TO_SAVED = TO_LIST;
	private static String TO_CANCELED = "main.xhtml";
	
	public DataModel<EmailAddress> getEmailAddrs() {
		String fromPage = sessionBean.getRequestParam("frompage");
		logger.info("getEmailAddrs() - fromPage = " + fromPage);
		if (fromPage != null && fromPage.equals("main")) {
			resetPagingVo();
		}
		if (!getPagingVo().getPageAction().equals(PagingVo.PageAction.CURRENT) || emailAddrs == null) {
			getPagingVo().setStatusId(null);
			getPagingVo().setOrderBy(PagingVo.Column.address, true);
			logger.info("PagingVo Before: " + getPagingVo());
			List<EmailAddress> emailAddrList = getEmailAddressService().getAddrListByPagingVo(getPagingVo());
			getPagingVo().setPageAction(PagingVo.PageAction.CURRENT);
			logger.info("PagingVo After: " + getPagingVo());
			emailAddrs = new ListDataModel<EmailAddress>(emailAddrList);
		}
		return emailAddrs;
	}

	@Override
	public long getRowCount() {
		getPagingVo().setStatusId(null);
		long rowCount = getEmailAddressService().getEmailAddressCount(getPagingVo());
		getPagingVo().setRowCount(rowCount);
		return rowCount;
	}

	/*
	 * Use String signature for rowId to support JSF script.
	 */
	public String findAddressByRowId(String rowId) {
		EmailAddress addr = getEmailAddressService().getByRowId(Integer.parseInt(rowId));
		if (addr != null) {
			return addr.getAddress();
		}
		else {
			return "";
		}
	}

	public void searchByAddress(AjaxBehaviorEvent event) {
		boolean changed = false;
		
		PagingVo.Criteria criteria = getPagingVo().getSearchBy().getCriteria(PagingVo.Column.origAddress);
		if (this.searchString == null) {
			if (criteria != null && criteria.getValue() != null) {
				changed = true;
			}
		}
		else {
			if (criteria != null && !this.searchString.equals(criteria.getValue())) {
				changed = true;
			}
		}
		if (changed) {
			resetPagingVo();
			getPagingVo().setSearchCriteria(PagingVo.Column.origAddress, new PagingVo.Criteria(RuleCriteria.CONTAINS, searchString));
		}
		return; // TO_SELF;
	}
	
	public String resetSearch() {
		searchString = null;
		getPagingVo().setSearchCriteria(PagingVo.Column.origAddress, new PagingVo.Criteria(RuleCriteria.CONTAINS, null));
		resetPagingVo();
		return TO_SELF;
	}
	
	public void resetSearchListener(AjaxBehaviorEvent event) {
		resetSearch();
	}

	@Override
	protected void refresh() {
		emailAddrs = null;
	}

	public void refreshPage(AjaxBehaviorEvent event) {
		refresh(); // stay on the same page
		//resetPagingVo(); // jump to first page
	}
	
	public EmailAddressService getEmailAddressService() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getWebAppContext().getBean(EmailAddressService.class);
		}
		return emailAddrDao;
	}

	public void setEmailAddressService(EmailAddressService emailAddrDao) {
		this.emailAddrDao = emailAddrDao;
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

	public void viewEmailAddrListener(AjaxBehaviorEvent event) {
		viewEmailAddr();
	}
	
	public String viewEmailAddr() {
		if (isDebugEnabled)
			logger.debug("viewEmailAddr() - Entering...");
		if (emailAddrs == null) {
			logger.warn("viewEmailAddr() - EmailAddr List is null.");
			actionFailure = "EmailAddr List is null";
			return TO_FAILED;
		}
		if (!emailAddrs.isRowAvailable()) {
			logger.warn("viewEmailAddr() - EmailAddr Row not available.");
			actionFailure = "EmailAddr Row not available.";
			return TO_FAILED;
		}
		reset();
		this.emailAddr = (EmailAddress) emailAddrs.getRowData();
		logger.info("viewEmailAddr() - EmailAddr to be edited: " + emailAddr.getAddress());
		emailAddr.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		mailingLists = getMailingListService().getByAddressWithCounts(emailAddr.getAddress());
		if (isDebugEnabled) {
			logger.debug("viewEmailAddr() - EmailAddress to be passed to jsp: " + emailAddr);
		}
		return TO_EDIT;
	}

	public String saveEmailAddr() {
		if (isDebugEnabled)
			logger.debug("saveEmailAddr() - Entering...");
		if (emailAddr == null) {
			logger.warn("saveEmailAddr() - EmailAddress is null.");
			actionFailure = "EmailAddress is null.";
			return TO_FAILED;
		}
		reset();
		// update database
		if (TestUtil.isRunningInJunitTest() == false) {
			if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
				emailAddr.setUpdtUserId(FacesUtil.getLoginUserId());
			}
		}
		if (editMode == true) {
			getEmailAddressService().update(emailAddr);
			refresh();
			logger.info("saveEmailAddr() - Rows Updated: " + 1);
		}
		else {
			getEmailAddressService().insert(emailAddr);
			addToList(emailAddr);
			getPagingVo().setRowCount(getPagingVo().getRowCount() + 1);
			resetPagingVo();
			logger.info("saveEmailAddr() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}
	
	public void saveEmailAddrListener(AjaxBehaviorEvent event) {
		saveEmailAddr();
		testResult = "changesAreSaved";
	}

	private void addToList(EmailAddress vo) {
		@SuppressWarnings("unchecked")
		List<EmailAddress> list = (List<EmailAddress>) emailAddrs.getWrappedData();
		list.add(vo);
	}

	public void deleteEmailAddrs(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("deleteEmailAddrs() - Entering...");
		if (emailAddrs == null) {
			logger.warn("deleteEmailAddrs() - EmailAddr List is null.");
			actionFailure = "EmailAddr List is null";
			return; // TO_FAILED;
		}
		reset();
		List<EmailAddress> addrList = getEmailAddrList();
		for (int i=0; i<addrList.size(); i++) {
			EmailAddress vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getEmailAddressService().deleteByRowId(vo.getRowId());
				if (rowsDeleted > 0) {
					logger.info("deleteEmailAddrs() - EmailAddr deleted: " + vo.getAddress());
					getPagingVo().setRowCount(getPagingVo().getRowCount() - rowsDeleted);
				}
				addrList.remove(i);
			}
		}
		//refresh();
		return; // TO_DELETED;
	}

	public String saveEmailAddrs() {
		if (isDebugEnabled)
			logger.debug("saveEmailAddrs() - Entering...");
		if (emailAddrs == null) {
			logger.warn("saveEmailAddrs() - EmailAddr List is null.");
			actionFailure = "EmailAddr List is null";
			return TO_FAILED;
		}
		reset();
		List<EmailAddress> addrList = getEmailAddrList();
		for (int i=0; i<addrList.size(); i++) {
			EmailAddress vo = addrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				if (!vo.getAddress().equals(vo.getCurrAddress())) {
					EmailAddress vo2 = getEmailAddressService().getByAddress(vo.getAddress());
					if (vo2 != null && vo2.getRowId() != vo.getRowId()) {
						actionFailure = "Email address " + vo.getAddress() + " already exists.";
						return TO_SELF;
					}
				}
				getEmailAddressService().update(vo);
				logger.info("saveEmailAddrs() - EmailAddr updated: " + vo.getAddress());
				vo.setMarkedForDeletion(false);
			}
		}
		//refresh();
		beanMode = BeanMode.list;
		return TO_SAVED;
	}
	
	public void addEmailAddrListener(AjaxBehaviorEvent event) {
		addEmailAddr();
	}

	public String addEmailAddr() {
		if (isDebugEnabled)
			logger.debug("addEmailAddr() - Entering...");
		reset();
		this.emailAddr = new EmailAddress();
		emailAddr.setMarkedForEdition(true);
		emailAddr.setUpdtUserId(Constants.DEFAULT_USER_ID);
		if (mailingLists != null) {
			mailingLists.clear();
		}
		editMode = false;
		beanMode = BeanMode.insert;
		return TO_EDIT;
	}

	public void cancelEditListener(AjaxBehaviorEvent event) {
		cancelEdit();
	}
	
	public String cancelEdit() {
		//refresh();
		beanMode = BeanMode.list;
		if (StringUtils.contains(FacesUtil.getCurrentViewId(), TO_EDIT)) {
			return TO_LIST;
		}
		else {
			return TO_CANCELED;
		}
	}

	public boolean getAnyEmailAddrsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyEmailAddrsMarkedForDeletion() - Entering...");
		if (emailAddrs == null) {
			logger.warn("getAnyEmailAddrsMarkedForDeletion() - EmailAddr List is null.");
			return false;
		}
		List<EmailAddress> addrList = getEmailAddrList();
		for (Iterator<EmailAddress> it=addrList.iterator(); it.hasNext();) {
			EmailAddress vo = it.next();
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
		String address = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - address: " + address);
		EmailAddress vo = getEmailAddressService().getByAddress(address);
		if (vo == null) return;
		if (editMode == true && emailAddr != null
				&& vo.getRowId() != emailAddr.getRowId()) {
			// emailAddr does not exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					//"jpa.msgui.messages", "emailAddrDoesNotExist", null);
	        		"jpa.msgui.messages", "emailAddrAlreadyExist", new String[] {address});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false) {
			// emailAddr already exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "emailAddrAlreadyExist", new String[] {address});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	void reset() {
		emailAddrInput = null;
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings("unchecked")
	private List<EmailAddress> getEmailAddrList() {
		if (emailAddrs == null) {
			return new ArrayList<EmailAddress>();
		}
		else {
			return (List<EmailAddress>)emailAddrs.getWrappedData();
		}
	}

	public EmailAddress getEmailAddr() {
		if (emailAddr == null) {
			// When navigate from List -> Edit, request parameter for current email address is populated
			String addrStr = sessionBean.getRequestParam("emailaddr");
			if (StringUtils.isNotBlank(addrStr)) {
				emailAddr = getEmailAddressService().getByAddress(addrStr);
			}
		}
		if (emailAddr == null && emailAddrs != null) {
			if (emailAddrs.isRowAvailable()) {
				emailAddr = emailAddrs.getRowData();
			}
		}
		return emailAddr;
	}

	public void setEmailAddr(EmailAddress emailAddr) {
		this.emailAddr = emailAddr;
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

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
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

	public UIInput getEmailAddrInput() {
		return emailAddrInput;
	}

	public void setEmailAddrInput(UIInput emailAddrInput) {
		this.emailAddrInput = emailAddrInput;
	}

	public List<MailingList> getMailingLists() {
		return mailingLists;
	}

	public void setMailingLists(List<MailingList> mailingLists) {
		this.mailingLists = mailingLists;
	}

	public boolean isMailingListsEmpty() {
		return (mailingLists == null || mailingLists.isEmpty()); 
	}
}
