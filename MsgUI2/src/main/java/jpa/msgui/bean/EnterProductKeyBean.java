package jpa.msgui.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.msgui.util.SpringUtil;
import jpa.service.common.SenderDataService;
import jpa.util.ProductKey;

import org.apache.log4j.Logger;

@ManagedBean(name="enterProductKey")
@RequestScoped
public class EnterProductKeyBean implements java.io.Serializable {
	private static final long serialVersionUID = 5162094104987950893L;
	static final Logger logger = Logger.getLogger(EnterProductKeyBean.class);
	private String name = null;
	private String productKey = null;
	private String message = null;
	
	private transient SenderDataService senderDataDao = null;
	
	public String enterProductKey() {
		message = null;
		if (!ProductKey.validateKey(productKey)) {
			message = "Invalid Product Key.";
			return "enterkey.failed";
		}
		SenderData data = getSenderDataService().getBySenderId(Constants.DEFAULT_SENDER_ID);
		data.setSystemKey(productKey);
		getSenderDataService().update(data);
		logger.info("enterProductKey() - rows updated: " + 1);
		return "enterkey.saved";
	}
	
	private SenderDataService getSenderDataService() {
		if (senderDataDao == null) {
			senderDataDao = SpringUtil.getWebAppContext().getBean(SenderDataService.class);
		}
		return senderDataDao;
	}
    
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProductKey() {
		return productKey;
	}

	public void setProductKey(String productKey) {
		this.productKey = productKey;
	}
}
