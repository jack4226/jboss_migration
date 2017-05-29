package jpa.constant;

public enum EmailAddrType {
	FROM_ADDR("From"),
	REPLYTO_ADDR("Reply-To"),
	TO_ADDR("To"),
	CC_ADDR("Cc"),
	BCC_ADDR("Bcc"),
	
	FORWARD_ADDR("Forward"),
	FINAL_RCPT_ADDR("FinalRcpt"),
	ORIG_RCPT_ADDR("OrigRcpt");
	
	private String value;
	EmailAddrType(String value) {
		this.value=value;
	}
	public String getValue() {
		return value;
	}
	@Override public String toString() {
		return getValue();
	}
	public EmailAddrType fromValue(String value) {
		for (EmailAddrType v : EmailAddrType.values()) {
			if (v.getValue().equalsIgnoreCase(value)) {
				return v;
			}
		}
		return null;
	}
}
