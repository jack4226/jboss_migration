package jpa.msgui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;

import jpa.util.EmailAddrUtil;

@FacesValidator("emailAddressValidator")
public class EmailAddressValidator implements Validator<Object>, java.io.Serializable {
	private static final long serialVersionUID = 4514200411820518442L;

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) {
		String emailAddr = (String) value;
		if (StringUtils.isBlank(emailAddr)) {
			return;
		}
		if (!EmailAddrUtil.isRemoteEmailAddress(emailAddr)) {
			FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "invalidEmailAddress", new String[] {emailAddr});
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(message);
		}
	}
}
