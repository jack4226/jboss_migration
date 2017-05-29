package jpa.msgui.converter;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;

@FacesConverter("TimestampConverter")
public class TimestampConverter implements Converter {

	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2)
			throws ConverterException {
		Timestamp result = null;
		if (StringUtils.isNotBlank(arg2)) {
			String input = arg2.replace('-', '/');
			try {
				result = new Timestamp(sdf.parse(input).getTime());
			} catch (ParseException e) {
				throw new ConverterException(
						"Timestamp format must be 'MM/dd/yyyy HH:mm'.(" + input + ")");
			}
		}
		return result;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2)
			throws ConverterException {
		String result = "";
		if (null != arg2) {
			try {
				result = sdf.format(arg2);
			}
			catch (IllegalArgumentException e) {
				throw new ConverterException("Invalid Object type received: " + arg2.getClass().getName());
			}
		}
		return result;
	}
}