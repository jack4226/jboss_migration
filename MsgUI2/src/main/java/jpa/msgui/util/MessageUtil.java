package jpa.msgui.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageUtil implements java.io.Serializable {
	private static final long serialVersionUID = -4832621618090969451L;
	static final Logger logger = LogManager.getLogger(MessageUtil.class);
	
	public static FacesMessage getMessage(String bundleName, String resourceId, Object[] params) {
		if (StringUtils.isBlank(bundleName)) {
			logger.error("Resource bundle name can not be blank!");
			throw new RuntimeException("Resource bundle name must be provided!");
		}
		FacesContext context = FacesContext.getCurrentInstance();
		Locale locale = getLocale(context);
		ClassLoader loader = getClassLoader();
		String appBundleName = context.getApplication().getMessageBundle();
		ResourceBundle appBundle = null;
		if (StringUtils.isNotBlank(appBundleName)) {
			try {
				appBundle = ResourceBundle.getBundle(appBundleName, locale, loader);
			}
			catch (MissingResourceException e) {
				logger.info("MissingResourceException caught: " + e.getMessage());
			}
		}
		ResourceBundle reqBundle = null;
		try {
			reqBundle = ResourceBundle.getBundle(bundleName, locale, loader);
		}
		catch (MissingResourceException e) {
			logger.info("MissingResourceException caught: " + e.getMessage());
		}
		String summary = getString(appBundle, reqBundle, resourceId, locale, params);
		if (summary == null) {
			summary = "???" + resourceId + "???";
		}
		String detail = getString(appBundle, reqBundle, resourceId + "_detail", locale, params);
		if (detail != null) {
			logger.info("ResourceId: " + resourceId + ", Summary/Detail: " + summary + " => " + detail);
		}
		return new FacesMessage(summary, detail);
	}

	static String getString(ResourceBundle bundle, String resourceId, Object[] params) {
		FacesContext context = FacesContext.getCurrentInstance();
		Locale locale = getLocale(context);
		return getString(bundle, bundle, resourceId, locale, params);
	}

	static String getString(ResourceBundle appBundle, ResourceBundle reqBundle, String resourceId, Locale locale, Object[] params) {
		String resource = null;

		if (appBundle != null) { // try application bundle first
			try {
				resource = appBundle.getString(resourceId);
			} catch (MissingResourceException ex) {
				logger.error("MissingResourceException: " + ex.getMessage());
			}
		}

		if (resource == null) { // resource not found, try request bundle
			if (reqBundle != null) {
				try {
					resource = reqBundle.getString(resourceId);
				} catch (MissingResourceException ex) {
					logger.error("MissingResourceException: " + ex.getMessage());
				}
			}
		}

		if (resource == null) {
			return null; // no match
		}
		
		if (params == null) {
			return resource;
		}
		else {
			MessageFormat formatter = new MessageFormat(resource, locale);
			return formatter.format(params);
		}
	}

	public static ResourceBundle getBundle(String bundleName) {
		if (StringUtils.isBlank(bundleName)) {
			logger.error("Resource bundle name can not be blank!");
			throw new RuntimeException("Resource bundle name must be provided!");
		}
		FacesContext context = FacesContext.getCurrentInstance();
		Locale locale = getLocale(context);
		ClassLoader loader = getClassLoader();
		String appBundleName = context.getApplication().getMessageBundle();
		ResourceBundle appBundle = null;
		if (StringUtils.isNotBlank(appBundleName)) {
			try {
				appBundle = ResourceBundle.getBundle(appBundleName, locale, loader);
				return appBundle;
			}
			catch (MissingResourceException e) {
				logger.info("MissingResourceException caught: " + e.getMessage());
			}
		}
		ResourceBundle reqBundle = null;
		try {
			reqBundle = ResourceBundle.getBundle(bundleName, locale, loader);
			return reqBundle;
		}
		catch (MissingResourceException e) {
			logger.info("MissingResourceException caught: " + e.getMessage());
		}
		return null;
	}
	
	public static Locale getLocale(FacesContext context) {
		Locale locale = null;
		UIViewRoot viewRoot = context.getViewRoot();
		if (viewRoot != null) {
			locale = viewRoot.getLocale();
		}
		if (locale == null) {
			locale = Locale.getDefault();
		}
		return locale;
	}

	public static ClassLoader getClassLoader() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = ClassLoader.getSystemClassLoader();
		}
		return loader;
	}

	public static void main(String[] args) {
		// en_US
		System.out.println("Current Locale: " + Locale.getDefault());
		ResourceBundle msgbundle = ResourceBundle.getBundle("jpa.msgui.messages");
		try {
			ResourceBundle.getBundle("jpa.msgui.messages_not_exist");
			throw new RuntimeException();
		}
		catch (MissingResourceException e) {
			// Expected, ignore
		}

		// read jpa.msgui.messages.properties
		System.out.println("US English: " + msgbundle.getString("selectDifferentRuleText"));

		String msgText = getString(msgbundle, msgbundle, "userDoesNotExist", Locale.getDefault(), new String[] {"New User"});
		System.out.println("getString(): " + msgText);

		Locale.setDefault(new Locale("de"));

		// read jpa.msgui.messages_de.properties
		System.out.println("Current Locale: " + Locale.getDefault());
		msgbundle = ResourceBundle.getBundle("jpa.msgui.messages");
		
		msgText = getString(msgbundle, msgbundle, "currentScore", Locale.getDefault(), new Number[] {123});
		
		System.out.println("German language: " + msgText);
	}
}
