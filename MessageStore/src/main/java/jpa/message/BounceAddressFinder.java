package jpa.message;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpa.util.EmailAddrUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public final class BounceAddressFinder {
	static final Logger logger = Logger.getLogger(BounceAddressFinder.class);
	static final boolean isDebugEnabled = false; //logger.isDebugEnabled();

	private final List<MyPattern> patternList = new ArrayList<MyPattern>();
	private static BounceAddressFinder addressFinder = null;
	
	private BounceAddressFinder() {
		if (patternList.isEmpty()) {
			loadPatterns();
		}
	}
	
	public static synchronized BounceAddressFinder getInstance() {
		if (addressFinder == null) {
			addressFinder = new BounceAddressFinder();
		}
		return addressFinder;
	}
	
	public String find(String body) {
		if (StringUtils.isNotBlank(body)) {
			for (MyPattern myPattern : patternList) {
				Matcher m = myPattern.getPattern().matcher(body);
				if (m.find()) {
					if (isDebugEnabled) {
						for (int i = 1; i <= m.groupCount(); i++) {
							logger.debug(myPattern.getPatternName() + ", group(" + i + ") - "
									+ m.group(i));
						}
					}
					return m.group(m.groupCount());
				}
			}
		}
		return null;
	}
	
	private static final class MyPattern {
		private final String patternName;
		private final String patternRegex;
		private final Pattern pattern;
		MyPattern(String name, String value) {
			this.patternName = name;
			this.patternRegex = value;
			pattern = Pattern.compile(patternRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		}
		
		public Pattern getPattern() {
			return pattern;
		}
		public String getPatternName() {
			return patternName;
		}
		//public String getPatternRegex() {
		//	return patternRegex;
		//}
	}
	
	private final void loadPatterns() {
		String bodyGmail = 
			"Delivery .{4,10} following recipient(?:s)? failed[\\.|\\s](?:permanently:)?\\s+" +
			"<?(" + EmailAddrUtil.getEmailRegex() + ")>?\\s+";
		patternList.add(new MyPattern("Gmail",bodyGmail));
		
		String bodyAol = 
			"\\-{3,6} The following address(?:es|\\(es\\))? had (?:permanent fatal errors|delivery problems) \\-{3,6}\\s+" +
			"<?(" + EmailAddrUtil.getEmailRegex() + ")>?(?:\\s|;)";
		patternList.add(new MyPattern("AOL",bodyAol));
		
		String bodyYahoo = 
			"This .{1,10} permanent error.\\s+I(?:'ve| have) given up\\. Sorry it did(?:n't| not) work out\\.\\s+" +
			"<?(" + EmailAddrUtil.getEmailRegex() + ")>?";
		patternList.add(new MyPattern("Yahoo",bodyYahoo));
		
		String bodyPostfix = 
			"message\\s.*could\\s+not\\s+be\\s+.{0,10}delivered\\s+to\\s.*(?:recipient(?:s)?|destination(?:s)?)" +
			".{80,180}\\sinclude\\s+this\\s+problem\\s+report.{60,120}" +
			"\\s+<(" + EmailAddrUtil.getEmailRegex() + ")>";
		patternList.add(new MyPattern("Postfix",bodyPostfix));
		
		String bodyFailed = 
			"Failed\\s+to\\s+deliver\\s+to\\s+\\'(" + EmailAddrUtil.getEmailRegex() + ")\\'" +
			".{1,20}\\smodule.{5,100}\\sreports";
		patternList.add(new MyPattern("Failed",bodyFailed));
		
		String bodyFirewall = 
			"Your\\s+message\\s+to:\\s+(" + EmailAddrUtil.getEmailRegex() + ")\\s+" +
			".{1,10}\\sblocked\\s+by\\s.{1,20}\\sSpam\\s+Firewall";
		patternList.add(new MyPattern("SpamFirewall",bodyFirewall));
		
		String bodyFailure = 
			"message\\s.{8,20}\\scould\\s+not\\s+be\\s+delivered\\s.{10,40}\\srecipients" +
			".{6,20}\\spermanent\\s+error.{10,20}\\saddress(?:\\(es\\))?\\s+failed:" +
			"\\s+(" + EmailAddrUtil.getEmailRegex() + ")\\s";
		patternList.add(new MyPattern("Failure",bodyFailure));
		
		String bodyUnable = 
			"Unable to deliver message to the following address(?:\\(es\\))?.{0,5}" +
			"\\s+<(" + EmailAddrUtil.getEmailRegex() + ")>";
		patternList.add(new MyPattern("Unable",bodyUnable));
		
		String bodyEtrust = 
			"\\scould not deliver the e(?:\\-)?mail below because\\s.{10,20}\\srecipient(?:s)?\\s.{1,10}\\srejected"+
			".{60,200}\\s(" + EmailAddrUtil.getEmailRegex() + ")";
		patternList.add(new MyPattern("eTrust",bodyEtrust));
		
		String bodyReport = 
			"\\scollection of report(?:s)? about email delivery\\s.+\\sFAILED:\\s.{1,1000}" +
			"Final Recipient:.{0,20};\\s*(" + EmailAddrUtil.getEmailRegex() + ")";
		patternList.add(new MyPattern("Report",bodyReport));
		
		String bodyNotReach = 
			"Your message.{1,400}did not reach the following recipient(?:\\(s\\))?:" +
			"\\s+(" + EmailAddrUtil.getEmailRegex() + ")";
		patternList.add(new MyPattern("NotReach",bodyNotReach));
		
		String bodyFailed2 = 
			"Could not deliver message to the following recipient(?:\\(s\\))?:" +
			"\\s+Failed Recipient:\\s+(" + EmailAddrUtil.getEmailRegex() + ")\\s";
		patternList.add(new MyPattern("Failed2",bodyFailed2));
		
		String bodyExceeds = 
			"User(?:'s)?\\s+mailbox\\s+exceeds\\s+allowed\\s+size:\\s+" +
			"(" + EmailAddrUtil.getEmailRegex() + ")\\s+";
		patternList.add(new MyPattern("Exceeds",bodyExceeds));
		
		String bodyDelayed = 
			"Message\\s+delivery\\s+to\\s+\\'(" + EmailAddrUtil.getEmailRegex() + ")\\'" +
			"\\s+delayed.{1,20}\\smodule.{5,100}\\sreports";
		patternList.add(new MyPattern("Delayed",bodyDelayed));
		
		String bodyInvalid = 
			"Invalid\\s+Address(?:es)?.{1,20}\\b(?:TO|addr)\\b.{1,20}\\s+<?(" + EmailAddrUtil.getEmailRegex() + ")>?\\s+";
		patternList.add(new MyPattern("Invalid",bodyInvalid));
	}
}
