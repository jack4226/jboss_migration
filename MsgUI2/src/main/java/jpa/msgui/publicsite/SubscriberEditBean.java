package jpa.msgui.publicsite;

import org.apache.log4j.Logger;

public class SubscriberEditBean implements java.io.Serializable {
	private static final long serialVersionUID = 6676261160629601090L;
	static final Logger logger = Logger.getLogger(SubscriberEditBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private boolean editMode = true;
	private String msgid = null;
	private String listid = null;
	private String sbsrid = null;
	private String sbsrAddr = null;
	private String submit = null;

	public boolean getEditMode() {
		return editMode;
	}

	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getListid() {
		return listid;
	}

	public void setListid(String listid) {
		this.listid = listid;
	}

	public String getSbsrid() {
		return sbsrid;
	}

	public void setSbsrid(String sbsrid) {
		this.sbsrid = sbsrid;
	}

	public String getSubmit() {
		return submit;
	}

	public void setSubmit(String submit) {
		this.submit = submit;
	}

	public String getSbsrAddr() {
		return sbsrAddr;
	}

	public void setSbsrAddr(String sbsrAddr) {
		this.sbsrAddr = sbsrAddr;
	}
}
