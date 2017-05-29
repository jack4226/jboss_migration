package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import jpa.constant.RuleCriteria;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriptionService;
import jpa.service.maillist.MailingListService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@ManagedBean(name="subscription")
@javax.faces.bean.ViewScoped
public class SubscriptionBean extends PaginationBean implements java.io.Serializable {
	private static final long serialVersionUID = 6216351042518651517L;
	static final Logger logger = Logger.getLogger(SubscriptionBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient SubscriptionService subscriptionDao = null;
	private transient EmailAddressService emailAddrDao = null;
	private transient MailingListService mailingListDao = null;
	
	private transient DataModel<Subscription> subscriptions = null;
	private Subscription subscription = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private String listId = null;

	private transient HtmlDataTable dataTable;
	private String searchString = null;
	
	private String testResult = null;
	private String actionFailure = null;
	
	static final String TO_SELF = null;
	static final String TO_FAILED = null;
	static final String TO_PAGING = TO_SELF;
	static final String TO_DELETED = TO_SELF;
	static final String TO_SAVED = TO_SELF;
	static final String TO_EDIT = "subscriptions.xhtml";
	static final String TO_CANCELED = "configureMailingLists.xhtml";

	public DataModel<Subscription> getSubscriptions() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (StringUtils.equals(fromPage,"main")) {
			resetPagingVo();
		}
		else if (StringUtils.equals(fromPage, "mailinglist")) {
			listId = FacesUtil.getRequestParameter("listId"); 
			if (StringUtils.isNotBlank(listId)) {
				resetPagingVo();
			}
		}
		logger.info("getSubscriptions() - From Page: " + fromPage + ", List Id: " + listId);
		// retrieve total number of rows
		if (getPagingVo().getRowCount() < 0) {
			long rowCount = getSubscriptionService().getSubscriptionCount(listId, getPagingVo());
			getPagingVo().setRowCount(rowCount);
		}
		if (subscriptions == null || !getPagingVo().getPageAction().equals(PagingVo.PageAction.CURRENT)) {
			List<Subscription> subscriptionList = getSubscriptionService().getSubscriptionsWithPaging(listId, getPagingVo());
			logger.info("PagingVo After: " + getPagingVo());
			getPagingVo().setPageAction(PagingVo.PageAction.CURRENT);
			subscriptions = new ListDataModel<Subscription>(subscriptionList);
		}
		return subscriptions;
	}

	@Override
	public long getRowCount() {
		if (StringUtils.equals(FacesUtil.getRequestParameter("frompage"), "mailinglist")) {
			listId = FacesUtil.getRequestParameter("listId"); 
		}
		long rowCount = getSubscriptionService().getSubscriptionCount(listId, getPagingVo());
		getPagingVo().setRowCount(rowCount);
		return rowCount;
	}

	public void searchByAddressListener(AjaxBehaviorEvent event) {
		searchByAddress();
	}
	
	public String searchByAddress() {
		boolean changed = false;
		PagingVo.Criteria criteria = getPagingVo().getSearchCriteria(PagingVo.Column.origAddress);
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
		return null;
	}
	
	public void resetSearchListener(AjaxBehaviorEvent event) {
		resetSearch();
	}
	
