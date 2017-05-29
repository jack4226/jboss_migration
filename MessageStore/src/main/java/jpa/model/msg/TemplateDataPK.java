package jpa.model.msg;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.model.SenderData;
import jpa.msgui.vo.TimestampAdapter;

@Embeddable
public class TemplateDataPK implements Serializable {
	private static final long serialVersionUID = 3942772930671670809L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=SenderData.class)
	@JoinColumn(name="SenderDataRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private SenderData senderData;

	@Column(name="TemplateId", nullable=false, length=26)
	private String templateId = "";
	@Column(name="StartTime", length=3, nullable=false)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp startTime = new Timestamp(System.currentTimeMillis());

	public TemplateDataPK() {}
	
	public TemplateDataPK(SenderData senderData, String templateId, Timestamp startTime) {
		this.senderData = senderData;
		this.templateId = templateId;
		this.startTime = startTime;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

}