package com.es.util.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.es.ejb.ws.vo.EmailAddrVo;
import com.es.ejb.ws.vo.JasonTestVo;
import com.es.ejb.ws.vo.SubscriptionVo;
import com.es.tomee.util.JasonParser;

import jpa.util.PrintUtil;

public class JasonParserTest {
	static final Logger logger = LoggerFactory.getLogger(JasonParserTest.class);
	
	private SubscriptionVo subsVo;
	private List<SubscriptionVo> subsList;
	
	@Before
	public void setup() {
		String jsonStr = "{\"SubscriptionVo\":{\"rowId\":1,\"updtTime\":\"2017-11-09T15:41:22.526-05:00\",\"updtUserId\":\"MsgMaint\",\"listId\":\"SMPLLST1\",\"description\":\"Sample mailing list 1\",\"isSubscribed\":true,\"address\":\"jsmith@test.com\",\"CreateTime\":\"2017-11-09T15:36:09.119-05:00\"}}";
		logger.info("Json String: " + jsonStr);
		subsVo = JasonParser.jsonToObject(jsonStr, SubscriptionVo.class);
		assertNotNull(subsVo);
		subsVo.setDescription("Test description 1");
		logger.info("SubscriptionVo" + PrintUtil.prettyPrint(subsVo, 3));
		assertEquals("Test description 1", subsVo.getDescription());
		assertEquals("2017-11-09 15:41:22.526", subsVo.getUpdtTime().toString());

		String jsonArray = "{\"SubscriptionVo\":[{\"rowId\":1,\"updtTime\":\"2017-11-09T15:41:22.526-05:00\",\"updtUserId\":\"MsgMaint\",\"listId\":\"SMPLLST1\",\"description\":\"Sample mailing list 1\",\"isSubscribed\":true,\"address\":\"jsmith@test.com\",\"CreateTime\":\"2017-11-09T15:36:09.119-05:00\"},{\"rowId\":4,\"updtTime\":\"2017-11-09T15:36:09.291-05:00\",\"updtUserId\":\"MsgMaint\",\"listId\":\"SMPLLST2\",\"description\":\"Sample mailing list 2\",\"isSubscribed\":false,\"address\":\"jsmith@test.com\",\"CreateTime\":\"2017-11-09T15:36:09.119-05:00\"}]}";
		subsList = JasonParser.jsonArrayToList(jsonArray, SubscriptionVo.class);
		assertNotNull(subsList);
		assertEquals(2, subsList.size());
		SubscriptionVo vo1 = subsList.get(0);
		SubscriptionVo vo2 = subsList.get(1);
		assertEquals(1, vo1.getRowId());
		assertEquals(4, vo2.getRowId());
		assertEquals("SMPLLST1", vo1.getListId());
		assertEquals("SMPLLST2", vo2.getListId());
		assertEquals(true, vo1.isSubscribed());
		assertEquals(false, vo2.isSubscribed());
	}
	
