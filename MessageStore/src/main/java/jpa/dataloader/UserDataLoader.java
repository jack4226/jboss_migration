package jpa.dataloader;

import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.SenderData;
import jpa.model.SessionUpload;
import jpa.model.SessionUploadPK;
import jpa.model.UserData;
import jpa.service.common.SenderDataService;
import jpa.service.common.SessionUploadService;
import jpa.service.common.UserDataService;
import jpa.spring.util.SpringUtil;

import org.apache.log4j.Logger;

public class UserDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(UserDataLoader.class);
	private UserDataService service;
	private SenderDataService senderService;
	private SessionUploadService uploadService;

	public static void main(String[] args) {
		UserDataLoader loader = new UserDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(UserDataService.class);
		senderService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		uploadService = SpringUtil.getAppContext().getBean(SessionUploadService.class);
		startTransaction();
		try {
			loadUserData();
			loadSessionUploads();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadUserData() {
		SenderData cd = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		UserData data = new UserData();
		data.setSenderData(cd);
		data.setUserId("admin");
		data.setPassword("admin");
		data.setFirstName("default");
		data.setLastName("admin");
		data.setCreateTime(new Timestamp(System.currentTimeMillis()));
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setRole(Constants.ADMIN_ROLE);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		data = new UserData();
		data.setSenderData(cd);
		data.setUserId(getProperty("user.id.1"));
		data.setPassword(getProperty("user.password.1"));
		data.setFirstName("default");
		data.setLastName("user");
		data.setCreateTime(new Timestamp(System.currentTimeMillis()));
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setRole(Constants.USER_ROLE);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);

		logger.info("EntityManager persisted the record.");
	}
	
	private void loadSessionUploads() {
		UserData usr = service.getByUserId(getProperty("user.id.1"));

		SessionUploadPK pk1 = new SessionUploadPK("test_session_id",0);
		SessionUpload data = new SessionUpload();
		data.setSessionUploadPK(pk1);
		data.setFileName("test1.txt");
		data.setContentType("text/plain");
		data.setUserData(usr);
		data.setSessionValue("test upload text 1".getBytes());
		uploadService.insert(data);

		pk1 = new SessionUploadPK("test_session_id",1);
		data = new SessionUpload();
		data.setSessionUploadPK(pk1);
		data.setFileName("test2.txt");
		data.setContentType("text/plain");
		data.setUserData(usr);
		data.setSessionValue("test upload text 2".getBytes());
		uploadService.insert(data);
	}
}

