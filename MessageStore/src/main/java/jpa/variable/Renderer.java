package jpa.variable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpa.constant.VariableType;
import jpa.exception.TemplateException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A template is a text string with variables identified by ${ and } tokens.<br>
 * Variables are replaced by their values during rendering process.
 * @author  Jack Wang
 */
public final class Renderer implements java.io.Serializable {
	private static final long serialVersionUID = -1670472296238983560L;
	static final Logger logger = LogManager.getLogger(Renderer.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	final static String OpenDelimiter="${";
	final static String CloseDelimiter="}";
	final static int DelimitersLen=OpenDelimiter.length()+CloseDelimiter.length();
	final static int VARIABLE_NAME_LENGTH = 26;
	
	final static int MAX_LOOP_COUNT = 10; // maximum depth of recursive variables
	
	private static Renderer renderer = null;

	private Renderer() {
		// empty constructor
	}

	/** return a instance of Renderer which is a singleton. */
	public static Renderer getInstance() {
		if (renderer == null) {
			renderer = new Renderer();
		}
		return renderer;
	}

	/**
	 * Render a template.
	 * @param templateText
	 * @param variables - a map contains variable name and RenderVariableVo pairs.
	 * @param errors - an empty map
	 * @return rendered text.
	 * @throws TemplateException
	 * @throws ParseException
	 */
	public String render(String templateText, Map<String, RenderVariableVo> variables, Map<String, ErrorVariableVo> errors)
			throws TemplateException, ParseException {
		return renderTemplate(templateText, variables, errors, 0);
	}

	private String renderTemplate(String templateText, Map<String, RenderVariableVo> variables, Map<String, ErrorVariableVo> errors, int loopCount)
			throws TemplateException, ParseException {
		
		if (templateText == null) {
			throw new IllegalArgumentException("Template Text must be provided");
		}
		if (variables == null) {
			throw new IllegalArgumentException("A Map for variables must be provided");
		}
		if (errors == null) {
			throw new IllegalArgumentException("A Map for errors must be provided");
		}
		templateText = convertUrlBraces(templateText);
		int currPos = 0;
		StringBuffer sb = new StringBuffer();
		VarProperties varProp;
		while ((varProp = getNextVariableName(templateText, currPos)) != null) {
			if (isDebugEnabled) {
				logger.debug("varName:" + varProp.name + ", bgnPos:" + varProp.bgnPos + ", endPos:"
						+ varProp.endPos);
			}
			sb.append(templateText.substring(currPos, varProp.bgnPos));
			// move to next position
			currPos = varProp.endPos;
			if (variables.get(varProp.name) != null) { // main section
				Object value = variables.get(varProp.name);
				if (value instanceof String) {
					value = new RenderVariableVo(varProp.name, (String) value);
				}
				else if (!(value instanceof RenderVariableVo)) {
					ErrorVariableVo err = new ErrorVariableVo(varProp.name, "Position: " + varProp.bgnPos
							+ ", rendered as: " + value, "Invalue variable value type: "
							+ value.getClass().getName());
					errors.put(err.getVariableName(), err);
					sb.append(value);
					continue;
				}
				RenderVariableVo r = (RenderVariableVo) value;
				if (VariableType.TEXT.equals(r.getVariableType())
						|| VariableType.ADDRESS.equals(r.getVariableType())
						|| VariableType.X_HEADER.equals(r.getVariableType())) {
					if (r.getVariableValue() != null) {
						if (getNextVariableName((String) r.getVariableValue(), 0) != null) {
							// recursive variable
							if (loopCount <= MAX_LOOP_COUNT) // check infinite loop
								sb.append(renderTemplate((String) r.getVariableValue(), variables, errors,
										++loopCount));
						}
						else {
							sb.append(r.getVariableValue());
						}
					}
				}
				else if (VariableType.NUMERIC.equals(r.getVariableType())) {
					if (r.getVariableValue() != null) {
						DecimalFormat formatter = new DecimalFormat();
						if (r.getVariableFormat() != null) {
							formatter.applyPattern(r.getVariableFormat());
						}
						if (r.getVariableValue() instanceof BigDecimal) {
							sb.append(formatter.format(((BigDecimal) r.getVariableValue()).doubleValue()));
						}
						else if (r.getVariableValue() instanceof String) {
							try {
								NumberFormat parser = NumberFormat.getNumberInstance();
								Number number = parser.parse((String) r.getVariableValue());
								sb.append(formatter.format(number));
							}
							catch (ParseException e) {
								logger.error("ParseException caught", e);
								sb.append((String) r.getVariableValue());
							}
						}
					}
				}
				else if (VariableType.DATETIME.equals(r.getVariableType())) {
					if (r.getVariableValue() != null) {
						SimpleDateFormat fmt = new SimpleDateFormat(RenderVariableVo.DEFAULT_DATETIME_FORMAT);
						if (r.getVariableFormat() != null) {
							fmt.applyPattern(r.getVariableFormat());
						}
						if (r.getVariableValue() instanceof java.util.Date) {
							sb.append(fmt.format(r.getVariableValue()));
						}
						else {
							try {
								java.util.Date date = fmt.parse((String) r.getVariableValue());
								sb.append(fmt.format(date));
							}
							catch (ParseException e) {
								logger.error("ParseException caught", e);
								sb.append((String) r.getVariableValue());
							}
						}
					}
				}
			}
			else { // variable name not on render variables list
				ErrorVariableVo err = new ErrorVariableVo(
						varProp.name,
						"Position: " + varProp.bgnPos + ", not rendered",
						"Variable name not on Render Variables list.");
				errors.put(err.getVariableName(), err);
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
	
	/*
	 * Curly braces are encoded in URL as "%7B" and "%7D". This method convert
	 * them back to "{" and "}".
	 */
	static String convertUrlBraces(String text) {
		//Sample input: "Web Beacon<img src='http://localhost/es/wsmopen.php?msgid=$%7BBroadcastMsgId%7D&amp;listid=$%7BListId%7D' width='1' height='1' alt=''>"
		String regex = "\\$\\%7B(.{1," + VARIABLE_NAME_LENGTH + "}?)\\%7D";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "\\$\\{" + m.group(1)+"\\}");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static class VarProperties implements Serializable {
		private static final long serialVersionUID = -1269338697408688103L;
		int bgnPos = 0;
		int endPos = 0;
		String name = null;
	}
}
