package jpa.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;

/**
 * Use HtmlParser package to convert html text to plain text
 */
public final class HtmlConverter implements java.io.Serializable {
	private static final long serialVersionUID = 3467371895574167831L;
	static final Logger logger = LogManager.getLogger(HtmlConverter.class);
	
	boolean showUrl = true;
	private static final  HtmlConverter INSTANCE = new HtmlConverter();
	
	/**
	 * private constructor, needed to make the class a singleton.
	 */
	private HtmlConverter() {
		// empty constructor
	}

	/**
	 * returns a HtmlConverter instance
	 * 
	 * @return HtmlConverter object
	 */
	public static HtmlConverter getInstance() {
		return INSTANCE;
	}

	// readResolve method to preserve singleton property (from deserialization)
	private Object readResolve() throws java.io.ObjectStreamException {
		// return the one true HtmlConverter and let the garbage collector
		// take care of the HtmlConverter impersonator.
		return INSTANCE;
	}
	
	/**
	 * Convert html text to plain text
	 * 
	 * @param html_text -
	 *            html text to be converted
	 * @return converted plain text
	 * @throws ParserException
	 */
	public String convertToText(String htmlText) throws ParserException {
		StringBean sb = new StringBean();
		Parser parser = Parser.createParser(htmlText, null); // use default charset
		sb.setCollapse(false); // true if sequences of whitespace are to be replaced with a single space.
		sb.setLinks(showUrl); // default is false, true to show URL links
		sb.setReplaceNonBreakingSpaces(true); // default is true
		parser.visitAllNodesWith(sb);
		if (logger.isDebugEnabled()) {
			logger.debug("Html to Plain text conversion completed.");
		}
		return sb.getStrings();
	}

	public void setShowURL(boolean showUrl) {
		this.showUrl = showUrl;
	}

	public boolean getShowURL() {
		return showUrl;
	}

	public static void main(String[] args) {
		try {
			// the HTML to convert
			byte[] bytes = FileUtil.loadFromFile("samples/","HtmlSample.html");
			HtmlConverter parser = HtmlConverter.getInstance();
			String text = parser.convertToText(new String(bytes));
			System.out.println(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}