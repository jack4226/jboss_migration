package jpa.service.msgin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jpa.data.preload.RuleNameEnum;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Scan input string for RFC1893/RFC2821 mail status code
 */
public final class RfcCodeScan implements java.io.Serializable {
	private static final long serialVersionUID = 7560466702245969864L;
	static final Logger logger = Logger.getLogger(RfcCodeScan.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final int maxLenToScan = 8192*4; // scan up to 32k
	
	private static final HashMap<String, String> RFC1893_STATUS_CODE = new HashMap<String, String>();
	private static final HashMap<String, String> RFC1893_STATUS_DESC = new HashMap<String, String>();
	private static final HashMap<String, String> RFC2821_STATUS_CODE = new HashMap<String, String>();
	private static final HashMap<String, String> RFC2821_STATUS_DESC = new HashMap<String, String>();
	private static final HashMap<String, String> RFC2821_STATUS_MATCHINGTEXT = new HashMap<String, String>();
	
	private static RfcCodeScan rfcCodeScan = null;
	
	private static final String
		LETTER_S = "s",
		LETTER_H = "h",
		LETTER_F = "f",
		LETTER_L = "l",
		LETTER_B = "b",
		LETTER_K = "k",
		LETTER_U = "u";
		
	/**
	 * default constructor
	 */
	private RfcCodeScan() throws IOException {
		loadRfc1893StatusCode();
		loadRfc2821StatusCode();
	}
	
	public static RfcCodeScan getInstance() throws IOException {
		if (rfcCodeScan==null) {
			rfcCodeScan = new RfcCodeScan();
		}
		return rfcCodeScan;
	}
	
	/**
	 * scan message subject for a message id
	 * 
	 * @param str -
	 *            message subject
	 * @return message id, default if not found
	 */
	String examineSubject(String subj) {
		String RuleName = examineBody(subj);
		if (RuleName != null) {
			return RuleName;
		}
		else {
			return RuleNameEnum.GENERIC.getValue();
		}
	}
	
	/**
	 * returns a message id, null if not found
	 * 
	 * @param str -
	 *            message body
	 * @return message id, null if not found
	 */
	public String examineBody(String body) {
		String RuleName = examineBody(body, 1);
		if (RuleName == null) {
			RuleName = examineBody(body, 2);
		}
		return RuleName;
	}
	
	private static Pattern pattern1 = Pattern.compile("\\s([245]\\.\\d{1,3}\\.\\d{1,3})\\s",
			Pattern.DOTALL);
	private static Pattern pattern2 = Pattern.compile("\\s([245]\\d\\d)\\s", Pattern.DOTALL);
	
	/**
	 * <ul>
	 * <li> first pass: check if it contains a RFC1893 code. RFC1893 codes are
	 * from 5 to 9 bytes long (x.x.x -> x.xxx.xxx) and start with 2.x.x or 4.x.x
	 * or 5.x.x
	 * <li> second pass: check if it contains a 3 digit numeric number: 2xx, 4xx
	 * or 5xx.
	 * </ul>
	 * 
	 * @param str -
	 *            message body
	 * @param pass -
	 *            1) first pass: look for RFC1893 token (x.x.x).
	 *            2) second pass: look for RFC2821 token (xxx), must also match reply text.
	 * @return rule name or null if no RFC code is found.
	 */
	private String examineBody(String body, int pass) {
		if (isDebugEnabled)
			logger.debug("Entering the examineBody method, pass " + pass);
		if (StringUtils.isBlank(body)) { // sanity check
			return null;
		}
		RuleNameEnum RuleName = null;
		if (pass == 1) {
			Matcher m = pattern1.matcher(StringUtils.left(body, maxLenToScan));
			if (m.find()) { // only one time
				String token = m.group(m.groupCount());
				logger.info("examineBody(): RFC1893 token found: " + token);
				if ((RuleName = searchRfc1893CodeTable(token)) != null) {
					return RuleName.getValue();
				}
				else if (token.startsWith("5.")) { // 5.x.x
					return RuleNameEnum.HARD_BOUNCE.getValue();
				}
				else if (token.startsWith("4.")) { // 4.x.x
					return RuleNameEnum.SOFT_BOUNCE.getValue();
				}
				else if (token.startsWith("2.")) { // 2.x.x
					// 2.x.x = OK message returned, MDN receipt.
					return RuleNameEnum.MDN_RECEIPT.getValue();
				}
			}
		}
		else if (pass == 2) {
			Matcher m = pattern2.matcher(StringUtils.left(body, maxLenToScan));
			int end = 0;
			int count = 0;
			while (m.find(end) && count++ < 2) { // repeat two times
				String token = m.group(m.groupCount());
				end = m.end(m.groupCount());
				logger.info("examineBody(): Numeric token found: " + token);
				if ((RuleName = searchRfc2821CodeTable(token)) != null) {
					//return RuleName;
					return matchRfcText(RuleName, token, body, end);
				}
				if (token.startsWith("5")) {
					// 5xx = permanent failure, re-send will fail
					String r = matchRfcText(RuleNameEnum.HARD_BOUNCE, token, body, end);
					if (r != null) return r;
					// else look for the second token
				}
				else if(token.equals("422")) {
					// 422 = mailbox full, re-send may be successful
					return matchRfcText(RuleNameEnum.MAILBOX_FULL, token, body, end);
				}
				else if (token.startsWith("4")) {
					// 4xx = persistent transient failure, re-send may be successful
					String r = matchRfcText(RuleNameEnum.SOFT_BOUNCE, token, body, end);
					if (r != null) return r;
					// else look for the second token
				}
				else if(token.startsWith("2")) {
					// 2xx = OK message returned.
				}
			}
		}
		return null;
	}

	/**
	 * For RFC 2821, to further match reply text to prevent false positives.
	 * 
	 * @param ruleName -
	 *            Rule Name
	 * @param code -
	 *            RFC2821 code
	 * @param tokens -
	 *            message text stored in an array, each element holds a word.
	 * @param idx -
	 *            where the RFC2821 code located in the array
	 * @return ruleName, or null if failed to match reply text.
	 */
	private String matchRfcText(RuleNameEnum ruleName, String code, String body, int idx) {
		String matchingText = RFC2821_STATUS_MATCHINGTEXT.get(code);
		if (matchingText == null) {
			if (code.startsWith("4")) {
				matchingText = RFC2821_STATUS_MATCHINGTEXT.get("4xx");
			}
			else if (code.startsWith("5")) {
				matchingText = RFC2821_STATUS_MATCHINGTEXT.get("5xx");
			}
			if (matchingText == null) { // just for safety
				return null;
			}
		}
		// RFC reply text - the first 120 characters after the RFC code 
		String rfcText = StringUtils.left(body.substring(idx), 120);
		try {
			Pattern p = Pattern.compile(matchingText, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(rfcText);
			if (m.find()) {
				logger.info("Match Succeeded: [" + rfcText + "] matched [" + matchingText + "]");
				return ruleName.getValue();
			}
			else {
				logger.info("Match Failed: [" + rfcText + "] did not match [" + matchingText + "]");
			}
		}
		catch (PatternSyntaxException e) {
			logger.error("PatternSyntaxException caught", e);
		}
		return null;
	}
	
	/**
	 * search smtp code table by RFC1893 token.
	 * 
	 * @param token
	 *            DSN status token, for example: 5.0.0
	 * @return message id related to the token
	 */
	private RuleNameEnum searchRfc1893CodeTable(String token) {
		// search rfc1893 hash table - x.x.x
		RuleNameEnum RuleName = searchRfcCodeTable(token, RFC1893_STATUS_CODE);
		// search rfc1893 hash table - .x.x
		if (RuleName == null) {
			RuleName = searchRfcCodeTable(token.substring(1), RFC1893_STATUS_CODE);
		}
		return RuleName;
	}
	
	/**
	 * search smtp code table by RFC token.
	 * 
	 * @param token -
	 *            DSN status token, for example: 5.0.0, or 500 depending on the
	 *            map used
	 * @param map -
	 *            either RFC1893_STATUS_CODE or RFC2821_STATUS_CODE
	 * @return message id of the token
	 */
	private RuleNameEnum searchRfcCodeTable(String token, HashMap<String, String> map) {
		String type = map.get(token);

		if (type != null) { // found RFC status code
			logger.info("searchRfcCodeTable(): A match is found for type: " + type);
			if (type.equals(LETTER_H)) {
				return RuleNameEnum.HARD_BOUNCE;
			}
			else if (type.equals(LETTER_S)) {
				return RuleNameEnum.SOFT_BOUNCE;
			}
			else if (type.equals(LETTER_F)) {
				return RuleNameEnum.MAILBOX_FULL;
			}
			else if (type.equals(LETTER_L)) {
				return RuleNameEnum.SIZE_TOO_LARGE;
			}
			else if (type.equals(LETTER_B)) {
				return RuleNameEnum.MAIL_BLOCK;
			}
			else if (type.equals(LETTER_K)) {
				return RuleNameEnum.MDN_RECEIPT;
			}
			else if (type.equals(LETTER_U)) {
				if (token.startsWith("4")) {
					return RuleNameEnum.SOFT_BOUNCE;
				}
				else if (token.startsWith("5")) {
					return RuleNameEnum.HARD_BOUNCE;
				}
			}
		}
		return null;
	}
	
	/**
	 * search smtp code table by RFC token.
	 * 
	 * @param token -
	 *            RFC2821 token, for example: 500
	 * @return message id of the token
	 */
	private RuleNameEnum searchRfc2821CodeTable(String token) {
		return searchRfcCodeTable(token, RFC2821_STATUS_CODE);
	}
	
	/**
	 * load the rfc1893 code table, from Rfc1893Codes.conf file, into memory
	 * 
	 * @throws IOException
	 *             if error occurred
	 */
	private void loadRfc1893StatusCode() throws IOException {
		ClassLoader loader = this.getClass().getClassLoader();
		try {
			// read in RFC1893 status code file and store it in two property objects
			InputStream is = loader.getResourceAsStream("Rfc1893Codes.conf");
			BufferedReader fr = new BufferedReader(new InputStreamReader(is));
			String inStr=null, code=null;
			while ((inStr = fr.readLine()) != null) {
				if (!inStr.startsWith("#")) {
					if (isDebugEnabled)
						logger.debug("loadRfc1893StatusCode(): " + inStr);
					StringTokenizer st = new StringTokenizer(inStr, "^\r\n");
					if (st.countTokens() == 3) {
						code = st.nextToken();
						RFC1893_STATUS_CODE.put(code, st.nextToken());
						RFC1893_STATUS_DESC.put(code, st.nextToken());
					}
					else if (st.countTokens() == 0) {
						// ignore
					}
					else {
						logger.fatal("loadStatusCode: Wrong record format: " + inStr);
					}
				}
			}
			fr.close();
		}
		catch (FileNotFoundException ex) {
			logger.fatal("file statcode.conf does not exist", ex);
			throw ex;
		}
		catch (IOException ex) {
			logger.fatal("IOException caught during loading statcode.conf", ex);
			throw ex;
		}
	}

	/**
	 * load the rfc2821 code table, from Rfc2821Codes.conf file, into memory
	 * 
	 * @throws IOException
	 *             if error occurred
	 */
	private void loadRfc2821StatusCode() throws IOException {
		ClassLoader loader = this.getClass().getClassLoader();
		try {
			// read in RFC2821 status code file and store it in two property objects
			InputStream is = loader.getResourceAsStream("Rfc2821Codes.conf");
			BufferedReader fr = new BufferedReader(new InputStreamReader(is));
			String inStr=null, code=null;
			while ((inStr = fr.readLine()) != null) {
				if (!inStr.startsWith("#")) {
					if (isDebugEnabled)
						logger.debug("loadRfc2821StatusCode(): " + inStr);
					StringTokenizer st = new StringTokenizer(inStr, "^\r\n");
					if (st.countTokens() == 3) {
						code = st.nextToken(); // 1st token = RFC code
						RFC2821_STATUS_CODE.put(code, st.nextToken()); // 2nd token = type
						String desc = st.nextToken(); // 3rd token = description
						RFC2821_STATUS_DESC.put(code, desc);
						// extract regular expression to be further matched
						String matchingRegex = getMatchingRegex(desc);
						if (matchingRegex != null) {
							RFC2821_STATUS_MATCHINGTEXT.put(code, matchingRegex);
						}
					}
					else if (st.countTokens() == 0) {
						// ignore
					}
					else {
						logger.fatal("loadStatusCode: Wrong record format: " + inStr);
					}
				}
			}
			fr.close();
		}
		catch (FileNotFoundException ex) {
			logger.fatal("file statcode.conf does not exist", ex);
			throw ex;
		}
		catch (IOException ex) {
			logger.fatal("IOException caught during loading statcode.conf", ex);
			throw ex;
		}
	}
	
	private String getMatchingRegex(String desc) throws IOException {
		int left = desc.indexOf("{");
		if (left < 0) {
			return null;
		}
		Stack<Integer> stack = new Stack<Integer>();
		stack.push(Integer.valueOf(left));
		int nextPos = left;
		while (stack.size() > 0) {
			int leftPos = desc.indexOf("{", nextPos + 1);
			int rightPos = desc.indexOf("}", nextPos + 1);
			if (leftPos > rightPos) {
				if (rightPos > 0) {
					stack.pop();
					nextPos = rightPos;
				}
			}
			else if (leftPos < rightPos) {
				if (leftPos > 0) {
					nextPos = leftPos;
					stack.push(Integer.valueOf(leftPos));
				}
				else if (rightPos > 0) {
					stack.pop();
					nextPos = rightPos;
				}
			}
			else {
				break;
			}
		}
		if (nextPos > left) {
			if (stack.size() == 0) {
				return desc.substring(left + 1, nextPos);
			}
			else {
				logger.error("getMatchingRegex() - missing close curly brace: " + desc);
				throw new IOException("Missing close curly brace: " + desc);
			}
		}
		return null;
	}

	public static void main(String[] args) {
		try {
			RfcCodeScan scan = RfcCodeScan.getInstance();
			String ruleName = scan.examineBody("aaaaab\n5.0.0\nefg ");
			logger.info("RuleName: " + ruleName);
			ruleName = scan.examineBody("aaa 201 aab\n422\naccount is full ");
			logger.info("RuleName: " + ruleName);
			ruleName = scan.examineBody("aaaaab\n400\ntemporary failure ");
			logger.info("RuleName: " + ruleName);
			logger.info(scan.getMatchingRegex("{(?:mailbox|account).{0,180}(?:storage|full|limit|quota)}"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

