package jpa.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.msgui.vo.TimestampAdapter;

@Entity
@Table(name="email_address")
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="EmailAddressEntiry",
		entities={
		 @EntityResult(entityClass=EmailAddress.class),
	  	}),
	  @SqlResultSetMapping(name="EmailAddressWithCounts",
		entities={
		 @EntityResult(entityClass=EmailAddress.class),
	  	},
	  	columns={
		 @ColumnResult(name="sentCount"),
		 @ColumnResult(name="openCount"),
		 @ColumnResult(name="clickCount"),
	  	}),
	})
@XmlRootElement(name="emailAddress")
public class EmailAddress extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -6508051650541209578L;

	@Transient
	public static final String MAPPING_EMAIL_ADDR_ENTITY = "EmailAddressEntiry";
	@Transient
	public static final String MAPPING_EMAIL_ADDR_WITH_COUNTS = "EmailAddressWithCounts";

	@Column(nullable=false, length=255, unique=true)
	private String address = "";
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp statusChangeTime = null;
	@Column(nullable=true, length=20)
 	private String statusChangeUserId = null;
	@Column(nullable=false)
	private int bounceCount = 0;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastBounceTime = null;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastSentTime = null;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastRcptTime= null;
	@Column(nullable=false,length=1,columnDefinition="boolean not null")
	private boolean isAcceptHtml = true;
	@Column(nullable=false, length=255)
	private String origAddress = "";
	@Version
	@Column(name="opt_lock", nullable=false)
	private long optLock = 0L;

	@OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.LAZY, mappedBy="emailAddress", orphanRemoval=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	@XmlTransient
	private List<Subscription> subscriptionList;

	@OneToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch=FetchType.EAGER, optional=true, mappedBy="emailAddress")
	private SubscriberData subscriberData;
	
	@OneToOne(cascade={CascadeType.PERSIST,CascadeType.MERGE}, fetch=FetchType.EAGER, optional=true, mappedBy="emailAddress")
	private UserData userData;

	//TODO
	// used when join with MsgInbox table
	@Transient
	private String ruleName = null;

	// As the table already has a column called OrigAddress, use currAddress to avoid confusion.
	@Transient
	private String currAddress = null;

	@Transient
	private Integer sentCount;
	@Transient
	private Integer openCount;
	@Transient
	private Integer clickCount;
	

	public EmailAddress() {
		// must have a no-argument constructor
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getOrigAddress() {
		return origAddress;
	}

	public void setOrigAddress(String origAddress) {
		this.origAddress = origAddress;
	}

	public Timestamp getStatusChangeTime() {
		return statusChangeTime;
	}

	public void setStatusChangeTime(Timestamp statusChangeTime) {
		this.statusChangeTime = statusChangeTime;
	}

	public String getStatusChangeUserId() {
		return statusChangeUserId;
	}

	public void setStatusChangeUserId(String statusChangeUserId) {
		this.statusChangeUserId = statusChangeUserId;
	}

	public int getBounceCount() {
		return bounceCount;
	}

	public void setBounceCount(int bounceCount) {
		this.bounceCount = bounceCount;
	}

	public Timestamp getLastBounceTime() {
		return lastBounceTime;
	}

	public void setLastBounceTime(Timestamp lastBounceTime) {
		this.lastBounceTime = lastBounceTime;
	}

	public Timestamp getLastSentTime() {
		return lastSentTime;
	}

	public void setLastSentTime(Timestamp lastSentTime) {
		this.lastSentTime = lastSentTime;
	}

	public Timestamp getLastRcptTime() {
		return lastRcptTime;
	}

	public void setLastRcptTime(Timestamp lastRcptTime) {
		this.lastRcptTime = lastRcptTime;
	}

	public boolean isAcceptHtml() {
		return isAcceptHtml;
	}

	public void setAcceptHtml(boolean isAcceptHtml) {
		this.isAcceptHtml = isAcceptHtml;
	}

	public SubscriberData getSubscriberData() {
		return subscriberData;
	}

	public void setSubscriberData(SubscriberData subscriberData) {
		this.subscriberData = subscriberData;
	}

	public List<Subscription> getSubscriptions() {
		if (subscriptionList == null) {
			subscriptionList = new ArrayList<>();
		}
		return subscriptionList;
	}

	public void setSubscriptions(List<Subscription> subscriptions) {
		this.subscriptionList = subscriptions;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getCurrAddress() {
		return currAddress;
	}

	public void setCurrAddress(String currAddress) {
		this.currAddress = currAddress;
	}

	public Integer getSentCount() {
		return sentCount;
	}

	public void setSentCount(Integer sentCount) {
		this.sentCount = sentCount;
	}

	public Integer getOpenCount() {
		return openCount;
	}

	public void setOpenCount(Integer openCount) {
		this.openCount = openCount;
	}

	public Integer getClickCount() {
		return clickCount;
	}

	public void setClickCount(Integer clickCount) {
		this.clickCount = clickCount;
	}

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}
}
