package jpa.dataloader;

import java.util.List;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.MailProtocol;
import jpa.constant.StatusId;
import jpa.data.preload.MailInboxEnum;
import jpa.model.MailInbox;
import jpa.model.MailInboxPK;
import jpa.service.common.EmailAddressService;
import jpa.service.msgin.MailInboxService;
import jpa.spring.util.SpringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailInboxLoader extends AbstractDataLoader {
	static final Logger logger = LogManager.getLogger(MailInboxLoader.class);
	private MailInboxService service;
	private EmailAddressService emailService;

	public static void main(String[] args) {
		MailInboxLoader loader = new MailInboxLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(MailInboxService.class);
		emailService = SpringUtil.getAppContext().getBean(EmailAddressService.class);
		startTransaction();
		try {
			loadMailInboxs();
			//loadTestUsers();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadMailInboxs() {
		for (MailInboxEnum mc : MailInboxEnum.values()) {
			MailInbox in = buildMailBoxFromEnum(mc);
			service.insert(in);
			
			loadMailInboxEmailAddrs();
		}
		logger.info("EntityManager persisted the record.");
	}
	
	public static MailInbox buildMailBoxFromEnum(MailInboxEnum mc) {
		MailInbox in = new MailInbox();
		MailInboxPK pk = new MailInboxPK(mc.getUserId(),mc.getHostName());
		in.setMailInboxPK(pk);
		in.setUserPswd(mc.getUserPswd());
		in.setPortNumber(mc.getPort());
		in.setProtocol(mc.getProtocol().value());
		in.setDescription(mc.getDescription());
		in.setStatusId(mc.getStatus().getValue());
		in.setIsInternalOnly(mc.getIsInternalOnly());
		in.setReadPerPass(mc.getReadPerPass());
		in.setUseSsl(mc.isUseSsl());
		in.setNumberOfThreads(mc.getNumberOfThreads());
		in.setMaximumRetries(mc.getMaximumRetries());
		in.setMinimumWait(mc.getMinimumWait());
		in.setMessageCount(mc.getMessageCount());
		in.setIsToPlainText(mc.getIsToPlainText());
		in.setToAddressDomain(mc.getToAddressDomain());
		if (StringUtils.isBlank(in.getToAddressDomain())) {
			in.setToAddressDomain(in.getMailInboxPK().getHostName());
		}
		in.setIsCheckDuplicate(mc.getIsCheckDuplicate());
		in.setIsAlertDuplicate(mc.getIsAlertDuplicate());
		in.setIsLogDuplicate(mc.getIsLogDuplicate());
		in.setPurgeDupsAfter(mc.getPurgeDupsAfter());
		return in;
	}
	
	private void loadMailInboxEmailAddrs() {
		List<MailInbox> mailBoxes = service.getAll(false);
		int count = 0;
		for (MailInbox mailbox : mailBoxes) {
			emailService.findSertAddress(mailbox.getMailInboxPK().getUserId() + "@" + mailbox.getMailInboxPK().getHostName());
			count ++;
		}
		logger.info("Inserted/Upadted (" + count + ") EmailAddress records.");
	}
	
	void loadTestUsers() {
		MailInbox vo = new MailInbox();
		vo.setPortNumber(-1);
		vo.setProtocol(MailProtocol.POP3.value());
		vo.setFolderName("Inbox");
		vo.setDescription("Test User");
		vo.setStatusId(StatusId.ACTIVE.getValue());
		vo.setCarrierCode(CarrierCode.SMTPMAIL.getValue());
		vo.setIsInternalOnly(false);
		vo.setReadPerPass(5);
		vo.setUseSsl(false);
		vo.setNumberOfThreads(2);
		vo.setMaximumRetries(5);
		vo.setMinimumWait(10);
		vo.setMessageCount(-1);
		vo.setIsToPlainText(false);
		vo.setToAddressDomain("localhost");
		vo.setIsCheckDuplicate(true);
		vo.setIsAlertDuplicate(true);
		vo.setIsLogDuplicate(true);
		vo.setPurgeDupsAfter(Integer.valueOf(6));
		vo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		int usersInserted = 0;
		for (int i = 0; i < 100; i++) {
			String user = "user" + StringUtils.leftPad(i + "", 2, "0");
			MailInboxPK pk = new MailInboxPK(user,"localhost");
			vo.setMailInboxPK(pk);
			vo.setUserPswd(user);
			service.insert(vo);
			usersInserted++;
		}
		logger.info("Users inserted: " + usersInserted);
	}

}

