package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.model.UserData;
import jpa.service.common.UserDataService;
import jpa.spring.util.BoTestBase;
import jpa.spring.util.SpringUtil;

public class UserDataTest extends BoTestBase {

	@BeforeClass
	public static void UserDataPrepare() {
	}

	@Autowired
	UserDataService service;

	@Test
	public void userDataService() {
		DataSource ds = (DataSource) SpringUtil.getAppContext().getBean("msgDataSource");
		Connection con = null;
		try {
			con = ds.getConnection();
			assertNotNull(con);
			//System.err.println("AutoCommit?" + con.getAutoCommit());
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			if (con!=null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		List<UserData> list = service.getAll();
		assertFalse(list.isEmpty());
		
		UserData tkn0 = service.getByUserId(list.get(0).getUserId());
		assertNotNull(tkn0);
		
		tkn0 = service.getByRowId(list.get(0).getRowId());
		assertNotNull(tkn0);
		
		service.getForLogin(list.get(0).getUserId(), list.get(0).getPassword());
		
		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		service.update4Web(tkn0);
		
		UserData tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert
		UserData tkn2 = new UserData();
		tkn1.copyPropertiesTo(tkn2);
		tkn2.setUserId(tkn1.getUserId()+"_v2");
		service.insert(tkn2);
		
		UserData tkn3 = service.getByUserId(tkn2.getUserId());
		assertTrue(tkn3.getRowId()!=tkn1.getRowId());
		// end of test insert
		
		// test select returning null
		service.delete(tkn3);
		assertNull(service.getByUserId(tkn2.getUserId()));
		
		assertTrue(0==service.deleteByUserId(tkn3.getUserId()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
}
