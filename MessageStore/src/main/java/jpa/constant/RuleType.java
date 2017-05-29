package jpa.constant;

/** define rule type constants */
public enum RuleType {
	SIMPLE("Simple"),
	ALL("All"),
	ANY("Any"),
	NONE("None");
	
	private String value;
	private RuleType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static RuleType getByValue(String value) {
		for (RuleType type : RuleType.values()) {
			if (type.getValue().equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("No enum const value jpa.constant.RuleType." + value);
	}
} 

