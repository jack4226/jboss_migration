package jpa.dataloader;

import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.data.preload.FolderEnum;
import jpa.model.msg.MessageFolder;
import jpa.service.msgdata.MessageFolderService;
import jpa.spring.util.SpringUtil;

public class MessageFolderLoader extends AbstractDataLoader {
	static final Logger logger = LogManager.getLogger(MessageFolderLoader.class);
	private MessageFolderService service;

	public static void main(String[] args) {
		MessageFolderLoader loader = new MessageFolderLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(MessageFolderService.class);
		startTransaction();
		try {
			loadMessageFolders();
			//loadTestUsers();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadMessageFolders() {
		for (FolderEnum mc : FolderEnum.values()) {
			MessageFolder in = buildFolderFromEnum(mc);
			service.insert(in);
		}
		logger.info("EntityManager persisted the records.");
	}
	
	public static MessageFolder buildFolderFromEnum(FolderEnum mc) {
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		MessageFolder in = new MessageFolder();
		in.setFolderName(mc.name());
		in.setDescription(mc.getDescription());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setUpdtTime(updtTime);
		in.setUpdtUserId(Constants.DEFAULT_USER_ID);
		logger.info("Folder inserted: " + mc.name());
		return in;
	}

}

