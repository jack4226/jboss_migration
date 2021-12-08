package jpa.msgui.bean;

import java.io.ObjectStreamException;

import javax.faces.context.FacesContext;

/*
 * JSF's managed properties prevent the injection of narrowly scoped artifacts into broader scopes. 
 * This helps prevent stale objects leaking out of scope. It also means that you can't inject 
 * #{facesContext} as a managed property into scopes broader than the request scope.
 *
 * An application scoped utility bean can be used to overcome this problem:
 */

@javax.inject.Named("facesBroker")
@javax.enterprise.context.ApplicationScoped
public class FacesBroker implements java.io.Serializable {
	private static final long serialVersionUID = -6798783685568558029L;
	
	private static final FacesBroker INSTANCE = new FacesBroker();

	public FacesContext getContext() {
		return FacesContext.getCurrentInstance();
	}

	private Object readResolve() throws ObjectStreamException {
		return INSTANCE;
	}
}