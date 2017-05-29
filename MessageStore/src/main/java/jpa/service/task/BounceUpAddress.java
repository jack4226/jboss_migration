package jpa.service.task;

import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.EmailAddrType;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;

@Component("bounceUpAddress")
@Transactional(propagation=Propagation.REQUIRED)
public class BounceUpAddress extends TaskBaseAdapter {
	private static final long serialVersionUID = 8370212136984767775L;
	static final Logger logger = Logger.getLogger(BounceUpAddress.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressService emailAddrDao;

	/**
	 * Increase the bounce count to the email addresses involved. The column
	 * "DataTypeValues" from MsgAction table should contain address types (FROM,
	 * TO, etc) that need to be updated (bounce count increment).
	 * 
	 * @return a Long representing the number of addresses updated.
	 */
	public Integer process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		if (StringUtils.isBlank(ctx.getTaskArguments())) {
			throw new DataValidationException("Arguments is not valued, nothing to suspend");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + ctx.getTaskArguments());
		}
		
		MessageBean messageBean = ctx.getMessageBean();
		// example: $FinalRcpt,$OriginalRcpt,badaddress@baddomain.com
		int addrsUpdated = 0;
		StringTokenizer st = new StringTokenizer(ctx.getTaskArguments(), ",");
		while (st.hasMoreTokens()) {
			String addrs = null;
			String token = st.nextToken();
			if (token != null && token.startsWith("$")) { // address type
				token = token.substring(1);
				if (EmailAddrType.FROM_ADDR.getValue().equals(token)) {
					addrs = messageBean.getFromAsString();
				}
				else if (EmailAddrType.FINAL_RCPT_ADDR.getValue().equals(token)) {
					addrs = messageBean.getFinalRcpt();
				}
				else if (EmailAddrType.ORIG_RCPT_ADDR.getValue().equals(token)) {
					addrs = messageBean.getOrigRcpt();
				}
				else if (EmailAddrType.FORWARD_ADDR.getValue().equals(token)) {
					addrs = messageBean.getForwardAsString();
				}
				else if (EmailAddrType.TO_ADDR.getValue().equals(token)) {
					addrs = messageBean.getToAsString();
				}
				else if (EmailAddrType.REPLYTO_ADDR.getValue().equals(token)) {
					addrs = messageBean.getReplytoAsString();
				}
			}
			else { // address
				addrs = token;
			}
			
			Address[] iAddrs = null;
			if (StringUtils.isNotBlank(addrs)) {
				try {
					iAddrs = InternetAddress.parse(addrs);
				}
				catch (AddressException e) {
					logger.error("AddressException caught for: " + addrs + ", skip...");
				}
			}
			if (isDebugEnabled) {
				logger.debug("Address(es) to increase bounce count: " + addrs);
			}
			for (int i=0; iAddrs!=null && i<iAddrs.length; i++) {
				Address iAddr = iAddrs[i];
				String addr = iAddr.toString();
				EmailAddress emailAddrVo = emailAddrDao.getByAddress(addr);
				if (emailAddrVo == null) {
					if (isDebugEnabled) {
						logger.debug("Address (" + addr + ") does not exist, failed to increase bounce count!");
					}
					continue;
				}
				if (isDebugEnabled) {
					logger.debug("Increasing bounce count to EmailAddr: " + addr);
				}
				emailAddrDao.updateBounceCount(emailAddrVo);
				addrsUpdated++;
			}
		}
		return Integer.valueOf(addrsUpdated);
	}
}
