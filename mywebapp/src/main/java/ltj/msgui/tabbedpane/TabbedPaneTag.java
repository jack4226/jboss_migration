package ltj.msgui.tabbedpane;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.event.MethodExpressionActionListener;
import javax.faces.webapp.UIComponentELTag;

// This tag supports the following attributes
//
// binding (supported by UIComponentELTag)
// id (supported by UIComponentELTag)
// rendered (supported by UIComponentELTag)
// style
// styleClass
// tabClass
// selectedTabClass
// resourceBundle
// actionListener

public class TabbedPaneTag extends UIComponentELTag {
	private ValueExpression style;
	private ValueExpression styleClass;
	private ValueExpression tabClass;
	private ValueExpression selectedTabClass;
	private ValueExpression resourceBundle;
	private MethodExpression actionListener;

	public String getRendererType() {
		return "ltj.msgui.TabbedPane";
	}

	public String getComponentType() {
		return "ltj.msgui.TabbedPane";
	}

	public void setTabClass(ValueExpression newValue) {
		tabClass = newValue;
	}

	public void setSelectedTabClass(ValueExpression newValue) {
		selectedTabClass = newValue;
	}

	public void setStyle(ValueExpression newValue) {
		style = newValue;
	}

	public void setStyleClass(ValueExpression newValue) {
		styleClass = newValue;
	}

	public void setResourceBundle(ValueExpression newValue) {
		resourceBundle = newValue;
	}

	public void setActionListener(MethodExpression newValue) {
		actionListener = newValue;
	}

	protected void setProperties(UIComponent component) {
		// make sure you always call the superclass
		super.setProperties(component);

		component.setValueExpression("style", style);
		component.setValueExpression("styleClass", styleClass);
		component.setValueExpression("tabClass", tabClass);
		component.setValueExpression("selectedTabClass", selectedTabClass);
		component.setValueExpression("resourceBundle", resourceBundle);
		if (actionListener != null) {
			((ActionSource) component).addActionListener(new MethodExpressionActionListener(actionListener));
		}
	}

	public void release() {
		// always call the superclass method
		super.release();

		style = null;
		styleClass = null;
		tabClass = null;
		selectedTabClass = null;
		resourceBundle = null;
		actionListener = null;
	}
}