package jpa.msgui.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.validation.ValidationException;

import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.MsgDirectionCode;
import jpa.constant.MsgStatusCode;
import jpa.data.preload.FolderEnum;
import jpa.exception.DataValidationException;
import jpa.message.BodypartBean;
import jpa.message.MessageBean;
import jpa.message.MessageBodyBuilder;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.model.SessionUpload;
import jpa.model.SessionUploadPK;
import jpa.model.UserData;
import jpa.model.msg.MessageAttachment;
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageInbox;
import jpa.model.msg.MessageRfcField;
import jpa.model.rule.RuleLogic;
import jpa.msgui.util.FacesUtil;
import jpa.msgui.util.MessageThreadsBuilder;
import jpa.msgui.util.SpringUtil;
import jpa.msgui.vo.PagingVo.PageAction;
import jpa.msgui.vo.PagingVo;
import jpa.msgui.vo.SearchFieldsVo;
import jpa.service.common.EmailAddressService;
import jpa.service.common.EntityManagerService;
import jpa.service.common.SessionUploadService;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgin.MessageInboxBo;
import jpa.service.msgout.MessageBeanBo;
import jpa.service.rule.RuleLogicService;
import jpa.service.task.AssignRuleName;
import jpa.service.task.TaskBaseBo;
import jpa.util.EmailAddrUtil;
import jpa.util.PrintUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

@ManagedBean(name = "messageInbox")
@javax.faces.bean.ViewScoped
public class MessageInboxBean extends PaginationBean implements java.io.Serializable {
	private static final long serialVersionUID = -1682128466807436660L;

	static final Logger logger = Logger.getLogger(MessageInboxBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final boolean isInfoEnabled = logger.isInfoEnabled();
	final static String LF = System.getProperty("line.separator", "\n");
	final static boolean DisplaySearchVo = true;

	@ManagedProperty("#{facesBroker}")
	private FacesBroker broker;

	private transient MessageInboxService msgInboxDao = null;
	private transient EmailAddressService emailAddrDao = null;
	private transient RuleLogicService ruleLogicDao = null;
	private transient MessageInboxBo msgInboxBo = null;
	private transient SessionUploadService sessionUploadDao = null;
	private transient EntityManagerService entityDao = null;
	private transient MessageBeanBo msgBeanBo = null;
	private transient MessageFolderService folderDao = null;

	private transient DataModel<MessageInbox> folder = null;
	private MessageInbox message = null;

	private boolean editMode = true;
	private BeanMode beanMode = BeanMode.list;

	private boolean isHtml = false;
	private boolean checkAll = false;

	private MessageInbox replyMessageVo = null;
	private List<MessageInbox> messageThreads = null;
	private List<SessionUpload> uploads = null;
	
	private transient UIInput fromAddrInput = null;
	private transient UIInput toAddrInput = null;

	private MessageRfcField rfcFields = null;

	private final SearchFieldsVo searchVo = new SearchFieldsVo(getPagingVo());

	private String newRuleName = "";
	
	private javax.servlet.http.Part file;

	private static String TO_SELF = null;
	private static String TO_FAILED = TO_SELF;
	private static String TO_EDIT = "msgInboxView.xhtml";
	private static String TO_LIST = "msgInboxList.xhtml";
	private static String TO_SEND = "msgInboxSend.xhtml";
	private static String TO_DELETED = TO_LIST;
	private static String TO_CANCELED = TO_LIST;
	private static String TO_FORWARD = TO_SEND;
	private static String TO_REPLY = TO_SEND;
	private static String TO_CLOSED = TO_LIST;

	@Override
	protected void refresh() {
		folder = null;
		replyMessageVo = null;
		messageThreads = null;
		isHtml = false;
		checkAll = false;
	}

	public SearchFieldsVo getSearchFieldsVo() {
		return getSearchVo();
	}

	public SearchFieldsVo getSearchVo() {
		return searchVo;
	}

	SearchFieldsVo getMenuSearchVo() {
		SearchFieldsVo menuSearchVo = null;
		SimpleMailTrackingMenu menu = (SimpleMailTrackingMenu) FacesUtil.getManagedBean("mailTracking");
		if (menu != null) {
			menuSearchVo = menu.getSearchFieldsVo();
		} else {
			throw new RuntimeException("Failed to get managed bean \"mailTracking\" !");
		}
		return menuSearchVo;
	}

	public DataModel<MessageInbox> getAll() {
		String fromPage = sessionBean.getRequestParam("frompage");
		logger.info("getAll() - fromPage = " + fromPage);
		if (StringUtils.equals(fromPage, "main")) {
			resetPagingVo();
		}
		SimpleMailTrackingMenu menu = (SimpleMailTrackingMenu) FacesUtil.getManagedBean("mailTracking");
		if (menu != null) {
			SearchFieldsVo menuSearchVo = menu.getSearchFieldsVo();
			// logger.info("Menu SearchFieldVo: " + menuSearchVo);
			// logger.info("Inbox SearchFieldVo: " + getSearchVo());
			if (!menuSearchVo.equalsLevel1(getSearchVo())) {
				if (menuSearchVo.getPagingVo().getLogList().size() > 0) {
					logger.info("getAll() - Search criteria changes:  After <-> Before" + LF
							+ menuSearchVo.getPagingVo().listChanges());
				}
				menuSearchVo.copyLevel1To(getSearchVo());
				resetPagingVo();
			}
		}
		if (!getPagingVo().getPageAction().equals(PageAction.CURRENT) || folder == null) {
			// logger.info("SearchVo Before: " + getSearchVo());
			// retrieve rows based on page action
			getPagingVo().setOrderBy(PagingVo.Column.receivedTime, false);
			// getRowCount();
			if (DisplaySearchVo) {
				logger.info("SearchVo After: " + PrintUtil.prettyPrintRecursive(getSearchVo()));
			}
			List<MessageInbox> msgInboxList = getMessageInboxService().getListForWeb(getSearchVo());
			// reset page action
			getPagingVo().setPageAction(PageAction.CURRENT);
			// wrap the list into PagedListDataModel
			folder = new ListDataModel<MessageInbox>(msgInboxList);
		}
		return folder;
	}

	@Override
	public long getRowCount() {
		long rowCount = getMessageInboxService().getRowCountForWeb(getSearchVo());
		getPagingVo().setRowCount(rowCount);
		return rowCount;
	}

	public String getFromDisplayName(String fromAddrRowId) {
		// logger.info("getFromDisplayName() - fromAddrRowId: " + fromAddrRowId);
		EmailAddress addr = getEmailAddressService().getByRowId(Integer.parseInt(fromAddrRowId));
		if (addr == null) {
			return "";
		}
		if (EmailAddrUtil.hasDisplayName(addr.getAddress())) {
			return EmailAddrUtil.getDisplayName(addr.getAddress());
		} else {
			return addr.getAddress();
		}
	}

	public String getEmailAddress(String addressRowId) {
		EmailAddress addr = getEmailAddressService().getByRowId(Integer.parseInt(addressRowId));
		if (addr != null) {
			return addr.getAddress();
		} else {
			return "";
		}
	}

	public String getRuleName(String ruleLogicRowId) {
		RuleLogic rule = getRuleLogicService().getByRowId(Integer.parseInt(ruleLogicRowId));
		if (rule != null) {
			return rule.getRuleName();
		} else {
			return "";
		}
	}

	public String getLevelPrefix(String level) {
		int threadLevel = Integer.parseInt(level);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < threadLevel; i++) {
			sb.append("&nbsp;&nbsp;"); // &bull;"); //&sdot;");
		}
		return sb.toString();
	}

