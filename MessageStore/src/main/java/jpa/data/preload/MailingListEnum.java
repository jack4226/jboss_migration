package jpa.data.preload;

import jpa.constant.StatusId;

/*
 * define sample mailing lists
 */
public enum MailingListEnum {

	SMPLLST1("Sample List 1","demolist1","Sample mailing list 1",StatusId.ACTIVE,false,false),
	SMPLLST2("Sample List 2","demolist2","Sample mailing list 2",StatusId.ACTIVE,false,false),
	SYSLIST1("NOREPLY Empty List","noreply","Auto-Responder, confirm subscription",StatusId.INACTIVE,true,false),
	ALERT_LIST("NOREPLY Empty List","noreply","System alert, subscriber order or internal errors",StatusId.INACTIVE,true,true),
	ORDER_LIST("Order Confirmation List","support","Auto-Responder, confirm subscriber order",StatusId.INACTIVE,true,true);
	
	private String displayName;
	private String acctName;
	private String description;
	private StatusId statusId;
	private boolean isBuiltin;
	private boolean isProd;

	private MailingListEnum(String displayName, String acctName,
			String description, StatusId statusId, boolean isBuiltin,
			boolean isProd) {
		this.displayName = displayName;
		this.acctName = acctName;
		this.description = description;
		this.statusId = statusId;
		this.isBuiltin = isBuiltin;
		this.isProd = isProd;
	}
	public String getDisplayName() {
		return displayName;
	}
	public String getAcctName() {
		return acctName;
	}
	public String getDescription() {
		return description;
	}
	public StatusId getStatusId() {
		return statusId;
	}
	public boolean isBuiltin() {
		return isBuiltin;
	}
	public boolean isProd() {
		return isProd;
	}
}
