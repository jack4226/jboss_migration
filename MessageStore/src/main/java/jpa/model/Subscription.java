package jpa.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.constant.CodeType;
import jpa.msgui.vo.TimestampAdapter;
import jpa.util.StringUtil;

@Entity
@Table(name="subscription", uniqueConstraints=@UniqueConstraint(columnNames = {"EmailAddrRowId", "MailingListRowId"}))
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="SubscriptionEntiry",
		entities={
		 @EntityResult(entityClass=Subscription.class),
	  	}),
	})
@XmlRootElement(name="subscription")
public class Subscription extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 5306761711116978942L;

	@Transient
	public static final String MAPPING_SUBSCRIPTION_ENTITY = "SubscriptionEntiry";

	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="EmailAddrRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	//@XmlTransient
	private EmailAddress emailAddress; // subscriber email address
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="MailingListRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private MailingList mailingList; // mailing list isSubscribed to
	
	@Column(length=1,nullable=false,columnDefinition="boolean not null default true")
	private boolean isSubscribed = true;
	@Column(nullable=true,columnDefinition="Boolean")
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isOptIn = null;
	@Column(length=3, nullable=false)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp CreateTime;
	@Column(nullable=false)
	private int sentCount = 0;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastSentTime = null;
	@Column(nullable=false)
	private int openCount = 0;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastOpenTime = null;
	@Column(nullable=false)
	private int clickCount = 0;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastClickTime = null;
	@Version
	@Column(name="opt_lock", nullable=false)
	private long optLock = 0L;

	public Subscription() {
		// must have a no-argument constructor
	}

	/** define components for UI */
	public String getEmailAddrShort() {
		if (getEmailAddress()!=null) {
			return StringUtil.cutWithDots(getEmailAddress().getAddress(), 100);
		}
		else {
			throw new IllegalStateException("Subscription instance must be loaded with data!");
		}
	}
	
	public String getSubscribedDesc() {
		return isSubscribed ? CodeType.YES.getValue() : CodeType.NO.getValue();
	}
	
	public String getAcceptHtmlDesc() {
		boolean acceptHtml = getEmailAddress()==null?true:getEmailAddress().isAcceptHtml();
		return (acceptHtml==false ? CodeType.NO.getValue() : CodeType.YES.getValue());
	}
	
	public String getSubscriberName() {
		if (getEmailAddress()!=null && getEmailAddress().getSubscriberData()!=null) {
			String firstName = getEmailAddress().getSubscriberData().getFirstName();
			String lastName = getEmailAddress().getSubscriberData().getLastName();
			return (firstName == null ? "" : firstName + " ") + (lastName == null ? "" : lastName);
		}
		else {
			return "";
		}
	}
	/** end of UI */

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(EmailAddress emailAddr) {
		this.emailAddress = emailAddr;
	}

	public MailingList getMailingList() {
		return mailingList;
	}

	public void setMailingList(MailingList mailingList) {
		this.mailingList = mailingList;
	}

	public boolean isSubscribed() {
		return isSubscribed;
	}

	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}

	public Boolean getIsOptIn() {
		return isOptIn;
	}

	public void setIsOptIn(Boolean isOptIn) {
		this.isOptIn = isOptIn;
	}

	public Timestamp getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
	}

	public int getSentCount() {
		return sentCount;
	}

	public void setSentCount(int sentCount) {
		this.sentCount = sentCount;
	}

	public Timestamp getLastSentTime() {
		return lastSentTime;
	}

	public void setLastSentTime(Timestamp lastSentTime) {
		this.lastSentTime = lastSentTime;
	}

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}

	public Timestamp getLastOpenTime() {
		return lastOpenTime;
	}

	public void setLastOpenTime(Timestamp lastOpenTime) {
		this.lastOpenTime = lastOpenTime;
	}

	public int getClickCount() {
		return clickCount;
	}

	public void setClickCount(int clickCount) {
		this.clickCount = clickCount;
	}

	public Timestamp getLastClickTime() {
		return lastClickTime;
	}

	public void setLastClickTime(Timestamp lastClickTime) {
		this.lastClickTime = lastClickTime;
	}
}
