package jpa.model.msg;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.constant.CarrierCode;
import jpa.constant.MsgDirectionCode;
import jpa.constant.MsgStatusCode;
import jpa.message.MessageBodyBuilder;
import jpa.model.BaseModel;
import jpa.model.EmailAddress;
import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.model.rule.RuleLogic;
import jpa.msgui.vo.TimestampAdapter;
import jpa.util.StringUtil;

import org.hibernate.annotations.Type;

@Entity
@Table(name="message_inbox"
	// Tomee 1.7.4 does not support JPA 2.1 (JavaEE 6), Considering Tomee 7.0
	, indexes = {
		@Index(columnList = "ReferringMsgRowId", name = "MsgInbox_RefMsg_Idx"),
		@Index(columnList = "LeadMsgRowId", name = "MsgInbox_LeadMsg_Idx"),
		@Index(columnList = "FromAddressRowId", name = "MsgInbox_FromAddr_Idx"),
		@Index(columnList = "ToAddressRowId", name = "MsgInbox_ToAddr_Idx")
	}
)
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="MessageInboxNative",
		entities={
		 @EntityResult(entityClass=MessageInbox.class),
	  	}
	  	),
	})
public class MessageInbox extends BaseModel implements Serializable {
	private static final long serialVersionUID = -5868053593529617642L;

	@Transient
	public static final String MAPPING_MESSAGE_INBOX = "MessageInboxNative";

	/*
	 * Define following fields as individual columns instead of Relationships
	 * to simplify the implementation of cascade delete.
	 */
	@Column(name="ReferringMsgRowId", nullable=true, columnDefinition="Integer")
	private Integer referringMessageRowId;
	@Transient
	private MessageInbox referringMessage;
	
