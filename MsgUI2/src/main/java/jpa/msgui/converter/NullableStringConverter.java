package jpa.msgui.converter;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang3.StringUtils;

@FacesConverter("NullableStringConverter")
public class NullableStringConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent comp,
			String value) throws ConverterException {
		if (StringUtils.isBlank(value)) {
            if (comp instanceof EditableValueHolder) {
                ((EditableValueHolder) comp).setSubmittedValue(null);
            }
			return null;
		}
		else {
			return value;
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component,
			Object object) throws ConverterException {
		if (object == null) {
			return null;
		}
		else {
			return object.toString();
		}
	}
}