package jpa.data.preload;

public enum QueueNameEnum {
	RMA_REQUEST_INPUT("rmaRequestInputJmsTemplate"),
	SUBSCRIBER_CARE_INPUT("subscriberCareInputJmsTemplate");
	
	private String jmstemplate;
	private QueueNameEnum(String jmstemplate) {
		this.jmstemplate = jmstemplate;
	}
	public String getJmstemplate() {
		return jmstemplate;
	}
}
