package jpa.dataloader;

import java.sql.SQLException;
import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.TableColumnName;
import jpa.constant.VariableName;
import jpa.data.preload.EmailTemplateEnum;
import jpa.data.preload.MailingListEnum;
import jpa.data.preload.QueueNameEnum;
import jpa.data.preload.RuleActionDetailEnum;
import jpa.data.preload.RuleActionEnum;
import jpa.data.preload.RuleDataTypeEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.model.SenderData;
import jpa.model.rule.RuleAction;
import jpa.model.rule.RuleActionDetail;
import jpa.model.rule.RuleDataType;
import jpa.model.rule.RuleDataValue;
import jpa.model.rule.RuleDataValuePK;
import jpa.model.rule.RuleLogic;
import jpa.service.common.SenderDataService;
import jpa.service.rule.RuleActionDetailService;
import jpa.service.rule.RuleActionService;
import jpa.service.rule.RuleDataTypeService;
import jpa.service.rule.RuleDataValueService;
import jpa.service.rule.RuleLogicService;
import jpa.spring.util.SpringUtil;
import jpa.util.PrintUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RuleActionLoader extends AbstractDataLoader {
	static final Logger logger = LogManager.getLogger(RuleActionLoader.class);
	private RuleDataTypeService typeService;
	private RuleDataValueService valueService;
	private RuleActionDetailService detailService;
	private RuleActionService actionService;
	private RuleLogicService logicService;
	private SenderDataService senderService;

	public static void main(String[] args) {
		RuleActionLoader loader = new RuleActionLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		typeService = SpringUtil.getAppContext().getBean(RuleDataTypeService.class);
		valueService = SpringUtil.getAppContext().getBean(RuleDataValueService.class);
		detailService = SpringUtil.getAppContext().getBean(RuleActionDetailService.class);
		actionService = SpringUtil.getAppContext().getBean(RuleActionService.class);
		logicService = SpringUtil.getAppContext().getBean(RuleLogicService.class);
		senderService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		startTransaction();
		try {
			loadRuleDataTypeAndValues();
			loadRuleActionDetails();
			loadRuleActions();
		} catch (SQLException e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadRuleDataTypeAndValues() throws SQLException {
		String jndiProperties = 
				"java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory" + LF +
				"java.naming.provider.url=jnp:////localhost:2099" + LF +
				"java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces";

		for (RuleDataTypeEnum type : RuleDataTypeEnum.values()) {
			RuleDataType tp = null;
			if (RuleDataTypeEnum.EMAIL_ADDRESS.equals(type)) {
				tp = new RuleDataType(RuleDataTypeEnum.EMAIL_ADDRESS.name(), RuleDataTypeEnum.EMAIL_ADDRESS.getDescription());
				typeService.insert(tp);
				// insert email address values
				for (EmailAddrType addrType : EmailAddrType.values()) {
					RuleDataValuePK pk1 = new RuleDataValuePK(tp, "$" + addrType.getValue());
					RuleDataValue data = new RuleDataValue(pk1, "MessageBean");
					valueService.insert(data);
				}
				// insert column names storing email address
				for (TableColumnName addrColumn : TableColumnName.values()) {
					RuleDataValuePK pk1 = new RuleDataValuePK(tp, "$" + addrColumn.getValue());
					RuleDataValue data = new RuleDataValue(pk1, "senderDataService");
					valueService.insert(data);
				}
			}
			else if (RuleDataTypeEnum.QUEUE_NAME.equals(type)) {
				tp = new RuleDataType(RuleDataTypeEnum.QUEUE_NAME.name(), RuleDataTypeEnum.QUEUE_NAME.getDescription());
				typeService.insert(tp);
				for (QueueNameEnum queue : QueueNameEnum.values()) {
					RuleDataValuePK pk1 = new RuleDataValuePK(tp, "$" + queue.name());
					RuleDataValue data = new RuleDataValue(pk1, queue.getJmstemplate());
					valueService.insert(data);
				}
			}
			else if (RuleDataTypeEnum.TEMPLATE_ID.equals(type)) {
				tp = new RuleDataType(RuleDataTypeEnum.TEMPLATE_ID.name(), RuleDataTypeEnum.TEMPLATE_ID.getDescription());
				typeService.insert(tp);
				for (EmailTemplateEnum tmp : EmailTemplateEnum.values()) {
					RuleDataValuePK pk1 = new RuleDataValuePK(tp, tmp.name());
					RuleDataValue data = null;
					if (EmailTemplateEnum.SubscribeByEmailReply.equals(tmp)) {
						data = new RuleDataValue(pk1, jndiProperties);
					}
					else {
						data  = new RuleDataValue(pk1, null);
					}
					valueService.insert(data);
				}
			}
			else if (RuleDataTypeEnum.RULE_NAME.equals(type)) {
				tp = new RuleDataType(RuleDataTypeEnum.RULE_NAME.name(), RuleDataTypeEnum.RULE_NAME.getDescription());
				typeService.insert(tp);
				for (RuleNameEnum name : RuleNameEnum.values()) {
					if (RuleNameEnum.GENERIC.equals(name)) {
						continue; // skip GENERIC
					}
					RuleDataValuePK pk1 = new RuleDataValuePK(tp, name.getValue());
					RuleDataValue data = new RuleDataValue(pk1, null);
					valueService.insert(data);
				}
			}
			else if (RuleDataTypeEnum.MAILING_LIST.equals(type)) {
				tp = new RuleDataType(RuleDataTypeEnum.MAILING_LIST.name(), RuleDataTypeEnum.MAILING_LIST.getDescription());
				typeService.insert(tp);
				// TODO
				for (MailingListEnum list : MailingListEnum.values()) {
					RuleDataValuePK pk1 = new RuleDataValuePK(tp, "$" + list.name());
					RuleDataValue data = new RuleDataValue(pk1, list.getAcctName());
					valueService.insert(data);
				}
			}
			else if (RuleDataTypeEnum.EMAIL_PROPERTY.equals(type)) {
				tp = new RuleDataType(RuleDataTypeEnum.EMAIL_PROPERTY.name(), RuleDataTypeEnum.EMAIL_PROPERTY.getDescription());
				typeService.insert(tp);
				for (VariableName var : VariableName.values()) {
					RuleDataValuePK pk1 = new RuleDataValuePK(tp, "$" + var.getValue());
					RuleDataValue data = new RuleDataValue(pk1, null);
					valueService.insert(data);
				}
			}
			else {
				tp = new RuleDataType(type.name(), type.getDescription());
				typeService.insert(tp);
			}
		}
		
//		tp = new RuleDataType(RuleDataTypeEnum.TEMPLATE_ID.name(), RuleDataTypeEnum.TEMPLATE_ID.getDescription());
//		typeService.insert(tp);
//		pk1 = new RuleDataValuePK(tp, "SubscribeByEmailReply");
//		data = new RuleDataValue(pk1, jndiProperties);
//		valueService.insert(data);

		logger.info("EntityManager persisted the record.");
	}
	
	private void loadRuleActionDetails() {
		for (RuleActionDetailEnum ruleAction : RuleActionDetailEnum.values()) {
			RuleDataType tp1 = null;
			if (ruleAction.getDataType()!=null) {
				tp1 = typeService.getByDataType(ruleAction.getDataType().name());
			}
			RuleActionDetail act = new RuleActionDetail(tp1, ruleAction.name(),
					ruleAction.getDescription(), ruleAction.getServiceName(),
					ruleAction.getClassName());
			detailService.insert(act);
		}
		
		logger.info("EntityManager persisted the record.");
	}
	
	private void loadRuleActions() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		for (RuleActionEnum act : RuleActionEnum.values()) {
			RuleLogic logic = logicService.getByRuleName(act.getRuleName().getValue());
			RuleActionDetail detail = detailService.getByActionId(act.getActionDetail().name());
			SenderData sender = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
			RuleAction action = new RuleAction(logic,act.getSequence(),now,sender,detail,act.getFieldValues());
			actionService.insert(action);
		}

		SenderData sender = senderService.getBySenderId("JBatchCorp");
		if (sender != null)
			logger.debug("JbatchCorp Sender found: " + PrintUtil.prettyPrint(sender));
		/*
		logic = logicService.getByRuleName(RuleNameEnum.GENERIC.getValue());
		dtl = detailService.getByActionId(RuleActionDetailEnum.SAVE.name());
		act = new RuleAction(logic,1,now,sender,dtl,null);
		actionService.insert(act);
		dtl = detailService.getByActionId(RuleActionDetailEnum.FORWARD.name());
		act = new RuleAction(logic,2,now,sender,dtl,"$"+TableColumnName.SUBSCRIBER_CARE_ADDR);
		actionService.insert(act);
		 */

		logger.info("EntityManager persisted the record.");
	}
}

