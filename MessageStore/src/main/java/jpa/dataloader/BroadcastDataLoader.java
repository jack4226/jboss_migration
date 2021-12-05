package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import jpa.constant.Constants;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.StatusId;
import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;
import jpa.model.EmailAddress;
import jpa.model.EmailTemplate;
import jpa.model.MailingList;
import jpa.service.common.EmailAddressService;
import jpa.service.common.EmailTemplateService;
import jpa.service.maillist.BroadcastMessageService;
import jpa.service.maillist.BroadcastTrackingService;
import jpa.service.maillist.MailingListService;
import jpa.spring.util.SpringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BroadcastDataLoader extends AbstractDataLoader {
	static final Logger logger = LogManager.getLogger(BroadcastDataLoader.class);
	private MailingListService mlistService;
	private EmailTemplateService etmpltService;
	private EmailAddressService emailService;
	private BroadcastMessageService bcastService;
	private BroadcastTrackingService trkngService;;

	public static void main(String[] args) {
		BroadcastDataLoader loader = new BroadcastDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		mlistService = SpringUtil.getAppContext().getBean(MailingListService.class);
		etmpltService = SpringUtil.getAppContext().getBean(EmailTemplateService.class);
		emailService = SpringUtil.getAppContext().getBean(EmailAddressService.class);
		bcastService = SpringUtil.getAppContext().getBean(BroadcastMessageService.class);
		trkngService = SpringUtil.getAppContext().getBean(BroadcastTrackingService.class);
		startTransaction();
		try {
			loadBroadcastData();
			loadEmailBroadcasts();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadBroadcastData() throws SQLException {
		List<MailingList> mllst = mlistService.getAll(true);
		List<EmailTemplate> etlst = etmpltService.getAll();
		if (etlst.isEmpty()) {
			return;
		}
		Timestamp createTime = new Timestamp(System.currentTimeMillis());
		int count = 0;
		for (MailingList ml : mllst) {
			innerLoop: 
			for (int i = 0; i < etlst.size(); i++) {
				EmailTemplate tmplt = etlst.get(i);
				List<BroadcastMessage> msgList = bcastService.getByListIdAndTemplateId(ml.getListId(), tmplt.getTemplateId());
				if (msgList.size() > 0) {
					continue innerLoop;
				}
				BroadcastMessage vo = new BroadcastMessage();
				vo.setMailingList(ml);
				vo.setEmailTemplate(tmplt);
				vo.setMsgSubject("Test message for " + ml.getListId() + " - " + tmplt.getTemplateId());
				vo.setMsgBody("Test body for " + tmplt.getTemplateId() + LF + "<br/>" + LF + tmplt.getBodyText());
				vo.setDeliveryType(MailingListDeliveryType.ALL_ON_LIST.getValue());
				vo.setStatusId(StatusId.ACTIVE.getValue());
				vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
				vo.setSentCount(1);
				vo.setStartTime(createTime);
				vo.setUpdtTime(createTime);
				bcastService.insert(vo);
				count++;
				//break;
			}
		}
		
		logger.info("EntityManager persisted broadcast message records: " + count);
	}
	
	private void loadEmailBroadcasts() {
		java.sql.Timestamp createTime = new java.sql.Timestamp(System.currentTimeMillis());
		List<BroadcastMessage> bdlist = bcastService.getTop100();
		
		int count = 0;
		int nextIdx = 0;
		for (BroadcastMessage bd : bdlist) {
			EmailAddress ea = emailService.findSertAddress(getNextUserEmailAddr(nextIdx++));
			if (trkngService.getByPrimaryKey(ea.getRowId(), bd.getRowId()) == null) {
				BroadcastTracking eb = new BroadcastTracking();
				eb.setBroadcastMessage(bd);
				eb.setStatusId(StatusId.ACTIVE.getValue());
				eb.setUpdtUserId(Constants.DEFAULT_USER_ID);
				eb.setUpdtTime(createTime);
				eb.setEmailAddress(ea);
				trkngService.insert(eb);
				count++;
			}
			
			ea = emailService.findSertAddress(getNextUserEmailAddr(nextIdx++));
			if (trkngService.getByPrimaryKey(ea.getRowId(), bd.getRowId()) == null) {
				BroadcastTracking eb = new BroadcastTracking();
				eb.setBroadcastMessage(bd);
				eb.setStatusId(StatusId.ACTIVE.getValue());
				eb.setUpdtUserId(Constants.DEFAULT_USER_ID);
				eb.setUpdtTime(createTime);
				eb.setEmailAddress(ea);
				trkngService.insert(eb);
				count++;
			}
			if (count >= 100) {
				break;
			}
		}
		
		logger.info("EntityManager persisted broadcast tracking records: " + count);
	}
	
	private String getNextUserEmailAddr(int nextIdx) {
		String suffix = StringUtils.leftPad(nextIdx + "", 2, '0');
		return "user" + suffix + "@localhost";
	}
}