	private void reset() {
		fromAddrInput = null;
		toAddrInput = null;
	}

	public void refreshClickedListener(AjaxBehaviorEvent event) {
		resetPagingVo();
		return; // TO_SELF;
	}

	public void viewAllListener(AjaxBehaviorEvent event) {
		logger.info("Entering viewAllListener()...");
		viewAll();
	}

	public String viewAll() {
		resetPagingVo();
		getSearchVo().resetFlags();
		getMenuSearchVo().resetFlags();
		return TO_SELF;
	}

	public void viewUnreadListener(AjaxBehaviorEvent event) {
		logger.info("Entering viewUnreadListener()...");
		resetPagingVo();
		getSearchVo().resetFlags();
		getSearchVo().setIsRead(Boolean.valueOf(false));
		getMenuSearchVo().resetFlags();
		getMenuSearchVo().setIsRead(Boolean.valueOf(false));
		return; // TO_SELF;
	}

	public void viewReadListener(AjaxBehaviorEvent event) {
		logger.info("Entering viewReadListener()...");
		resetPagingVo();
		getSearchVo().resetFlags();
		getSearchVo().setIsRead(Boolean.valueOf(true));
		getMenuSearchVo().resetFlags();
		getMenuSearchVo().setIsRead(Boolean.valueOf(true));
		return; // TO_SELF;
	}

	public void viewFlaggedListener(AjaxBehaviorEvent event) {
		logger.info("Entering viewFlaggedListener()...");
		resetPagingVo();
		getSearchVo().resetFlags();
		getSearchVo().setIsFlagged(Boolean.valueOf(true));
		getMenuSearchVo().resetFlags();
		getMenuSearchVo().setIsFlagged(Boolean.valueOf(true));
		return; // TO_SELF;
	}

	private void clearUploads() {
		String sessionId = FacesUtil.getSessionId();
		if (uploads != null) {
			uploads.clear();
		}
		int rowsDeleted = getSessionUploadService().deleteBySessionId(sessionId);
		logger.info("clearUploads() - SessionId: " + sessionId + ", rows deleted: " + rowsDeleted);
		uploads = null;
	}

	public void viewMessageListener(AjaxBehaviorEvent event) {
		viewMessage();
	}

	public String viewMessage() {
		if (isDebugEnabled)
			logger.debug("viewMessage() - Entering...");
		if (folder == null) {
			logger.warn("viewMessage() - Inbox fodler is null.");
			return TO_FAILED;
		}
		if (!folder.isRowAvailable()) {
			logger.warn("viewMessage() - Inbox folder Row not available.");
			return TO_FAILED;
		}
		clearUploads(); // clear session upload records
		MessageInbox webVo = (MessageInbox) folder.getRowData();

		webVo.setReadCount(webVo.getReadCount() + 1);
		// update ReadCount
		getMessageInboxService().updateReadCount(webVo);
		logger.info("viewMessage() - Message updated: " + webVo.getRowId());

		return viewMessage(webVo.getRowId());
	}

