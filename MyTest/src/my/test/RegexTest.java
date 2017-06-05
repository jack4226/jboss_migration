package my.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class RegexTest {

	public static void main(String[] args) {
		new RegexTest().testLogicalAnd();
		new RegexTest().testExtractContat();
		
		testCallParam(0);
		testCallParam(Integer.valueOf(0));
		testCallParam(true);
		testCallParam(Boolean.TRUE);
		
		testAssignableFrom(Integer.class);
		
		testReplace();
		
		testParseFilename();
		
		//System.getProperties().list(System.out);
	}
	
	static void testParseFilename() {
		String str = "form-data; name=\"reply:msgsend:file\"; filename=\"cdSpreadsheetLoader.pl\"";
		//str = "form-data; name=\"reply:msgsend:file\"; filename='cdSpreadsheetLoader.pl'";
		Pattern p = Pattern.compile("filename=[\"']?([\\w\\s\\.,-]{1,100})[\"']?", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(str);
		if (m.find() && m.groupCount() >= 1) {
			for (int i=0; i<=m.groupCount(); i++) {
				System.out.println("Group[" + i + "]: " + m.group(i));
			}
		}
	}
	
	static void testReplace() {
		String str = StringUtils.replace("test   replace with", " ", "|");
		System.out.println(str);
		System.out.println(StringUtils.replaceAll("test   replace with", "[ ]+", "|"));
		str = "test   replace with".replaceAll("[\\s]+", "|");
		System.out.println(str);
	}
	
	static void testAssignableFrom(Class<?> cls) {
		if (java.sql.Timestamp.class.isAssignableFrom(cls) || java.sql.Date.class.isAssignableFrom(cls)) {
			System.out.println(cls.getName() + " is assignable from java.sql.Timestamp or java.sql.Date!");
		}
		else if (Boolean.class.isAssignableFrom(cls)) {
			System.out.println(cls.getName() + " is assignable from Boolean");
		}
		else if (Number.class.isAssignableFrom(cls)) {
			System.out.println(cls.getName() + " is assignable from Number");
		}
		else if (String.class.isAssignableFrom(cls)) {
			System.out.println(cls.getName() + " is assignable from String");
		}
	}
	
	static void testCallParam(Object value) {
		if (value instanceof Integer) {
			System.out.println("Integer: " + value);
		}
		else if (value instanceof Boolean) {
			System.out.println("Boolean: " + value);
		}
		else {
			System.out.println("Unknopwn: " + value);
		}
		
		if (Number.class.isAssignableFrom(Integer.class)) {
			System.out.println("Integer is type of Number");
		}
	}
	
	void testLogicalAnd() {
		Pattern p = Pattern.compile("^Start(?=.*kind)(?=.*good).*deed$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		p = Pattern.compile("(?=.*kind)(?=.*good).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		
		String str1 = "Start with a good word and end with a kind deed";
		String str2 = "Start with a kind word and end with a good deed";
		
		Matcher m = p.matcher(str1);
		if (m.find()) {
			for (int i=0; i<=m.groupCount(); i++) {
				System.out.println("Group[" + i + "]: " + m.group(i));
			}
		}
		
		m = p.matcher(str2);
		if (m.find()) {
			for (int i=0; i<=m.groupCount(); i++) {
				System.out.println("Group[" + i + "]: " + m.group(i));
			}
		}
	}
	
	void testExtractContat() {
		String queryStr = "SELECT CONCAT(c.FirstName, ' ', c.LastName) as ResultStr FROM subscriber_data c, email_address e where e.Row_Id=c.email_addr_id and e.Row_Id=?1";
		queryStr = "SELECT CONCAT_WS('-',c.StreetAddress2,c.StreetAddress) as ResultStr FROM subscriber_data c, email_address e where e.Row_Id=c.email_addr_id and e.Row_Id=?1";
		Pattern p = Pattern.compile("^(\\w{1,20} )((CONCAT|CONCAT_WS)\\(.*\\))(.*)$",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(queryStr);
		if (m.find() && m.groupCount() >= 4) {
			String concat = "";
			for (int i = 0; i <= m.groupCount(); i++) {
				System.out.println("Group[" + i + "]: " + m.group(i));
				if (i == 2) {
					concat = StringUtils.removeStartIgnoreCase(m.group(i), "CONCAT");
					if (StringUtils.startsWith(concat, "_WS")) {
						concat = convertToCONCAT(concat);
					}
				}
			}
			System.out.println("concat stage 1: " + concat);
			String separator = findSeparator(concat);
			
			String[] items = concat.split("[(),\\|\\']");
			List<String> coalesces = new ArrayList<String>();
			for (String item : items) {
				if (StringUtils.length(StringUtils.trim(item)) > 2) {
					System.out.println("item: " + item);
					coalesces.add("coalesce(" + item +", '')");
				}
			}
			concat = "(";
			for (int i=0; i<coalesces.size(); i++) {
				concat += coalesces.get(i);
				if (i<(coalesces.size()-1)) {
					concat += " || " + "'" + separator + "'" + " || ";
				}
			}
			concat += ")";
			System.out.println("concat stage 2: " + concat);

			queryStr = m.group(1) + concat + m.group(4);
			System.out.println("Query String: " + queryStr);
		}
	}
	
	String convertToCONCAT(String concat_ws) {
		String concat_tmp = StringUtils.removeStartIgnoreCase(concat_ws, "_WS");
		String separator = findSeparator(concat_tmp);
		String[] items = concat_tmp.split("[(),\\'\\|]");
		List<String> names = new ArrayList<String>();
		for (int i=0; i<items.length; i++) {
			String item = items[i];
			//System.out.println("item: " + item);
			if (StringUtils.isNotBlank(item) && !StringUtils.equals(item.trim(), separator)) {
				names.add(item);
			}
		}
		String concat = "(";
		for (int i=0; i<names.size(); i++) {
			concat += names.get(i);
			if (i<(names.size()-1)) {
				concat += ", '" + separator + "', ";
			}
		}
		concat += ")";
		return concat;
	}
	
	String findSeparator(String str) {
		Pattern p = Pattern.compile(".*\\'([\\p{Punct}])\\'.*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		System.out.println("String to match: " + str);
		Matcher m = p.matcher(str);
		if (m.find() && m.groupCount() >= 1) {
			for (int i = 0; i <= m.groupCount(); i++) {
				System.out.println("Group[" + i + "]: " + m.group(i));
			}
			return m.group(1);
		}
		return " ";
	}
}
