package jpa.data.preload;

import jpa.constant.MailServerType;
import jpa.constant.StatusId;

public enum SmtpServerEnum {
	SUPPORT("localhost", -1, "smtpServer", "smtp server on localhost", false,
			"support", "support", false, StatusId.ACTIVE, MailServerType.SMTP,
			4, 10, 6, 10, 5, "error", 0),
	EXCHANGE("localhost", 25, "exchServer", "exch server on localhost", false,
			"uid", "pwd", false, StatusId.ACTIVE, MailServerType.EXCHANGE,
			1, 4, 1, 10, 15, "error", 0),
	DynMailRelay("outbound.mailhop.org", 465, "DyndnsMailRelay", "smtp server on dyndns", true,
			"jackwng", "jackwng", false, StatusId.INACTIVE, MailServerType.SMTP,
			1, 10, 5, 10, 5, "error", 0),
	GMailSmtp("smtp.gmail.com", -1, "gmailServer", "smtp server on gmail.com", true,
			"jackwng", "jackwng", false, StatusId.INACTIVE, MailServerType.SMTP,
			2, 10, 5, 10, 5, "error", 0);

	private String smtpHost;
	private int smtpPort;
	private String serverName;
	private String description;
	private boolean isUseSsl;
	private String userId;
	private String userPswd;
	private boolean isPersistence;
	private StatusId status;
	private MailServerType serverType;
	private int numberOfThreads;
	private Integer maximumRetries;
	private int retryFreq;
	private Integer minimumWait;
	private Integer alertAfter;
	private String alertLevel;
	private int messageCount;
	private SmtpServerEnum(String hostName,
			int port, String serverName, String description, boolean isUseSsl, String userId, String userPswd, 
			boolean isPersistence, StatusId status, MailServerType serverType, 
			int numberOfThreads, Integer maximumRetries, int retryFreq,
			Integer minimumWait, Integer alertAfter, String alertLevel, int messageCount) {
		this.smtpHost = hostName;
		this.smtpPort = port;
		this.serverName = serverName;
		this.description = description;
		this.isUseSsl = isUseSsl;
		this.userId = userId;
		this.userPswd = userPswd;
		this.isPersistence = isPersistence;
		this.status = status;
		this.serverType = serverType;
		this.numberOfThreads = numberOfThreads;
		this.maximumRetries = maximumRetries;
		this.retryFreq = retryFreq;
		this.minimumWait = minimumWait;
		this.alertAfter = alertAfter;
		this.alertLevel = alertLevel;
		this.messageCount = messageCount;
	}
	public String getSmtpHost() {
		return smtpHost;
	}
	public int getSmtpPort() {
		return smtpPort;
	}
	public String getServerName() {
		return serverName;
	}
	public String getDescription() {
		return description;
	}
	public boolean isUseSsl() {
		return isUseSsl;
	}
	public String getUserId() {
		return userId;
	}
	public String getUserPswd() {
		return userPswd;
	}
	public boolean isPersistence() {
		return isPersistence;
	}
	public StatusId getStatus() {
		return status;
	}
	public MailServerType getServerType() {
		return serverType;
	}
	public int getNumberOfThreads() {
		return numberOfThreads;
	}
	public Integer getMaximumRetries() {
		return maximumRetries;
	}
	public int getRetryFreq() {
		return retryFreq;
	}
	public Integer getMinimumWait() {
		return minimumWait;
	}
	public Integer getAlertAfter() {
		return alertAfter;
	}
	public String getAlertLevel() {
		return alertLevel;
	}
	public int getMessageCount() {
		return messageCount;
	}
}
