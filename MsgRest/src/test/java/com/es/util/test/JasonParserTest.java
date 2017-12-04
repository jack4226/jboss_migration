package com.es.util.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.es.ejb.ws.vo.SubscriptionVo;
import com.es.tomee.util.JasonParser;

import jpa.util.PrintUtil;

public class JasonParserTest {
	static final Logger logger = LoggerFactory.getLogger(JasonParserTest.class);
	
	private SubscriptionVo subsVo;
	
	@Before
	public void setup() {
		String mappingStr = "{\"SubscriptionVo\":[{\"rowId\":1,\"updtTime\":\"2017-11-09T15:41:22.526-05:00\",\"updtUserId\":\"MsgMaint\",\"listId\":\"SMPLLST1\",\"description\":\"Sample mailing list 1\",\"isSubscribed\":true,\"address\":\"jsmith@test.com\",\"CreateTime\":\"2017-11-09T15:36:09.119-05:00\"},{\"rowId\":4,\"updtTime\":\"2017-11-09T15:36:09.291-05:00\",\"updtUserId\":\"MsgMaint\",\"listId\":\"SMPLLST2\",\"description\":\"Sample mailing list 2\",\"isSubscribed\":true,\"address\":\"jsmith@test.com\",\"CreateTime\":\"2017-11-09T15:36:09.119-05:00\"}]}";
		mappingStr = "{\"SubscriptionVo\":{\"rowId\":1,\"updtTime\":\"2017-11-09T15:41:22.526-05:00\",\"updtUserId\":\"MsgMaint\",\"listId\":\"SMPLLST1\",\"description\":\"Sample mailing list 1\",\"isSubscribed\":true,\"address\":\"jsmith@test.com\",\"CreateTime\":\"2017-11-09T15:36:09.119-05:00\"}}";
		logger.info("mappingStr: " + mappingStr);
		subsVo = JasonParser.JsonToObject(mappingStr, SubscriptionVo.class);
		assertNotNull(subsVo);
		subsVo.setDescription("Test description 1");
		logger.info("subsVo" + PrintUtil.prettyPrint(subsVo, 3));
		assertEquals("Test description 1", subsVo.getDescription());
	}
	
	
	@Test
	public void testJson2Object() {
		Map<String, Object> map = new HashMap<>();
		map.put("rowId", "101");
		map.put("listId", "SMPLLST_t");
		map.put("description", "Test description 2");
		
		try {
			String str1 = JasonParser.MapToJson(map);
			logger.info("Json String 1: " + str1);
			
			SubscriptionVo vo2 = JasonParser.JsonToObject(str1, SubscriptionVo.class);
			logger.info("vo2" + PrintUtil.prettyPrint(vo2, 3));
			
			String str2 = JasonParser.ObjectToJson(vo2, SubscriptionVo.class);
			logger.info("Json String 2: " + str2);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

}
