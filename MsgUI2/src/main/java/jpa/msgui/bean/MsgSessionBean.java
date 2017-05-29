package jpa.msgui.bean;

import javax.faces.bean.ManagedBean;

import jpa.msgui.util.FacesUtil;

@ManagedBean(name="sessionBean")
@javax.faces.bean.SessionScoped //ViewScoped
public class MsgSessionBean implements java.io.Serializable {
	private static final long serialVersionUID = -5665449341441493983L;

	public Object getSessionParam(String paramKey) {
        Object obj = FacesUtil.getSessionMapValue(paramKey);
        return obj;
    }
	
	public String getRequestParam(String paramKey) {
        String value = FacesUtil.getRequestParameter(paramKey);
        return value;
    }
	
	public Object getManagedBean(String beanName) {
		Object obj = FacesUtil.getManagedBean(beanName);
		return obj;
	}
	
	public void setSessionParam(String paramKey, String paramValue) {
		FacesUtil.setSessionMapValue(paramKey, paramValue);
	}
}
