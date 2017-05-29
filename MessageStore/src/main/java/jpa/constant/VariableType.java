package jpa.constant;
public enum VariableType {
	ADDRESS("A"), 
	TEXT("T"), 
	NUMERIC("N"), 
	DATETIME("D"), 
	X_HEADER("X"), 
	LOB("L"), // body template only
	COLLECTION("C"); // a collection of <HashMap>s (for Table section)

	private final String value;
	private VariableType(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	
	public static VariableType getByValue(String value) {
		for (VariableType t : VariableType.values()) {
			if (t.getValue().equalsIgnoreCase(value)) {
				return t;
			}
		}
		throw new IllegalArgumentException("No enum value (" + value + ") found in class jpa.constant.VariableType");
	}
}