	public String resetSearch() {
		searchString = null;
		getPagingVo().setSearchCriteria(PagingVo.Column.origAddress, new PagingVo.Criteria(RuleCriteria.CONTAINS, searchString));
		resetPagingVo();
		return null;
	}
	
	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}
	
	public void refresh() {
		subscriptions = null;
	}

	public String refreshPage() {
		refresh();
		getPagingVo().setRowCount(-1);
		return "";
	}
	
	public void refreshPageListener(AjaxBehaviorEvent event) {
		refreshPage();
	}
	
	public SubscriptionService getSubscriptionService() {
		if (subscriptionDao == null) {
			subscriptionDao = SpringUtil.getWebAppContext().getBean(SubscriptionService.class);
		}
		return subscriptionDao;
	}

	public void setSubscriptionService(SubscriptionService subscriptionDao) {
		this.subscriptionDao = subscriptionDao;
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

	public MailingListService getMailingListDao() {
		if (mailingListDao == null) {
			mailingListDao = SpringUtil.getWebAppContext().getBean(MailingListService.class);
		}
		return mailingListDao;
	}

	public void deleteSubscriptionsListener(AjaxBehaviorEvent event) {
		deleteSubscriptions();
	}
	
	public String deleteSubscriptions() {
		if (isDebugEnabled)
			logger.debug("deleteSubscriptions() - Entering...");
		if (subscriptions == null) {
			logger.warn("deleteSubscriptions() - Subscription List is null.");
			return TO_FAILED;
		}
		if (listId == null) {
			logger.warn("deleteSubscriptions() - ListId is null.");
			return TO_FAILED;
		}
		reset();
		List<Subscription> subrList = getSubscriptionList();
		for (int i=0; i<subrList.size(); i++) {
			Subscription vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getSubscriptionService().deleteByAddressAndListId(vo.getEmailAddress().getAddress(), listId);
				if (rowsDeleted > 0) {
					logger.info("deleteSubscriptions() - Subscription deleted: " + vo.getEmailAddress());
					getPagingVo().setRowCount(getPagingVo().getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}

	public void saveSubscriptionsListener(AjaxBehaviorEvent event) {
		saveSubscriptions();
	}
	
	public String saveSubscriptions() {
		if (isDebugEnabled)
			logger.debug("saveSubscriptions() - Entering...");
		if (subscriptions == null) {
			logger.warn("saveSubscriptions() - Subscription List is null.");
			return TO_FAILED;
		}
		reset();
		List<Subscription> subrList = getSubscriptionList();
		for (int i=0; i<subrList.size(); i++) {
			Subscription vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				getSubscriptionService().update(vo);
//				boolean acceptHtml =  CodeType.YES.getValue().equalsIgnoreCase(vo.getAcceptHtmlDesc());
//				EmailAddress emailAddr = getEmailAddressService().getByRowId(vo.getEmailAddr().getRowId());
//				if (acceptHtml!=emailAddr.isAcceptHtml()) {
//					emailAddr.setAcceptHtml(acceptHtml);
//					getEmailAddressService().update(emailAddr);
//				}
				logger.info("saveSubscriptions() - Subscription updated: " + vo.getEmailAddress().getAddress());
			}
		}
		refresh();
		beanMode = BeanMode.list;
		return TO_SAVED;
	}

	public void addSubscriptionListener(AjaxBehaviorEvent event) {
		addSubscription();
	}
	
	public String addSubscription() {
		if (isDebugEnabled)
			logger.debug("addSubscription() - Entering...");
		reset();
		this.subscription = new Subscription();
		MailingList list = getMailingListDao().getByListId(listId);
		subscription.setMailingList(list);
		subscription.setSubscribed(true);
		subscription.setMarkedForEdition(true);
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
		
		// it could be called from mailing list page
		MailingListBean mailingList = (MailingListBean) FacesUtil.getManagedBean("mailingList");
		if (mailingList != null) {
			mailingList.cancelEdit();
		}
		
		return TO_CANCELED;
	}

	public boolean getSubscriptionsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getSubscriptionsMarkedForDeletion() - Entering...");
		if (subscriptions == null) {
			logger.warn("getSubscriptionsMarkedForDeletion() - Subscription List is null.");
			return false;
		}
		List<Subscription> subrList = getSubscriptionList();
		for (Iterator<Subscription> it=subrList.iterator(); it.hasNext();) {
			Subscription vo = it.next();
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
		String subId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - subscriptionId: " + subId);
		Subscription vo = getSubscriptionService().getByAddressAndListId(subId, listId);
		if (editMode == false && vo != null) {
			// subscription already exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "subscriptionAlreadyExist", new String[] {subId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == true && vo == null) {
			// subscription does not exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "subscriptionDoesNotExist", new String[] {subId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	void reset() {
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings({ "unchecked" })
	private List<Subscription> getSubscriptionList() {
		if (subscriptions == null) {
			return new ArrayList<Subscription>();
		}
		else {
			return (List<Subscription>)subscriptions.getWrappedData();
		}
	}

	public Subscription getSubscription() {
		return subscription;
	}

	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
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

	public String getListId() {
		return listId;
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

	public HtmlDataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(HtmlDataTable dataTable) {
		this.dataTable = dataTable;
	}
}
