package jpa.dataloader;

import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.data.preload.SubscriberEnum.Subscriber;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.spring.util.SpringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class EmailAddressLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(EmailAddressLoader.class);
	private EmailAddressService service;

	public static void main(String[] args) {
		EmailAddressLoader loader = new EmailAddressLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(EmailAddressService.class);
		startTransaction();
		try {
			loadEmailAddress();
			loadTestAddresses();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadEmailAddress() {
		int count = 0;
		for (Subscriber sub : Subscriber.values()) {
			if (service.getByAddress(sub.getAddress()) != null) {
				logger.info("Address \"" + sub.getAddress() + "\" already exists!");
				continue;
			}
			EmailAddress data = new EmailAddress();
			data.setOrigAddress(sub.getAddress());
			data.setAddress(data.getOrigAddress());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStatusChangeTime(new Timestamp(System.currentTimeMillis()));
			data.setStatusChangeUserId("testuser" + (++count));
			data.setBounceCount(0);
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		
		logger.info("EntityManager persisted the record.");
	}
	
	void loadTestAddresses() {
		int count = 50;
		for (int i = 0; i < count; i++) {
			service.findSertAddress("user" + StringUtils.leftPad(i + "", 2, '0') + "@localhost");
		}
	}
}

