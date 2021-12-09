package jpa.msgui.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.SpringUtil;
import jpa.service.common.SenderDataService;
import jpa.util.EmailAddrUtil;
import jpa.util.SenderUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@javax.inject.Named("senderData")
@javax.enterprise.context.RequestScoped
public class SenderDataBean implements java.io.Serializable {
	private static final long serialVersionUID = 1121882547043576165L;
	static final Logger logger = LogManager.getLogger(SenderDataBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private transient SenderDataService senderDao = null;
	
	private transient DataModel<SenderData> siteProfiles = null;
	private SenderData sender = null;
	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;
	
	private transient UIInput senderIdInput = null;
	private transient UIInput returnPathLeftInput = null;
	private transient UIInput verpEnabledInput = null;
	private transient UIInput useTestAddrInput = null;
	private String testResult = null;
	private String actionFailure = null;
	
	private static String TO_EDIT = "emailProfileEdit.xhtml";
	private static String TO_SAVED = "configureSiteProfiles.xhtml";
	private static String TO_FAILED = null;
	private static String TO_DELETED = TO_SAVED;
	private static String TO_CANCELED = TO_SAVED;
	
	private final SenderData siteMeta = new SenderData();
	
	//public SenderDataBean() {
	//	getData();
	//}
	
	public DataModel<SenderData> getAll() {
		String fromPage = FacesUtil.getRequestParameter("frompage");
		if (fromPage != null && fromPage.equals("main")) {
			refresh();
		}
		if (siteProfiles == null) {
			List<SenderData> senderList = null;
			if (!SenderUtil.isProductKeyValid() && SenderUtil.isTrialPeriodEnded()) {
				senderList = getSenderDataService().getAll();
			}
			else {
				senderList = getSenderDataService().getAll();
			}
			List<SenderData> systemAtTop = new ArrayList<SenderData>();
			for (int i = 0; i < senderList.size(); i++) {
				SenderData vo = senderList.get(i);
				if (Constants.DEFAULT_SENDER_ID.equals(vo.getSenderId())) {
					systemAtTop.add(vo);
					senderList.remove(i);
				}
			}
			systemAtTop.addAll(senderList);
			siteProfiles = new ListDataModel<SenderData>(systemAtTop);
		}
		return siteProfiles;
	}
	
	public void refreshListener(AjaxBehaviorEvent event) {
		refresh();
	}
	
	public String refresh() {
		siteProfiles = null;
		return "";
	}
	
	public void refreshSenderListener(AjaxBehaviorEvent event) {
		refreshSender();
	}
	
	public String refreshSender() {
		if (siteProfiles != null && siteProfiles.isRowAvailable()) {
			SenderData sd = siteProfiles.getRowData();
			if (sd != null) {
				sender = getSenderDataService().getBySenderId(sd.getSenderId());
			}
		}
		reset();
		//FacesUtil.refreshCurrentJSFPage();
		return null;
	}
	
	public SenderDataService getSenderDataService() {
		if (senderDao == null) {
			senderDao = SpringUtil.getWebAppContext().getBean(SenderDataService.class);
		}
		return senderDao;
	}

	public void setSenderDataService(SenderDataService senderDao) {
		this.senderDao = senderDao;
	}
	
	void reset() {
		testResult = null;
		actionFailure = null;
		senderIdInput = null;
		returnPathLeftInput = null;
		verpEnabledInput = null;
		useTestAddrInput = null;
	}
	
	public void viewSiteProfileListener(AjaxBehaviorEvent event) {
		viewSiteProfile();
	}
	
	public String viewSiteProfile() {
		if (isDebugEnabled)
			logger.debug("viewSiteProfile() - Entering...");
		if (siteProfiles == null) {
			logger.warn("viewSiteProfile() - SiteProfile List is null.");
			return TO_FAILED;
		}
		if (!siteProfiles.isRowAvailable()) {
			logger.warn("viewMailingList() - SiteProfile Row not available.");
			return TO_FAILED;
		}
		reset();
		this.sender = (SenderData) siteProfiles.getRowData();
		logger.info("viewSiteProfile() - Site to be edited: " + sender.getSenderId());
		sender.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		if (isDebugEnabled)
			logger.debug("viewSiteProfile() - SenderData to be passed to jsp: " + sender);
		
		return TO_EDIT;
	}
	
	public void saveSenderListener(AjaxBehaviorEvent event) {
		saveSender();
//		try {
//			FacesContext.getCurrentInstance().getExternalContext().redirect(TO_SAVED);
//		}
//		catch (IOException e) {
//			logger.error("IOException caught during Faces redirect", e);
//		}
	}

	public String saveSender() {
		if (isDebugEnabled)
			logger.debug("saveSender() - Entering...");
		if (sender == null) {
			logger.warn("saveSender() - SenderData is null.");
			return TO_FAILED;
		}
		reset();
		// update database
		if (StringUtils.isNotBlank(FacesUtil.getLoginUserId())) {
			sender.setUpdtUserId(FacesUtil.getLoginUserId());
		}
		if (editMode == true) {
			getSenderDataService().update(sender);
			logger.info("saveSender() - Rows Updated: " + 1);
		}
		else {
			getSenderDataService().insert(sender);
			getSiteProfilesList().add(sender);
			logger.info("saveSender() - Rows Inserted: " + 1);
		}
		beanMode = BeanMode.list;
		return TO_SAVED;
	}

	public void deleteSiteProfilesListener(AjaxBehaviorEvent event) {
		logger.info("deleteSiteProfilesListener() - event source: " + event.getSource());
		deleteSiteProfiles();
	}
	
	public String deleteSiteProfiles() {
		if (isDebugEnabled)
			logger.debug("deleteSiteProfiles() - Entering...");
		if (siteProfiles == null) {
			logger.warn("deleteSiteProfiles - SiteProfile List is null.");
			return TO_FAILED;
		}
		reset();
		List<SenderData> list = getSiteProfilesList();
		for (int i=0; i<list.size(); i++) {
			SenderData vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getSenderDataService().deleteBySenderId(vo.getSenderId());
				if (rowsDeleted > 0) {
					logger.info("deleteSiteProfiles() - Sender deleted: " + vo.getSenderId());
				}
				list.remove(vo);
			}
		}
		return TO_DELETED;
	}
	
