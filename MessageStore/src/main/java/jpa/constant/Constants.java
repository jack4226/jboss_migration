package jpa.constant;

import org.apache.commons.lang3.StringUtils;

public class Constants {
	public final static String DEFAULT_USER_ID = "MsgMaint";
	public final static String DEFAULT_SENDER_ID = "System";

	public final static String VENDER_DOMAIN_NAME = "Emailsphere.com";
	public final static String VENDER_SUPPORT_EMAIL = "support@" + VENDER_DOMAIN_NAME;
	public final static String POWERED_BY_HTML_TAG = "<div style='color: blue;'>Powered by " +
			"<a style='color: darkblue;' href='http://www." + VENDER_DOMAIN_NAME + "' target='_blank'>"+ VENDER_DOMAIN_NAME + "</a></div>";
	public final static String POWERED_BY_TEXT = "Powered by " + VENDER_DOMAIN_NAME;
	public static final boolean EmbedPoweredByToFreeVersion = true;

	public final static String ADMIN_ROLE = "admin";
	public final static String USER_ROLE = "user";
	
	final static String DB_PRODNAME_MYSQL = "MySQL";
	final static String DB_PRODNAME_PSQL  = "PostgreSQL";
	final static String DB_PRODNAME_DERBY = "Derby";
	final static String DB_PRODNAME_APACHEDERBY = "Apache Derby";
	
	public final static String RULE_ACTION = "Rule Action"; 
	
	public final static boolean isDerbyDatabase(String db_name) {
		return (StringUtils.containsIgnoreCase(db_name, DB_PRODNAME_DERBY));
	}
	
	public final static boolean isMySQLDatabase(String db_name) {
		return (StringUtils.containsIgnoreCase(db_name, DB_PRODNAME_MYSQL));
	}
	
	public final static boolean isPgSQLDatabase(String db_name) {
		return (StringUtils.containsIgnoreCase(db_name, DB_PRODNAME_PSQL));
	}

	public final static int BOUNCE_SUSPEND_THRESHOLD = 5;
		// suspend email address after 5 times of consecutive bounces

	//
	// define mail types for rule engine
	//
	public static final String SMTP_MAIL = "smtpmail";
	public static final String WEB_MAIL = "webmail";
	
	// define message related constants 
	public static final String MESSAGE_TRUNCATED = "=== message truncated ===";
	public static final String MSG_DELIMITER_BEGIN = "--- ";
	public static final String MSG_DELIMITER_END = " wrote:";
	public static final String CRLF = "\r\n";
	public static final String DASHES_OF_33 = "---------------------------------"; 

	//
	// define VERP constants
	//
	public final static String VERP_BOUNCE_ADDR_XHEADER = "X-VERP_Bounce_Addr";
	public final static String VERP_BOUNCE_EMAILID_XHEADER = "X-VERP_Bounce_EmailId";
	public final static String VERP_REMOVE_ADDR_XHEADER = "X-VERP_Remove_Addr";
	public final static String VERP_REMOVE_LISTID_XHEADER = "X-VERP_Remove_ListId";
}
