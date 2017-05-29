package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.constant.MailingListDeliveryType;
import jpa.msgui.vo.TimestampAdapter;

@Entity
@Table(name="broadcast_message")
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="BroadcastMessageEntiry",
		entities={
		 @EntityResult(entityClass=BroadcastMessage.class),
	  	}),
	})
@XmlRootElement(name="broadcastMessage")
public class BroadcastMessage extends BaseModel implements Serializable {
	private static final long serialVersionUID = -2366817532593091084L;

	@Transient
	public static final String MAPPING_BROADCAST_MESSAGE_ENTITY = "BroadcastMessageEntiry";

	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="MailingListRowId",insertable=true,referencedColumnName="row_id",nullable=false,
			table="broadcast_message", foreignKey=@ForeignKey(name="FK_broadcast_mesage_MailingListRowId"))
	@XmlTransient
	private MailingList mailingList;

	@ManyToOne(fetch=FetchType.EAGER, optional=true)
	@JoinColumn(name="EmailTemplateRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="broadcast_message", foreignKey=@ForeignKey(name="FK_broadcast_message_EmailTemplateRowId"))
	@XmlTransient
	private EmailTemplate emailTemplate;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true, mappedBy="broadcastMessage")
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<BroadcastTracking> broadcastTrackings;

	@Column(nullable=true, length=255)
	private String msgSubject = null;
	@Lob
	@Column(nullable=true,length=65530)
	private String msgBody = null;
	@Column(nullable=true, columnDefinition="Integer")
	private Integer renderId = null; // TODO
	@Column(length=20, nullable=false)
	private String deliveryType = MailingListDeliveryType.ALL_ON_LIST.getValue();
	@Column(nullable=false, columnDefinition="int")
	private int sentCount = 0;
	@Column(nullable=false, columnDefinition="int")
	private int openCount = 0;
	@Column(nullable=false, columnDefinition="int")
	private int clickCount = 0;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastOpenTime = null;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastClickTime = null;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp startTime = null;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp endTime = null;
	@Column(nullable=false, columnDefinition="int")
	private int unsubscribeCount = 0;
	@Column(nullable=false, columnDefinition="int")
	private int complaintCount = 0;
	@Column(nullable=false, columnDefinition="int")
	private int referralCount = 0;
	@Version
	@Column(name="opt_lock", nullable=false)
	private long optLock = 0L;

	public BroadcastMessage() {}

	public MailingList getMailingList() {
		return mailingList;
	}

	public void setMailingList(MailingList mailingList) {
		this.mailingList = mailingList;
	}

	public EmailTemplate getEmailTemplate() {
		return emailTemplate;
	}

	public void setEmailTemplate(EmailTemplate emailTemplate) {
		this.emailTemplate = emailTemplate;
	}

	public List<BroadcastTracking> getBroadcastTrackings() {
		return broadcastTrackings;
	}

	public void setBroadcastTrackings(List<BroadcastTracking> broadcastTrackings) {
		this.broadcastTrackings = broadcastTrackings;
	}

	public String getMsgSubject() {
		return msgSubject;
	}

	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public Integer getRenderId() {
		return renderId;
	}

	public void setRenderId(Integer renderId) {
		this.renderId = renderId;
	}

	public String getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(String deliveryType) {
		this.deliveryType = deliveryType;
	}

	public int getSentCount() {
		return sentCount;
	}

	public void setSentCount(int sentCount) {
		this.sentCount = sentCount;
	}

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}

	public int getClickCount() {
		return clickCount;
	}

	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}

	public Timestamp getLastOpenTime() {
		return lastOpenTime;
	}

	public void setLastOpenTime(Timestamp lastOpenTime) {
		this.lastOpenTime = lastOpenTime;
	}

	public Timestamp getLastClickTime() {
		return lastClickTime;
	}

	public void setLastClickTime(Timestamp lastClickTime) {
		this.lastClickTime = lastClickTime;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public int getUnsubscribeCount() {
		return unsubscribeCount;
	}

	public void setUnsubscribeCount(int unsubscribeCount) {
		this.unsubscribeCount = unsubscribeCount;
	}

	public int getComplaintCount() {
		return complaintCount;
	}

	public void setComplaintCount(int complaintCount) {
		this.complaintCount = complaintCount;
	}

	public int getReferralCount() {
		return referralCount;
	}

	public void setReferralCount(int referralCount) {
		this.referralCount = referralCount;
	}

}