	public void copySiteProfileListener(AjaxBehaviorEvent event) {
		copySiteProfile();
	}

	public String copySiteProfile() {
		if (isDebugEnabled)
			logger.debug("copySiteProfile() - Entering...");
		if (siteProfiles == null) {
			logger.warn("copySiteProfile() - Sender List is null.");
			return TO_FAILED;
		}
		reset();
		List<SenderData> mailList = getSiteProfilesList();
		for (int i=0; i<mailList.size(); i++) {
			SenderData vo = mailList.get(i);
			if (vo.isMarkedForDeletion()) {
				this.sender = new SenderData();
				try {
					vo.copyPropertiesTo(this.sender);
					sender.setMarkedForDeletion(false);
					vo.setMarkedForDeletion(false);
					sender.setRuleActions(null);
					sender.setSenderVariables(null);
				}
				catch (Exception e) {
					logger.error("BeanUtils.copyProperties() failed: ", e);
				}
				sender.setSenderId(null);
				sender.setMarkedForEdition(true);
				editMode = false;
				beanMode = BeanMode.insert;
				return TO_EDIT;
			}
		}
		return null;
	}
	
	public void addSiteProfileListener(AjaxBehaviorEvent event) {
		addSiteProfile();
	}
	
	public String addSiteProfile() {
		if (isDebugEnabled)
			logger.debug("addSiteProfile() - Entering...");
		reset();
		this.sender = new SenderData();
		sender.setMarkedForEdition(true);
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
	
	public boolean getAnySitesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnySitesMarkedForDeletion() - Entering...");
		if (siteProfiles == null) {
			logger.warn("getAnySitesMarkedForDeletion() - Sender List is null.");
			return false;
		}
		List<SenderData> mailList = getSiteProfilesList();
		for (Iterator<SenderData> it=mailList.iterator(); it.hasNext();) {
			SenderData vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}
	
	public void validateEmailAddress(FacesContext context, UIComponent component, Object value) {
		String emailAddr = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailAddress() - addr: " + emailAddr);
		if (StringUtils.isNotBlank(emailAddr) && !EmailAddrUtil.isRemoteEmailAddress(emailAddr)) {
			// invalid email address
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidEmailAddress", new String[] {emailAddr});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validateEmailLocalPart(FacesContext context, UIComponent component, Object value) {
		String localPart = (String) value;
		if (isDebugEnabled)
			logger.debug("validateEmailLocalPart() - local part: " + localPart);
		if (!EmailAddrUtil.isValidEmailLocalPart(localPart)) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidEmailLocalPart", new String[] {localPart});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public void validatePrimaryKey(FacesContext context, UIComponent component, Object value) {
		String senderId = (String) value;
		if (isDebugEnabled)
			logger.debug("validatePrimaryKey() - senderId: " + senderId);
		SenderData vo = getSenderDataService().getBySenderId(senderId);
		if (editMode == true && vo != null && sender != null && vo.getRowId() != sender.getRowId()) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
	        		"jpa.msgui.messages", "siteProfileAlreadyExist", new String[] {senderId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
		else if (editMode == false && vo != null) {
			// mailingList already exist
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "siteProfileAlreadyExist", new String[] {senderId});
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
	
	public boolean getIsVerpEnabledInput() {
		if (verpEnabledInput != null) {
			if (verpEnabledInput.getLocalValue() != null) {
				return CodeType.YES.getValue().equals(verpEnabledInput.getLocalValue());
			}
			else if (verpEnabledInput.getValue() != null) {
				return CodeType.YES.getValue().equals(verpEnabledInput.getValue());
			}
		}
		return true; // for safety
	}
	
	public boolean getIsUseTestAddrInput() {
		if (useTestAddrInput != null) {
			if (useTestAddrInput.getLocalValue() != null) {
				return CodeType.YES.getValue().equals(useTestAddrInput.getLocalValue());
			}
			else if (useTestAddrInput.getValue() != null) {
				return CodeType.YES.getValue().equals(useTestAddrInput.getValue());
			}
		}
		return true; // for safety
	}
	
	@SuppressWarnings("unchecked")
	private List<SenderData> getSiteProfilesList() {
		if (siteProfiles == null) {
			return new ArrayList<SenderData>();
		}
		else {
			return (List<SenderData>)siteProfiles.getWrappedData();
		}
	}
	
	public SenderData getSender() {
		return sender;
	}

	public void setSender(SenderData sender) {
		this.sender = sender;
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

	public DataModel<SenderData> getSiteProfiles() {
		return siteProfiles;
	}

	public void setSiteProfiles(DataModel<SenderData> siteProfiles) {
		this.siteProfiles = siteProfiles;
	}

	public UIInput getSenderIdInput() {
		return senderIdInput;
	}

	public void setSenderIdInput(UIInput senderIdInput) {
		this.senderIdInput = senderIdInput;
	}

	public UIInput getVerpEnabledInput() {
		return verpEnabledInput;
	}

	public void setVerpEnabledInput(UIInput verpEnabledInput) {
		this.verpEnabledInput = verpEnabledInput;
	}

	public UIInput getUseTestAddrInput() {
		return useTestAddrInput;
	}

	public void setUseTestAddrInput(UIInput useTestAddrInput) {
		this.useTestAddrInput = useTestAddrInput;
	}

	public UIInput getReturnPathLeftInput() {
		return returnPathLeftInput;
	}

	public void setReturnPathLeftInput(UIInput returnPathLeftInput) {
		this.returnPathLeftInput = returnPathLeftInput;
	}

	public SenderData getSiteMeta() {
		return siteMeta;
	}
}
