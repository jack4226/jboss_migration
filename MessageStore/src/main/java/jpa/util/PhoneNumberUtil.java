package jpa.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhoneNumberUtil {
	static final Logger logger = LogManager.getLogger(PhoneNumberUtil.class);

	private static final Map<String, Integer> alphaMap = new HashMap<String, Integer>();
	
	static {
		alphaMap.clear();
		alphaMap.put("A", 2);
		alphaMap.put("B", 2);
		alphaMap.put("C", 2);
		alphaMap.put("D", 3);
		alphaMap.put("E", 3);
		alphaMap.put("F", 3);
		alphaMap.put("G", 4);
		alphaMap.put("H", 4);
		alphaMap.put("I", 4);
		alphaMap.put("J", 5);
		alphaMap.put("K", 5);
		alphaMap.put("L", 5);
		alphaMap.put("M", 6);
		alphaMap.put("N", 6);
		alphaMap.put("O", 6);
		alphaMap.put("P", 7);
		alphaMap.put("Q", 7);
		alphaMap.put("R", 7);
		alphaMap.put("S", 7);
		alphaMap.put("T", 8);
		alphaMap.put("U", 8);
		alphaMap.put("V", 8);
		alphaMap.put("W", 9);
		alphaMap.put("X", 9);
		alphaMap.put("Y", 9);
		alphaMap.put("Z", 9);
	}
	
    private static final String phoneRegex = "^(?:1[ -]?)?((?:\\(\\d{3}\\)|\\d{3}))[ -]?((?:[\\da-z]{3}))[ -]?((?:[\\da-z]{4}))$";
    private final static Pattern phonePattern = Pattern.compile(phoneRegex, Pattern.CASE_INSENSITIVE);
    
    /*
     * Matches: 2405525009 | (240)552-5009 | 1(240) 552-5009 | 240 JOE-CELL
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
    	if (StringUtils.isBlank(phoneNumber)) {
    		return false;
    	}
    	Matcher matcher = phonePattern.matcher(phoneNumber);
    	return matcher.matches();
    }


	/**
	 * this method convert phone number with letters 614-JOe-Cell to 614-563-2355
	 * @param phoneNumber - phone number contains alpha-numeric letters
	 * @return phone number with only numeric numbers
	 */
	public static String convertPhoneLetters(String phoneNumber) {
		if (StringUtils.isBlank(phoneNumber)) {
			return phoneNumber;
		}
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<phoneNumber.length(); i++) {
			String letter = Character.toString(phoneNumber.charAt(i)).toUpperCase();
			if (alphaMap.containsKey(letter)) {
				sb.append(alphaMap.get(letter));
			}
			else {
				sb.append(phoneNumber.charAt(i));
			}
		}
		return sb.toString();
	}
	
	/**
	 * This method removes hyphens or spaces in a phone number.
	 * @param phoneNumber contains hyphens or spaces
	 * @return a 10 digit number
	 * @throws NumberFormatException if an invalid number is passed in.
	 */
	public static String convertTo10DigitNumber(String phoneNumber) throws NumberFormatException {
		if (!isValidPhoneNumber(phoneNumber)) {
			throw new NumberFormatException("Invalid phone number (" + phoneNumber + ") passed in.");
		}
		String phone = convertPhoneLetters(phoneNumber);
		Matcher m = phonePattern.matcher(phone);
		if (m.find() && m.groupCount()>=3) {
			return (m.group(1)+m.group(2)+m.group(3));
		}
		return phoneNumber;
	}
	
	public static void main(String[] args) {
		logger.info(convertPhoneLetters("1 614-JOe-Cell"));
		logger.info(convertTo10DigitNumber("1 614-JO6-G0LO"));
	}
}