	private String viewMessage(int rowId) {
		// retrieve other message properties including attachments
		message = getMessageInboxService().getAllDataByPrimaryKey(rowId);
		// logger.info(StringUtil.prettyPrint(message, 1));

		String contentType = message.getBodyContentType();
		if (contentType != null && contentType.toLowerCase().startsWith("text/html")) {
			// set default value for HTML check box
			setHtml(true);
		}
		if (message.getMessageAttachmentList() != null) {
			// empty attachment bodies to reduce HTTP session size
			for (MessageAttachment vo : message.getMessageAttachmentList()) {
				if (vo.getAttachmentValue() != null) {
					vo.setAttachmentSize(vo.getAttachmentValue().length);
					// vo.setAttachmentValue(null); // this updates the database
				}
			}
		}
		if (isInfoEnabled) {
			logger.info("viewMessage() - Message to be viewed: " + message.getMsgSubject() + ", " + message.getRowId());
		}

		if (!message.getMessageRfcFieldList().isEmpty()) {
			rfcFields = message.getMessageRfcFieldList().get(0);
		} else {
			rfcFields = null;
		}

		message.setMarkedForEdition(true);
		editMode = true;
		beanMode = BeanMode.edit;
		// message.setReadCount(message.getReadCount() + 1);
		// // update ReadCount
		// getMessageInboxService().updateReadCount(message);
		// logger.info("viewMessage() - Message updated: " + message.getRowId());
		// fetch message threads
		List<MessageInbox> threads = getMessageInboxService().getByLeadMsgId(message.getLeadMessageRowId());
		if (threads != null && threads.size() > 1) {
			messageThreads = MessageThreadsBuilder.buildThreads(threads);
		} else {
			messageThreads = null;
		}
		if (isDebugEnabled) {
			// logger.debug("viewMessage() - MessageInbox to be passed to view page: " + message);
			logger.debug("viewMessage() - MessageInbox to be passed to jsp: " + LF + "Msg RowId: " + message.getRowId()
					+ LF + "Msg Status: " + message.getStatusId() + LF + "Number of Attachments: "
					+ message.getAttachmentCount() + LF + "Subject: " + message.getMsgSubject() + LF + "Message Body: "
					+ LF + message.getMsgBody());
		}
		return TO_EDIT;
	}

	public void viewThreadListener(AjaxBehaviorEvent event) {
		viewThread();
	}

	public String viewThread() {
		String msgId = FacesUtil.getRequestParameter("msgThreadId");
		logger.info("viewThread() - msgRowId: " + msgId);
		if (msgId == null) {
			return null;
		}

		return viewMessage(Integer.parseInt(msgId));
	}

	public void deleteMessagesListener(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("deleteMessages() - Entering...");
		if (folder == null) {
			logger.warn("deleteMessages() - MsgInbox is null.");
			return; // TO_FAILED;
		}
		List<MessageInbox> list = getMessageList();
		for (int i = 0; i < list.size(); i++) {
			MessageInbox vo = list.get(i);
			if (vo.isMarkedForDeletion()) {
				int rowsDeleted = getMessageInboxService().deleteByRowId(vo.getRowId());
				vo.setMarkedForDeletion(false);
				if (rowsDeleted > 0) {
					logger.info("deleteMessages() - Mailbox message deleted: " + vo.getRowId());
					getPagingVo().setRowCount(getPagingVo().getRowCount() - rowsDeleted);
				}
			}
		}
		refresh();
		// return TO_SELF;
	}

