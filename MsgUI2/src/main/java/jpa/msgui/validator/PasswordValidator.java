package jpa.msgui.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;

@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator<Object>, java.io.Serializable {
	private static final long serialVersionUID = 1491495991826601838L;

	@Override
	public void validate(FacesContext context, UIComponent component, Object value)
			throws ValidatorException {
		// Obtain the first password field from f:attribute.
		String password1 = (String) component.getAttributes().get("password1");
		// Find the actual JSF component for the password.
		UIInput passwordInput = (UIInput) context.getViewRoot().findComponent(password1);
		// Get entered value of the first password.
		String password = (String) passwordInput.getValue();
		// get the entered value of the second password
		String confirm = (String) value;
		// Check if the first password is actually entered and compare it with
		// second password.
		if (StringUtils.isNotBlank(password) && !password.equals(confirm)) {
	        FacesMessage message = jpa.msgui.util.MessageUtil.getMessage(
					"jpa.msgui.messages", "passwordsNotEqual", null);
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			throw new ValidatorException(message);
		}
	}
}
