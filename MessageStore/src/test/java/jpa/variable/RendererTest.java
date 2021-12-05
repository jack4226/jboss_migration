package jpa.variable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jpa.constant.VariableType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public final class RendererTest {
	static final Logger logger = LogManager.getLogger(RendererTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	@Test
	public void testRender() throws Exception {
		String template = "BeginTemplate\n"
			+ "Current Date: ${CurrentDate} ${0}\n"
			+ "${name1}, ${name2} Some Text ${name3} More Text\n"
			+ "Some Numberic values: ${numeric1}, ${numeric2}${numeric3}\n"
			+ "Some Datetime values: ${datetime1}, ${datetime2}, ${datetime3}, ${datetime4}\n"
			+ "${name4.recurrsive.long.name}\n"
			+ "$EndTemplate\n";
		
		java.util.Date currDate = new java.util.Date();
		String dateTimeFormat1 = "yyyy-MM-dd";
		SimpleDateFormat fmt = new SimpleDateFormat(dateTimeFormat1);
		String expected = "BeginTemplate\n"
			+ "Current Date: " + fmt.format(currDate) + " ${0}\n"
			+ "Jack Wang, John Lin Some Text Ramana More Text\n"
			+ "Some Numberic values: 12,345.678, (-000,012,345.68)-$99,999.99\n"
			+ "Some Datetime values: 2007-10-01 15:23:12, 12/01/2007, , 2009-07-29 13.04\n"
			+ "Recursive Variable Jack Wang End\n"
			+ "$EndTemplate\n";
		
		Map<String, RenderVariableVo> map = createVariableMap(currDate, dateTimeFormat1);
		
		Renderer renderer = Renderer.getInstance();
		try {
			Map<String, ErrorVariableVo> errors = new HashMap<String, ErrorVariableVo>();
			String renderedText = renderer.render(template, map, errors);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			assertEquals(renderedText, expected);
			assertTrue(!errors.isEmpty());
			if (!errors.isEmpty()) {
				logger.info("Display Error Variables..........");
				Set<String> set = errors.keySet();
				for (Iterator<String> it=set.iterator(); it.hasNext();) {
					String key = (String) it.next();
					ErrorVariableVo req = (ErrorVariableVo) errors.get(key);
					logger.info(req.toString());
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw e;
		}
	}

	@Test
	public void testBadTemplate() throws Exception {
		Map<String, RenderVariableVo> map = createVariableMap(new java.util.Date(), "yyyy-MM-dd");
		Renderer renderer = Renderer.getInstance();
		int exceptionCaught = 0;
		
		String template1 = "Missing first closing delimiter\n"
			+ "Some Numberic values: ${numeric1, ${numeric2}, ${numeric3}\n"
			+ "$EndTemplate\n";
		
		try {
			renderer.render(template1, map, new HashMap<String, ErrorVariableVo>());
		}
		catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			assertTrue(e.getMessage().indexOf("${numeric1,") > 0);
			exceptionCaught++;
		}
		
		String template2 = "Missing middle closing delimiter\n"
			+ "Some Numberic values: ${numeric1}, ${numeric2, ${numeric3}\n"
			+ "$EndTemplate\n";
		
		try {
			renderer.render(template2, map, new HashMap<String, ErrorVariableVo>());
		}
		catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			assertTrue(e.getMessage().indexOf("${numeric2,") > 0);
			exceptionCaught++;
		}
		
		String template3 = "Missing last closing delimiter\n"
			+ "Some Numberic values: ${numeric1}, ${numeric2}, ${numeric3\n"
			+ "$EndTemplate\n";
		
		try {
			renderer.render(template3, map, new HashMap<String, ErrorVariableVo>());
		}
		catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			assertTrue(e.getMessage().indexOf("${numeric3") > 0);
			exceptionCaught++;
		}
		
		assertTrue(exceptionCaught == 3);
	}

	private Map<String, RenderVariableVo> createVariableMap(java.util.Date currDate, String dateTimeFormat1) {
		Map<String, RenderVariableVo> map = new HashMap<String, RenderVariableVo>();
		
		RenderVariableVo currentDate = new RenderVariableVo(
				"CurrentDate", 
				currDate, 
				VariableType.DATETIME,
				dateTimeFormat1
			);
		map.put(currentDate.getVariableName(), currentDate);
		
		RenderVariableVo req1 = new RenderVariableVo(
				"name1", 
				"Jack Wang"
			);
		RenderVariableVo req2 = new RenderVariableVo(
				"name2", 
				"John Lin"
			);
		RenderVariableVo req3 = new RenderVariableVo(
				"name3", 
				"Ramana"
			);
		RenderVariableVo req4 = new RenderVariableVo(
				"name4.recurrsive.long.name", 
				"Recursive Variable ${name1} End", 
				VariableType.TEXT
			);
		RenderVariableVo req5 = new RenderVariableVo(
				"name5", 
				"Roger Banner", 
				VariableType.TEXT
			);
		
		RenderVariableVo req6_1 = new RenderVariableVo(
				"numeric1", 
				"12345.678", // use default format
				VariableType.NUMERIC
			);
		
		RenderVariableVo req6_2 = new RenderVariableVo(
				"numeric2", 
				"-12345.678",
				VariableType.NUMERIC,
				"000,000,000.0#;(-000,000,000.0#)"
			);
		
		RenderVariableVo req6_3 = new RenderVariableVo(
				"numeric3", 
				new BigDecimal(-99999.99),
				VariableType.NUMERIC,
				"$###,###,##0.00;-$###,###,##0.00"
			);
		
		RenderVariableVo req7_1 = new RenderVariableVo(
				"datetime1", 
				"2007-10-01 15:23:12", // use default format
				VariableType.DATETIME
			);
		
		RenderVariableVo req7_2 = new RenderVariableVo(
				"datetime2", 
				"12/01/2007", 
				VariableType.DATETIME,
				"MM/dd/yyyy" // custom format
			);
		
		RenderVariableVo req7_3 = new RenderVariableVo(
				"datetime3", 
				null,
				VariableType.DATETIME,
				"yyyy-MM-dd:hh.mm.ss a" // custom format
			);
		
		RenderVariableVo req7_4 = new RenderVariableVo(
				"datetime4", 
				new java.util.Date(), // current date time
				VariableType.DATETIME,
				"yyyy-MM-dd HH.mm" // custom format
			);
		
		map.put(req1.getVariableName(), req1);
		map.put(req2.getVariableName(), req2);
		map.put(req3.getVariableName(), req3);
		map.put(req4.getVariableName(), req4);
		map.put(req5.getVariableName(), req5);
		map.put(req6_1.getVariableName(), req6_1);
		map.put(req6_2.getVariableName(), req6_2);
		map.put(req6_3.getVariableName(), req6_3);
		map.put(req7_1.getVariableName(), req7_1);
		map.put(req7_2.getVariableName(), req7_2);
		map.put(req7_3.getVariableName(), req7_3);
		map.put(req7_4.getVariableName(), req7_4);
		
		req7_4 = new RenderVariableVo(
				"datetime4", 
				"2009-07-29 13.04",
				VariableType.DATETIME,
				"yyyy-MM-dd HH.mm" // custom format
			);
		map.put(req7_4.getVariableName(), req7_4);
		
		return map;
	}
}
