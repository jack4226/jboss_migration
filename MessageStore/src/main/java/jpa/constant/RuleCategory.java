package jpa.constant;

public enum RuleCategory {
	PRE_RULE("E"),
	MAIN_RULE("M"),
	POST_RULE("P");
	
	private String value;
	private RuleCategory(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
