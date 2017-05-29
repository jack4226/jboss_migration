package jpa.data.preload;

import jpa.constant.MailProtocol;
import jpa.constant.StatusId;

public enum MailInboxEnum {
	SUPPORT("support","support","localhost",-1,MailProtocol.POP3, "Site Return Path",StatusId.ACTIVE,
			false,5,false,4,5,10,-1,false,null,true,true,true,24),
	SITEMASTER("sitemaster","sitemaster","localhost",-1,MailProtocol.POP3, "Site owner's mailbox",StatusId.ACTIVE,
			false,4,false,2,5,8,-1,false,null,true,true,true,24),
	BOUNCE("bounce","bounce","localhost",-1,MailProtocol.POP3, "VERP Bounce",StatusId.ACTIVE,
			false,4,false,2,5,8,-1,false,null,true,true,true,24),
	NOREPLY("noreply","noreply","localhost",-1,MailProtocol.POP3, "For all NOREPLY messages",StatusId.ACTIVE,
			false,4,false,2,5,8,-1,false,null,true,true,true,24),
	DEMOLST1("demolist1","demolist1","localhost",-1,MailProtocol.POP3, "Test List 1",StatusId.ACTIVE,
			false,4,false,2,5,8,-1,false,null,true,true,true,24),
	DEMOLST2("demolist2","demolist2","localhost",-1,MailProtocol.POP3, "Test List 2",StatusId.ACTIVE,
			false,4,false,2,5,8,-1,false,null,true,true,true,24),
	TESTTO("testto","testto","localhost",-1,MailProtocol.POP3, "Test TO Address",StatusId.ACTIVE,
			false,5,false,4,5,10,-1,false,null,true,true,true,24),
	TESTFROM("testfrom","testfrom","localhost",-1,MailProtocol.POP3, "Test FROM Address",StatusId.ACTIVE,
			false,5,false,4,5,10,-1,false,null,true,true,true,24),
	CUSTCARE("custcare","custcare","localhost",-1,MailProtocol.POP3, "Customer Care Address",StatusId.INACTIVE,
			false,5,false,4,5,10,-1,false,null,true,true,true,24),
	ALERT("alert","alert","localhost",-1,MailProtocol.POP3, "Customer Care Address",StatusId.INACTIVE,
			false,5,false,4,5,10,-1,false,null,true,true,true,24),

	postmaster("postmaster","postmaster","localhost",-1,MailProtocol.POP3, "James Server postmaster",StatusId.ACTIVE,
			false,4,false,2,5,8,-1,false,"legacytojava.com,emailsphere.com",true,true,true,24),
	webmaster("webmaster","webmaster","localhost",-1,MailProtocol.POP3, "Emailsphere demo webmaster",StatusId.ACTIVE,
			false,4,false,2,5,8,-1,false,"legacytojava.com,emailsphere.com",true,true,true,24),
	jwang("jwang","jwang","localhost",-1,MailProtocol.POP3, "local pop3 Server",StatusId.INACTIVE,
			true,4,false,2,5,8,-1,false,"legacytojava.com,jbatch.com",true,true,true,24),
	twang("twang","twang","localhost",-1,MailProtocol.POP3, "local pop3 Server",StatusId.INACTIVE,
			true,4,false,2,5,8,-1,false,"legacytojava.com,jbatch.com",true,true,true,24),
	jackwng("jackwng","jackwng","pop.gmail.com",995,MailProtocol.POP3, "GMail Secure Server",StatusId.INACTIVE,
			false,4,true,2,5,8,-1,false,"legacytojava.com,emailsphere.com",true,true,true,24),
	jwang_rr("jwang","jwang","pop-server.nc.rr.com",-1,MailProtocol.POP3, "Road Runner Server",StatusId.INACTIVE,
			false,4,false,2,5,6,-1,false,"legacytojava.com,emailsphere.com",true,true,true,24),
	df153("df153","df153","imap.aim.com",-1,MailProtocol.IMAP, "AIM Mail Server",StatusId.INACTIVE,
			false,10,false,2,5,10,-1,false,"legacytojava.com,emailsphere.com",true,true,true,24);

	private String userId;
	private String userPswd;
	private String hostName;
	private int port;
	private MailProtocol protocol;
	private String description;
	private StatusId status;
	private Boolean isInternalOnly;
	private int readPerPass;
	private boolean isUseSsl;
	private int numberOfThreads;
	private Integer maximumRetries;
	private Integer minimumWait;
	private int messageCount;
	private Boolean isToPlainText;
	private String toAddressDomain;
	private Boolean isCheckDuplicate;
	private Boolean isAlertDuplicate;
	private Boolean isLogDuplicate;
	private Integer purgeDupsAfter;
	private MailInboxEnum(String userId, String userPswd, String hostName,
			int port, MailProtocol protocol, String description,
			StatusId status, Boolean isInternalOnly, int readPerPass,
			boolean isUseSsl, int numberOfThreads, Integer maximumRetries,
			Integer minimumWait, int messageCount, Boolean isToPlainText,
			String toAddressDomain, Boolean isCheckDuplicate,
			Boolean isAlertDuplicate, Boolean isLogDuplicate,
			Integer purgeDupsAfter) {
		this.userId = userId;
		this.userPswd = userPswd;
		this.hostName = hostName;
		this.port = port;
		this.protocol = protocol;
		this.description = description;
		this.status = status;
		this.isInternalOnly = isInternalOnly;
		this.readPerPass = readPerPass;
		this.isUseSsl = isUseSsl;
		this.numberOfThreads = numberOfThreads;
		this.maximumRetries = maximumRetries;
		this.minimumWait = minimumWait;
		this.messageCount = messageCount;
		this.isToPlainText = isToPlainText;
		this.toAddressDomain = toAddressDomain;
		this.isCheckDuplicate = isCheckDuplicate;
		this.isAlertDuplicate = isAlertDuplicate;
		this.isLogDuplicate = isLogDuplicate;
		this.purgeDupsAfter = purgeDupsAfter;
	}
	public String getUserId() {
		return userId;
	}
	public String getUserPswd() {
		return userPswd;
	}
	public String getHostName() {
		return hostName;
	}
	public int getPort() {
		return port;
	}
	public MailProtocol getProtocol() {
		return protocol;
	}
	public String getDescription() {
		return description;
	}
	public StatusId getStatus() {
		return status;
	}
	public Boolean getIsInternalOnly() {
		return isInternalOnly;
	}
	public int getReadPerPass() {
		return readPerPass;
	}
	public boolean isUseSsl() {
		return isUseSsl;
	}
	public int getNumberOfThreads() {
		return numberOfThreads;
	}
	public Integer getMaximumRetries() {
		return maximumRetries;
	}
	public Integer getMinimumWait() {
		return minimumWait;
	}
	public int getMessageCount() {
		return messageCount;
	}
	public Boolean getIsToPlainText() {
		return isToPlainText;
	}
	public String getToAddressDomain() {
		return toAddressDomain;
	}
	public Boolean getIsCheckDuplicate() {
		return isCheckDuplicate;
	}
	public Boolean getIsAlertDuplicate() {
		return isAlertDuplicate;
	}
	public Boolean getIsLogDuplicate() {
		return isLogDuplicate;
	}
	public Integer getPurgeDupsAfter() {
		return purgeDupsAfter;
	}
}
