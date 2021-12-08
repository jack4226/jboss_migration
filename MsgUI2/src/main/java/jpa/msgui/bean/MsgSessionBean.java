package jpa.msgui.bean;

import jpa.msgui.util.FacesUtil;

@javax.inject.Named("sessionBean")
@javax.enterprise.context.SessionScoped //ViewScoped
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
