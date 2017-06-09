package jpa.msgui.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import jpa.model.BroadcastTracking;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingVo;
import jpa.service.maillist.BroadcastTrackingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.data.domain.Page;

@ManagedBean(name="broadcastTrk")
@javax.faces.bean.ViewScoped
public class BroadcastTrkBean extends PaginationBean implements java.io.Serializable {
	private static final long serialVersionUID = -7017525272055704057L;
	static final Logger logger = Logger.getLogger(BroadcastTrkBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private transient BroadcastTrackingService broadcastTrkDao = null;
	
	private transient DataModel<BroadcastTracking> msgTrackings = null;
	
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.recipients;
	
	private Integer bcstRowId = null;

	private String testResult = null;
	private String actionFailure = null;
	
	static final String TO_SELF = null; // null -> remains in the same view
	static final String TO_FAILED = null;
	static final String TO_CANCELED = "broadcastsList.xhtml";

	public DataModel<BroadcastTracking> getMsgTrackings() {
		String fromPage = sessionBean.getRequestParam("frompage");
		bcstRowId = getBroadcastRowIdFromRequest();
		logger.info("getMsgTrackings() - fromPage = " + fromPage + ", Broadcast message row_id: " + bcstRowId);
		if ("broadcast".equals(fromPage)) {
			resetPagingVo();
		}
		if (!getPagingVo().getPageAction().equals(PagingVo.PageAction.CURRENT) || msgTrackings == null) {
			Page<BroadcastTracking> trkList = getBroadcastTrackingService().getBroadcastTrackingsForWeb(bcstRowId, getPagingVo());
			logger.info("PagingVo After: " + getPagingVo());
			getPagingVo().setPageAction(PagingVo.PageAction.CURRENT);
			msgTrackings = new ListDataModel<BroadcastTracking>(trkList.getContent());
		}
		return msgTrackings;
	}
	
	@Override
	public long getRowCount() {
		if (bcstRowId == null) {
			bcstRowId = getBroadcastRowIdFromRequest();
		}
		int rowCount = getBroadcastTrackingService().getMessageCountForWeb(bcstRowId);
		getPagingVo().setRowCount(rowCount);
		return rowCount;
	}
	
	private int getBroadcastRowIdFromRequest() {
		String bcstRowIdStr = sessionBean.getRequestParam("bcstrowid");
		if (StringUtils.isBlank(bcstRowIdStr) || !StringUtils.isNumeric(bcstRowIdStr)) {
			logger.error("getMsgTrackings() - Invalid broadcast message row_id! (" + bcstRowIdStr + ")");
			bcstRowIdStr = "0";
		}
		return Integer.parseInt(bcstRowIdStr);
	}
	
	@Override
	public void refresh() {
		msgTrackings = null;
	}

	public String refreshPage() {
		refresh();
		getPagingVo().setRowCount(-1);
		return "";
	}
	
	public BroadcastTrackingService getBroadcastTrackingService() {
		if (broadcastTrkDao == null) {
			broadcastTrkDao = SpringUtil.getWebAppContext().getBean(BroadcastTrackingService.class);
		}
		return broadcastTrkDao;
	}

	public void cancelEditListener(AjaxBehaviorEvent event) {
		cancelEdit();
	}
	
	public String cancelEdit() {
		refresh();
		beanMode = BeanMode.list;
		return TO_CANCELED;
	}

	void reset() {
		testResult = null;
		actionFailure = null;
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
