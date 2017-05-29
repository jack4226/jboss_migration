package jpa.constant;

public enum SenderType {
	Custom("C"),
	System("S");
	
	private final String value;
	private SenderType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
} 
