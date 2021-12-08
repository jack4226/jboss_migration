package jpa.service.maillist;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;
import jpa.model.EmailAddress;
import jpa.model.Subscription;
import jpa.model.UnsubComment;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriptionService;
import jpa.service.common.UnsubCommentService;

@Component("broadcastTrackingBo")
@Transactional(propagation=Propagation.REQUIRED)
public class BroadcastTrackingBo implements java.io.Serializable {
	private static final long serialVersionUID = 3992085332692245886L;
	
	static final Logger logger = LogManager.getLogger(BroadcastTrackingBo.class);

	@Autowired
	private BroadcastMessageService bcstMsgService;
	@Autowired
	private BroadcastTrackingService trackingService;
	@Autowired
	private SubscriptionService subService;
	@Autowired
	private EmailAddressService emailService;
	@Autowired
	private UnsubCommentService unsubCmtService;
	
	public Subscription removeFromList(int bcstMsgRowId, int emailAddrRowId) {
		EmailAddress ea = emailService.getByRowId(emailAddrRowId);
		if (ea == null) {
			logger.info("EmailAddress record not found by RowId (" + emailAddrRowId + "), ignored.");
		}
		else {
			Optional<BroadcastMessage> bm = bcstMsgService.getByRowId(bcstMsgRowId);
			if (bm.isPresent()) {
				bcstMsgService.updateUnsubscribeCount(bcstMsgRowId);
				Subscription sub = subService.unsubscribe(ea.getAddress(), bm.get().getMailingList().getListId());
				return sub;
			}
			else {
				logger.info("BroadcastMessage record not found by RowId (" + bcstMsgRowId + "), ignored.");
			}
		}
		return null;
	}

	public Subscription removeFromList(int bcstTrkRowId) {
		return removeFromList(bcstTrkRowId, null);
	}

	public Subscription removeFromList(int bcstTrkRowId, String comment) {
		Optional<BroadcastTracking> bt = trackingService.getByRowId(bcstTrkRowId);
		if (!bt.isPresent()) {
			logger.info("BroadcastTracking record not found by RowId (" + bcstTrkRowId + "), ignored.");
			return null;
		}
		EmailAddress ea = bt.get().getEmailAddress();
		BroadcastMessage bm  = bt.get().getBroadcastMessage();
		if (StringUtils.isNotBlank(comment)) {
			UnsubComment uc = new UnsubComment();
			uc.setEmailAddress(ea);
			uc.setMailingList(bm.getMailingList());
			uc.setComments(comment);
			unsubCmtService.insert(uc);
		}
		return removeFromList(bm.getRowId(), ea.getRowId());
	}

}
