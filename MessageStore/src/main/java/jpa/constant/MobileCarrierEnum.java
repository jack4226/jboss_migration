package jpa.constant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum MobileCarrierEnum {
	TMobile("T-Mobile USA", "tmomail.net", null, "1"),
	Verizon("Verizon Wireless", "vtext.com", "vzwpix.com", null),
	ATT("AT&T Mobility", "txt.att.net", "mms.att.net", null),
	Sprint("Sprint Nextel", "messaging.sprintpcs.com", "pm.sprint.com", null),
	Nextel("Nextel Direct", "page.nextel.com", "messaging.nextel.com", null),
	TracFone("TracFone Wireless", "mmst5.tracfone.com", null, null),
	MetroPCS("MetroPCS", "mymetropcs.com", null, null),
	USCellular("U.S. Cellular", "email.uscc.net", "mms.uscc.net", null),
	Cricket("Leap Wireless", "sms.mycricket.com", "mms.mycricket.com", null),
	Alltel("Alltel, Verizon", "text.wireless.alltel.com", "mms.alltel.net", null),
	Boost("Boost Mobile","sms.myboostmobile.com",null, null),
	Virgin("Virgin Mobile USA", "vmobl.com", "vmpix.com", null);
	
	private String value; // carrier name
	private String text; // email address for text
	private String mmedia; // picture and video
	private String country; // country code
	MobileCarrierEnum(String value, String text, String mmedia, String country) {
		this.value=value;
		this.text=text;
		this.mmedia=mmedia;
		this.country=country;
	}
	public String getValue() {
		return value;
	}
	public String getText() {
		return text;
	}
	public String getMmedia() {
		return mmedia;
	}

	public String getCountry() {
		return country;
	}
	public static MobileCarrierEnum getByValue(String value) throws IllegalArgumentException {
		for (MobileCarrierEnum mc : MobileCarrierEnum.values()) {
			if (mc.getValue().equalsIgnoreCase(value)) {
				return mc;
			}
		}
		throw new IllegalArgumentException("No enum const found by value (" + value + ")");
	}
	
	public static void main(String[] args) {
		Logger logger = LogManager.getLogger(MobileCarrierEnum.class);
		logger.info(MobileCarrierEnum.valueOf("TMobile"));
		logger.info(MobileCarrierEnum.getByValue("T-Mobile USA"));
	}
}
