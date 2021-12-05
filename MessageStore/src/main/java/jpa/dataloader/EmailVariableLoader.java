package jpa.dataloader;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.data.preload.EmailVariableEnum;
import jpa.model.EmailVariable;
import jpa.service.common.EmailVariableService;
import jpa.spring.util.SpringUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmailVariableLoader extends AbstractDataLoader {
	static final Logger logger = LogManager.getLogger(EmailVariableLoader.class);
	private EmailVariableService service;

	public static void main(String[] args) {
		EmailVariableLoader loader = new EmailVariableLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(EmailVariableService.class);
		startTransaction();
		try {
			loadEmailVariables();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadEmailVariables() {
		for (EmailVariableEnum variable : EmailVariableEnum.values()) {
			EmailVariable data = new EmailVariable();
			data.setVariableName(variable.name());
			data.setVariableType(variable.getVariableType().getValue());
			data.setTableName(variable.getTableName());
			data.setColumnName(variable.getColumnName());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setBuiltin(variable.isBuiltin());
			data.setDefaultValue(variable.getDefaultValue());
			data.setVariableQuery(variable.getVariableQuery());
			data.setVariableProcName(variable.getVariableProcName());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		
		logger.info("EntityManager persisted the record.");
	}
	
}