	public String deleteMessage() {
		if (message == null) {
			logger.error("deleteMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		int rowsDeleted = getMessageInboxService().deleteByRowId(message.getRowId());
		if (rowsDeleted > 0) {
			logger.info("deleteMessage() - Mailbox message deleted: " + message.getRowId());
			getPagingVo().setRowCount(getPagingVo().getRowCount() - rowsDeleted);
		}
		getMessageList().remove(message);
		refresh();
		beanMode = BeanMode.list;
		return TO_DELETED;
	}

	public String attachFiles() {
		String pageUrl = "/upload/msgInboxAttachFiles.jsp?frompage=msgreply";
		@SuppressWarnings("static-access")
		FacesContext context = broker.getContext().getCurrentInstance();
		ExternalContext ectx = context.getExternalContext();
		try {
			ectx.redirect(ectx.encodeResourceURL(ectx.getRequestContextPath() + pageUrl));
		} catch (IOException e) {
			logger.error("attachFiles() - IOException caught", e);
			throw new FacesException("Cannot redirect to " + pageUrl + " due to IO exception.", e);
		}
		return null;
	}

	public void validateFile(FacesContext ctx, UIComponent comp, Object value) {
		List<FacesMessage> msgs = new ArrayList<FacesMessage>();
		
		try {
			javax.servlet.http.Part file = (javax.servlet.http.Part) value;
			if (file.getSize() > (256 * 1024)) { // limit to 256KB
				FacesMessage msg = jpa.msgui.util.MessageUtil.getMessage("jpa.msgui.messages", "uploadFileTooBig",
						new String[] { "256kb" });
				msgs.add(msg);
			}
		}
		catch (Exception e) {
			throw new ValidationException(e);
		}
		if (!msgs.isEmpty()) {
			throw new ValidatorException(msgs);
		}
	}
	
	
	public void uploadFileListener(AjaxBehaviorEvent event) {
		uploadFile();
	}
	
	public String uploadFile() {
        logger.info("uploadFile() Enetring...");
        
        String fileName = file.getName();
        for (String hdrName : file.getHeaderNames()) {
        	logger.info("Header mame / value: " + hdrName + " / " + file.getHeader(hdrName));
        	if (StringUtils.contains(hdrName, "content-disposition")) {
        		String parsedName = parseFileName(file.getHeader(hdrName));
        		if (StringUtils.isNotBlank(parsedName)) {
        			fileName = parsedName;
        		}
        	}
        }
        
        String contentType = file.getContentType();
        logger.info("content-type: " + contentType);
        logger.info("filename: " + fileName);
        logger.info("size: " + file.getSize());
        
        SessionUpload sessVo = new SessionUpload();
    	SessionUploadPK pk = new SessionUploadPK();
    	sessVo.setSessionUploadPK(pk);
    	sessVo.getSessionUploadPK().setSessionSequence(0); // ignored by insertLast() method
    	
        String sessionId = FacesUtil.getSessionId();   	
    	sessVo.getSessionUploadPK().setSessionId(sessionId);
        
        UserData userVo = (UserData) FacesUtil.getLoginUserData();
        if (userVo != null) {
        	sessVo.setUserData(userVo);
        }
        else {
        	logger.warn("process() - UserData not found in httpSession!");
        }
    	sessVo.setFileName(fileName);
    	sessVo.setContentType(contentType);
    	
    	try {
	    	InputStream is = file.getInputStream();
	    	sessVo.setSessionValue(IOUtils.toByteArray(is));
	        // Write uploaded file to database
	    	getSessionUploadService().insertLast(sessVo);
	    	logger.info("process() - rows inserted: " + 1);
			//uploads = retrieveUploadFiles(); // TODO only retrieve the one inserted
			if (uploads == null) {
				uploads = new ArrayList<SessionUpload>();
			}
			uploads.add(sessVo);
		}
		catch (IOException ex) {
           logger.error("IOException caught", ex);
        }
    	
		FacesMessage message = jpa.msgui.util.MessageUtil.getMessage("jpa.msgui.messages", "uploadFileResult",
				new String[] { fileName });
		message.setSeverity(FacesMessage.SEVERITY_WARN);
        return TO_SELF;
    }
	
	private String parseFileName(String headerValue) {
		Pattern p = Pattern.compile("filename=[\"']?([\\w\\s\\.,-]{1,100})[\"']?",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(headerValue);
		if (m.find() && m.groupCount() >= 1) {
			for (int i = 0; i <= m.groupCount(); i++) {
				//System.out.println("Group[" + i + "]: " + m.group(i));
			}
			return m.group(1);
		}
		return null;
	}


	public void markAsRead(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("markAsRead() - Entering...");
		if (folder == null) {
			logger.warn("markAsRead() - Inbox folder is null.");
			return; // TO_FAILED;
		}
		List<MessageInbox> list = getMessageList();
		// update Read Count
		for (Iterator<MessageInbox> it = list.iterator(); it.hasNext();) {
			MessageInbox vo = it.next();
			if (vo.isMarkedForDeletion()) {
				vo.setMarkedForDeletion(false);
				if (vo.getReadCount() <= 0) {
					vo.setReadCount(1);
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
					getMessageInboxService().updateReadCount(vo);
					logger.info("markAsRead() - Message updated: " + vo.getRowId());
				}
			}
		}
		return; // TO_SELF;
	}

	public void markAsUnread(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("markAsUnread() - Entering...");
		if (folder == null) {
			logger.warn("markAsUnread() - MsgInbox is null.");
			return; // TO_FAILED;
		}
		List<MessageInbox> list = getMessageList();
		// update Read Count
		for (Iterator<MessageInbox> it = list.iterator(); it.hasNext();) {
			MessageInbox vo = it.next();
			if (vo.isMarkedForDeletion()) {
				vo.setMarkedForDeletion(false);
				if (vo.getReadCount() > 0) {
					vo.setReadCount(0);
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
					getMessageInboxService().updateReadCount(vo);
					logger.info("markAsUnread() - Message updated: " + vo.getRowId());
				}
			}
		}
		return; // TO_SELF;
	}

	public void markAsFlagged(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("markAsFlagged() - Entering...");
		if (folder == null) {
			logger.warn("markAsFlagged() - MsgInbox is null.");
			return; // TO_FAILED;
		}
		List<MessageInbox> list = getMessageList();
		// update Flagged
		for (Iterator<MessageInbox> it = list.iterator(); it.hasNext();) {
			MessageInbox vo = it.next();
			if (vo.isMarkedForDeletion()) {
				vo.setMarkedForDeletion(false);
				if (!vo.isFlagged()) {
					vo.setFlagged(true);
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
					getMessageInboxService().updateIsFlagged(vo);
					logger.info("markAsFlagged() - Message updated: " + vo.getRowId());
				}
			}
		}
		return; // TO_SELF;
	}

	public void markAsUnflagged(AjaxBehaviorEvent event) {
		if (isDebugEnabled)
			logger.debug("markAsUnflagged() - Entering...");
		if (folder == null) {
			logger.warn("markAsUnflagged() - MsgInbox is null.");
			return; // TO_FAILED;
		}
		List<MessageInbox> list = getMessageList();
		// update Flagged
		for (Iterator<MessageInbox> it = list.iterator(); it.hasNext();) {
			MessageInbox vo = it.next();
			if (vo.isMarkedForDeletion()) {
				vo.setMarkedForDeletion(false);
				if (vo.isFlagged()) {
					vo.setFlagged(false);
					vo.setUpdtUserId(FacesUtil.getLoginUserId());
					getMessageInboxService().updateIsFlagged(vo);
					logger.info("markAsUnflagged() - Message updated: " + vo.getRowId());
				}
			}
		}
		return; // TO_SELF;
	}

	public void replyMessageListener(AjaxBehaviorEvent event) {
		replyMessage();
	}

	public String replyMessage() {
		if (message == null) {
			logger.error("replyMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		try {
			replyMessageVo = new MessageInbox();
			message.copyPropertiesTo(replyMessageVo);
		} catch (Exception e) {
			logger.error("BeanUtils.copyProperties() failed: ", e);
			return TO_FAILED;
		}
		replyMessageVo.setReply(true);
		replyMessageVo.setComposeFromAddress(message.getToAddress().getAddress());
		replyMessageVo.setComposeToAddress(message.getFromAddress().getAddress());
		replyMessageVo.setMsgSubject("Re:" + message.getMsgSubject());
		replyMessageVo.setMsgBody(getReplyEnvelope() + message.getMsgBody());
		reset(); // avoid carrying over the current bound value

		// retrieve uploaded files
		retrieveUploadFiles();
		beanMode = BeanMode.send;
		return TO_REPLY;
	}

	public void closeMessageListener(AjaxBehaviorEvent event) {
		closeMessage();
	}

	public String closeMessage() {
		if (message == null) {
			logger.error("closeMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		message.setStatusId(MsgStatusCode.CLOSED.getValue());
		message.setUpdtUserId(FacesUtil.getLoginUserId());
		MessageFolder folder = getMessageFolderService().getOneByFolderName(FolderEnum.Closed.name());
		if (folder != null) {
			message.setMessageFolder(folder);
		}
		getMessageInboxService().update(message);
		logger.info("closeMessage() - Mailbox message closed: " + message.getRowId());
		getPagingVo().setRowCount(getPagingVo().getRowCount() - 1);
		refresh();
		beanMode = BeanMode.list;
		return TO_CLOSED;
	}

	public void closeThreadListener(AjaxBehaviorEvent event) {
		closeThread();
	}

	public String closeThread() {
		if (message == null) {
			logger.error("closeThread() - MessageInbox is null");
			return TO_FAILED;
		}
		message.setStatusId(MsgStatusCode.CLOSED.getValue());
		message.setUpdtUserId(FacesUtil.getLoginUserId());
		int rowsUpdated = getMessageInboxService().closeMessagesByLeadMsgId(message);
		if (rowsUpdated > 0) {
			logger.info("closeThread() - messages closed (LeadMsgId): " + message.getLeadMessageRowId());
			getPagingVo().setRowCount(getPagingVo().getRowCount() - rowsUpdated);
		}
		refresh();
		beanMode = BeanMode.list;
		return TO_CLOSED;
	}

	public void openMessageListener(AjaxBehaviorEvent event) {
		openMessage();
	}

	public String openMessage() {
		if (message == null) {
			logger.error("closeMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		message.setStatusId(MsgStatusCode.OPENED.getValue());
		message.setUpdtUserId(FacesUtil.getLoginUserId());
		if (MsgDirectionCode.RECEIVED.getValue().equals(message.getMsgDirection())) {
			// move message to Received folder
			getMessageInboxService().moveMessageToAnotherFolder(message, FolderEnum.Inbox.name());
		} else {
			// move message to Sent folder
			getMessageInboxService().moveMessageToAnotherFolder(message, FolderEnum.Sent.name());
		}
		logger.info("openMessage() - Mailbox message opened: " + message.getRowId());
		getPagingVo().setRowCount(getPagingVo().getRowCount() - 1);
		refresh();
		beanMode = BeanMode.list;
		return TO_CLOSED;
	}

	public void reassignRuleListener(AjaxBehaviorEvent event) {
		reassignRule();
	}

	public String reassignRule() {
		if (message == null) {
			logger.error("reassignRule() - MessageInbox is null");
			return TO_FAILED;
		}
		if (StringUtils.equals(message.getRuleLogic().getRuleName(), newRuleName)) {
			return null;
		}
		// 1) send the message to rule-engine queue with new rule name
		try {
			MessageBean msgBean = getMessageBeanBo().createMessageBean(message); // msgData);
			TaskBaseBo assignRuleBo = (TaskBaseBo) SpringUtil.getWebAppContext().getBean(AssignRuleName.class);
			MessageContext ctx = new MessageContext(msgBean);
			ctx.setTaskArguments(newRuleName); // message.getRuleLogic().getRuleName());
			msgBean.setSendDate(new java.util.Date());
			assignRuleBo.process(ctx);
			logger.info("reassignRule() - assign rule to: " + newRuleName); // message.getRuleLogic().getRuleName());
			message = getMessageInboxService().getAllDataByPrimaryKey(message.getRowId());
			if (message.getRuleLogic() != null) {
				logger.info("reassignRule() - rule name after: " + message.getRuleLogic().getRuleName());
			}
		} catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			return TO_FAILED;
		} catch (Exception e) {
			logger.error("Exception caught", e);
			return TO_FAILED;
		}
		// 2) close the current message
		return closeMessage();
	}

	public List<SessionUpload> retrieveUploadFiles() {
		String sessionId = FacesUtil.getSessionId();
		boolean valid = FacesUtil.isSessionIdValid();
		logger.info("retrieveUploadFiles() - SessionId: " + sessionId + ", Valid? " + valid);
		uploads = getSessionUploadService().getBySessionId(sessionId);
		if (isDebugEnabled && uploads != null) {
			logger.debug("retrieveUploadFiles() - files retrieved: " + uploads.size());
		}
		return uploads;
	}

	private String getReplyEnvelope() {
		StringBuffer sb = new StringBuffer();
		sb.append(LF + LF);
		sb.append(Constants.MSG_DELIMITER_BEGIN + message.getFromAddress().getAddress() + Constants.MSG_DELIMITER_END);
		sb.append(LF + LF);
		sb.append(Constants.DASHES_OF_33 + LF);
		return sb.toString();
	}

	public void forwardMessageListener(AjaxBehaviorEvent event) {
		forwardMessage();
	}

	public String forwardMessage() {
		if (message == null) {
			logger.error("forwardMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		try {
			replyMessageVo = new MessageInbox();
			message.copyPropertiesTo(replyMessageVo);
		} catch (Exception e) {
			logger.error("BeanUtils.copyProperties() failed: ", e);
			return TO_FAILED;
		}
		replyMessageVo.setForward(true);
		replyMessageVo.setComposeFromAddress(message.getToAddress().getAddress());
		replyMessageVo.setComposeToAddress("");
		replyMessageVo.setMsgSubject("Fwd:" + message.getMsgSubject());
		replyMessageVo.setMsgBody(getForwardEnvelope() + message.getMsgBody());
		reset(); // avoid carrying over the current bound value
		beanMode = BeanMode.send;
		return TO_FORWARD;
	}

	private String getForwardEnvelope() {
		StringBuffer sb = new StringBuffer();
		sb.append(LF + LF);
		sb.append(Constants.MSG_DELIMITER_BEGIN + message.getFromAddress().getAddress() + Constants.MSG_DELIMITER_END + LF);
		sb.append(LF);
		sb.append("> From: " + message.getFromAddress().getAddress() + LF);
		sb.append("> To: " + message.getToAddress().getAddress() + LF);
		sb.append("> Date: " + message.getReceivedTime() + LF);
		sb.append("> Subject: " + message.getMsgSubject() + LF);
		sb.append(">" + LF + LF);
		sb.append(Constants.DASHES_OF_33 + LF);
		return sb.toString();
	}
	
	public void removeUploadFileListener(AjaxBehaviorEvent event) {
		removeUploadFile();
	}

	public String removeUploadFile() {
		String seq = FacesUtil.getRequestParameter("seq");
		String name = FacesUtil.getRequestParameter("name");
		String id = FacesUtil.getSessionId();
		logger.info("removeUploadFile() - id/seq/name: " + id + "/" + seq + "/" + name);
		try {
			int sessionSeq = Integer.parseInt(seq);
			for (int i = 0; uploads != null && i < uploads.size(); i++) {
				SessionUpload vo = uploads.get(i);
				if (sessionSeq == vo.getSessionUploadPK().getSessionSequence()) {
					uploads.remove(i);
					break;
				}
			}
			SessionUploadPK pk = new SessionUploadPK(id, sessionSeq);
			int rowsDeleted = getSessionUploadService().deleteByPrimaryKey(pk);
			logger.info("removeUploadFile() - rows deleted: " + rowsDeleted + ", file name: " + name);
		} catch (RuntimeException e) {
			logger.error("RuntimeException caught", e);
		}
		return TO_SELF;
	}

	public void sendMessageListener(AjaxBehaviorEvent event) {
		sendMessage();
	}

	public String sendMessage() {
		if (message == null) {
			logger.error("sendMessage() - MessageInbox is null");
			return TO_FAILED;
		}
		if (replyMessageVo == null) {
			logger.error("sendMessage() - replyMessageVo is null");
			return TO_FAILED;
		}
		// make sure we have all the data to rebuild a message bean
		// retrieve original message
		MessageInbox msgData = message; // getMessageInboxBo().getMessageByPK(message.getRowId());
		if (msgData == null) {
			logger.error("sendMessage() - Original message has been deleted, msgId: " + message.getRowId());
			return TO_FAILED;
		}
		Integer msgsSent = null;
		try {
			// retrieve original message
			MessageBean messageBean = getMessageBeanBo().createMessageBean(msgData);
			// retrieve new addresses
			Address[] from = InternetAddress.parse(replyMessageVo.getComposeFromAddress());
			Address[] to = InternetAddress.parse(replyMessageVo.getComposeToAddress());
			// retrieve new message body
			String msgBodyText = replyMessageVo.getMsgBody();
			msgBodyText = msgBodyText == null ? "" : msgBodyText; // just for safety
			String origContentType = messageBean.getBodyContentType();
			if (origContentType == null) { // should never happen
				origContentType = "text/plain";
			}
			String replyMsg = null;
			// remove original message from message body
			int pos1 = msgBodyText.indexOf(Constants.MSG_DELIMITER_BEGIN);
			int pos2 = msgBodyText.indexOf(Constants.MSG_DELIMITER_END, pos1 + 1);
			if (pos1 >= 0 && pos2 > pos1) {
				replyMsg = msgBodyText.substring(0, pos1);
				int pos3 = msgBodyText.indexOf(Constants.DASHES_OF_33, pos2 + 1);
				if (pos3 > pos2) {
					String origMsg = msgBodyText.substring(pos3 + Constants.DASHES_OF_33.length());
					logger.info("Orig Msg: " + origMsg);
				} else {
					String origMsg = msgBodyText.substring(pos2 + Constants.MSG_DELIMITER_END.length());
					logger.info("Orig Msg: " + origMsg);
				}
			} else {
				replyMsg = msgBodyText;
			}
			// construct messageBean for new message
			if (replyMessageVo.isForward()) { // forward
				// leave body content type unchanged
				byte[] bytes = messageBean.getBodyNode().getValue();
				if (replyMsg.trim().length() > 0) {
					// append original message's headers to new message body
					replyMsg += MessageBodyBuilder.constructOriginalHeader(messageBean,
							origContentType.indexOf("html") >= 0);
				}
				// append original message
				messageBean.getBodyNode().setValue(replyMsg + new String(bytes));
				// set addresses and subject
				messageBean.setFrom(from);
				messageBean.setTo(to);
				// use new subject
				messageBean.setSubject(replyMessageVo.getMsgSubject());
				if (StringUtils.isBlank(messageBean.getSenderId())) {
					messageBean.setSenderId(FacesUtil.getLoginUserSenderId());
				}
				// process the message
				TaskBaseBo forwardBo = (TaskBaseBo) SpringUtil.getWebAppContext().getBean("forwardMessage");
				MessageContext ctx = new MessageContext(messageBean);
				ctx.setTaskArguments("$" + EmailAddrType.TO_ADDR.getValue());
				msgsSent = (Integer) forwardBo.process(ctx);
				logger.info("sendMessage() - Message to send:\n" + messageBean);
			} else { // reply
				MessageBean mBean = new MessageBean();
				mBean.setOriginalMail(messageBean);
				// mBean.setBody(msgBody); // new message body
				String contentType = origContentType;
				if (origContentType.startsWith("text/plain") && isHtml) {
					contentType = "text/html";
				}
				// retrieve upload files
				String sessionId = FacesUtil.getSessionId();
				List<SessionUpload> list = getSessionUploadService().getBySessionId(sessionId);
				if (list != null && list.size() > 0) {
					// construct multipart
					mBean.setContentType("multipart/mixed");
					// message body part
					BodypartBean aNode = new BodypartBean();
					aNode.setContentType(contentType);
					aNode.setValue(replyMsg);
					aNode.setSize(replyMsg.length());
					mBean.put(aNode);
					// message attachments
					for (int i = 0; i < list.size(); i++) {
						SessionUpload vo = list.get(i);
						BodypartBean subNode = new BodypartBean();
						subNode.setContentType(vo.getContentType());
						subNode.setDisposition(javax.mail.Part.ATTACHMENT);
						subNode.setDescription(vo.getFileName());
						byte[] bytes = vo.getSessionValue();
						subNode.setValue(bytes);
						if (bytes != null) {
							subNode.setSize(bytes.length);
						} else {
							subNode.setSize(0);
						}
						mBean.put(subNode);
						mBean.updateAttachCount(1);
						mBean.getComponentsSize().add(Integer.valueOf(subNode.getSize()));
					}
					// remove uploaded files from session table
					clearUploads();
				} else {
					mBean.setContentType(contentType);
					mBean.setBody(replyMsg);
				}
				// set addresses and subject
				mBean.setFrom(from);
				mBean.setTo(to);
				mBean.setSubject(replyMessageVo.getMsgSubject());
				mBean.setSenderId(FacesUtil.getLoginUserSenderId());
				// process the message
				TaskBaseBo csrReplyBo = (TaskBaseBo) SpringUtil.getWebAppContext().getBean("csrReplyMessage");
				MessageContext ctx = new MessageContext(mBean);
				msgsSent = (Integer) csrReplyBo.process(ctx);
				logger.info("sendMessage() - Message to send:" + LF + mBean);
			}
		} catch (DataValidationException e) {
			logger.error("DataValidationException caught", e);
			return TO_FAILED;
		} catch (AddressException e) {
			logger.error("AddressException caught", e);
			return TO_FAILED;
		} catch (Exception e) {
			logger.error("Exception caught", e);
			return TO_FAILED;
		}
		// update replyCount or forwardCount
		if (msgsSent != null && msgsSent.intValue() > 0) {
			if (replyMessageVo.isReply()) {
				message.setReplyCount(message.getReplyCount() + 1);
			}
			if (replyMessageVo.isForward()) {
				message.setForwardCount(message.getForwardCount() + 1);
			}
			getMessageInboxService().update(message);
			logger.info("sendMessage() - Message updated: " + message.getRowId());
		}
		beanMode = BeanMode.list;
		refresh();
		return TO_LIST;
	}

	public void cancelViewListener(AjaxBehaviorEvent event) {
		beanMode = BeanMode.list;
	}

	public void cancelSendListener(AjaxBehaviorEvent event) {
		cancelSend();
	}

	public String cancelSend() {
		replyMessageVo = null;
		beanMode = BeanMode.edit;
		return TO_CANCELED;
	}

	public boolean getAnyMessagesMarkedForDeletion() {
		if (isDebugEnabled)
			logger.debug("getAnyMessagesMarkedForDeletion() - Entering...");
		if (folder == null) {
			logger.warn("getAnyMessagesMarkedForDeletion() - MsgInbox is null.");
			return false;
		}
		List<MessageInbox> list = getMessageList();
		for (Iterator<MessageInbox> it = list.iterator(); it.hasNext();) {
			MessageInbox vo = it.next();
			if (vo.isMarkedForDeletion()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Validate FROM email address
	 * 
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validateFromAddress(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateFromAddress() - From Address: " + value);
		String fromAddr = (String) value;
		if (!isValidEmailAddress(fromAddr)) {
			// invalid email address
			FacesMessage message = jpa.msgui.util.MessageUtil.getMessage("jpa.msgui.messages", "invalidEmailAddress",
					new String[] { fromAddr });
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	/**
	 * Validate TO email address
	 * 
	 * @param context
	 * @param component
	 * @param value
	 */
	public void validateToAddress(FacesContext context, UIComponent component, Object value) {
		if (isDebugEnabled)
			logger.debug("validateToAddress() - To Address: " + value);
		String toAddr = (String) value;
		if (!isValidEmailAddress(toAddr)) {
			// invalid email address
			FacesMessage message = jpa.msgui.util.MessageUtil.getMessage("jpa.msgui.messages", "invalidEmailAddress",
					new String[] { toAddr });
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}

	private boolean isValidEmailAddress(String addrs) {
		List<String> list = getAddressList(addrs);
		for (int i = 0; i < list.size(); i++) {
			if (!EmailAddrUtil.isRemoteOrLocalEmailAddress(list.get(i))) {
				return false;
			}
		}
		return true;
	}

	private List<String> getAddressList(String addrs) {
		List<String> list = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(addrs, ",");
		while (st.hasMoreTokens()) {
			String addr = st.nextToken();
			list.add(EmailAddrUtil.removeDisplayName(addr, true));
		}
		return list;
	}

	@SuppressWarnings({ "unchecked" })
	private List<MessageInbox> getMessageList() {
		if (folder == null) {
			return new ArrayList<MessageInbox>();
		} else {
			return (List<MessageInbox>) folder.getWrappedData();
		}
	}

	public FacesBroker getBroker() {
		return broker;
	}

	public void setBroker(FacesBroker broker) {
		this.broker = broker;
	}

	public MessageInboxService getMessageInboxService() {
		if (msgInboxDao == null) {
			msgInboxDao = SpringUtil.getWebAppContext().getBean(MessageInboxService.class);
		}
		return msgInboxDao;
	}

	public void setMessageInboxService(MessageInboxService msgInboxDao) {
		this.msgInboxDao = msgInboxDao;
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

	public RuleLogicService getRuleLogicService() {
		if (ruleLogicDao == null) {
			ruleLogicDao = SpringUtil.getWebAppContext().getBean(RuleLogicService.class);
		}
		return ruleLogicDao;
	}

	public void setRuleLogicService(RuleLogicService ruleLogicDao) {
		this.ruleLogicDao = ruleLogicDao;
	}

	public MessageInboxBo getMessageInboxBo() {
		if (msgInboxBo == null) {
			msgInboxBo = SpringUtil.getWebAppContext().getBean(MessageInboxBo.class);
		}
		return msgInboxBo;
	}

	public void setMessageInboxBo(MessageInboxBo msgInboxBo) {
		this.msgInboxBo = msgInboxBo;
	}

	public SessionUploadService getSessionUploadService() {
		if (sessionUploadDao == null) {
			sessionUploadDao = SpringUtil.getWebAppContext().getBean(SessionUploadService.class);
		}
		return sessionUploadDao;
	}

	public void setSessionUploadService(SessionUploadService sessionUploadDao) {
		this.sessionUploadDao = sessionUploadDao;
	}

	public EntityManagerService getEntityManagerService() {
		if (entityDao == null) {
			entityDao = SpringUtil.getWebAppContext().getBean(EntityManagerService.class);
		}
		return entityDao;
	}

	public void setEntityManagerService(EntityManagerService entityDao) {
		this.entityDao = entityDao;
	}

	public MessageBeanBo getMessageBeanBo() {
		if (msgBeanBo == null) {
			msgBeanBo = SpringUtil.getWebAppContext().getBean(MessageBeanBo.class);
		}
		return msgBeanBo;
	}

	public void setMessageBeanBo(MessageBeanBo msgBeanBo) {
		this.msgBeanBo = msgBeanBo;
	}

	public MessageFolderService getMessageFolderService() {
		if (folderDao == null) {
			folderDao = SpringUtil.getWebAppContext().getBean(MessageFolderService.class);
		}
		return folderDao;
	}

	public MessageInbox getMessage() {
		return message;
	}

	public void setMessage(MessageInbox message) {
		this.message = message;
	}

	public MessageInbox getReplyMessageVo() {
		return replyMessageVo;
	}

	public void setReplyMessageVo(MessageInbox replyMessageVo) {
		this.replyMessageVo = replyMessageVo;
	}

	public List<MessageInbox> getMessageThreads() {
		return messageThreads;
	}

	public void setMessageThreads(List<MessageInbox> messageThreads) {
		this.messageThreads = messageThreads;
	}

	public List<SessionUpload> getUploads() {
		return uploads;
	}

	public void setUploads(List<SessionUpload> uploads) {
		this.uploads = uploads;
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
		} catch (Exception e) {
		}
	}

	public boolean isHtml() {
		return isHtml;
	}

	public void setHtml(boolean isHtml) {
		this.isHtml = isHtml;
	}

	public boolean isCheckAll() {
		return checkAll;
	}

	public void setCheckAll(boolean checkAll) {
		this.checkAll = checkAll;
	}

	public UIInput getFromAddrInput() {
		return fromAddrInput;
	}

	public void setFromAddrInput(UIInput fromAddrInput) {
		this.fromAddrInput = fromAddrInput;
	}

	public UIInput getToAddrInput() {
		return toAddrInput;
	}

	public void setToAddrInput(UIInput toAddrInput) {
		this.toAddrInput = toAddrInput;
	}

	public MessageRfcField getRfcFields() {
		return rfcFields;
	}

	public void setRfcFields(MessageRfcField rfcFields) {
		this.rfcFields = rfcFields;
	}

	public String getNewRuleName() {
		return newRuleName;
	}

	public void setNewRuleName(String newRuleName) {
		this.newRuleName = newRuleName;
	}

	public javax.servlet.http.Part getFile() {
		return file;
	}

	public void setFile(javax.servlet.http.Part file) {
		this.file = file;
	}

}
