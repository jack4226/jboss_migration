package jpa.constant;

import org.apache.commons.lang3.StringUtils;

public enum XHeaderName {
	//
	// define X-Header names, used to communicate between MailSender and its
	// calling programs when a raw message stream is passed to MailSender.
	// Calling programs should set their values accordingly.
	//
	MSG_ID("X-Msg_Id"),
	MSG_REF_ID("X-MsgRef_Id"),
	USE_SECURE_SMTP("X-Use_Secure_Smtp"), // Yes/No
	SAVE_RAW_STREAM("X-Save_Raw_Stream"), // Yes/No
	OVERRIDE_TEST_ADDR("X-Override_Test_Addr"), // Yes/No
	EMBED_EMAILID("X-Embed_Email_Id"), // Yes/No
	EMAIL_ID(EmailIdToken.XHEADER_NAME),
	RENDER_ID("X-Render_Id"),
	RULE_NAME("X-Rule_Name"),
	ORIG_RULE_NAME("X-Orig_Rule_Name"),
	SENDER_ID("X-Sender_Id"),
	SUBSCRIBER_ID("X-Subscriber_Id"),
	PRIORITY("X-Priority"),
	MAILER("X-Mailer"),
	RETURN_PATH("Return-Path"),
	PRECEDENCE("Precedence"),
	SPAM_SCORE("X_Spam_Score");

	private final String value;
	private XHeaderName(String value) {
		this.value = value;
	}
	public String value() {
		return value;
	}
	
	public static XHeaderName getByValue(String value) {
		if (StringUtils.isBlank(value)) {
			return null;
		}
		for (XHeaderName hdr : XHeaderName.values()) {
			if (hdr.value().equals(value)) {
				return hdr;
			}
		}
		throw new IllegalArgumentException("No enum const value jpa.constant.XHeaderName." + value);
	}
}
