package jpa.variable;

import java.util.ArrayList;
import java.util.List;

import jpa.constant.VariableName;
import jpa.exception.DataValidationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class RenderUtil {
	static final Logger logger = LogManager.getLogger(RenderUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator", "\n");
	
	private RenderUtil() {
		// static methods only
	}
	
	/**
	 * Retrieve variable names from a input text string. It throws a Data
	 * Validation exception if the length of any variable name exceeds 26
	 * characters, or a closing variable delimiter is missing.
	 * 
	 * @param text -
	 *            template text
	 * @return list of variable names
	 * @throws DataValidationException
	 */
	public static List<String> retrieveVariableNames(String text) throws DataValidationException {
		List<String> varNames = new ArrayList<String>();
		if (text == null || text.trim().length() == 0)
			return varNames;
		text = Renderer.convertUrlBraces(text);
		int bgnPos = 0;
		int nextPos;
		while ((bgnPos = text.indexOf(Renderer.OpenDelimiter, bgnPos)) >= 0) {
			if ((nextPos = text.indexOf(Renderer.CloseDelimiter, bgnPos + Renderer.OpenDelimiter.length())) > 0) {
				String name = text.substring(bgnPos + Renderer.OpenDelimiter.length(), nextPos);
				if (name.length() > Renderer.VARIABLE_NAME_LENGTH) {
					int _openPos = text.indexOf(Renderer.OpenDelimiter, bgnPos + Renderer.OpenDelimiter.length());
					if (_openPos > 0 && _openPos < nextPos) {
						int _endPos = Math.min(text.length(), bgnPos + 26);
						throw new DataValidationException("Missing " + Renderer.CloseDelimiter
								+ " from position " + bgnPos + ", around: "
								+ text.substring(bgnPos, _endPos));
					}
					else {
						throw new DataValidationException("Variable name: ${" + name
								+ "} exceeded maximum length: " + Renderer.VARIABLE_NAME_LENGTH);
					}
				}
				bgnPos = nextPos + Renderer.CloseDelimiter.length();
				if (!varNames.contains(name)) {
					varNames.add(name);
				}
			}
			else {
				int _endPos = Math.min(text.length(), bgnPos + 26);
				throw new DataValidationException("Missing " + Renderer.CloseDelimiter + " from position "
						+ bgnPos + ", around: " + text.substring(bgnPos, _endPos));
			}
		}
		return varNames;
	}

	/**
	 * Check the variables in the input text against the provided variable name
	 * (loopName) for possible loop. It throws a Data Validation exception if a
	 * variable name from the input text matches the "loopName", or the length
	 * of a variable name exceeds 26 characters, or a closing variable delimiter
	 * is missing.<br/>
	 * 
	 * This method should be called before an email variable is saved to the
	 * database.
	 * 
	 * @param text -
	 *            email variable value
	 * @param loopName -
	 *            variable name to match
	 * @throws DataValidationException
	 */
	public static void checkVariableLoop(String text, String loopName)
			throws DataValidationException {
		List<String> varNames = new ArrayList<String>();
		checkVariableLoop(text, loopName, varNames, 0);
	}
	
	/**
	 * Check the variables in the input text against the provided variable name
	 * (loopName) for possible loop. It throws a Data Validation exception if a
	 * variable name from the input text matches the "loopName", or the length
	 * of a variable name exceeds 26 characters, or a closing variable delimiter
	 * is missing.
	 * 
	 * @param text -
	 *            template text
	 * @param loopName -
	 *            variable name to match
	 * @param varNames -
	 *            variable names found from template so far
	 * @param loops -
	 *            number of recursive loops
	 * @throws DataValidationException
	 */
	static void checkVariableLoop(String text, String loopName, List<String> varNames, int loops)
			throws DataValidationException {
		if (text == null || text.trim().length() == 0)
			return;
		text = Renderer.convertUrlBraces(text);
		int bgnPos = 0;
		int nextPos;
		while ((bgnPos = text.indexOf(Renderer.OpenDelimiter, bgnPos)) >= 0) {
			if ((nextPos = text.indexOf(Renderer.CloseDelimiter, bgnPos + Renderer.OpenDelimiter.length())) > 0) {
				String name = text.substring(bgnPos + Renderer.OpenDelimiter.length(), nextPos);
				if (name.length() > Renderer.VARIABLE_NAME_LENGTH) {
					int _openPos = text.indexOf(Renderer.OpenDelimiter, bgnPos + Renderer.OpenDelimiter.length());
					if (_openPos > 0 && _openPos < nextPos) {
						int _endPos = Math.min(text.length(), bgnPos + 26);
						throw new DataValidationException("Missing " + Renderer.CloseDelimiter + " in ${"
								+ loopName + "} from position " + bgnPos + ", around: "
								+ text.substring(bgnPos, _endPos));
					}
					else {
						throw new DataValidationException("Variable name: ${" + name
								+ "} exceeded maximum length: " + Renderer.VARIABLE_NAME_LENGTH
								+ " in ${" + loopName + "}");
					}
				}
				bgnPos = nextPos + Renderer.CloseDelimiter.length();
				logger.info("Loop " + loops + " - " + loopName + " -> " + name);
				if (!varNames.contains(name)) {
					varNames.add(name);
					if (varNames.contains(loopName)) {
						throw new DataValidationException("Loop found, please check ${" + loopName
								+ "} and its contents.");
					}
					if (isListVariable(name)) {
						continue;
					}
//					EmailVariableVo vo = getEmailVariableDao().getByName(name);
//					if (vo != null) {
//						String newText = vo.getDefaultValue();
//						checkVariableLoop(newText, loopName, varNames, ++loops);
//					}
				}
			}
			else {
				int _endPos = Math.min(text.length(), bgnPos + 26);
				throw new DataValidationException("Missing " + Renderer.CloseDelimiter + " in ${"
						+ loopName + "} from position " + bgnPos + ", around: "
						+ text.substring(bgnPos, _endPos));
			}
		}
	}

	public static boolean isListVariable(String name) {
		VariableName.LIST_VARIABLE_NAME[] listNames = VariableName.LIST_VARIABLE_NAME.values();
		for (VariableName.LIST_VARIABLE_NAME listName : listNames) {
			if (listName.name().equals(name)) {
				return true;
			}
		}
		return false;
 	}

}
