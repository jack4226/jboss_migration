package jpa.variable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

public final class PropertyRendererTest {
	static final Logger logger = Logger.getLogger(PropertyRendererTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	@Test
	public void testRender() throws Exception {
		PropertyRenderer renderer = PropertyRenderer.getInstance();

		Properties map = loadVariableMap();
		String template = map.getProperty("dataSource.url");
		try {
			String renderedText = renderer.render(template, map);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			assertTrue(StringUtils.startsWith(renderedText, "jdbc:mysql://localhost:3306/emaildb"));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw e;
		}
		template = map.getProperty("jndi.url");
		try {
			String renderedText = renderer.render(template, map);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			assertEquals(renderedText, "remote://localhost:4447");
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw e;
		}
		template = map.getProperty("jdbc.host");
		try {
			String renderedText = renderer.render(template, map);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			assertEquals(renderedText, "localhost");
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw e;
		}

		template = "Variable ${not_found} doesn't exist.";
		try {
			String renderedText = renderer.render(template, map);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			assertEquals(renderedText, template);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw e;
		}
		
		template = "Variable with maximum length test ${loooooooooooooooooooooooonnnnnnnnnnnnnnnnnnnnnnnng} 50 letters variable.";
		map.put("loooooooooooooooooooooooonnnnnnnnnnnnnnnnnnnnnnnng",  "rendered");
		try {
			String renderedText = renderer.render(template, map);
			logger.info("\n++++++++++ Template Text++++++++++\n" + template);
			logger.info("\n++++++++++ Rendered Text++++++++++\n" + renderedText);
			String expected = "Variable with maximum length test rendered 50 letters variable.";
			assertEquals(renderedText, expected);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			throw e;
		}

		template = "Variable name too long test ${loooooooooooooooooooooooonnnnnnnnnnnnnnnnnnnnnnnng1} 51 letters variable.";
		try {
			renderer.render(template, map);
			fail();
		}
		catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			// expected
		}

	}

	@Test
	public void testBadTemplate() throws Exception {
		PropertyRenderer renderer = PropertyRenderer.getInstance();

		Properties map = loadVariableMap();
		
		String template1 = "Missing first closing delimiter\n"
			+ "Some Numberic values: ${numeric1, ${numeric2}, ${numeric3}\n"
			+ "$EndTemplate\n";
		
		try {
			renderer.render(template1, map);
			fail();
		}
		catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			assertTrue(e.getMessage().indexOf("${numeric1,") > 0);
		}
	}

	private Properties loadVariableMap() {
		String fileName = "META-INF/msgstore.mysql.properties";
		return VarReplProperties.loadMyProperties(fileName);
	}
}
