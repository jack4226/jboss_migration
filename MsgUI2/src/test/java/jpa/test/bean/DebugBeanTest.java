package jpa.test.bean;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.junit.Test;
import org.mockito.Mockito;

import jpa.msgui.bean.DebugBean;
import jpa.msgui.bean.FacesBroker;

public class DebugBeanTest {
	
	@Test
	public void testIncrementCount() {
		Map<String, Object> session = new HashMap<String, Object>();
		ExternalContext ext = Mockito.mock(ExternalContext.class);
		Mockito.when(ext.getSessionMap()).thenReturn(session);
		FacesContext context = Mockito.mock(FacesContext.class);
		Mockito.when(context.getExternalContext()).thenReturn(ext);
		FacesBroker broker = Mockito.mock(FacesBroker.class);
		Mockito.when(broker.getContext()).thenReturn(context);
		
		DebugBean bean = new DebugBean();
		bean.setBroker(broker);
		bean.incrementCount();
		assertEquals(1, session.get(DebugBean.key));
		bean.incrementCount();
		assertEquals(2, session.get(DebugBean.key));
	}

}
