package jpa.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Use HTMLEditorKit swing component to convert html text to plain text
 */
public class HtmlToText extends HTMLEditorKit.ParserCallback implements Serializable {
	private static final long serialVersionUID = -8389006976316629513L;
	static final Logger logger = LogManager.getLogger(HtmlToText.class);
	private StringBuffer sb;
	static final String LF = System.getProperty("line.separator", "\n");
	
	public HtmlToText() {
	}

	public String parse(Reader in) throws IOException {
		sb = new StringBuffer();
		ParserDelegator delegator = new ParserDelegator();
		// the third parameter is TRUE to ignore charset directive
		delegator.parse(in, this, Boolean.TRUE);
		return sb.toString();
	}

	public String parse(String text) throws IOException {
		StringReader sr = new StringReader(text);
		try {
			return parse(sr);
		}
		finally {
			sr.close();
		}
	}

	@Override
	public void handleText(char[] text, int pos) {
		sb.append(text).append(LF);
	}
	
	@Override
	public void handleError(String errorMsg, int pos){
		logger.error("Error caught from parser: " + errorMsg);
	}

	public static void main(String[] args) {
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			// 1, use FileReader
			java.net.URL url = loader.getResource("samples/HtmlSample.html");
			File file = new File(url.getFile());
			FileReader in = new FileReader(file);
			HtmlToText parser = new HtmlToText();
			parser.parse(in);
			in.close();
			
			// 2, use StringReader
			byte[] bytes = FileUtil.loadFromFile("samples/","HtmlSample.html");
			String text = parser.parse(new String(bytes));
			System.out.println(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