	@Column(name="LeadMsgRowId", nullable=true, columnDefinition="Integer")
	private Integer leadMessageRowId;
	@Transient
	private MessageInbox leadMessage;
	/* end of simplify */

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="FromAddressRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_inbox", foreignKey=@ForeignKey(name="FK_MsgInbox_FromAddrRowId"))
	@XmlTransient
	private EmailAddress fromAddress;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, optional=true)
	@JoinColumn(name="ReplytoAddressRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_inbox", foreignKey=@ForeignKey(name="FK_MsgInbox_RplytoAddrRowId"))
	@XmlTransient
	private EmailAddress replytoAddress;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="ToAddressRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_inbox", foreignKey=@ForeignKey(name="FK_MsgInbox_ToAddrRowId"))
	@XmlTransient
	private EmailAddress toAddress;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, optional=true)
	@JoinColumn(name="SenderDataRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_inbox", foreignKey=@ForeignKey(name="FK_MsgInbox_SenderRowId"))
	@XmlTransient
	private SenderData senderData;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, optional=true)
	@JoinColumn(name="SubscriberDataRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_inbox", foreignKey=@ForeignKey(name="FK_MsgInbox_SbsrDataRowId"))
	@XmlTransient
	private SubscriberData subscriberData;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, optional=true)
	@JoinColumn(name="RuleLogicRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_inbox", foreignKey=@ForeignKey(name="FK_MsgInbox_RuleLogicRowId"))
	@XmlTransient
	private RuleLogic ruleLogic;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, optional=true)
	@JoinColumn(name="MessageRenderedRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_inbox", foreignKey=@ForeignKey(name="FK_MsgInbox_MsgRenderdRowId"))
	@XmlTransient
	private MessageRendered messageRendered;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="MessageFolderRowId",insertable=true,updatable=true,referencedColumnName="row_id",nullable=false,
			table="message_inbox", foreignKey=@ForeignKey(name="FK_MsgInbox_MsgFolderRowId"))
	@XmlTransient
	private MessageFolder messageFolder;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageHeaderPK.messageInbox", orphanRemoval=true)
	@OrderBy
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<MessageHeader> messageHeaderList;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageInbox", orphanRemoval=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<MessageAddress> messageAddressList;

	@OneToOne(cascade={CascadeType.ALL},fetch=FetchType.LAZY,mappedBy="messageInbox", orphanRemoval=true, optional=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
//	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
//	@org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
	private MessageStream messageStream;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageRfcFieldPK.messageInbox", orphanRemoval=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<MessageRfcField> messageRfcFieldList;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageAttachmentPK.messageInbox", orphanRemoval=true)
	@OrderBy
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<MessageAttachment> messageAttachmentList;

	@OneToOne(cascade={CascadeType.ALL},fetch=FetchType.LAZY,mappedBy="messageInbox", orphanRemoval=true, optional=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
//	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
//	@org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
	private MessageUnsubComment messageUnsubComment;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageActionLogPK.messageInbox", orphanRemoval=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<MessageActionLog> messageActionLogList;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="messageDeliveryStatusPK.messageInbox", orphanRemoval=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<MessageDeliveryStatus> messageDeliveryStatusList;

	@Column(nullable=false, length=1, columnDefinition="char")
	private String carrierCode = CarrierCode.SMTPMAIL.getValue();
	@Column(nullable=false, length=1, columnDefinition="char")
	private String msgDirection = MsgDirectionCode.RECEIVED.getValue();
	@Column(nullable=true, length=255)
	private String msgSubject = null;
	@Column(nullable=true, length=16)
	private String msgPriority = null;
	@Column(length=3, nullable=false)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp receivedTime;
	@Column(nullable=true)
	private java.sql.Date purgeDate = null;
	@Column(nullable=false,columnDefinition="smallint")
	private int readCount = 0;
	@Column(nullable=false,columnDefinition="smallint")
	private int replyCount = 0;
	@Column(nullable=false,columnDefinition="smallint")
	private int forwardCount = 0;
	@Column(nullable=false, columnDefinition="boolean")
	private boolean isFlagged = false;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp deliveryTime;
	@Column(nullable=true, length=255)
	private String smtpMessageId = null;
	@Column(nullable=false,columnDefinition="boolean")
	private boolean isOverrideTestAddr = false;
	@Column(nullable=false,columnDefinition="smallint")
	private int attachmentCount = 0;
	@Column(nullable=false)
	private int attachmentSize = 0;
	@Column(nullable=false)
	private int msgBodySize = 0;
	@Column(nullable=false,length=100)
	private String msgContentType = "";
	@Column(nullable=true,length=50)
	private String bodyContentType = null;
	
	/*
	 *  XXX Added @Type annotation to fix PostgreSQL ERROR with Hibernate: operator does not exist: text ~~ bigint
	 *  	or use @Column(columnDefinition = "text") but it requires underlying DB to support "text" column type
	 */
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Column(nullable=true,length=262136)
	private String msgBody = null;
	
	@Version
	@Column(name="opt_lock", nullable=false)
	private long optLock = 0L;

	@Transient
	@XmlTransient
	private int origReadCount = -1;
	@Transient
	@XmlTransient
	private String origStatusId = null;

	
	/*
	 * define properties and methods for UI components 
	 */
	@Transient
	@XmlTransient
	private boolean isReply = false;
	@Transient
	@XmlTransient
	private boolean isForward = false;
	@Transient
	@XmlTransient
	private String composeFromAddress = null;
	@Transient
	@XmlTransient
	private String composeToAddress = null;
	@Transient
	@XmlTransient
	private int threadLevel = -1; // don't change
	@Transient
	@XmlTransient
	private boolean showAllHeaders = false;
	@Transient
	@XmlTransient
	private boolean showRawMessage = false;
	
	
	public String getMsgStatusCodeDesc() { // Status description
		try {
			String desc = getStatusId();
			if (MsgStatusCode.CLOSED.getValue().equals(desc)) {
				desc = "Closed";
			}
			else if (MsgStatusCode.RECEIVED.getValue().equals(desc)) {
				desc = "Received";
			}
			else if (MsgStatusCode.OPENED.getValue().equals(desc)) {
				desc = "Opened";
			}
			else if (MsgStatusCode.PENDING.getValue().equals(desc)) {
				desc = "Pending";
			}
			else if (MsgStatusCode.DELIVERED.getValue().equals(desc)) {
				desc = "Delivered";
			}
			else if (MsgStatusCode.DELIVERY_FAILED.getValue().equals(desc)) {
				desc = "Delivery Failed";
			}
			else { 
				desc = super.getStatusIdDesc();
			}
			return desc;
		}
		catch (Exception e) {
			logger.warn("Exception caught: " + e.getMessage());
			// ignored
			return null;
		}
	}
	/* End of UI */

	
	public boolean isReply() {
		return isReply;
	}

	public void setReply(boolean isReply) {
		this.isReply = isReply;
	}

	public boolean isForward() {
		return isForward;
	}

	public void setForward(boolean isForward) {
		this.isForward = isForward;
	}

	public String getComposeFromAddress() {
		return composeFromAddress;
	}

	public void setComposeFromAddress(String composeFromAddress) {
		this.composeFromAddress = composeFromAddress;
	}

	public String getComposeToAddress() {
		return composeToAddress;
	}

	public void setComposeToAddress(String composeToAddress) {
		this.composeToAddress = composeToAddress;
	}

	public int getThreadLevel() {
		return threadLevel;
	}

	public void setThreadLevel(int threadLevel) {
		this.threadLevel = threadLevel;
	}

	public boolean isShowAllHeaders() {
		return showAllHeaders;
	}

	public void setShowAllHeaders(boolean showAllHeaders) {
		this.showAllHeaders = showAllHeaders;
	}

	public boolean isShowRawMessage() {
		return showRawMessage;
	}

	public void setShowRawMessage(boolean showRawMessage) {
		this.showRawMessage = showRawMessage;
	}

	/**
	 * Email body is displayed in an HTML TextArea field. So HTML tags need to
	 * be removed for HTML message, and PRE tags need to be added for plain text
	 * message.
	 * 
	 * <pre>
	 * check body content type. if text/plain, surround the body text by PRE
	 * tag. if text/html, remove HTML and BODY tags from email body text.
	 * otherwise, return the body text unchanged.
	 * </pre>
	 * 
	 * @return body text
	 */
	public String getDisplayBody() {
		if (bodyContentType == null) bodyContentType = "text/plain";
		if (bodyContentType.toLowerCase().startsWith("text/plain")
				|| bodyContentType.toLowerCase().startsWith("message")) {
			return StringUtil.getHtmlDisplayText(msgBody);
		}
		else if (bodyContentType.toLowerCase().startsWith("text/html")) {
			return MessageBodyBuilder.removeHtmlBodyTags(msgBody);
		}
		else { // unknown type
			return msgBody;
		}
	}

	/**
	 * Always surround raw text by PRE tag as it is displayed in an HTML
	 * TextArea field.
	 * 
	 * @return message raw text
	 */
	public String getRawMessage() {
		if (messageStream == null) {
			// just for safety
			return getDisplayBody();
		}
		else {
			String txt = new String(messageStream.getMsgStream());
			return StringUtil.getHtmlDisplayText(txt);
		}
	}
	
	public int getRows() {
		int rows = msgBodySize / 120;
		return (rows > 20 ? 40 : 20);
	}

	/* end of UI */

	public MessageInbox() {
		// must have a no-argument constructor
	}

	public Integer getReferringMessageRowId() {
		return referringMessageRowId;
	}

	public void setReferringMessageRowId(Integer referringMessageRowId) {
		this.referringMessageRowId = referringMessageRowId;
	}

	public Integer getLeadMessageRowId() {
		return leadMessageRowId;
	}

	public void setLeadMessageRowId(Integer leadMessageRowId) {
		this.leadMessageRowId = leadMessageRowId;
	}

	public List<MessageHeader> getMessageHeaderList() {
		if (messageHeaderList==null) {
			messageHeaderList = new ArrayList<MessageHeader>();
		}
		return messageHeaderList;
	}

	public void setMessageHeaderList(List<MessageHeader> messageHeaderList) {
		this.messageHeaderList = messageHeaderList;
	}

	public List<MessageAddress> getMessageAddressList() {
		if (messageAddressList == null) {
			messageAddressList = new ArrayList<MessageAddress>();
		}
		return messageAddressList;
	}

	public void setMessageAddressList(List<MessageAddress> messageAddressList) {
		this.messageAddressList = messageAddressList;
	}

	public MessageStream getMessageStream() {
		return messageStream;
	}

	public void setMessageStream(MessageStream messageStream) {
		this.messageStream = messageStream;
	}

	public List<MessageRfcField> getMessageRfcFieldList() {
		if (messageRfcFieldList==null) {
			messageRfcFieldList = new ArrayList<MessageRfcField>();
		}
		return messageRfcFieldList;
	}

	public void setMessageRfcFieldList(List<MessageRfcField> messageRfcFieldList) {
		this.messageRfcFieldList = messageRfcFieldList;
	}

	public List<MessageAttachment> getMessageAttachmentList() {
		if (messageAttachmentList==null) {
			messageAttachmentList = new ArrayList<MessageAttachment>();
		}
		return messageAttachmentList;
	}

	public void setMessageAttachmentList(
			List<MessageAttachment> messageAttachmentList) {
		this.messageAttachmentList = messageAttachmentList;
	}

	public MessageUnsubComment getMessageUnsubComment() {
		return messageUnsubComment;
	}

	public void setMessageUnsubComment(MessageUnsubComment messageUnsubComment) {
		this.messageUnsubComment = messageUnsubComment;
	}

	public List<MessageActionLog> getMessageActionLogList() {
		if (messageActionLogList==null) {
			messageActionLogList = new ArrayList<MessageActionLog>();
		}
		return messageActionLogList;
	}

	public void setMessageActionLogList(List<MessageActionLog> messageActionLogList) {
		this.messageActionLogList = messageActionLogList;
	}

	public List<MessageDeliveryStatus> getMessageDeliveryStatusList() {
		if (messageDeliveryStatusList==null) {
			messageDeliveryStatusList = new ArrayList<MessageDeliveryStatus>();
		}
		return messageDeliveryStatusList;
	}

	public void setMessageDeliveryStatusList(
			List<MessageDeliveryStatus> messageDeliveryStatusList) {
		this.messageDeliveryStatusList = messageDeliveryStatusList;
	}

	public String getCarrierCode() {
		return carrierCode;
	}

	public void setCarrierCode(String carrierCode) {
		this.carrierCode = carrierCode;
	}

	public String getMsgDirection() {
		return msgDirection;
	}

	public void setMsgDirection(String msgDirection) {
		this.msgDirection = msgDirection;
	}

	public String getMsgSubject() {
		return msgSubject;
	}

	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}

	public String getMsgPriority() {
		return msgPriority;
	}

	public void setMsgPriority(String msgPriority) {
		this.msgPriority = msgPriority;
	}

	public Timestamp getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(Timestamp receivedTime) {
		this.receivedTime = receivedTime;
	}

	public java.sql.Date getPurgeDate() {
		return purgeDate;
	}

	public void setPurgeDate(java.sql.Date purgeDate) {
		this.purgeDate = purgeDate;
	}

	public int getReadCount() {
		return readCount;
	}

	public void setReadCount(int readCount) {
		this.readCount = readCount;
	}

	public int getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(int replyCount) {
		this.replyCount = replyCount;
	}

	public int getForwardCount() {
		return forwardCount;
	}

	public void setForwardCount(int forwardCount) {
		this.forwardCount = forwardCount;
	}

	public boolean isFlagged() {
		return isFlagged;
	}

	public void setFlagged(boolean isFlagged) {
		this.isFlagged = isFlagged;
	}

	public Timestamp getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(Timestamp deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	public String getSmtpMessageId() {
		return smtpMessageId;
	}

	public void setSmtpMessageId(String smtpMessageId) {
		this.smtpMessageId = smtpMessageId;
	}

	public boolean isOverrideTestAddr() {
		return isOverrideTestAddr;
	}

	public void setOverrideTestAddr(boolean isOverrideTestAddr) {
		this.isOverrideTestAddr = isOverrideTestAddr;
	}

	public int getAttachmentCount() {
		return attachmentCount;
	}

	public void setAttachmentCount(int attachmentCount) {
		this.attachmentCount = attachmentCount;
	}

	public int getAttachmentSize() {
		return attachmentSize;
	}

	public void setAttachmentSize(int attachmentSize) {
		this.attachmentSize = attachmentSize;
	}

	public int getMsgBodySize() {
		return msgBodySize;
	}

	public void setMsgBodySize(int msgBodySize) {
		this.msgBodySize = msgBodySize;
	}

	public String getMsgContentType() {
		return msgContentType;
	}

	public void setMsgContentType(String msgContentType) {
		this.msgContentType = msgContentType;
	}

	public String getBodyContentType() {
		return bodyContentType;
	}

	public void setBodyContentType(String bodyContentType) {
		this.bodyContentType = bodyContentType;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public int getOrigReadCount() {
		return origReadCount;
	}

	public void setOrigReadCount(int origReadCount) {
		this.origReadCount = origReadCount;
	}

	public String getOrigStatusId() {
		return origStatusId;
	}

	public void setOrigStatusId(String origStatusId) {
		this.origStatusId = origStatusId;
	}

	public MessageInbox getReferringMessage() {
		return referringMessage;
	}

	public void setReferringMessage(MessageInbox referringMessage) {
		this.referringMessage = referringMessage;
	}

	public MessageInbox getLeadMessage() {
		return leadMessage;
	}

	public void setLeadMessage(MessageInbox leadMessage) {
		this.leadMessage = leadMessage;
	}

	public EmailAddress getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(EmailAddress fromAddress) {
		this.fromAddress = fromAddress;
	}

	public EmailAddress getReplytoAddress() {
		return replytoAddress;
	}

	public void setReplytoAddress(EmailAddress replytoAddress) {
		this.replytoAddress = replytoAddress;
	}

	public EmailAddress getToAddress() {
		return toAddress;
	}

	public void setToAddress(EmailAddress toAddress) {
		this.toAddress = toAddress;
	}

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
	}

	public SubscriberData getSubscriberData() {
		return subscriberData;
	}

	public void setSubscriberData(SubscriberData subscriberData) {
		this.subscriberData = subscriberData;
	}

	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public MessageRendered getMessageRendered() {
		return messageRendered;
	}

	public void setMessageRendered(MessageRendered messageRendered) {
		this.messageRendered = messageRendered;
	}

	public MessageFolder getMessageFolder() {
		return messageFolder;
	}

	public void setMessageFolder(MessageFolder messageFolder) {
		this.messageFolder = messageFolder;
	}

}