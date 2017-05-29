package jpa.model;

import java.sql.Timestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jpa.constant.Constants;
import jpa.msgui.vo.TimestampAdapter;

@Entity
@Table(name="user_data")
@XmlRootElement(name="userData")
public class UserData extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 14989739185873317L;

	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="SenderDataRowId", insertable=true, updatable=true, referencedColumnName="row_id", nullable=false)
	@OnDelete( action = OnDeleteAction.CASCADE )
	@XmlTransient
	private SenderData senderData; // sender user is associated to

	@OneToOne(fetch=FetchType.EAGER, cascade=CascadeType.ALL, optional=true, targetEntity=EmailAddress.class)
	@JoinColumn(name="EmailAddrRowId", insertable=true, updatable=true, referencedColumnName="row_id", nullable=true)
	@XmlTransient
	private EmailAddress emailAddress; // user email address - optional
	
	@Column(nullable=false, length=20, unique=true)
	private String userId = "";
	@Column(nullable=false, length=32)
	private String password = "";
	@Column(length=50)
	private String sessionId = null;
	@Column(length=32)
	private String firstName = null;
	@Column(length=32)
	private String lastName = null;
	@Column(length=1,columnDefinition="char")
	private String middleInit = null;
	@Column(length=3, nullable=false)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp createTime;
	@Column(length=3, nullable=true)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp lastVisitTime;
	@Column(nullable=false)
	private int hits = 0;
	@Column(length=5, nullable=false)
	private String role = "";
	
	@Column(length=26, nullable=true)
	private String defaultRuleName = null;

	public UserData() {
		// must have a no-argument constructor
	}

	/** define UI components */
	public void addHit() {
		hits++;
		lastVisitTime = new Timestamp(System.currentTimeMillis());
	}
	
	public boolean getIsAdmin() {
		return Constants.ADMIN_ROLE.equalsIgnoreCase(role);
	}
	/** end of UI components */

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
	}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(EmailAddress emailAddr) {
		this.emailAddress = emailAddr;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleInit() {
		return middleInit;
	}

	public void setMiddleInit(String middleInit) {
		this.middleInit = middleInit;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getLastVisitTime() {
		return lastVisitTime;
	}

	public void setLastVisitTime(Timestamp lastVisitTime) {
		this.lastVisitTime = lastVisitTime;
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDefaultRuleName() {
		return defaultRuleName;
	}

	public void setDefaultRuleName(String defaultRuleName) {
		this.defaultRuleName = defaultRuleName;
	}
}
