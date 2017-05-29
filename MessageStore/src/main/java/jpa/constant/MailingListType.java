package jpa.constant;

public enum MailingListType {

	// define mailing list types
	TRADITIONAL("Traditional"),
	PERSONALIZED("Personalized");

	private final String value;
	private MailingListType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
