package jpa.dataloader;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import jpa.constant.SenderType;
import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.SenderData;
import jpa.service.common.SenderDataService;
import jpa.spring.util.SpringUtil;
import jpa.util.ProductUtil;
import jpa.util.TimestampUtil;

public class SenderDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(SenderDataLoader.class);
	private SenderDataService service;

	public static void main(String[] args) {
		SenderDataLoader loader = new SenderDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(SenderDataService.class);
		startTransaction();
		try {
			loadSenderData(true);
			loadJBatchData();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadSenderData(boolean loadTestData) {
		SenderData data = new SenderData();
		data.setSenderId(Constants.DEFAULT_SENDER_ID);
		data.setSenderName(getProperty("sender.name"));
		data.setDomainName(getProperty("sender.domain")); // domain name
		data.setSenderType(SenderType.System.getValue());
		data.setContactName(getProperty("sender.contact.name"));
		data.setContactPhone(getProperty("sender.contact.phone"));
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setIrsTaxId("0000000000");
		data.setWebSiteUrl(getProperty("sender.website.url"));
		data.setSaveRawMsg(true); // save raw stream
		data.setVirusCntrlEmail(getProperty("sender.contact.email"));
		data.setSecurityEmail(getProperty("sender.security.email"));
		data.setSubrCareEmail(getProperty("sender.subscriber.care.email"));
		data.setRmaDeptEmail(getProperty("sender.rma.dept.email"));
		data.setSpamCntrlEmail(getProperty("sender.spam.control.email"));
		data.setChaRspHndlrEmail(getProperty("sender.challenge.email"));
		data.setEmbedEmailId(true); // Embed EmailId 
		data.setReturnPathLeft("support"); // return-path left
		data.setUseTestAddr(true); // use testing address
		data.setTestFromAddr(getProperty("sender.test.from.address"));
		data.setTestToAddr(getProperty("sender.test.to.address"));
		data.setVerpEnabled(true); // is VERP enabled
		data.setVerpSubDomain(null); // VERP sub-domain
		data.setVerpInboxName("bounce"); // VERP bounce mailbox
		data.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		String systemId = TimestampUtil.db2ToDecimalString(TimestampUtil.getCurrentDb2Tms());
		data.setSystemId(systemId);
		data.setSystemKey(ProductUtil.getProductKeyFromFile());
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		logger.info("EntityManager persisted the record.");
	}
	
	private void loadJBatchData() throws SQLException {
		SenderData data = new SenderData();
		data.setSenderId("JBatchCorp");
		data.setSenderName("JBatch Corp. Site");
		data.setDomainName("jbatch.com"); // domain name
		data.setSenderType(SenderType.Custom.getValue());
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setIrsTaxId( "0000000000");
		data.setWebSiteUrl("http://www.jbatch.com");
		data.setSaveRawMsg(true); // save raw stream
		data.setVirusCntrlEmail("sitemaster@jbatch.com");
		data.setSecurityEmail("security@jbatch.com");
		data.setSubrCareEmail("subrcare@jbatch.com");
		data.setRmaDeptEmail("rma_dept@jbatch.com");
		data.setSpamCntrlEmail("spam.control@jbatch.com");
		data.setChaRspHndlrEmail("challenge@jbatch.com");
		data.setEmbedEmailId(true);
		data.setReturnPathLeft("support"); // return-path left
		data.setUseTestAddr(false); // use testing address
		data.setTestFromAddr("testfrom@jbatch.com");
		data.setTestToAddr("testto@jbatch.com");
		data.setVerpEnabled(false); // is VERP enabled
		data.setVerpSubDomain(null); // VERP sub domain
		data.setVerpInboxName("bounce"); // VERP bounce mailbox
		data.setVerpRemoveInbox("remove"); // VERP un-subscribe mailbox
		data.setSystemId("");
		data.setSystemKey(null);
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		service.insert(data);
		logger.info("EntityManager persisted the record.");
	}
}

