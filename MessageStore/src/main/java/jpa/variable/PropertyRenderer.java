package jpa.variable;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Properties;

import jpa.exception.TemplateException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A template is a text string with variables identified by ${ and } tokens.<br>
 * Variables are replaced by their values during rendering process.
 * @author  Jack Wang
 */
public final class PropertyRenderer implements java.io.Serializable {
	private static final long serialVersionUID = -1670472296238983560L;
	static final Logger logger = LogManager.getLogger(PropertyRenderer.class);
	
	final static String OpenDelimiter="${";
	final static String CloseDelimiter="}";
	final static int DelimitersLen=OpenDelimiter.length()+CloseDelimiter.length();
	final static int VARIABLE_NAME_LENGTH = 50;
	
	final static int MAX_LOOP_COUNT = 10; // maximum depth of recursive variables
	
	private static PropertyRenderer renderer = null;

	private PropertyRenderer() {
		// empty constructor
	}

	/** return a instance of Renderer which is a singleton. */
	public static PropertyRenderer getInstance() {
		if (renderer == null) {
			renderer = new PropertyRenderer();
		}
		return renderer;
	}

	/**
	 * Render a template.
	 * @param templateText
	 * @param variables - a map contains variable name and value pairs.
	 * @param errors - an empty map
	 * @return rendered text.
	 * @throws TemplateException
	 * @throws ParseException
	 */
	public String render(String templateText, Properties variables)
			throws TemplateException, ParseException {
		return renderTemplate(templateText, variables, 0);
	}

	private String renderTemplate(String templateText,
			Properties variables, int loopCount)
			throws TemplateException, ParseException {
		
		if (templateText == null) {
			throw new IllegalArgumentException("Template Text must be provided");
		}
		if (variables == null) {
			throw new IllegalArgumentException("A Map for variables must be provided");
		}
		int currPos = 0;
		StringBuffer sb = new StringBuffer();
		VarProperties varProp;
		while ((varProp = getNextVariableName(templateText, currPos)) != null) {
			//logger.debug("varName:" + varProp.name + ", bgnPos:"
			//		+ varProp.bgnPos + ", endPos:" + varProp.endPos);
			sb.append(templateText.substring(currPos, varProp.bgnPos));
			// move to next position
			currPos = varProp.endPos;
			if (variables.get(varProp.name) != null) { // main section
				String value = variables.getProperty(varProp.name);
				if (value != null) {
					if (getNextVariableName(value, 0) != null) {
						// recursive variable
						if (loopCount <= MAX_LOOP_COUNT) { // check infinite loop
							sb.append(renderTemplate(value, variables, ++loopCount));
						}
					}
					else {
						sb.append(value);
					}
				}
			}
			else { // variable name not on render variables list
				sb.append(OpenDelimiter + varProp.name + CloseDelimiter);
			}
		}
		sb.append(templateText.substring(currPos));
		return sb.toString();
	}

	private VarProperties getNextVariableName(String text, int pos) throws TemplateException {
		VarProperties varProps = new VarProperties();
		int nextPos;
		if ((varProps.bgnPos = text.indexOf(OpenDelimiter, pos)) >= 0) {
			if ((nextPos = text.indexOf(CloseDelimiter, varProps.bgnPos + OpenDelimiter.length())) > 0
					&& (nextPos - OpenDelimiter.length() - varProps.bgnPos) <= VARIABLE_NAME_LENGTH) {
				varProps.endPos = nextPos + CloseDelimiter.length();
				varProps.name = text.substring(varProps.bgnPos + OpenDelimiter.length(), nextPos);
				if (varProps.name.indexOf(OpenDelimiter) >= 0) {
					throw new TemplateException("Missing the Closing Delimiter from position "
							+ varProps.bgnPos + ": " + OpenDelimiter
							+ varProps.name.substring(0, varProps.name.indexOf(OpenDelimiter)));
				}
				return varProps;
			}
			else {
				int len = varProps.bgnPos + VARIABLE_NAME_LENGTH + OpenDelimiter.length();
				len = text.length() < len ? text.length() : len;
				throw new TemplateException("Missing the Closing Delimiter from position " + varProps.bgnPos
						+ ": " + text.substring(varProps.bgnPos, len));
			}
		}
		return null;
	}

	private static class VarProperties implements Serializable {
		private static final long serialVersionUID = -2576136491515598700L;
		int bgnPos = 0;
		int endPos = 0;
		String name = null;
	}
}
