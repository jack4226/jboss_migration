package jpa.data.preload;

import org.apache.commons.lang3.StringUtils;

import jpa.service.task.ActivateAddress;
import jpa.service.task.AssignRuleName;
import jpa.service.task.AutoReplyMessage;
import jpa.service.task.BounceUpAddress;
import jpa.service.task.BroadcastToList;
import jpa.service.task.CloseMessage;
import jpa.service.task.CsrReplyMessage;
import jpa.service.task.DeliveryError;
import jpa.service.task.DropMessage;
import jpa.service.task.ForwardMessage;
import jpa.service.task.ForwardToCsr;
import jpa.service.task.OpenMessage;
import jpa.service.task.SaveMessage;
import jpa.service.task.SendMessage;
import jpa.service.task.SubscribeToList;
import jpa.service.task.SuspendAddress;
import jpa.service.task.UnsubscribeFromList;

/*
 * define rule actions
 */
public enum RuleActionDetailEnum {
	ACTIVATE("activete email address",serviceName(ActivateAddress.class),null,RuleDataTypeEnum.EMAIL_ADDRESS),
	BOUNCE_UP("increase bounce count",serviceName(BounceUpAddress.class),null,RuleDataTypeEnum.EMAIL_ADDRESS),
	CLOSE("close the message",serviceName(CloseMessage.class),null,null),
	CSR_REPLY("send off the reply from csr",serviceName(CsrReplyMessage.class),null,null),
	AUTO_REPLY("reply to the message automatically",serviceName(AutoReplyMessage.class),null,RuleDataTypeEnum.TEMPLATE_ID),
	MARK_DLVR_ERR("mark delivery error",serviceName(DeliveryError.class),null,null),
	DROP("drop the message",serviceName(DropMessage.class),DropMessage.class.getName(), null),
	FORWARD("forward the message",serviceName(ForwardMessage.class),null,RuleDataTypeEnum.EMAIL_ADDRESS),
	TO_CSR("redirect to message queue",serviceName(ForwardToCsr.class),null,RuleDataTypeEnum.EMAIL_ADDRESS),
	SAVE("save the message",serviceName(SaveMessage.class),null,null),
	SENDMAIL("simply send the mail off",serviceName(SendMessage.class),null,RuleDataTypeEnum.EMAIL_ADDRESS),
	SUSPEND("suspend email address",serviceName(SuspendAddress.class),null,RuleDataTypeEnum.EMAIL_ADDRESS),
	UNSUBSCRIBE("remove from the mailing list",serviceName(UnsubscribeFromList.class),null,RuleDataTypeEnum.EMAIL_ADDRESS),
	SUBSCRIBE("subscribe to the mailing list",serviceName(SubscribeToList.class),null,RuleDataTypeEnum.EMAIL_ADDRESS),
	ASSIGN_RULENAME("set a rule mame and re-process",serviceName(AssignRuleName.class),null,RuleDataTypeEnum.RULE_NAME),
	OPEN("open the message",serviceName(OpenMessage.class),null,null),
	BROADCAST("broadcast to mailing list",serviceName(BroadcastToList.class),null,RuleDataTypeEnum.MAILING_LIST);

	private String description;
	private String serviceName;
	private String className;
	private RuleDataTypeEnum dataType;

	private RuleActionDetailEnum(String description, String serviceName,
			String className, RuleDataTypeEnum dataType) {
		this.description = description;
		this.serviceName = serviceName;
		this.className = className;
		this.dataType = dataType;
	}
	
	static String serviceName(Class<?> clazz) {
		return StringUtils.uncapitalize(clazz.getSimpleName());
	}

	public String getDescription() {
		return description;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getClassName() {
		return className;
	}

	public RuleDataTypeEnum getDataType() {
		return dataType;
	}

	public static void main(String[] args) {
		System.out.println(ACTIVATE.getServiceName());
	}
}
