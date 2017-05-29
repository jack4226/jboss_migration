package jpa.constant;

public enum MailingListDeliveryType {

	// define mailing list delivery options
	ALL_ON_LIST("All on list"),
	SUBSCRIBERS_ONLY("Subscribers only"),
	PROSPECTS_ONLY("Prospects only");

	private final String value;
	private MailingListDeliveryType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
