package jpa.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jpa.constant.EmailIdToken;

public class EmailAddrUtil {
	static final Logger logger = LogManager.getLogger(EmailAddrUtil.class);

	final static String localPart = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*";
	final static String remotePart = "@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])+";
	final static String intraPart = "@[a-z0-9](?:[a-z0-9-]*[a-z0-9])+";

	final static Pattern remotePattern = Pattern.compile("^" + localPart + remotePart + "$", Pattern.CASE_INSENSITIVE);

	final static Pattern intraPattern = Pattern.compile("^" + localPart + intraPart + "$", Pattern.CASE_INSENSITIVE);

	final static Pattern localPattern = Pattern.compile("^" + localPart + "$", Pattern.CASE_INSENSITIVE);

	final static String bounceRegex = (new StringBuilder("\\s*\\W?((\\w+)\\-("))
			.append(EmailIdToken.XHDR_BEGIN).append("\\d+").append(EmailIdToken.XHDR_END)
			.append(")\\-(.+\\=.+)\\@(.+\\w))\\W?\\s*").toString();

	// for ex.: bounce-10.07410251.0-jsmith=test.com@localhost
	final static Pattern bouncePattern = Pattern.compile(bounceRegex);

	/**
	 * convert Address array to string, addresses are comma delimited. Display
	 * names are removed from returned addresses by default.
	 * 
	 * @param addrs -
	 *            Address array
	 * @return addresses in string format comma delimited, or null if input is
	 *         null
	 */
	public static String addressToString(Address[] addrs) {
		return addressToString(addrs, true);
	}

