package jpa.test.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.SerializationUtils;

import jpa.constant.EmailVariableType;
import jpa.model.EmailVariable;
import jpa.msgui.bean.EmailVariableBean;
import jpa.msgui.mock.ContextMocker;
import jpa.msgui.util.FacesUtil;
import jpa.service.common.EmailVariableService;
import jpa.util.PrintUtil;

public class EmailVariableBeanTest {
	static final Logger logger = LogManager.getLogger(EmailVariableBeanTest.class);
	
	static AbstractApplicationContext applContext = null;
	static EmailVariableService ev_svc = null;
	
	@BeforeClass
	public static void setupSpring() {
		//PrintClassPath.print();
		applContext = jpa.spring.util.SpringUtil.getAppContext();
		ev_svc = applContext.getBean(EmailVariableService.class);
	}
	
	
	@Test
	public void testSerializarion() {
		EmailVariableBean evb1 = Mockito.mock(EmailVariableBean.class);
		SerializationUtils.serialize(evb1);
		
		EmailVariableBean evb2 = new EmailVariableBean();
		SerializationUtils.serialize(evb2);
	}

	@Test
	public void testContextMocker() {
		FacesContext context = ContextMocker.mockFacesContext();
		try {
			Map<String, Object> session = new HashMap<String, Object>();
			Map<String, Object> request = new HashMap<String, Object>();
			ExternalContext ext = Mockito.mock(ExternalContext.class);
			Mockito.when(ext.getSessionMap()).thenReturn(session);
			Mockito.when(ext.getRequestMap()).thenReturn(request);
			Mockito.when(context.getExternalContext()).thenReturn(ext);
			
			session.put("session_test", "session test value");
			org.junit.Assert.assertEquals("session test value", FacesUtil.getSessionMapValue("session_test"));
			
			request.put("request_test", "request test value");
			org.junit.Assert.assertEquals("request test value", FacesUtil.getRequestMapValue("request_test"));
		}
		finally {
			context.release();
		}
	}
	
	@Test
	public void testGetAllAndCRUD() {
		EmailVariableBean evb = new EmailVariableBean();
		evb.setEmailVariableService(ev_svc);
		
		FacesContext context = ContextMocker.mockFacesContext();
		try {
			Map<String, Object> session = new HashMap<String, Object>();
			Map<String, Object> request = new HashMap<String, Object>();
			ExternalContext ext = Mockito.mock(ExternalContext.class);
			Mockito.when(ext.getSessionMap()).thenReturn(session);
			Mockito.when(ext.getRequestMap()).thenReturn(request);
			Mockito.when(context.getExternalContext()).thenReturn(ext);
			
			request.put("frompage", "main");
			
			DataModel<EmailVariable> dm1 = evb.getAll();
			
			assertTrue(dm1.getRowCount() > 0);
			assertEquals(dm1.getClass(), ListDataModel.class);
			
			ListDataModel<EmailVariable> ldm1 = (ListDataModel<EmailVariable>) dm1;
			@SuppressWarnings("unchecked")
			List<EmailVariable> evList1 = (List<EmailVariable>) dm1.getWrappedData();
			assertTrue(ldm1.getRowCount() >= evList1.size());
			assertFalse(evList1.isEmpty());
			assertFalse(evb.getAnyListsMarkedForDeletion());
			for (int i=0; i<evList1.size(); i++) {
				EmailVariable ev = evList1.get(i);
				// verify queries
				if (StringUtils.isNotBlank(ev.getVariableQuery())) {
					logger.info("Query: " + ev.getVariableQuery());
					evb.setEmailVariable(ev);
					evb.testEmailVariableListener(null);
					logger.info("Is query valid? " + evb.getTestResult());
					assertTrue(StringUtils.contains(evb.getTestResult(), "Success"));
				}
				if (i == 0 || i == (evList1.size() - 1)) {
					ev.setMarkedForDeletion(true);
					logger.info("EmailVariable[" + i + "]: " + PrintUtil.prettyPrint(ev));
				}
			}
			assertTrue(evb.getAnyListsMarkedForDeletion());
			
			// test viewVariable()
			assertFalse(evb.getEmailVariable().isMarkedForEdition());
			evb.viewEmailVariable();
			assertTrue(evb.getEmailVariable().isMarkedForEdition());
			
			// test saveVariable()
			java.sql.Timestamp tms = new java.sql.Timestamp(System.currentTimeMillis());
			evb.getEmailVariable().setUpdtTime(tms);
			evb.saveEmailVariable();
			assertEquals(tms, evb.getEmailVariable().getUpdtTime());
			
			// test insert
			evb.addEmailVariable();
			EmailVariable ev_new = evb.getEmailVariable();
			ev_new.setDefaultValue("test insert");
			ev_new.setVariableName("test_insert_variable_name");
			ev_new.setVariableType(EmailVariableType.Custom.getValue());
			evb.saveEmailVariable();
			EmailVariable ev_after = evb.getEmailVariable();
			assertNotNull(ev_after);
			assertNotNull(ev_after.getRowId());
			logger.info("Record inserted, Row_Id = " + ev_after.getRowId());
			
			// test delete
			int rows_before = evList1.size();
			for (EmailVariable ev : evList1) {
				ev.setMarkedForDeletion(false);
			}
			ev_after.setMarkedForDeletion(true);
			evb.deleteEmailVariables();
			int rows_after = evList1.size();
			assertTrue(rows_before > rows_after);
		}
		finally {
			context.release();
		}
	}

}
