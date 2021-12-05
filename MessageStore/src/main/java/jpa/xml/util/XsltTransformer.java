package jpa.xml.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XsltTransformer {
	static Logger logger = LogManager.getLogger(XsltTransformer.class);
	static boolean isDebugEnabled = logger.isDebugEnabled();

    private final String xslt_path;
    private final String xslt_file;
    
    private Transformer transformer = null;
    private Templates translet = null;
    
    public XsltTransformer(String xslt_path, String xslt_file) {
    	this.xslt_path = xslt_path;
    	this.xslt_file = xslt_file;
    }
    
	/**
	 * Transform a DOM document to another document using xsltc. This method is
	 * synchronized and it reuses the compiled transformer. This method
	 * illustrates how to compile a translet for a single transformation.
	 * 
	 * @param doc
	 *            - the document to be transformed
	 * @return a Node - the transformed document
	 * @throws TransformerException
	 */
    public synchronized Node transform(Node doc) throws TransformerException {
    	DOMSource ds = new DOMSource(doc);
    	DOMResult dr = new DOMResult();
    	try {
	    	getTransformerInstance().transform(ds, dr);
	    	logger.info("The xml document is transformed to DOMResult using xalan");
	    	return dr.getNode();
    	}
		catch (TransformerException e) {
			// Error generated by the parser
			logger.error("TransformerException caught", e);
			// Use the contained exception, if any
			if (e.getException() != null) {
				Throwable x = e.getException();
				logger.error("cause", x);
			}
			if (e.getLocator() != null) {
				SourceLocator loc = e.getLocator();
				logger.error("Location - Line: " + loc.getLineNumber() + ", Col: " + loc.getColumnNumber());
				logger.error("PublicId: " + loc.getPublicId());
				logger.error("SystemId: " + loc.getSystemId());
			}
			throw e;
		}
    }

	/**
	 * Transform a DOM document to a string using xsltc. This method is not
	 * synchronized but is thread safe. This method illustrates how to compile a
	 * translet for multiple concurrent transformations.
	 * 
	 * @param doc
	 *            - the document to be transformed
	 * @return string - the transformed document as string
	 * @throws TransformerException
	 */
	public String transformToStream(Node doc) throws TransformerException {
		DOMSource ds = new DOMSource(doc);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Transformer t = getTranslet().newTransformer();
			t.transform(ds, new StreamResult(baos));
			logger.info("The xml document is transformed to StreamResult using xalan");
			String output = new String(baos.toByteArray());
			try {
				baos.close();
			}
			catch (IOException e) {
				logger.error("IOException caught", e);
				throw new RuntimeException("Failed to close ByteArrayOutputStream: " + e.getMessage());
			}
			return output;
		}
		catch (TransformerException e) {
			// Error generated by the parser
			logger.error("TransformerException caught", e);
			// Use the contained exception, if any
			if (e.getException() != null) {
				Throwable x = e.getException();
				logger.error("cause", x);
			}
			if (e.getLocator() != null) {
				SourceLocator loc = e.getLocator();
				logger.error("Location - Line: " + loc.getLineNumber() + ", Col: " + loc.getColumnNumber());
				logger.error("PublicId: " + loc.getPublicId());
				logger.error("SystemId: " + loc.getSystemId());
			}
			throw e;
		}
	}

	private Transformer getTransformerInstance() throws TransformerConfigurationException {
		if (transformer == null) {
			long beginTime = System.currentTimeMillis();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			InputStream xslt_is = loader.getResourceAsStream(xslt_path+xslt_file);
			if (xslt_is == null) {
				throw new RuntimeException("Could not find xslt file: " + xslt_path+xslt_file);
			}
			TransformerFactory tf = getTransformerFactory();
			StreamSource xslt_source = new StreamSource(xslt_is);
			transformer = tf.newTransformer(xslt_source);
			logger.info("getTransformaer() - time: " + (System.currentTimeMillis() - beginTime) + " ms");
		}
		return transformer;
	}

	private Templates getTranslet() throws TransformerConfigurationException {
		if (translet == null) {
			long beginTime = System.currentTimeMillis();
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			InputStream xslt_is = loader.getResourceAsStream(xslt_path+xslt_file);
			if (xslt_is == null) {
				throw new RuntimeException("Could not find xslt file: " + xslt_path+xslt_file);
			}
			TransformerFactory tf = getTransformerFactory();
			StreamSource xslt_source = new StreamSource(xslt_is);
			translet = tf.newTemplates(xslt_source);
			logger.info("getTranslet() - time: " + (System.currentTimeMillis() - beginTime) + " ms");
		}
		return translet;
	}

	/**
	 * Pretty print XML document using Transformer Factory.
	 * 
	 * @param doc
	 *            - DOM document
	 * @return string - pretty XML document as string
	 * @throws TransformerException
	 */
	public static String printXml(Document doc) throws TransformerException {
		DOMSource ds = new DOMSource(doc);
		return printXml(ds);
	}

	/**
	 * Pretty print XML document using Transformer Factory.
	 * 
	 * @param node
	 *            - DOM node
	 * @return string - pretty XML document as string
	 * @throws TransformerException
	 */
	public static String printXml(Node node) throws TransformerException {
		DOMSource ds = new DOMSource(node);
		return printXml(ds);
	}

	private static String printXml(DOMSource ds) throws TransformerException {
		TransformerFactory tf = getTransformerFactory();
		tf.setAttribute("indent-number", Integer.valueOf(2)); // this one works
		Transformer t = tf.newTransformer();
		t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		//t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); // not working
		StringWriter writer = new StringWriter();
		StreamResult xmlOutput = new StreamResult(writer);
		t.transform(ds, xmlOutput);
		String output = xmlOutput.getWriter().toString();
		try {
			writer.close();
		}
		catch (IOException e) {
			logger.error("IOException caught", e);
			throw new RuntimeException("Failed to close StringWriter: " + e.getMessage());
		}
		return output;
	}

	public static TransformerFactory getTransformerFactory() {
		String origValue = System.getProperty("javax.xml.transform.TransformerFactory");
		try {
			Class.forName("org.apache.xalan.xsltc.trax.TransformerFactoryImpl");
			// use xalan 2 xsltc if it is found in classpath
			System.setProperty(
					"javax.xml.transform.TransformerFactory",
					"org.apache.xalan.xsltc.trax.TransformerFactoryImpl");
		}
		catch (ClassNotFoundException e) {
			logger.warn("Apache xalan not found, use system's default TransformerFactoryImpl.");
		}
		
		TransformerFactory tf = TransformerFactory.newInstance();
		// restore to WAS default
		if (origValue != null) {
			System.setProperty("javax.xml.transform.TransformerFactory", origValue);
		}
		else { // only supported by jdk 1.5 and above
			System.clearProperty("javax.xml.transform.TransformerFactory");
		}
		return tf;
	}

}
