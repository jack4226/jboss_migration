package jpa.constant;

public enum EmailVariableType {
	Custom("C"),
	System("S");
	
	private final String value;
	private EmailVariableType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
} 
