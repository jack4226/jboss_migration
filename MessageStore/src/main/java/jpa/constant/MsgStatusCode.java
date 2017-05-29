package jpa.constant;

public enum MsgStatusCode {
	//
	// define out-bound message statusId
	//
	PENDING("P"),
	DELIVERED("D"),
	DELIVERY_FAILED("F"),
	//
	// define in-bound message status
	//
	CLOSED("C"),
	OPENED("O"),
	RECEIVED("R");

	private final String value;
	private MsgStatusCode(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
