package jpa.constant;

public enum MsgDirectionCode {
	//
	// define message direction code
	//
	RECEIVED("R"),
	SENT("S");
	
	private String value;
	private MsgDirectionCode(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
