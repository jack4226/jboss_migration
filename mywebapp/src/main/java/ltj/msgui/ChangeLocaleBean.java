package ltj.msgui;

import java.util.Locale;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

public class ChangeLocaleBean {
	public String germanAction() {
		FacesContext context = FacesContext.getCurrentInstance();
		context.getViewRoot().setLocale(Locale.GERMAN);
		return null;
	}

	public String englishAction() {
		FacesContext context = FacesContext.getCurrentInstance();
		context.getViewRoot().setLocale(Locale.ENGLISH);
		return null;
	}

	/**
	 * change locale by parameter (from HTTP request)
	 * 
	 * @return
	 */
	public String changeLocaleByParam() {
		FacesContext context = FacesContext.getCurrentInstance();
		String languageCode = getLanguageCode(context);
		context.getViewRoot().setLocale(new Locale(languageCode));
		return null;
	}

	private String getLanguageCode(FacesContext context) {
		Map<String, String> params = context.getExternalContext().getRequestParameterMap();
		return params.get("languageCode");
	}

	/**
	 * change locale by attributes
	 * 
	 * @param event
	 */
	public void changeLocaleByAttr(ActionEvent event) {
		UIComponent component = event.getComponent();
		String languageCode = getLanguageCode(component);
		FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(languageCode));
	}

	private String getLanguageCode(UIComponent component) {
		Map<String, Object> attrs = component.getAttributes();
		return (String) attrs.get("languageCode");
	}

	/**
	 * change locale by setPropertyActionListener
	 */
	private String languageCode;

	public String changeLocale() {
		FacesContext context = FacesContext.getCurrentInstance();
		context.getViewRoot().setLocale(new Locale(languageCode));
		return null;
	}

	/**
	 * called by JSF using value from f:setPropertyActionListener component
	 * @param newValue
	 */
	public void setLanguageCode(String newValue) {
		languageCode = newValue;
	}

}
