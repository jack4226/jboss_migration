package jpa.constant;

public enum CodeType {
	YES_CODE("Y"),
	NO_CODE("N"),
	MANDATORY_CODE("M"),
	YES("Yes"),
	NO("No");
	
	private final String value;
	private CodeType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}