	/**
	 * convert Address array to string, addresses are comma delimited.
	 * 
	 * @param addrs -
	 *            Address array
	 * @param removeDisplayName -
	 *            remove display name from addresses if true
	 * @return addresses in string format comma delimited, or null if input is
	 *         null
	 */
	public static String addressToString(Address[] addrs, boolean removeDisplayName) {
		if (addrs == null || addrs.length == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		String str = addrs[0].toString();
		if (removeDisplayName) {
			str = removeDisplayName(str, false);
		}
		sb.append(str);
		for (int i = 1; i < addrs.length; i++) {
			if (removeDisplayName) {
				sb.append("," + removeDisplayName(addrs[i].toString(), false));
			}
			else {
				sb.append("," + addrs[i].toString());
			}
		}
		return sb.toString();
	}

	/**
	 * remove display name from an email address, and convert all characters 
	 * to lower case.
	 * 
	 * @param addr -
	 *            email address
	 * @return email address without display name, or null if input is null.
	 */
	public static String removeDisplayName(String addr) {
		return removeDisplayName(addr, true);
	}

	/**
	 * remove display name from an email address.
	 * 
	 * @param addr -
	 *            email address
	 * @param toLowerCase -
	 *            true to convert characters to lower case
	 * @return email address without display name, or null if input is null.
	 */
	public static String removeDisplayName(String addr, boolean toLowerCase) {
		if (StringUtils.isEmpty(addr)) {
			return addr;
		}
		int at_pos = addr.lastIndexOf("@");
		if (at_pos > 0) {
			int pos1 = addr.lastIndexOf("<", at_pos);
			int pos2 = addr.indexOf(">", at_pos + 1);
			if (pos1 >= 0 && pos2 > pos1) {
				addr = addr.substring(pos1 + 1, pos2);
			}
		}
		if (toLowerCase) {
			return addr.toLowerCase();
		}
		else {
			return addr;
		}
	}

	/**
	 * check if an email address has a display name.
	 * 
	 * @param addr -
	 *            email address
	 * @return true if it has a display name
	 */
	public static boolean hasDisplayName(String addr) {
		if (StringUtils.isBlank(addr)) {
			return false;
		}
		return addr.matches("^\\s*\\S+.{0,250}\\<.+\\>\\s*$");
	}

	/**
	 * return the display name of an email address.
	 * 
	 * @param addr -
	 *            email address
	 * @return - display name of the address, or null if the email does not have
	 *         a display name.
	 */
	public static String getDisplayName(String addr) {
		if (StringUtils.isBlank(addr)) {
			return null;
		}
		int at_pos = addr.lastIndexOf("@");
		if (at_pos > 0) {
			int pos1 = addr.lastIndexOf("<", at_pos);
			int pos2 = addr.indexOf(">", at_pos + 1);
			if (pos1 >= 0 && pos2 > pos1) {
				String dispName = addr.substring(0, pos1);
				return dispName.trim();
			}
		}
		return null;
	}

	/**
	 * Compare two email addresses. Email address could be enclosed by angle
	 * brackets and it should still be equal to the one without angle brackets.
	 * 
	 * Advanced feature: each parameter can contain multiple addresses, it is
	 * 	considered match if any address in one parameter matches one in another.
	 * 
	 * @param addr1 -
	 *            email address 1
	 * @param addr2 -
	 *            email address 2
	 * @return 0 if addr1 == addr2, -1 if addr1 < addr2, or 1 if addr1 > addr2.
	 */
	public static int compareEmailAddrs(String addr1, String addr2) {
		if (addr1 == null) {
			if (addr2 != null) {
				return -1;
			}
			else {
				return 0;
			}
		}
		else if (addr2 == null) {
			return 1;
		}
		String[] addr1s = addr1.split("[,;]");
		String[] addr2s = addr2.split("[,;]");
		int diffs = 0;
		for (String addr_1 : addr1s) {
			for (String addr_2 : addr2s) {
				if (StringUtils.isNotBlank(addr_1) && StringUtils.isNotBlank(addr_2)) {
					addr_1 = removeDisplayName(addr_1);
					addr_2 = removeDisplayName(addr_2);
					int same = addr_1.compareToIgnoreCase(addr_2);
					if (same == 0) {
						return 0;
					}
				}
			}
			diffs++;
		}
		return diffs;
	}

	/**
	 * returns the domain name of an email address.
	 * 
	 * @param addr -
	 *            email address
	 * @return domain name of the address, or null if it's local address
	 */
	public static String getEmailDomainName(String addr) {
		if (StringUtils.isBlank(addr)) {
			return null;
		}
		int pos;
		if ((pos = addr.lastIndexOf("@")) > 0) {
			String domain = addr.substring(pos + 1).trim();
			if (domain.endsWith(">")) {
				domain = domain.substring(0, domain.length() - 1);
			}
			return (domain.length() == 0 ? null : domain);
		}
		return null;
	}

	public static String getEmailUserName(String addr) {
		addr = addr == null ? "" : addr;
		addr = removeDisplayName(addr, false);
		String acctUserName = addr;
		int atSignPos = addr.indexOf("@");
		if (atSignPos >= 0) {
			return addr.substring(0, atSignPos);
		}
		return acctUserName;
	}

	public static String getEmailRegex() {
		return localPart + remotePart;
	}

	/**
	 * Check if the provided string is a valid email address. This conforms to
	 * the RFC822 and RFC1035 specifications. Both local part and remote part
	 * are required.
	 * 
	 * @param string
	 *            The string to be checked.
	 * @return True if string is an valid email address. False if not.
	 */
	public static boolean isInternetEmailAddress(String string) {
		if (string == null) {
			return false;
		}
		Matcher matcher = remotePattern.matcher(removeDisplayName(string, false));
		return matcher.matches();
	    //return string.matches(
	    //    "(?i)^[a-z0-9-~#&\\_]+(?:\\.[a-z0-9-~#&\\_]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,5}$");
	}

	/**
	 * Check if the provided string is a valid remote or Intranet email address.
	 * An Intranet email address could include only a sub-domain name such as
	 * "bounce" or "localhost" as its remote part.
	 * 
	 * @param string
	 *            The string to be checked.
	 * @return True if string is an valid email address. False if not.
	 */
	public static boolean isRemoteEmailAddress(String string) {
		if (string == null) {
			return false;
		}
		if (isInternetEmailAddress(string)) {
			return true;
		}
		Matcher matcher = intraPattern.matcher(removeDisplayName(string, false));
		return matcher.matches();
	    //return string.matches(
	    //    "(?i)^[a-z0-9-~#&\\_]+(?:\\.[a-z0-9-~#&\\_]+)*@(?:[a-z0-9-]+)$");
	}

	/**
	 * matches any remote or local email addresses like john or john@localhost
	 * or john@smith.com.
	 * 
	 * @param string
	 *            the email address to be checked
	 * @return true if it's a valid email address
	 */
	public static boolean isRemoteOrLocalEmailAddress(String string) {
		if (string == null) {
			return false;
		}
		if (isRemoteEmailAddress(string)) {
			return true;
		}
		Matcher matcher = localPattern.matcher(removeDisplayName(string, false));
		return matcher.matches();
	}

	public static boolean isValidEmailLocalPart(String string) {
		Matcher matcher = localPattern.matcher(string);
		return matcher.matches();
	}

	public static boolean hasValidEmailLocalPart(String string) {
		return isValidEmailLocalPart(getEmailUserName(string));
	}


	static String removeRegex = "\\s*\\W?((\\w+)\\-(\\w+)\\-(.+\\=.+)\\@(.+\\w))\\W?\\s*";
	// for ex.: remove-testlist-jsmith=test.com@localhost
	private static Pattern removePattern = Pattern.compile(removeRegex);
 	
	public static boolean isVERPAddress(String recipient) {
		if (StringUtils.isBlank(recipient)) {
			return false;
		}
		Matcher bounceMatcher = bouncePattern.matcher(recipient);
		Matcher removeMatcher = removePattern.matcher(recipient);
		return bounceMatcher.matches() || removeMatcher.matches();
	}

	public static String getDestAddrFromVERP(String verpAddr) {
		Matcher bounceMatcher = bouncePattern.matcher(verpAddr);
		if (bounceMatcher.matches()) {
			if (bounceMatcher.groupCount() >= 5) {
				String destAddr = bounceMatcher.group(2) + "@" + bounceMatcher.group(5);
				return destAddr;
			}
		}
		Matcher removeMatcher = removePattern.matcher(verpAddr);
		if (removeMatcher.matches()) {
			if (removeMatcher.groupCount() >= 5) {
				String destAddr = removeMatcher.group(2) + "@" + removeMatcher.group(5);
				return destAddr;
			}
		}
		return verpAddr;
	}

	public static String getOrigAddrFromVERP(String verpAddr) {
		Matcher bounceMatcher = bouncePattern.matcher(verpAddr);
		if (bounceMatcher.matches()) {
			if (bounceMatcher.groupCount() >= 4) {
				String origAddr = bounceMatcher.group(4).replace('=', '@');
				return origAddr;
			}
		}
		Matcher removeMatcher = removePattern.matcher(verpAddr);
		if (removeMatcher.matches()) {
			if (removeMatcher.groupCount() >= 4) {
				String origAddr = removeMatcher.group(4).replace('=', '@');
				return origAddr;
			}
		}
		return verpAddr;
	}

	public static String removeCRLFTabs(String str) {
		// remove possible CR/LF and tabs, that are inserted by some Email
		// servers, from the Email_ID found in bounced E-mails (MS exchange
		// server for one). MS exchange server inserted "\r\n\t" into the
		// Email_ID string, and it caused "check digit test" error.
		
		//return StringUtils.removeAll(str, "[\r\n\t]+"); // requires Apache commons lang3 3.5 or above
		return ("" + str).replaceAll("[\r\n\t]+", "");
	}

	
	public static void main(String[] args) {
		String addr = "\"ORCPT jwang@nc.rr.com\" <jwang@nc.rr.com>";
		addr = "DirectStarTV <fqusoogd.undlwfeteot@chaffingphotosensitive.com>";
		logger.info(addr+" --> "+EmailAddrUtil.removeDisplayName(addr));
		logger.info("StringUtils.removeAll() - " + EmailAddrUtil.removeCRLFTabs(addr));
		
		logger.info("EmailAddress: " + EmailAddrUtil.isRemoteEmailAddress("A!#$%&'*+/=?.^_`{|}~-BC@localhost.us"));
		logger.info("EmailAddress: " + EmailAddrUtil.isRemoteOrLocalEmailAddress("A!#$%&'*+/=?.^_`{|}~-BC"));
		logger.info(EmailAddrUtil.getOrigAddrFromVERP("bounce-10.07410251.0-jsmith=test.com@localhost"));
		logger.info(EmailAddrUtil.getOrigAddrFromVERP("remove-testlist-jsmith=test.com@localhost"));
		
		logger.info("Compare addrs (false): " + EmailAddrUtil.compareEmailAddrs("test@test.com", "test1<test1@test.com>"));
		logger.info("Compare addrs (true): " + EmailAddrUtil.compareEmailAddrs("test@test.com", "test <test@test.com>"));
		logger.info("Compare addrs (false): " + EmailAddrUtil.compareEmailAddrs("test@test.com;test3@test.com", "test1@test.com,test2@test.com"));
		logger.info("Compare addrs (true): " + EmailAddrUtil.compareEmailAddrs("test1@test.com;test3@test.com", "test2@test.com,test3@test.com"));
	}
}