	@Test
	public void testObjectToJson() {
		assertNotNull(subsVo);
		try {
			String jsonStr = JasonParser.objectToJson(subsVo, SubscriptionVo.class);
			logger.info("Json Object: " + jsonStr);
			
			assertTrue(jsonStr.indexOf("listId") > 0);
			assertTrue(jsonStr.indexOf("listId") < jsonStr.indexOf("SMPLLST1"));
			assertTrue(jsonStr.indexOf("rowId") > 0);
			assertTrue(jsonStr.indexOf("rowId") < jsonStr.indexOf("1"));
			
			SubscriptionVo vo = JasonParser.jsonToObject(jsonStr, SubscriptionVo.class);
			//logger.info("SubscriptionVo" + PrintUtil.prettyPrint(vo, 3));
			assertSubscriptionVosEqual(subsVo, vo);
		} catch (JAXBException e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
	@Test
	public void testListToJson() {
		assertNotNull(subsList);
		try {
			String jsonStr = JasonParser.listToJson(subsList);
			logger.info("Json Array: " + jsonStr);
			
			List<SubscriptionVo> list = JasonParser.jsonArrayToList(jsonStr, SubscriptionVo.class);
			assertNotNull(list);
			assertEquals(2, list.size());
			assertSubscriptionVosEqual(subsList.get(0), list.get(0));
			assertSubscriptionVosEqual(subsList.get(1), list.get(1));
		} catch (JAXBException e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
	@Test
	public void testObjectWithList() {
		JasonTestVo testvo1 = new JasonTestVo();
		testvo1.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
		testvo1.setDescription("Jason test with List");
		testvo1.setIsOptIn(true);
		testvo1.setListId("JSONLIST");
		testvo1.setSubscribed(true);
		testvo1.setRowId(101);
		testvo1.setUpdtTime(testvo1.getCreateTime());
		testvo1.setUpdtUserId("jsontest");
		EmailAddrVo addr = new EmailAddrVo();
		addr.setAcceptHtml(true);
		addr.setAddress("jsontest@test.org");
		addr.setRowId(9001);
		addr.setStatusChangeTime(testvo1.getCreateTime());
		addr.setStatusChangeUserId("jsontest");
		addr.setUpdtTime(addr.getStatusChangeTime());
		addr.setUpdtUserId("jsontest");
		testvo1.getAddrList().add(addr);
		testvo1.getAddrList().add(addr);
		try {
			String jsonStr = JasonParser.objectToJson(testvo1, JasonTestVo.class);
			logger.info("Json Test str: " + jsonStr);
			
			JasonTestVo testvo2 = JasonParser.jsonToObject(jsonStr, JasonTestVo.class);
			logger.info("Jaosn Test vo2" + PrintUtil.prettyPrintRecursive(testvo2));
			
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("listId", "JSONLIST2");
			map.put("ssOptIn", Boolean.FALSE);
			map.put("subscribed", false);
			map.put("rowId", 999);
			map.put("addrList", testvo2.getAddrList());
			
			String mapStr = JasonParser.mapToJson(map);
			logger.info("Json Map Str: " + mapStr);
			
			mapStr = "{ \"JasonTestVo\":" + mapStr + "}";
			JasonTestVo testvo3 = JasonParser.jsonToObject(mapStr, JasonTestVo.class);
			logger.info("Jaosn Test vo3" + PrintUtil.prettyPrintRecursive(testvo3));
		} catch (JAXBException e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
	@Test
	public void testMapToJson() {
		Map<String, Object> map = new HashMap<>();
		map.put("rowId", "101");
		map.put("listId", "SMPLLST_t");
		map.put("description", "Test description 2");
		
		try {
			String str1 = JasonParser.mapToJson(map);
			logger.info("Json String 1: " + str1);
			
			assertTrue(str1.indexOf("listId") > 0);
			assertTrue(str1.indexOf("listId") < str1.indexOf("SMPLLST_t"));
			assertTrue(str1.indexOf("rowId") > 0);
			assertTrue(str1.indexOf("rowId") < str1.indexOf("101"));
			
			str1 = "{\"SubscriptionVo\":" + str1 + "}";
			SubscriptionVo vo2 = JasonParser.jsonToObject(str1, SubscriptionVo.class);
			logger.info("SubscriptionVo 2" + PrintUtil.prettyPrint(vo2, 3));
			
			assertEquals("SMPLLST_t", vo2.getListId());
			assertEquals(101, vo2.getRowId());
			assertEquals("Test description 2", vo2.getDescription());
			
			String str2 = JasonParser.objectToJson(vo2, SubscriptionVo.class);
			logger.info("Json String 2: " + str2);
			
			assertTrue(str2.indexOf("listId") > 0);
			assertTrue(str2.indexOf("listId") < str2.indexOf("SMPLLST_t"));
			assertTrue(str2.indexOf("rowId") > 0);
			assertTrue(str2.indexOf("rowId") < str2.indexOf("101"));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	private void assertSubscriptionVosEqual(SubscriptionVo vo1, SubscriptionVo vo2) {
		assertEquals(vo1.getAddress(), vo2.getAddress());
		assertEquals(vo1.getCreateTime(), vo2.getCreateTime());
		assertEquals(vo1.getDescription(), vo2.getDescription());
		assertEquals(vo1.getIsOptIn(), vo2.getIsOptIn());
		assertEquals(vo1.getListId(), vo2.getListId());
		assertEquals(vo1.getRowId(), vo2.getRowId());
		assertEquals(vo1.getUpdtTime(), vo2.getUpdtTime());
		assertEquals(vo1.getUpdtUserId(), vo2.getUpdtUserId());
		assertEquals(vo1.isSubscribed(), vo2.isSubscribed());
	}
}
