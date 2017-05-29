package jpa.constant;

import jpa.data.preload.RuleDataTypeEnum;

/** define rule search field name for Internet mail */
public enum RuleDataName {
	FROM_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, EmailAddrType.FROM_ADDR.getValue()),
	TO_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, EmailAddrType.TO_ADDR.getValue()),
	REPLYTO_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, EmailAddrType.REPLYTO_ADDR.getValue()),
	CC_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, EmailAddrType.CC_ADDR.getValue()),
	BCC_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, EmailAddrType.BCC_ADDR.getValue()),
	
	SUBJECT(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.SUBJECT.getValue()),
	BODY(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.BODY.getValue()),
	MSG_ID(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.MSG_ID.getValue()),
	MSG_REF_ID(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.MSG_REF_ID.getValue()),
	RULE_NAME(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.RULE_NAME.getValue()),
	X_HEADER(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.XHEADER_DATA_NAME.getValue()),
	RETURN_PATH(RuleDataTypeEnum.EMAIL_PROPERTY, "ReturnPath"),
	// mailbox properties
	MAILBOX_USER(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.MAILBOX_USER.getValue()),
	MAILBOX_HOST(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.MAILBOX_HOST.getValue()),
	// the next two items are not implemented yet
	RFC822(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.RFC822.getValue()),
	DELIVERY_STATUS(RuleDataTypeEnum.EMAIL_PROPERTY, VariableName.DELIVERY_STATUS.getValue()),
	// define data type constants for Internet email attachments
	MIME_TYPE(RuleDataTypeEnum.EMAIL_PROPERTY, "MimeType"),
	FILE_NAME(RuleDataTypeEnum.EMAIL_PROPERTY, "FileName"),
	// define other email address properties
	FINAL_RCPT_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, EmailAddrType.FINAL_RCPT_ADDR.getValue()),
	ORIG_RCPT_ADDR(RuleDataTypeEnum.EMAIL_ADDRESS, EmailAddrType.ORIG_RCPT_ADDR.getValue());

	private RuleDataTypeEnum dataType;
	private String value;
	private RuleDataName(RuleDataTypeEnum dataType, String value) {
		this.dataType = dataType;
		this.value = value;
	}
	
	public RuleDataTypeEnum getRuleDataType() {
		return dataType;
	}

	public String getValue() {
		return value;
	}
}
