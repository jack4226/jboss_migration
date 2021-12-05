package jpa.dataloader;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.data.preload.EmailTemplateEnum;
import jpa.model.SenderData;
import jpa.model.EmailTemplate;
import jpa.model.MailingList;
import jpa.model.SchedulesBlob;
import jpa.service.common.EmailTemplateService;
import jpa.service.common.SenderDataService;
import jpa.service.maillist.MailingListService;
import jpa.spring.util.SpringUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailTemplateLoader extends AbstractDataLoader {
	static final Logger logger = LogManager.getLogger(EmailTemplateLoader.class);
	private EmailTemplateService service;
	private SenderDataService senderService;
	private MailingListService mlistService;

	public static void main(String[] args) {
		EmailTemplateLoader loader = new EmailTemplateLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(EmailTemplateService.class);
		senderService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		mlistService = SpringUtil.getAppContext().getBean(MailingListService.class);
		startTransaction();
		try {
			loadEmailTemplates();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadEmailTemplates() {
		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		
		for (EmailTemplateEnum tmp : EmailTemplateEnum.values()) {
			if (tmp.getMailingList().isProd()) {
				continue;
			}
			MailingList mlist = mlistService.getByListId(tmp.getMailingList().name());
			EmailTemplate data = new EmailTemplate();
			data.setSenderData(sender);
			data.setMailingList(mlist);
			data.setTemplateId(tmp.name());
			data.setSubject(tmp.getSubject());
			data.setBodyText(tmp.getBodyText());
			data.setHtml(tmp.isHtml());
			data.setListType(tmp.getListType().getValue());
			data.setDeliveryOption(tmp.getDeliveryType().getValue());
			data.setBuiltin(tmp.isBuiltin());
			data.setIsEmbedEmailId(tmp.getIsEmbedEmailId()); // use system default when null
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			SchedulesBlob blob1 = new SchedulesBlob();
			data.setSchedulesBlob(blob1);
			try {
				service.insert(data);
			}
			catch (Exception e) {
				logger.error("Failed to insert record: " + e.getMessage());
			}
		}

		logger.info("EntityManager persisted the record.");
	}
	
	void loadProdEmailTemplates() {
		SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);

		for (EmailTemplateEnum tmp : EmailTemplateEnum.values()) {
			if (!tmp.getMailingList().isProd()) continue;
			MailingList mlist = mlistService.getByListId(tmp.getMailingList().name());
			EmailTemplate data = new EmailTemplate();
			data.setSenderData(sender);
			data.setMailingList(mlist);
			data.setTemplateId(tmp.name());
			data.setSubject(tmp.getSubject());
			data.setBodyText(tmp.getBodyText());
			data.setHtml(tmp.isHtml());
			data.setListType(tmp.getListType().getValue());
			data.setDeliveryOption(tmp.getDeliveryType().getValue());
			data.setBuiltin(tmp.isBuiltin());
			data.setIsEmbedEmailId(tmp.getIsEmbedEmailId()); // use system default when null
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			SchedulesBlob blob1 = new SchedulesBlob();
			data.setSchedulesBlob(blob1);
			service.insert(data);
		}

		logger.info("EntityManager persisted the record.");
	}
}

