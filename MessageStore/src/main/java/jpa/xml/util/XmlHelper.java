package jpa.xml.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlHelper {
	static Logger logger = Logger.getLogger(XmlHelper.class);
	static boolean isDebugEnabled = logger.isDebugEnabled();

	/**
	 * Load xml file into Document without schema validation
	 */
	public static Document loadDocumentFromFile(File doc_file)
			throws ParserConfigurationException, SAXException, IOException {
		if (doc_file == null || doc_file.getPath() == null) {
			throw new IllegalArgumentException("Invalid input file.");
		}
		if (isDebugEnabled) {
			logger.debug("File Absolute path : " + doc_file.getAbsolutePath());
			logger.debug("File Canonical path: " + doc_file.getCanonicalPath());
		}
		logger.info("File path: " + doc_file.getPath());
		return loadDocumentFromFilePath(doc_file.getPath());
	}
	
	/**
	 * Load xml file into Document without schema validation
	 */
	public static Document loadDocumentFromFilePath(String doc_path)
			throws ParserConfigurationException, SAXException, IOException {
		InputSource source = loadInputSourceFromFilePath(doc_path);
		// use jaxp to initialize a DOM parser
		Document document = getDocumentBuilder().parse(source);
		return document;
	}

	public static InputSource loadInputSourceFromFilePath(String doc_path)
			throws IOException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream doc_is = loader.getResourceAsStream(doc_path);
		if (doc_is == null) {
			throw new IllegalArgumentException("Could not find xml file: " + doc_path);
		}
		InputSource source = new InputSource(doc_is);
		return source;
	}


	/**
	 * Load xml string document into Document without schema validation
	 */
	public static Document loadDocumentFromString(String xmlString)
			throws ParserConfigurationException, SAXException, IOException {
		if (xmlString == null) {
			throw new IllegalArgumentException("XML document is not present");
		}
		// use jaxp to initialize a DOM parser
		DocumentBuilder builder = getDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xmlString)));
		return document;
	}

	private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder;
	}

	/**
	 * Normalize xml using Normalizer with default Form.NFKD.
	 * @param xmlStr
	 * @return normalized xml
	 * @throws UnsupportedEncodingException
	 */
	public static String normalizeXml(String xmlStr) {
		java.text.Normalizer.Form form = java.text.Normalizer.Form.NFKD;
		return normalizeXml(xmlStr, form);
	}

	public static String normalizeXml(String xmlStr, java.text.Normalizer.Form form) {
		int pos = 0;
		if ((pos=xmlStr.indexOf("<"))>0) {
			/*
			 * XXX strip off garbage characters from the beginning of the xml
			 * received from Rave to avoid SAXParserException: Content is not
			 * allowed in prolog.
			 */
			xmlStr = xmlStr.substring(pos);
		}
		/*
		 * reconstruct the string using "UTF-8" charset will address this error:
		 * "Invalid byte 1 of 1-byte UTF-8 sequence" 
		 */
//		try {
//			xmlStr = new String(xmlStr.getBytes("UTF-8"));
//		}
//		catch (UnsupportedEncodingException e) { // should never happen
//			logger.error("UnsupportedEncodingException caught", e);
//		}
		String normStr = Normalizer.normalize(xmlStr, form);
		return normStr;
	}

    /**
	 * Pretty print XML document using Document Builder and XSLT Transformer.
	 * 
	 * @param xmldoc
	 *            - document as string
	 * @return - pretty document as string
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
     * @throws TransformerException 
	 */
    public static String printXml(String xmlString) throws IOException, SAXException,
			ParserConfigurationException, TransformerException {
		Document doc = loadDocumentFromString(xmlString);
		return XsltTransformer.printXml(doc);
    }

	/**
	 * This method ensures that the output String has only valid XML unicode
	 * characters as specified by the XML 1.0 standard. For reference, please
	 * see <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
	 * standard</a>. This method will return an empty String if the input is
	 * null or empty.
	 * 
	 * @param in
	 *            The String whose non-valid characters we want to remove.
	 * @return The in String, stripped of non-valid characters.
	 */
	public static String stripNonValidXMLCharacters(String in) {
		if (in == null) {
			return ""; // vacancy test.
		}
		StringBuffer out = new StringBuffer(); // Used to hold the output.
		char current; // Used to reference the current character.
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught
									// here; it should not happen.
			if ((current == 0x9) || (current == 0xA) || (current == 0xD)
					|| ((current >= 0x20) && (current <= 0xD7FF))
					|| ((current >= 0xE000) && (current <= 0xFFFD))
					|| ((current >= 0x10000) && (current <= 0x10FFFF)))
				out.append(current);
		}
		return out.toString();
	}

}
