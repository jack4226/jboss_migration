package jpa.constant;

public enum StatusId {
	// define general statusId	
	ACTIVE("A"),
	INACTIVE("I"),
	SUSPENDED("S"); // for email address

	private final String value;
	
	private StatusId(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static StatusId getByValue(String value) {
		for (StatusId cc : StatusId.values()) {
			if (cc.getValue().equalsIgnoreCase(value)) {
				return cc;
			}
		}
		return StatusId.ACTIVE; // default to ACTIVE
	}

}
