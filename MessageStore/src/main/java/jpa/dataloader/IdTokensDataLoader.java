package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.EmailIdToken;
import jpa.model.SenderData;
import jpa.model.IdTokens;
import jpa.service.common.IdTokensService;
import jpa.service.common.SenderDataService;
import jpa.spring.util.SpringUtil;

import org.apache.log4j.Logger;

public class IdTokensDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(IdTokensDataLoader.class);
	private IdTokensService itService;
	private SenderDataService senderService;

	public static void main(String[] args) {
		IdTokensDataLoader loader = new IdTokensDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		itService = SpringUtil.getAppContext().getBean(IdTokensService.class);
		senderService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		startTransaction();
		try {
			loadIdTokens();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	void loadIdTokens() throws SQLException {
		SenderData cd = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		IdTokens in = new IdTokens();

		Timestamp updtTime = new Timestamp(new java.util.Date().getTime());

		in.setSenderData(cd);
		in.setDescription("Default SenderId");
		in.setBodyBeginToken(EmailIdToken.BODY_BEGIN);
		in.setBodyEndToken(EmailIdToken.BODY_END);
		in.setXheaderName(EmailIdToken.XHEADER_NAME);
		in.setXhdrBeginToken(EmailIdToken.XHDR_BEGIN);
		in.setXhdrEndToken(EmailIdToken.XHDR_END);
		in.setMaxLength(EmailIdToken.MAXIMUM_LENGTH);
		in.setUpdtTime(updtTime);
		in.setUpdtUserId("SysAdmin");
		itService.insert(in);
		logger.info("EntityManager persisted the record.");
	}
	
}

