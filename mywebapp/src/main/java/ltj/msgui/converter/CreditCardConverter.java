package ltj.msgui.converter;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import ltj.msgui.CreditCard;

public class CreditCardConverter implements Converter {
	public Object getAsObject(FacesContext context, UIComponent component, String newValue) throws ConverterException {
		StringBuilder builder = new StringBuilder(newValue);
		boolean foundInvalidCharacter = false;
		char invalidCharacter = '\0';
		int i = 0;
		while (i < builder.length() && !foundInvalidCharacter) {
			char ch = builder.charAt(i);
			if (Character.isDigit(ch)) {
				i++;
			}
			else if (Character.isWhitespace(ch)) {
				builder.deleteCharAt(i);
			}
			else {
				foundInvalidCharacter = true;
				invalidCharacter = ch;
			}
		}

		if (foundInvalidCharacter) {
			FacesMessage message = ltj.msgui.util.Messages.getMessage("ltj.msgui.messages", "badCreditCardCharacter",
					new Object[] { Character.valueOf(invalidCharacter) });
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ConverterException(message);
		}

		return new CreditCard(builder.toString());
	}

	public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
		// length 13: xxxx xxx xxx xxx
		// length 14: xxxxx xxxx xxxxx
		// length 15: xxxx xxxxxx xxxxx
		// length 16: xxxx xxxx xxxx xxxx
		// length 22: xxxxxx xxxxxxxx xxxxxxxx
		String v = value.toString();
		int[] boundaries = null;
		int length = v.length();
		if (length == 13) {
			boundaries = new int[] { 4, 7, 10 };
		}
		else if (length == 14) {
			boundaries = new int[] { 5, 9 };
		}
		else if (length == 15) {
			boundaries = new int[] { 4, 10 };
		}
		else if (length == 16) {
			boundaries = new int[] { 4, 8, 12 };
		}
		else if (length == 22) {
			boundaries = new int[] { 6, 14 };
		}
		else {
			return v;
		}
		String separator = (String) component.getAttributes().get("separator");
		if (separator == null) {
			separator = " ";
		}
		StringBuilder result = new StringBuilder();
		int start = 0;
		for (int i = 0; i < boundaries.length; i++) {
			int end = boundaries[i];
			result.append(v.substring(start, end));
			result.append(separator);
			start = end;
		}
		result.append(v.substring(start));
		return result.toString();
	}
}
