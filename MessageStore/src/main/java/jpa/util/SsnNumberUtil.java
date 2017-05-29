package jpa.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SsnNumberUtil {

	private static final String ssnRegex = "^(?!000)(?:[0-6]\\d{2}|7(?:[0-6]\\d|7[0-2]))([ -]?)(?!00)\\d{2}\\1(?!0000)\\d{4}$";
    private final static Pattern ssnPattern = Pattern.compile(ssnRegex);
    
    /*
     * Valid SSN: 	078-05-1120 | 078 05 1120 | 078051120
     * Invalid SSN: 987-65-4320 | 000-00-0000 | (555) 555-5555
     */
    public static boolean isValidSSN(String ssn) {
    	if (ssn == null) return false;
    	Matcher matcher = ssnPattern.matcher(ssn);
    	return matcher.matches();
    }

}
