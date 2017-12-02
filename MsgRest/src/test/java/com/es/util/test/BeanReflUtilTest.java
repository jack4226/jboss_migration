package com.es.util.test;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.es.ejb.ws.vo.MailingListVo;
import com.es.tomee.util.BeanReflUtil;

import jpa.model.EmailAddress;
import jpa.util.PrintUtil;

public class BeanReflUtilTest {
	protected final static Logger logger = Logger.getLogger(BeanReflUtilTest.class);
	
	@Test
	public void testCopyMapToObject() {
		Map<String, String> formMap = new LinkedHashMap<String, String>();
		formMap.put("address", "mynewaddress@test.com");
		formMap.put("updtUserId", "new user");
		formMap.put("bounceCount", "123");
		formMap.put("acceptHtml", "false");
		formMap.put("lastSentTime", "2015-04-30 13:12:34.123456");
		formMap.put("updtTime", "2015-04-30 13:12:34.123456789");
		EmailAddress obj = new EmailAddress();
		BeanReflUtil.copyProperties(obj, formMap);
		logger.info(PrintUtil.prettyPrint(obj));
		
		assertEquals("mynewaddress@test.com", obj.getAddress());
		assertEquals("new user", obj.getUpdtUserId());
		assertEquals(123, obj.getBounceCount());
		assertEquals(false, obj.isAcceptHtml());
		assertEquals("2015-04-30 13:12:34.123456", obj.getLastSentTime().toString());
		assertEquals("2015-04-30 13:12:34.123456789", obj.getUpdtTime().toString());
	}

	@Test
	public void testCopyProperties() {
		// test fieldsDiff
		jpa.model.MailingList ml = new jpa.model.MailingList();
		MailingListVo vo = new MailingListVo();
		ml.setDisplayName("Display name 1");
		ml.setBuiltin(false);
		vo.setDisplayName("Display Name 2");
		vo.setListId("SMPLLST1");
		vo.setBuiltin(true);
		java.sql.Timestamp tms = new java.sql.Timestamp(System.currentTimeMillis());
		vo.setCreateTime(tms);
		BeanReflUtil.copyProperties(ml, vo);
		logger.info(PrintUtil.prettyPrint(ml));
		
		assertEquals("Display Name 2", ml.getDisplayName());
		assertEquals(true, ml.isBuiltin());
		assertEquals("SMPLLST1", ml.getListId());
		assertEquals(tms, ml.getCreateTime());
	}
}
