package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;

import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.constant.VariableType;
import jpa.constant.XHeaderName;
import jpa.data.preload.SenderVariableEnum;
import jpa.data.preload.GlobalVariableEnum;
import jpa.model.SenderData;
import jpa.model.SenderVariable;
import jpa.model.SenderVariablePK;
import jpa.model.GlobalVariable;
import jpa.model.GlobalVariablePK;
import jpa.service.common.GlobalVariableService;
import jpa.service.common.SenderDataService;
import jpa.service.common.SenderVariableService;
import jpa.spring.util.SpringUtil;

public class VariableDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(VariableDataLoader.class);
	private SenderVariableService cvService;
	private GlobalVariableService gvService;
	private SenderDataService senderService;

	public static void main(String[] args) {
		VariableDataLoader loader = new VariableDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		cvService = SpringUtil.getAppContext().getBean(SenderVariableService.class);
		gvService = SpringUtil.getAppContext().getBean(GlobalVariableService.class);
		senderService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		startTransaction();
		try {
			loadSenderVariables();
			loadGlobalVariables();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadSenderVariables() throws SQLException {
		SenderData cd = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);

		Timestamp updtTime = new Timestamp(System.currentTimeMillis());
		for (SenderVariableEnum variable : SenderVariableEnum.values()) {
			SenderVariable in = new SenderVariable();
			SenderVariablePK pk1 = new SenderVariablePK(cd, variable.name(), updtTime);
			in.setSenderVariablePK(pk1);
			in.setVariableValue(variable.getDefaultValue());
			in.setVariableFormat(variable.getVariableFormat());
			in.setVariableType(variable.getVariableType().getValue());
			in.setStatusId(StatusId.ACTIVE.getValue());
			in.setAllowOverride(variable.getAllowOverride().getValue());
			in.setRequired(Boolean.FALSE);
			cvService.insert(in);
		}
		
		List<SenderData> senders = senderService.getAll();
		for (SenderData sender : senders) {
			SenderVariable in = new SenderVariable();
			SenderVariablePK pk1 = new SenderVariablePK(sender, "SenderId", updtTime);
			in.setSenderVariablePK(pk1);
			in.setVariableValue(sender.getSenderId());
			in.setVariableFormat(null);
			in.setVariableType(VariableType.TEXT.getValue());
			in.setStatusId(StatusId.ACTIVE.getValue());
			in.setAllowOverride(CodeType.YES_CODE.getValue());
			in.setRequired(Boolean.FALSE);
			cvService.insert(in);
			
			in = new SenderVariable();
			pk1 = new SenderVariablePK(sender, "DomainName", updtTime);
			in.setSenderVariablePK(pk1);
			in.setVariableValue(sender.getDomainName());
			in.setVariableFormat(null);
			in.setVariableType(VariableType.TEXT.getValue());
			in.setStatusId(StatusId.ACTIVE.getValue());
			in.setAllowOverride(CodeType.YES_CODE.getValue());
			in.setRequired(Boolean.FALSE);
			cvService.insert(in);

			in = new SenderVariable();
			pk1 = new SenderVariablePK(sender, "SenderName", updtTime);
			in.setSenderVariablePK(pk1);
			in.setVariableValue(sender.getSenderName());
			in.setVariableFormat(null);
			in.setVariableType(VariableType.TEXT.getValue());
			in.setStatusId(StatusId.ACTIVE.getValue());
			in.setAllowOverride(CodeType.YES_CODE.getValue());
			in.setRequired(Boolean.FALSE);
			cvService.insert(in);

			in = new SenderVariable();
			pk1 = new SenderVariablePK(sender, "WebSiteUrl", updtTime);
			in.setSenderVariablePK(pk1);
			in.setVariableValue(sender.getWebSiteUrl());
			in.setVariableFormat(null);
			in.setVariableType(VariableType.TEXT.getValue());
			in.setStatusId(StatusId.ACTIVE.getValue());
			in.setAllowOverride(CodeType.YES_CODE.getValue());
			in.setRequired(Boolean.FALSE);
			cvService.insert(in);

			in = new SenderVariable();
			pk1 = new SenderVariablePK(sender, "ContactEmailAddress", updtTime);
			in.setSenderVariablePK(pk1);
			in.setVariableValue(sender.getSubrCareEmail());
			in.setVariableFormat(null);
			in.setVariableType(VariableType.ADDRESS.getValue());
			in.setStatusId(StatusId.ACTIVE.getValue());
			in.setAllowOverride(CodeType.YES_CODE.getValue());
			in.setRequired(Boolean.FALSE);
			cvService.insert(in);
		}
		logger.info("EntityManager persisted the record.");
	}
	
	private void loadGlobalVariables() throws SQLException {
		java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());

		for (GlobalVariableEnum variable : GlobalVariableEnum.values()) {
			GlobalVariable in = new GlobalVariable();
			GlobalVariablePK pk1 = new GlobalVariablePK(variable.name(), updtTime);
			in.setGlobalVariablePK(pk1);
			in.setVariableValue(variable.getDefaultValue());
			in.setVariableFormat(variable.getVariableFormat());
			in.setVariableType(variable.getVariableType().getValue());
			in.setStatusId(StatusId.ACTIVE.getValue());
			in.setAllowOverride(variable.getAllowOverride().getValue());
			in.setRequired(false);
			gvService.insert(in);
		}

		GlobalVariable in = new GlobalVariable();
		GlobalVariablePK pk1 = new GlobalVariablePK(XHeaderName.SENDER_ID.value(), updtTime);
		in.setGlobalVariablePK(pk1);
		in.setVariableValue(Constants.DEFAULT_SENDER_ID);
		in.setVariableFormat(null);
		in.setVariableType(VariableType.X_HEADER.getValue());
		in.setStatusId(StatusId.ACTIVE.getValue());
		in.setAllowOverride(CodeType.YES_CODE.getValue());
		in.setRequired(false);
		gvService.insert(in);
		
		logger.info("loadGlobalVariables() completed.\n"+in);
	}
}

