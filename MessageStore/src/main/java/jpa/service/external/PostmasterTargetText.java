package jpa.service.external;

import java.util.List;

import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.spring.util.SpringUtil;
import jpa.util.EmailAddrUtil;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("postmasterTargetText")
@Transactional(propagation=Propagation.REQUIRED)
public class PostmasterTargetText implements TargetTextProc {
	static final Logger logger = Logger.getLogger(PostmasterTargetText.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressService dao;
	
	/**
	 * retrieve all mailing list addresses, and construct a regular expression
	 * that matches any address on the list.
	 * 
	 * @return a regular expression
	 */
	public String process() {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		StringBuffer sb = new StringBuffer();
		List<EmailAddress> list = dao.getByAddressUser("(postmaster|mailmaster|mailadmin|administrator|mailer-(daemon|deamon)|smtp.gateway|majordomo)");
		for (int i = 0; i < list.size(); i++) {
			EmailAddress item = list.get(i);
			// no display name allowed for list address, just for safety
			String address = EmailAddrUtil.removeDisplayName(item.getAddress(), true);
			if (i > 0) {
				sb.append(",");
			}
			sb.append(address);
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		TargetTextProc resolver = SpringUtil.getAppContext().getBean(PostmasterTargetText.class);
		try {
			String regex = resolver.process();
			System.err.println("Email regular expression: " + regex);
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
}
