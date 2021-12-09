package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingVo;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.BroadcastMessageService;
import jpa.service.maillist.BroadcastTrackingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;

@javax.inject.Named("broadcastMsg")
@javax.enterprise.context.RequestScoped
public class BroadcastMsgBean extends PaginationBean implements java.io.Serializable {
	private static final long serialVersionUID = -5557435572452796392L;
	static final Logger logger = LogManager.getLogger(BroadcastMsgBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private transient BroadcastMessageService broadcastMsgDao = null;
	private transient EmailAddressService emailAddrDao = null;
	private transient BroadcastTrackingService broadcastTrkDao = null;
	
	private transient DataModel<BroadcastMessage> broadcasts = null;
	private transient DataModel<BroadcastTracking> msgTrackings = null;
	
	private BroadcastMessage broadcastMsg = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;

	private String testResult = null;
	private String actionFailure = null;
	
	final PagingVo trkPagingVo = new PagingVo();
	
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
	
	public BroadcastTrackingService getBroadcastTrackingService() {
		if (broadcastTrkDao == null) {
			broadcastTrkDao = SpringUtil.getWebAppContext().getBean(BroadcastTrackingService.class);
		}
		return broadcastTrkDao;
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
				vo.setMarkedForDeletion(false);
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
	
	@SuppressWarnings("unchecked")
	private List<BroadcastTracking> getMsgTrackingList() {
		if (msgTrackings == null) {
			return new ArrayList<BroadcastTracking>();
		}
		else {
			return (List<BroadcastTracking>)msgTrackings.getWrappedData();
		}
	}
	
	/*
	 * Methods for Message Tracking
	 */
	private Integer bcstRowId = null;
		
	public void viewMessageTrackingListener(String rowIdStr) {
		beanMode = BeanMode.recipients;
		bcstRowId = Integer.valueOf(rowIdStr);
		getTrkRowCount();
	}
	
	public DataModel<BroadcastTracking> getMsgTrackings() {
		String fromPage = sessionBean.getRequestParam("frompage");
		logger.info("getMsgTrackings() - fromPage = " + fromPage + ", Broadcast message row_id: " + bcstRowId);
		if ("broadcast".equals(fromPage)) {
			resetTrkPagingVo();
		}
		if (!getTrkPagingVo().getPageAction().equals(PagingVo.PageAction.CURRENT) || msgTrackings == null) {
			Page<BroadcastTracking> trkList = getBroadcastTrackingService().getBroadcastTrackingsForWeb(bcstRowId, getTrkPagingVo());
			logger.info("Tracking PagingVo After: " + getTrkPagingVo());
			getTrkPagingVo().setPageAction(PagingVo.PageAction.CURRENT);
			msgTrackings = new ListDataModel<BroadcastTracking>(trkList.getContent());
		}
		return msgTrackings;
	}
	
	public long getTrkRowCount() {
		int rowCount = getBroadcastTrackingService().getMessageCountForWeb(bcstRowId);
		getTrkPagingVo().setRowCount(rowCount);
		return rowCount;
	}
	
	public void deleteMsgTrackingsListener(AjaxBehaviorEvent event) {
		deleteMsgTrackings();
	}
	
	public String deleteMsgTrackings() {
		if (isDebugEnabled)
			logger.debug("deleteMsgTrackings() - Entering...");
		if (msgTrackings == null) {
			logger.warn("deleteMsgTrackings() - Msg Tracking List is null.");
			return TO_FAILED;
		}
		reset();
		List<BroadcastTracking> subrList = getMsgTrackingList();
		for (int i=0; i<subrList.size(); i++) {
			BroadcastTracking vo = subrList.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getBroadcastTrackingService().deleteByRowId(vo.getRowId());
				vo.setMarkedForDeletion(false);
				if (rowsDeleted > 0) {
					logger.info("deleteMsgTrackings() - Message tracking deleted: " + vo.getRowId());
					getTrkPagingVo().setRowCount(getTrkPagingVo().getRowCount() - rowsDeleted);
				}
			}
		}
		msgTrackings = null;
		return TO_DELETED;
	}

	public boolean getAnyMsgTrackinbgsMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyMsgTrackinbgsMarkedForDeletion() - Entering...");
		if (broadcasts == null) {
			logger.warn("getAnyMsgTrackinbgsMarkedForDeletion() - MsgTracking List is null.");
			return false;
		}
		List<BroadcastTracking> subrList = getMsgTrackingList();
		for (Iterator<BroadcastTracking> it=subrList.iterator(); it.hasNext();) {
			BroadcastTracking vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	public void trkPageFirst(AjaxBehaviorEvent event) {
		trkPagingVo.setPageAction(PagingVo.PageAction.FIRST);
		return; // TO_PAGING;
	}

	public void trkPagePrevious(AjaxBehaviorEvent event) {
		trkPagingVo.setPageAction(PagingVo.PageAction.PREVIOUS);
		return; // TO_PAGING;
	}

	public void trkPageNext(AjaxBehaviorEvent event) {
		trkPagingVo.setPageAction(PagingVo.PageAction.NEXT);
		return; // TO_PAGING;
	}

	public void trkPageLast(AjaxBehaviorEvent event) {
		if (trkPagingVo.getRowCount() < 0) {
			trkPagingVo.setRowCount(getRowCount());
		}
		trkPagingVo.setPageAction(PagingVo.PageAction.LAST);
		return; // TO_PAGING;
	}

	public long getTrkLastPageRow() {
		long lastRow = (trkPagingVo.getPageNumber() + 1) * trkPagingVo.getPageSize();
		if (trkPagingVo.getRowCount() < 0) {
			trkPagingVo.setRowCount(getTrkRowCount());
		}
		if (lastRow > trkPagingVo.getRowCount()) {
			return trkPagingVo.getRowCount();
		}
		else {
			return lastRow;
		}
	}
	
	public PagingVo getTrkPagingVo() {
		return trkPagingVo;
	}

	public void resetTrkPagingVo() {
		trkPagingVo.resetPageContext();
		msgTrackings = null;
	}
	/*
	 * End of Message Tracking
	 */
	
	
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
