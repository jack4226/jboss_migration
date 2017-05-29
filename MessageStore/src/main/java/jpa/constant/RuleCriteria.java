package jpa.constant;

/** define criteria for simple rule */
public enum RuleCriteria {
	STARTS_WITH("starts_with"),
	ENDS_WITH("ends_with"),
	CONTAINS("contains"),
	EQUALS("equals"),
	NOT_EQUALS("not_equals"),
	GREATER_THAN("greater_than"),
	LESS_THAN("less_than"),
	GE("greater_than_or_equal_to"),
	LE("less_than_or_equal_to"),
	IS_NOT_BLANK("is_not_blank"),
	IS_BLANK("is_blank"),
	REG_EX("reg_ex");

	private String value;
	private RuleCriteria(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	public static RuleCriteria getByValue(String value) {
		for (RuleCriteria type : RuleCriteria.values()) {
			if (type.getValue().equals(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("No enum const value jpa.constant.RuleCriteria." + value);
	}
}

