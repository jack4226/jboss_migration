package jpa.test.misc;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import jpa.util.StringUtil;

public class StringUtilTest {
	static final Logger logger = Logger.getLogger(StringUtilTest.class);
	
	@Test
	public void testRemoveMethods() {
		// Remove first
		String body1 = "<pre>12345abcdefklqhdkh</pre>";
		String removed1 = StringUtil.removeStringFirst(body1, "<pre>");
		logger.info("Removed1: " + removed1);
		assertEquals("12345abcdefklqhdkh</pre>",removed1);
		
		String removed2 = StringUtil.removeStringFirst(body1, "abcd");
		assertEquals("<pre>12345efklqhdkh</pre>",removed2);
		
		String removed3 = StringUtil.removeStringLast(body1, "</pre>");
		assertEquals("<pre>12345abcdefklqhdkh",removed3);
		
		String removed4 = StringUtil.removeStringLast(body1, "fkl");
		assertEquals("<pre>12345abcdeqhdkh</pre>",removed4);
		
		// removed quotes
		String body2 = "12345'abc'''def\"klq\"\"hdkh\"";
		String removed5 = StringUtil.removeQuotes(body2);
		assertEquals("12345abcdefklqhdkh", removed5);
		
		String body3 = "<trim/>12345abcdefklqhdkh<trim/>";
		String removed6 = StringUtil.trim(body3, "<trim/>");
		assertEquals("12345abcdefklqhdkh", removed6);
		
		String removed7 = StringUtil.trim(body3, "<trim");
		logger.info("Removed7: " + removed7);
		assertEquals("/>12345abcdefklqhdkh<trim/>", removed7);
		
		String removed8 = StringUtil.trim(body3, "trim");
		assertEquals("<trim/>12345abcdefklqhdkh<trim/>", removed8);
		
		String body4 = "   abwieufhuifh   ";
		String removed9 = StringUtil.trimRight(body4);
		assertEquals("   abwieufhuifh", removed9);
	}
	
	@Test
	public void testStripAllMethods() {
		String[] array1 = {"  abcd ", "efg ", "  hijklm", "nok", "  "};
		StringUtil.stripAll(array1);
		String[] array2 = {"abcd", "efg", "hijklm", "nok", ""};
		assertEquals(Arrays.asList(array1), Arrays.asList(array2));
		
		List<String> list1 = Arrays.asList(array1);
		StringUtil.stripAll(list1);
		List<String> list2 = Arrays.asList(array2);
		assertEquals(list1, list2);
		
		TestStripAll test = new TestStripAll();
		assertEquals("  abcd ,efg ,  hijklm,nok,  ", test.toString());
		StringUtil.stripAll(test);
		assertEquals("abcd,efg,hijklm,nok,", test.toString());
	}
	
	@Test
	public void testOtherMethods() {
		String after = StringUtil.cutWithDots("weqfhkjwfgikljgf", 10);
		assertEquals("weqfhkjwfg...", after);
		
		assertEquals(".....", StringUtil.getDots(5));
		
		String display = StringUtil.getHtmlDisplayText("<html><header>header test</header>hello world</html>");
		assertEquals("<pre>&lt;html&gt;&lt;header&gt;header test&lt;/header&gt;hello world&lt;/html&gt;</pre>", display);
	}
	
	public static class TestStripAll {
		private String field1;
		private String field2;
		private String field3;
		private String field4;
		private String field5;
		
		public TestStripAll() {
			field1 = "  abcd ";
			field2 = "efg ";
			field3 = "  hijklm";
			field4 = "nok";
			field5 = "  ";
		}
		
		public String getField1() {
			return field1;
		}
		public void setField1(String field1) {
			this.field1 = field1;
		}
		public String getField2() {
			return field2;
		}
		public void setField2(String field2) {
			this.field2 = field2;
		}
		public String getField3() {
			return field3;
		}
		public void setField3(String field3) {
			this.field3 = field3;
		}
		public String getField4() {
			return field4;
		}
		public void setField4(String field4) {
			this.field4 = field4;
		}
		public String getField5() {
			return field5;
		}
		public void setField5(String field5) {
			this.field5 = field5;
		}
		
		@Override
		public String toString() {
			return (field1 + "," + field2 + "," + field3 + "," + field4 + "," + field5);
		}
	}
}
