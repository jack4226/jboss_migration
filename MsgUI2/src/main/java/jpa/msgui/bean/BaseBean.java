package jpa.msgui.bean;

import javax.inject.Inject;

public abstract class BaseBean {

	@Inject
	//@javax.faces.annotation.ManagedProperty(value="#{sessionBean}")
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

	public String getRequestParam(String name) {
		return (String) sessionBean.getRequestParam(name);
	}

}
