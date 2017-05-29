package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import jpa.model.BroadcastMessage;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.BroadcastMessageService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;

@ManagedBean(name="broadcastMsg")
@javax.faces.bean.ViewScoped
public class BroadcastMsgBean extends PaginationBean implements java.io.Serializable {
	private static final long serialVersionUID = -5557435572452796392L;
	static final Logger logger = Logger.getLogger(BroadcastMsgBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private transient BroadcastMessageService broadcastMsgDao = null;
	private transient EmailAddressService emailAddrDao = null;
	private transient DataModel<BroadcastMessage> broadcasts = null;
	
	private BroadcastMessage broadcastMsg = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;

	private String testResult = null;
	private String actionFailure = null;
	
	static final String TO_VIEW = "broadcastMsgView.xhtml";
	static final String TO_SELF = null; // null -> remains in the same view
	static final String TO_PAGING = TO_SELF;
	static final String TO_FAILED = null;
	static final String TO_DELETED = TO_SELF;
	static final String TO_SAVED = "broadcastsList.xhtml";
	static final String TO_CANCELED = "main.xhtml";
	static final String TO_CANCELED_FROM_VIEW = TO_SAVED;

	public DataModel<BroadcastMessage> getBroadcasts() {
		String fromPage = sessionBean.getRequestParam("frompage");
		logger.info(" getBroadcasts() - fromPage = " + fromPage);
		if (fromPage != null && fromPage.equals("main")) {
			resetPagingVo();
		}
		if (!getPagingVo().getPageAction().equals(PagingVo.PageAction.CURRENT) || broadcasts == null) {
			Page<BroadcastMessage> brdList = getBroadcastMessageService().getMessageListForWeb(getPagingVo());
			logger.info("PagingVo After: " + getPagingVo());
			getPagingVo().setPageAction(PagingVo.PageAction.CURRENT);
			broadcasts = new ListDataModel<BroadcastMessage>(brdList.getContent());
		}
		return broadcasts;
	}
	
	@Override
	public long getRowCount() {
		long rowCount = getBroadcastMessageService().getMessageCountForWeb();
		getPagingVo().setRowCount(rowCount);
		return rowCount;
	}
	
	@Override
	public void refresh() {
		broadcasts = null;
	}

	public String refreshPage() {
		refresh();
		getPagingVo().setRowCount(-1);
		return "";
	}
	
	public BroadcastMessageService getBroadcastMessageService() {
		if (broadcastMsgDao == null) {
			broadcastMsgDao = SpringUtil.getWebAppContext().getBean(BroadcastMessageService.class);
		}
		return broadcastMsgDao;
	}

	public EmailAddressService getEmailAddressService() {
		if (emailAddrDao == null) {
			emailAddrDao = SpringUtil.getWebAppContext().getBean(EmailAddressService.class);
		}
		return emailAddrDao;
	}
	
	public void viewBroadcastMsgListener(AjaxBehaviorEvent event) {
		viewBroadcastMsg();
	}

	public String viewBroadcastMsg() {
		if (isDebugEnabled)
			logger.debug("viewBroadcastMsg() - Entering...");
		if (broadcasts == null) {
			logger.warn("viewBroadcastMsg() - Broadcast List is null.");
			return TO_FAILED;
		}
		if (!broadcasts.isRowAvailable()) {
			logger.warn("viewBroadcastMsg() - Broadcast Row not available.");
			return TO_FAILED;
		}
		reset();
		this.broadcastMsg = broadcasts.getRowData();
		logger.info("viewBroadcastMsg() - Broadcast to be viewed: " + broadcastMsg.getRowId());
		broadcastMsg.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled) {
			logger.debug("viewBroadcastMsg() - BroadcastMessage to be passed to jsp: " + broadcastMsg);
		}
		return TO_VIEW;
	}

	public void deleteBroadcastsListener(AjaxBehaviorEvent event) {
		deleteBroadcasts();
	}
	
	public String deleteBroadcasts() {
		if (isDebugEnabled)
			logger.debug("deleteBroadcasts() - Entering...");
		if (broadcasts == null) {
			logger.warn("deleteBroadcasts() - Broadcast List is null.");
			return TO_FAILED;
		}
		reset();
		List<BroadcastMessage> subrList = getBroadcastList();
		for (int i=0; i<subrList.size(); i++) {
			BroadcastMessage vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getBroadcastMessageService().deleteByRowId(vo.getRowId());
				if (rowsDeleted > 0) {
					logger.info("deleteBroadcasts() - Broadcast deleted: " + vo.getRowId());
					getPagingVo().setRowCount(getPagingVo().getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		return TO_DELETED;
	}
	
	public void saveBroadcastsListener(AjaxBehaviorEvent event) {
		saveBroadcasts();
	}

	public String saveBroadcasts() {
		if (isDebugEnabled)
			logger.debug("saveBroadcasts() - Entering...");
		if (broadcasts == null) {
			logger.warn("saveBroadcasts() - Broadcast List is null.");
			return TO_FAILED;
		}
		reset();
		List<BroadcastMessage> subrList = getBroadcastList();
		for (int i=0; i<subrList.size(); i++) {
			BroadcastMessage vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
				}
				getBroadcastMessageService().update(vo);
				logger.info("saveBroadcasts() - Broadcast updated: " + vo.getRowId());
			}
		}
		refresh();
		beanMode = BeanMode.list;
		return TO_SAVED;
	}

	public void cancelEditListener(AjaxBehaviorEvent event) {
		cancelEdit();
	}
	
	public String cancelEdit() {
		refresh();
		beanMode = BeanMode.list;
//		String viewId = FacesUtil.getCurrentViewId();
//		if (StringUtils.contains(viewId, "broadcastMsgView")) {
//			return TO_CANCELED_FROM_VIEW;
//		}
//		else {
//			return TO_CANCELED;
//		}
		return TO_CANCELED;
	}

	public boolean getAnyBroadcastsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyBroadcastsMarkedForDeletion() - Entering...");
		if (broadcasts == null) {
			logger.warn("getAnyBroadcastsMarkedForDeletion() - Broadcast List is null.");
			return false;
		}
		List<BroadcastMessage> subrList = getBroadcastList();
		for (Iterator<BroadcastMessage> it=subrList.iterator(); it.hasNext();) {
			BroadcastMessage vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	void reset() {
		testResult = null;
		actionFailure = null;
	}

	@SuppressWarnings("unchecked")
	private List<BroadcastMessage> getBroadcastList() {
		if (broadcasts == null) {
			return new ArrayList<BroadcastMessage>();
		}
		else {
			return (List<BroadcastMessage>)broadcasts.getWrappedData();
		}
	}

	public BroadcastMessage getBroadcastMsg() {
		return broadcastMsg;
	}

	public void setBroadcastMsg(BroadcastMessage subscriber) {
		this.broadcastMsg = subscriber;
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
