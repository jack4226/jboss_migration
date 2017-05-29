package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;


@Embeddable
public class RenderVariablePK implements Serializable {
	private static final long serialVersionUID = -1688573015142969399L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageRendered.class)
	@JoinColumn(name="MessageRenderedRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private MessageRendered messageRendered;

	@Column(nullable=false,length=26)
	private String variableName = "";

	public RenderVariablePK() {}
	
	public RenderVariablePK(MessageRendered messageRendered,  String variableName) {
		this.messageRendered = messageRendered;
		this.variableName = variableName;
	}

	public MessageRendered getMessageRendered() {
		return messageRendered;
	}

	public void setMessageRendered(MessageRendered messageRendered) {
		this.messageRendered = messageRendered;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

}