package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
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

import jpa.msgui.vo.TimestampAdapter;

@Entity
@Table(name="broadcast_tracking", uniqueConstraints=@UniqueConstraint(columnNames = {"BroadcastMessageRowId", "EmailAddressRowId"}))
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="BroadcastTrackingEntiry",
		entities={
		 @EntityResult(entityClass=BroadcastTracking.class),
	  	}),
	})
@XmlRootElement(name="broadcastTracking")
public class BroadcastTracking extends BaseModel implements Serializable {
	private static final long serialVersionUID = 8041670636070073207L;

	@Transient
	public static final String MAPPING_BROADCAST_TRACKING_ENTITY = "BroadcastTrackingEntiry";

	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="EmailAddressRowId",insertable=true,referencedColumnName="row_id",nullable=false,
			table="broadcast_tracking", foreignKey=@ForeignKey(name="FK_Broadcast_tracking_EmailAddrRowId"))
	@XmlTransient
	private EmailAddress emailAddress;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="BroadcastMessageRowId",insertable=true,referencedColumnName="row_id",nullable=false,
			table="broadcast_tracking", foreignKey=@ForeignKey(name="FK_Broadcast_tracking_BcstMsgRowId"))
	@XmlTransient
	private BroadcastMessage broadcastMessage;

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
	@Version
	@Column(name="opt_lock", nullable=false)
	private long optLock = 0L;

	public BroadcastTracking() {}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(EmailAddress emailAddress) {
		this.emailAddress = emailAddress;
	}

	public BroadcastMessage getBroadcastMessage() {
		return broadcastMessage;
	}

	public void setBroadcastMessage(BroadcastMessage broadcastMessage) {
		this.broadcastMessage = broadcastMessage;
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

}
