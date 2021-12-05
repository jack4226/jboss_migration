package jpa.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HtmlUtil extends LinkedHashMap<String, Boolean> {
	private static final long serialVersionUID = -5727226208745765863L;
	static final Logger logger = LogManager.getLogger(HtmlUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private final String fileName = "htmlTags.txt";
	private Pattern pattern = Pattern.compile("<([a-zA-Z]{1,10}|H[1-6])\\s?(\\.{3})?>");
	private static boolean debug = false;
	
	private HtmlUtil() {
		super();
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				Matcher m = pattern.matcher(line);
				if (m.find()) {
					if (debug && isDebugEnabled) {
						for (int i = 0; i <= m.groupCount(); i++) {
							logger.debug(i + " " + m.group(i));
						}
					}
					if (m.groupCount() == 2) {
						String tag = m.group(1);
						boolean hasAttributes = m.group(2) == null ? false : true;
						this.put(tag, hasAttributes);
					}
				}
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
	}
	
	final static Pattern matchedPairs = Pattern.compile(
			"<([a-zA-Z]{1,10}|H[1-6])(?:\\b.{1,1024}?)?>(.*?)</\\1>", Pattern.DOTALL);
	final static Pattern noAttrs = Pattern.compile("<([a-zA-Z]{1,10})[\\s]?[\\/]?>");
	final static Pattern hasAttrs = Pattern.compile("<([a-zA-Z]{1,10})(?:\\b.{1,1024}?)?[\\/]?>",
			Pattern.DOTALL);
	final static Pattern comment = Pattern.compile("<!--\\s+.*?-->", Pattern.DOTALL);
	final static Pattern doctype = Pattern.compile("<!DOCTYPE .*?>", Pattern.DOTALL
			| Pattern.CASE_INSENSITIVE);
	
	/**
	 * Check if the input text contains HTML tags 
	 * @param text
	 * @return true if contains any HTML tag
	 */
	public static boolean isHTML(String text) {
		if (StringUtils.isBlank(text)) {
			return false;
		}
		Matcher m1 = matchedPairs.matcher(text);
		int count = 0;
		while (m1.find() && count++ < 1) {
			String tag = m1.group(1).toUpperCase();
			if (getHtmlTagNames().containsKey(tag)) {
				if (debug && isDebugEnabled) {
					logger.debug("isHTML() - Found Tag With Closing tag: " + m1.group(0));
				}
				return true;
			}
			else {
				logger.warn("isHTML() - Found Non-HTML Tag: " + m1.group(0));
			}
		}
		Matcher m2 = noAttrs.matcher(text);
		count = 0;
		while (m2.find() && count++ < 2) { // allow one non-HTML tags
			String tag = m2.group(m2.groupCount()).toUpperCase();
			if (getHtmlTagNames().containsKey(tag)) {
				if (debug && isDebugEnabled) {
					logger.debug("isHTML() - Found Tag-Only Tag: " + m2.group(0));
				}
				return true;
			}
			else {
				logger.warn("isHTML() - Found Non-HTML Tag: " + m2.group(0));
			}
		}
		Matcher m3 = hasAttrs.matcher(text);
		count = 0;
		while (m3.find() && count++ < 2) {
			String tag = m3.group(m3.groupCount()).toUpperCase();
			if (getHtmlTagNames().containsKey(tag)) {
				if (getHtmlTagNames().get(tag)) {
					if (debug && isDebugEnabled) {
						logger.debug("isHTML() - Found Tag With Attributes: " + m3.group(0));
					}
					return true;
				}
				else {
					logger.warn("isHTML() - Found Tag-Only Tag with Attributes: " + m3.group(0));
				}
			}
			else {
				logger.warn("isHTML() - Found Non-HTML Tag: " + m3.group(0));
			}
		}
		Matcher m4 = doctype.matcher(text);
		return m4.find();
	}

	private static HtmlUtil tagNames = null;
	private static HtmlUtil getHtmlTagNames() {
		if (tagNames == null) {
			tagNames = new HtmlUtil();
		}
		return tagNames;
	}
	
	public static void main(String[] args) {
		System.out.println(isHTML("<p>Dear ${CustomerName}, <br></p><p>This is test template message body to ${SubscriberAddress}.</p><p>Time sent: ${CurrentDate}</p><p>Contact Email: ${ContactEmailAddress}</p><p>To unsubscribe from this mailing list, send an e-mail to: ${MailingListAddress}with \"unsubscribe\" (no quotation marks) in the subject of the message.</p>"));
		System.out.println(isHTML("plain no html text <nonhtml abc> tag<a abc>def"));
		System.out.println(isHTML("plain no html text <!DOCTYPE abcde> tag<aa abc>"));
		System.out.println(isHTML("<H1 LAST_MODIFIED=\"1194988178\">Bookmarks</H1>"));
	}
}
