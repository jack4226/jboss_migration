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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.model.BaseModel;
import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.msgui.vo.TimestampAdapter;

@Entity
@Table(name="message_rendered")
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="MessageRenderedNative",
		entities={
		 @EntityResult(entityClass=MessageRendered.class),
	  	}
	  	),
	})
public class MessageRendered extends BaseModel implements Serializable {
	private static final long serialVersionUID = -522522467087500772L;

	@Transient
	public static final String MAPPING_MESSAGE_RENDERED = "MessageRenderedNative";

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="renderVariablePK.messageRendered", orphanRemoval=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<RenderVariable> renderVariableList;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="renderAttachmentPK.messageRendered", orphanRemoval=true)
	@OrderBy
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<RenderAttachment> renderAttachmentList;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="MessageSourceRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_rendered", foreignKey=@ForeignKey(name="FK_MsgRenderd_MsgSrcRowId"))
	@XmlTransient
	private MessageSource messageSource;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="MessageTemplateRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_rendered", foreignKey=@ForeignKey(name="FK_MsgRenderd_MsgTmpltRowId"))
	@XmlTransient
	private TemplateData messageTemplate;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="SenderDataRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_rendered", foreignKey=@ForeignKey(name="FK_MsgRenderd_SenderDataRowId"))
	@XmlTransient
	private SenderData senderData;

	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="SubscriberDataRowId",insertable=true,referencedColumnName="row_id",nullable=true,
			table="message_rendered", foreignKey=@ForeignKey(name="FK_MsgRenderd_SbsrDataRowId"))
	@XmlTransient
	private SubscriberData subscriberData;

	@Column(length=3, nullable=false)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp startTime;

	@Column(nullable=true)
	private Integer purgeAfter = null;

	public MessageRendered() {
		// must have a no-argument constructor
		startTime = new Timestamp(System.currentTimeMillis());
	}

	public List<RenderVariable> getRenderVariableList() {
		if (renderVariableList == null) {
			renderVariableList = new ArrayList<RenderVariable>();
		}
		return renderVariableList;
	}

	public void setRenderVariableList(List<RenderVariable> renderVariableList) {
		this.renderVariableList = renderVariableList;
	}

	public List<RenderAttachment> getRenderAttachmentList() {
		if (renderAttachmentList==null) {
			renderAttachmentList = new ArrayList<RenderAttachment>();
		}
		return renderAttachmentList;
	}

	public void setRenderAttachmentList(List<RenderAttachment> renderAttachmentList) {
		this.renderAttachmentList = renderAttachmentList;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Integer getPurgeAfter() {
		return purgeAfter;
	}

	public void setPurgeAfter(Integer purgeAfter) {
		this.purgeAfter = purgeAfter;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public TemplateData getMessageTemplate() {
		return messageTemplate;
	}

	public void setMessageTemplate(TemplateData messageTemplate) {
		this.messageTemplate = messageTemplate;
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

}