package jpa.service.task;

import javax.mail.Address;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriptionService;
import jpa.service.maillist.MailingListService;

@Component("unsubscribeFromList")
@Transactional(propagation=Propagation.REQUIRED)
public class UnsubscribeFromList extends TaskBaseAdapter {
	private static final long serialVersionUID = -1655630086836497324L;
	static final Logger logger = LogManager.getLogger(UnsubscribeFromList.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressService emailAddrDao;
	@Autowired
	private MailingListService mailingListDao;
	@Autowired
	private SubscriptionService subService;

	/**
	 * Remove the FROM address from the mailing list (TO).
	 * 
	 * @return a Integer value representing number of addresses that have been
	 *         updated.
	 */
	public Integer process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		MessageBean messageBean = ctx.getMessageBean();
		int addrsUpdated = 0;
		Address[] toAddrs = messageBean.getTo();
		if (toAddrs == null || toAddrs.length == 0) { // just for safety
			logger.error("unsubscription address (TO) is null.");
			throw new DataValidationException("TO address is null");
		}
		MailingList mlist = null;
		for (int k = 0; k < toAddrs.length; k++) {
			Address toAddr = toAddrs[k];
			if (toAddr == null || StringUtils.isBlank(toAddr.toString())) {
				continue; // just for safety
			}
			mlist = mailingListDao.getByListAddress(toAddr.toString());
			if (mlist == null) {
				logger.warn("Mailing List not found for address: " + toAddr);
				continue;
			}
			Address[] addrs = messageBean.getFrom();
			for (int i = 0; addrs != null && i < addrs.length; i++) {
				Address addr = addrs[i];
				if (addr == null || StringUtils.isBlank(addr.toString())) {
					continue; // just for safety
				}
				EmailAddress emailAddrVo = emailAddrDao.findSertAddress(addr.toString());
				messageBean.setMailingListId(mlist.getListId());
				Subscription sub = subService.unsubscribe(emailAddrVo.getAddress(), mlist.getListId());
				ctx.getRowIds().add(sub.getRowId());
				logger.info(addr + " unsubscribed from: " + mlist.getListId());
				addrsUpdated++;
			}
		}
		return Integer.valueOf(addrsUpdated);
	}

}
