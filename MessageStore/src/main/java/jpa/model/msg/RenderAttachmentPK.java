package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;


@Embeddable
public class RenderAttachmentPK implements Serializable {
	private static final long serialVersionUID = 2536268592446565636L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageRendered.class)
	@JoinColumn(name="MessageRenderedRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private MessageRendered messageRendered;

	@Column(nullable=false, columnDefinition="decimal(2,0)")
	private int attachmentSequence;

	public RenderAttachmentPK() {}
	
	public RenderAttachmentPK(MessageRendered messageRendered,  int attachmentSequence) {
		this.messageRendered = messageRendered;
		this.attachmentSequence = attachmentSequence;
	}

	public MessageRendered getMessageRendered() {
		return messageRendered;
	}

	public void setMessageRendered(MessageRendered messageRendered) {
		this.messageRendered = messageRendered;
	}

	public int getAttachmentSequence() {
		return attachmentSequence;
	}

	public void setAttachmentSequence(int attachmentSequence) {
		this.attachmentSequence = attachmentSequence;
	}

}