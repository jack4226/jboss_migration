package jpa.service.rule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class TestRuleRegex {
	static final Logger logger = Logger.getLogger(TestRuleRegex.class);
	static final String LF = System.getProperty("line.separator", "\n");
	public static void main(String[] args) {
		try {
			TestRuleRegex test = new TestRuleRegex();
			test.challenge_response();
			test.postmaster();
			test.outOfOffice();
			test.auto_reply();
			test.deliveryFailure();
			test.spamblocker();
			test.virusBlocker();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void challenge_response() {
		String bodyEarthLink = "I apologize for this automatic reply to your email." + LF + LF +
		"To control spam, I now allow incoming messages only from senders I have approved beforehand." + LF + LF +
		"If you could like to be added to my list of approved senders, please fill out the short request form (see link" + LF +
		"below). Once I approve you, I will receive your original message in my inbox. You do not need to resend your" + LF +
		"message. I apologize for this one-time inconvenience." + LF + LF +
		"Click the link below to fill out the request.";
		
		String bodyIpermitMail = 
		"Please click the link below so that I can receive your message." + LF +
		"Your message is being held by my anti-spam system, iPermitMail." + LF +
		"http://www.ipermitmail.com/ipm/Messa...k=371054102204";
		
		String bodyDevnull = "Hello." + LF +
		"Your mail to peter@gradwell.com requires your confirmation." + LF +
		"To confirm that your mail is legitimate, simply click on this URL:" + LF +
		"https://www.gradwell.com/u/?c=1f7c824d2f59695d5b4dd5f0" + LF +
		"Once you have confirmed this mail then you will never have" + LF +
		"to confirm any more mails to peter@gradwell.com." + LF +
		"You must confirm your mail within one week or else it will be" + LF +
		"deleted with no further notice to you." + LF +
		"Thank you.";
		
		String bodyVanquish = 
		"The message that you sent to me (Kenneth E. Slaughter) has not yet been delivered:" + LF +
		"From: tech@openbsd.org" + LF +
		"Subject: Re: Re: Message" + LF +
		"Date: Tue, 27 Apr 2004 13:36:38 -0500" + LF +
		"I am now using Vanquish to avoid spam.  This automated message" + LF +
		"is an optional feature of that service, which I have enabled." + LF +
		"Please accept this one-time request to confirm that the above" + LF +
		"message actually came from you.  Your confirmation will release" + LF +
		"the message and allow all future messages from your address." + LF +
		"Click here to confirm:" + LF +
		"http://confirm.vanquish.com/?U=b+Etxkx0Uvgx5q2Hp4CETg" + LF +
		"Vanquish respects my privacy and yours.  Your confirmation" + LF +
		"gets your mail delivered to me now and in the future.  It" + LF +
		"does not serve any marketing purpose.  Learn how privacy is" + LF +
		"assured: www.vanquish.com/privacy";
		
		String bodyQurb = "This is an automated message." + LF +
		"I apologize for the inconvenience, but I need your help in fighting" + LF +
		"spam. I'm using a program called Qurb which automatically maintains" + LF +
		"a list of approved senders for me. Messages from approved senders go" + LF +
		"directly to my Inbox. Messages from addresses that Qurb hasn't seen" + LF +
		"before are quarantined until the address of the sender can be" + LF +
		"confirmed.";
		
		String body = "(?:Your mail .* requires your confirmation" +
			"|Your message .* anti-spam system.* iPermitMail" +
			"|apologize .* automatic reply.* control spam.* approved senders" +
			"|Vanquish to avoid spam.* automated message" +
			"|automated message.* apologize .* approved senders)";
		Pattern bodyPattern = Pattern.compile(body, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher matcher = bodyPattern.matcher(bodyEarthLink);
		logger.info("challenge_response body matching? " + matcher.find());
		matcher.reset(bodyIpermitMail);
		logger.info(", " + matcher.find());
		matcher.reset(bodyDevnull);
		logger.info(", " + matcher.find());
		matcher.reset(bodyVanquish);
		logger.info(", " + matcher.find());
		matcher.reset(bodyQurb);
		logger.info(", " + matcher.find());
		
		String header = "(?:spamblocker-challenge|spamhippo|devnull-quarantine)\\@" +
				"|\\@(?:spamstomp\\.com|ipermitmail\\.com)";
		Pattern headerPattern = Pattern.compile(header, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		matcher = headerPattern.matcher("challenge_resp@ipermitmail.com");
		logger.info("challenge_response header matching? " + matcher.find());
		matcher.reset("spamhippo@cr.com");
		logger.info(", " + matcher.find());
		
		String subj = "^(?:Your email requires verification verify:" +
				"|Please Verify Your Email Address" +
				"|Unverified email to " +
				"|Your mail to .* requires confirmation)" +
				"|\\[Qurb .\\d+\\]$";
		Pattern subjPattern = Pattern.compile(subj, Pattern.CASE_INSENSITIVE);
		matcher = subjPattern.matcher("Your mail to peter@gradwell.com requires confirmation");
		logger.info("challenge_response subject matching? " + matcher.find());
		matcher.reset("CR found as [Qurb 100]");
		logger.info(", " + matcher.find());
		
		String from = "confirm-\\S+\\@spamguard\\.vanquish\\.com";
		Pattern fromPattern = Pattern.compile(from, Pattern.CASE_INSENSITIVE);
		matcher = fromPattern.matcher("Confirmation from Kenneth E. Slaughter " + "<confirm-tech=openbsd.org@spamguard.vanquish.com>");
		logger.info("challenge_response from matching? " + matcher.find());
	}
	
	void postmaster() {
		String postmasterFrom = "^(?:postmaster|mailmaster|mailadmin|administrator)\\S*\\@";
		Pattern postPattern = Pattern.compile(postmasterFrom, Pattern.CASE_INSENSITIVE);
		Matcher m = postPattern.matcher("postmaster@abc.com");
		logger.info("postmater matching: " + m.find());
		m.reset("administrator2@abc.com");
		logger.info(", " + m.find());
		String mailerdaemonFrom = "^(?:mailer-(?:daemon|deamon)|smtp.gateway|majordomo)\\S*\\@";
		Pattern daemPattern = Pattern.compile(mailerdaemonFrom, Pattern.CASE_INSENSITIVE);
		m = daemPattern.matcher("mailer-daemon@abc.com");
		logger.info(", " + m.find());
	}
	
	void outOfOffice() {
		String subj1 = "(?:out\\s+of\\s+.*office|\\(away from the office\\)$)";
		Pattern pattern = Pattern.compile(subj1, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher("Re: (away from the office)");
		logger.info("Out of office matching: " + m.find());
		m.reset("out of the office auto reply");
		logger.info(", " + m.find());
	}
	
	void auto_reply() {
		String from = "^(?:automated-response|autoresponder|autoresponse-\\S+)\\S*\\@";
		Pattern fromPattern = Pattern.compile(from);
		Matcher m = fromPattern.matcher("autoresponder@abc.com");
		logger.info("Auto-reply matching: " + m.find());
		m.reset("autoresponse-ooo@abc.com");
		logger.info(", " + m.find());
		
		String subj1 = 
			"(?:Exception.*(?:Out\\b.*of\\b.*Office|Autoreply:)|\\(Auto Response\\))" +
		 	"|^(?:Automatically Generated Response from|Auto-Respond E-?Mail from" +
		 	"|AutoResponse - Email Returned|automated response|Yahoo! Auto Response)";
		Pattern subjPattern = Pattern.compile(subj1, Pattern.CASE_INSENSITIVE);
		m = subjPattern.matcher("Re: Exception Autoreply: from SAXK");
		logger.info(", " + m.find());
		m.reset("Auto-Respond E-Mail from aaa");
		logger.info(", " + m.find());
		
		String body1 = "^This messages was created automatically by mail delivery software" +
			"|(?:\\bThis is an autoresponder. I'll never see your message\\b" +
			"|(?:\\bI(?:.m|\\s+am|\\s+will\\s+be|.ll\\s+be)\\s+(?:(?:out\\s+of|away\\s+from)\\s+the\\s+office|on\\s+vacation)\\s+(?:from|to|until|after)\\b)" +
			"|\\bI\\s+am\\s+currently\\s+out\\s+of\\s+the\\s+office\\b" +
			"|\\bI ?.m\\s+away\\s+until\\s+.{10,20}\\s+and\\s+am\\s+unable\\s+to\\s+read\\s+your\\s+message\\b)";
		Pattern bodyPattern = Pattern.compile(body1, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = bodyPattern.matcher("This is an autoresponder. I'll never see your message SAXK");
		logger.info("Auto-reply matching: " + m.find());
		m.reset("I.ll be away from the office until next monday.");
		logger.info(", " + m.find());
		m.reset("I.ll be on vacation until next monday.");
		logger.info(", " + m.find());
		m.reset("I.m away until next monday and am unable to read your message.");
		logger.info(", " + m.find());
		m.reset("I am currently out of the office.");
		logger.info(", " + m.find());
	}
	
	void deliveryFailure() {
		String subj1 = 
			"^(?:Returned mail:\\s(?:User unknown|Data format error)" +
			"|Undeliverable: |Undeliver(?:able|ed) Mail\\b|Undeliverable Message" +
			"|Returned mail.{0,5}(?:Error During Delivery|see transcript for details)" +
			"|e-?mail addressing error \\(|No valid recipient in )" +
			"|(?:User.*unknown|failed.*delivery|delivery.*(?:failed|failure|problem)" +
			"|Returned mail:.*(?:failed|failure|error)|\\(Failure\\)|failure notice" +
			"|not.*delivered)";
		Pattern subjPattern = Pattern.compile(subj1, Pattern.CASE_INSENSITIVE);
		Matcher m = subjPattern.matcher("Returned mail: Error During Delivery");
		logger.info("Delivery Failure matching: " + m.find());
		m.reset("Returned mail: User Unknown");
		logger.info(", " + m.find());
		m.reset("Mail could not be delivered");
		logger.info(", " + m.find());
		m.reset("Undelivered Mail Returned to Sender");
		logger.info(", " + m.find());
		m.reset("Email Addressing Error (aaa)");
		logger.info(", " + m.find());
		m.reset("Returned mail: Delivery Error...");
		logger.info(", " + m.find());
		
		String body2 = "(?:\\bYou(?:.ve| have) reached a non.?working address\\.\\s+Please check\\b" +
				"|eTrust Secure Content Manager SMTPMAIL could not deliver the e-?mail" +
				"|\\bPlease do not resend your original message\\." +
				"|\\s[45]\\.\\d{1,3}\\.\\d{1,3}\\s)";
		Pattern bodyPattern = Pattern.compile(body2, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = bodyPattern.matcher("You've reached a non-working address. Please check");
		logger.info("Delivery Failure matching: " + m.find());
		m.reset("Delivery Status: 5.4.1 Please check");
		logger.info(", " + m.find());
	}
	
	void spamblocker() {
		String spamFrom = "(?:^surfcontrol|.*You_Got_Spammed)\\S*\\@";
		Pattern fromPattern = Pattern.compile(spamFrom, Pattern.CASE_INSENSITIVE);
		Matcher m = fromPattern.matcher("surfcontrol_agent@abc.com");
		logger.info("Spam blocker matching: " + m.find());
		String returnPath = "^(?:pleaseforward|quotaagent)\\S*\\@";
		Pattern pathPattern = Pattern.compile(returnPath, Pattern.CASE_INSENSITIVE);
		m = pathPattern.matcher("pleaseforward@abc.com");
		logger.info(", " + m.find());
		
		String subj1 = "^Spam rapport \\/ Spam report \\S+ -\\s+\\(\\S+\\)$" +
				"|^GWAVA Sender Notification .(?:RBL block|Spam|Content filter).$" +
				"|^\\[MailServer Notification\\]" +
				"|^MailMarshal has detected possible spam in your message";
		Pattern subjPattern = Pattern.compile(subj1, Pattern.CASE_INSENSITIVE);
		m = subjPattern.matcher("[MailServer Notification]Attachment Blocking Notification");
		logger.info("Spam blocker matching: " + m.find());
		m.reset("GWAVA Sender Notification .RBL block2");
		logger.info(", " + m.find());
	}
	
	void virusBlocker() {
		String virusFrom = "(?:virus|scanner|devnull)\\S*\\@";
		Pattern fromPattern = Pattern.compile(virusFrom, Pattern.CASE_INSENSITIVE);
		Matcher m = fromPattern.matcher("virus_blocker@abc.com");
		logger.info("Virus blocker matching" + m.find());
		
		String body1 = "(?:a potentially executable attachment " +
				"|\\bhas stripped one or more attachments from the following message\\b" +
				"|message contains file attachments that are not permitted" +
				"|host \\S+ said: 5\\d\\d\\s+Error: Message content rejected" +
				"|TRANSACTION FAILED - Unrepairable Virus Detected. " +
				"|Mail.?Marshal Rule: Inbound Messages : Block Dangerous Attachments" +
				"|The mail message \\S+ \\S+ you sent to \\S+ contains the virus" +
				"|mailsweeper has found that a \\S+ \\S+ \\S+ \\S+ one or more virus" +
				"|Attachment.{0,40}was Deleted" +
				"|Virus.{1,40}was found" +
				"|\\bblocked by Mailsweeper\\b" +
				"|\\bvirus scanner deleted your message\\b" +
				"|\\bThe attachment was quarantined\\b" +
				"|\\bGROUP securiQ.Wall\\b)";
		Pattern body1Pattern = Pattern.compile(body1, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = body1Pattern.matcher("There host The attachment was quarantined rejected found in ");
		logger.info(", " + m.find());
		
		String body2 = "(?:Reason: Rejected by filter" +
				"|antivirus system report" +
				"|the antivirus module has" +
				"|the infected attachment" +
				"|illegal attachment" +
				"|Unrepairable Virus Detected" +
				"|Reporting-MTA: Norton Anti.?Virus Gateway" +
				"|\\bV I R U S\\b)" +
			"|^(?:Found virus \\S+ in file \\S+" +
				"|Incident Information:)";
		Pattern body2Pattern = Pattern.compile(body2, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		m = body2Pattern.matcher("Incident Information: virus ");
		logger.info(", " + m.find());
		
		String subj1 = "^(?:Disallowed attachment type found" +
				"|Norton Anti.?Virus failed to scan an attachment in a message you sent" +
				"|Norton Anti.?Virus detected and quarantined" +
				"|Warning - You sent a Virus Infected Email to " +
				"|Warning:\\s*E-?mail virus(es)? detected" +
				"|MailMarshal has detected a Virus in your message" +
				"|Banned or potentially offensive material" +
				"|Failed to clean virus\\b" +
				"|Virus Alert\\b" +
				"|Virus detected " +
				"|Virus to sender" +
				"|NAV detected a virus in a document " +
				"|InterScan MSS for SMTP has delivered a message" +
				"|InterScan NT Alert" +
				"|Antigen found\\b" +
				"|MMS Notification" +
				"|VIRUS IN YOUR MAIL " +
				"|Scan.?Mail Message: ?.{0,30} virus found " +
				"|McAfee GroupShield Alert)";
		Pattern subj1Pattern = Pattern.compile(subj1, Pattern.CASE_INSENSITIVE);
		m = subj1Pattern.matcher("McAfee GroupShield Alert message you sent");
		logger.info(", " + m.find());
		
		String subj2 = "^(?:Undeliverable mail, invalid characters in header" +
				"|Delivery (?:warning|error) report id=" +
				"|The MIME information you requested" +
				"|Content violation" +
				"|Report to Sender" +
				"|RAV Anti.?Virus scan results" +
				"|Symantec AVF detected " +
				"|Symantec E-Mail-Proxy " +
				"|Virus Found in message" +
				"|Inflex scan report \\[" +
				"|\\[Mail Delivery .{10,100} infected attachment.*removed)" +
			"|(?:(Re: ?)+Wicked screensaver\\b" +
				"|\\bmailsweeper\\b" +
				"|\\bFile type Forbidden\\b" +
				"|AntiVirus scan results" +
				"|Security.?Scan Anti.?Virus)" +
			"|^(?:Message Undeliverable: Possible Junk\\/Spam Mail Identified" +
				"|EMAIL REJECTED" +
				"|Virusmelding)$";
		Pattern subj2Pattern = Pattern.compile(subj2, Pattern.CASE_INSENSITIVE);
		m = subj2Pattern.matcher("EMAIL REJECTED");
		logger.info(", " + m.find());
	}
}
