package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseModel;

@Entity
@Table(name="render_attachment", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageRenderedRowId", "attachmentSequence"}))
public class RenderAttachment extends BaseModel implements Serializable {
	private static final long serialVersionUID = -4004571570543548763L;

	@Embedded
	private RenderAttachmentPK renderAttachmentPK;
	
	@Column(nullable=true,length=100)
	private String attachmentName = null;
	@Column(nullable=true,length=100)
	private String attachmentType = null;
	@Column(nullable=true,length=100)
	private String attachmentDisp = null;
	@Lob
	@Column(nullable=true,length=262136)
	private byte[] attachmentValue = null;

	public RenderAttachment() {
		// must have a no-argument constructor
	}

	public RenderAttachmentPK getRenderAttachmentPK() {
		return renderAttachmentPK;
	}

	public void setRenderAttachmentPK(RenderAttachmentPK renderAttachmentPK) {
		this.renderAttachmentPK = renderAttachmentPK;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getAttachmentType() {
		return attachmentType;
	}

	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}

	public String getAttachmentDisp() {
		return attachmentDisp;
	}

	public void setAttachmentDisp(String attachmentDisp) {
		this.attachmentDisp = attachmentDisp;
	}

	public byte[] getAttachmentValue() {
		return attachmentValue;
	}

	public void setAttachmentValue(byte[] attachmentValue) {
		this.attachmentValue = attachmentValue;
	}

}