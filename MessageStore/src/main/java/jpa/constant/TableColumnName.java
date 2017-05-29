package jpa.constant;

public enum TableColumnName {

	// address names associated to sender_data table columns
	SUBSCRIBER_CARE_ADDR("subrCareEmail"),
	SECURITY_DEPT_ADDR("securityEmail"),
	RMA_DEPT_ADDR("rmaDeptEmail"),
	SPAM_CONTROL_ADDR("spamCntrlEmail"),
	VIRUS_CONTROL_ADDR("virusCntrlEmail"),
	CHALLENGE_HANDLER_ADDR("chaRspHndlrEmail");

	private final String value;
	private TableColumnName(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
