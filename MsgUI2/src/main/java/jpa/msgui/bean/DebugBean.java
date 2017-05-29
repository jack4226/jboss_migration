package jpa.msgui.bean;

import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

@ManagedBean(name="debug")
@ApplicationScoped
public class DebugBean implements java.io.Serializable {
	private static final long serialVersionUID = 6115363411392224312L;
	
	@ManagedProperty("#{facesBroker}")
	private FacesBroker broker;
	
	public final static String key = "debug_count";
	
	private boolean showMessages = true;

	public String incrementCount() {
		Map<String, Object> session = broker.getContext().getExternalContext().getSessionMap();
		Integer count = (Integer) session.get(key);
		count = (count == null) ? 1 : count + 1;
		session.put(key, count);
		return null;
	}
	
	public FacesBroker getBroker() {
		return broker;
	}
	public void setBroker(FacesBroker broker) {
		this.broker = broker;
	}
	
	public boolean isShowMessages() {
		return showMessages;
	}
	public void setShowMessages(boolean debug) {
		this.showMessages = debug;
	}
}
