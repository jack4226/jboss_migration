package jpa.util;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;

public class ProductKey {

	static String ValidChars = "0123456789ABCDEFGHJKLMNPQRTUVWXY";
	static String prodName = "Emailsphere.com rules. BounceRuleDriven TemplateBasedMailingList";
	static int RDM_LEN = 9;
	
	public static boolean validateKey(String key) {
		if (StringUtils.isBlank(key)) {
			return false;
		}
		String _key = removeDashs(key);
		if (_key.length() != (RDM_LEN + 16)) {
			return false;
		}
		return validateKey(_key.substring(0, RDM_LEN), _key.substring(RDM_LEN), prodName);
	}
	
	static String generateMD5(String random, String prodName) throws DigestException {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update((random + prodName).getBytes());
			MessageDigest tc1 = (MessageDigest) md.clone();
			byte[] md5Digest = tc1.digest();
			md.reset();
			return applyMod32(md5Digest);
		}
		catch (NoSuchAlgorithmException e) {
			throw new DigestException("invalid digest Algorithm");
		}
		catch (CloneNotSupportedException cnse) {
			throw new DigestException("couldn't make digest of partial content");
		}
	}
	
	private static String applyMod32(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			int r = bytes[i];
			if (r < 0) {
				r = 128 + r;
			}
			sb.append(ValidChars.charAt(r % 32));
		}
		return sb.toString();
	}
	
	private static boolean validateKey(String random, String md5Str, String prodName) {
		String md5 = null;
		try {
			md5 = generateMD5(random, prodName);
		}
		catch (DigestException e) {
			e.printStackTrace();
			return false;
		}
		return md5Str.equals(md5);
	}
	
	private static String removeDashs(String key) {
		StringBuffer sb = new StringBuffer();
		char[] chars = key.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] != '-') {
				sb.append(chars[i]);
			}
		}
		return sb.toString();
	}
 }
