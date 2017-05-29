package jpa.test.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import org.mockito.Mockito;

import jpa.model.UserData;
import jpa.msgui.bean.LoginBean;

public class LoginBeanTest {
	
	@Test
	public void testSetGetSessionMap() {
		Map<String, Object> session = new HashMap<String, Object>();
	    ExternalContext ext = Mockito.mock(ExternalContext.class);
	    Mockito.when(ext.getSessionMap()).thenReturn(session);
	    FacesContext context = Mockito.mock(FacesContext.class);
	    Mockito.when(context.getExternalContext()).thenReturn(ext);
	    
	    HttpSession httpSession = Mockito.mock(HttpSession.class);
	    Mockito.when(ext.getSession(Mockito.anyBoolean())).thenReturn(httpSession);
	    
	    UserData ud1 = new UserData();
	    Mockito.when(httpSession.getAttribute(Mockito.any())).thenReturn(ud1);
		
		LoginBean bean = new LoginBean();
		bean.setContext(context);
		
		UserData ud2 = bean.getSessionUserData();
		assertNotNull(ud2);
		assertEquals(ud1, ud2);
	}

}
