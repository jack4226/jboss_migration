package jpa.data.preload;

/*
 * define sample mailing list subscribers
 */
public enum SubscriberEnum {

	SUBLST1(MailingListEnum.SMPLLST1, Subscriber.values(),true),
	SUBLST2(MailingListEnum.SMPLLST2, Subscriber.values(),true);
	
	private MailingListEnum mailingList;
	private Subscriber[] subscribers;
	private boolean isSubscribed;
	private SubscriberEnum(MailingListEnum mailingList, Subscriber[] subscribers, boolean isSubscribed) {
		this.mailingList = mailingList;
		this.subscribers = subscribers;
		this.isSubscribed = isSubscribed;
	}
	
	public MailingListEnum getMailingList() {
		return mailingList;
	}

	public Subscriber[] getSubscribers() {
		return subscribers;
	}

	public boolean isSubscribed() {
		return isSubscribed;
	}

	public static enum Subscriber {
		Subscriber1("jsmith@test.com"),
		Subscriber2("test@test.com"),
		Subscriber3("testuser@test.com");
		
		private String address;
		private Subscriber(String address) {
			this.address = address;
		}
		public String getAddress() {
			return address;
		}
	}
}
