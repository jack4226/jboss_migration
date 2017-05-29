package jpa.msgui.bean;

import javax.faces.bean.ManagedProperty;

public abstract class BaseBean {

	@ManagedProperty(value="#{sessionBean}")
    protected MsgSessionBean sessionBean;

	public MsgSessionBean getSessionBean() {
		return sessionBean;
	}

	public void setSessionBean(MsgSessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

	public String getSessionParam(String name) {
		return (String) sessionBean.getSessionParam(name);
	}

}
