package jpa.message.util;

import java.math.BigInteger;
import java.util.Random;

import org.apache.log4j.Logger;

/*
 * 	See the theoretical basis at:
 *	http://theory.lcs.mit.edu/~rivest/rsapaper.pdf
 */

public final class MsgIdCipher {
	static final Logger logger = Logger.getLogger(MsgIdCipher.class);
	// Values from original author's web page.
	// See URL in above comments.
	static final BigInteger p = new BigInteger("47");
	static final BigInteger q = new BigInteger("59");
	static final BigInteger n = p.multiply(q);
	static final BigInteger d = new BigInteger("157");
	static final BigInteger e = new BigInteger("17");
	
	private static final int BLOCK_SIZE = 4;
	private static Random random = new Random();
	
	public static void main(String[] args) {
		int plainText = 103132;
		plainText = 926785317;
		//plainText = 10L;
		logger.info("plainText:      " + plainText);
		
		String withOnes = insertOnes(plainText+"");
		logger.info("After InsertZeros: "+withOnes);
		String withoutOnes = removeOnes(withOnes);
		logger.info("After RemoveZeros: "+withoutOnes);
		
		// Encrypt the encoded text and display the result.
		String cipherText = encode(plainText);
		logger.info("cipherText:     " + cipherText);

		int decipheredText = decode(cipherText);
		logger.info("decipheredText: " + decipheredText);
	}
	
	/**
	 * use RSA algorithm to encode the msgId, with check digit appended to the
	 * end.
	 * 
	 * @param msgId -
	 *            message id
	 * @return cipher text
	 */
	public static String encode(int msgId) throws NumberFormatException {
		String plainText = msgId + "" + getCheckDigit(msgId);
		// insert ones to make sure the value of each block is less than n
		// for example: 956789876 (after insert 1's) -> 195617891867
		plainText = insertOnes(plainText);
		plainText = insertZeros(plainText);
		String cipherText = doRSA(plainText, e, n);
		return cipherText;
	}
	
	/**
	 * use RSA algorithm to decode the cipherText, verify check digit to make
	 * sure the data was not tampered with.
	 * 
	 * @param cipherText -
	 *            encrypted text
	 * @return plain text
	 */
	public static int decode(String cipherText) throws NumberFormatException {
		cipherText = insertZeros(cipherText);
		String decipheredText = doRSA(cipherText, d, n);
		// restore to the original value by undoing the insert of ones
		// for example: 195617891867 (after remove 1's) ->  956789876
		decipheredText = removeOnes(decipheredText);
		String checkDigit = decipheredText.substring(decipheredText.length()-1);
		String plainText = decipheredText.substring(0,decipheredText.length()-1);
		if (Integer.parseInt(checkDigit) != getCheckDigit(Integer.parseInt(plainText))) {
			throw new NumberFormatException("Invalid checkdigit found, cipherText = " + cipherText
					+ ", decipheredText = " + decipheredText);
		}
		return Integer.parseInt(plainText);
	}

	private static String doRSA(String inputString, BigInteger exp, BigInteger n)
			throws NumberFormatException {
		BigInteger block;
		BigInteger output;
		String temp = "";
		String outputString = "";

		for (int cnt = 0; cnt < inputString.length(); cnt += BLOCK_SIZE) {
			temp = inputString.substring(cnt, cnt + BLOCK_SIZE);
			block = new BigInteger(temp);
			output = block.modPow(exp, n);
			temp = output.toString();
			if (temp.length() == 3) {
				temp = "0" + temp;
			}
			else if (temp.length() == 2) {
				temp = "00" + temp;
			}
			else if (temp.length() == 1) {
				temp = "000" + temp;
			}
			outputString += temp;
		}
		return outputString;
	}
	
	private static String insertZeros(String clearText) {
		// leave a room for check digit
		for (int i=0; i<(clearText.length()%BLOCK_SIZE); i++) {
			clearText = "0" + clearText;
		}
		return clearText;
	}
	
	private static String insertOnes(String clearText) {
		String outString = "";
		int i;
		for(i = clearText.length(); i >= 3; i -= 3) {
			outString = random.nextInt(2) + clearText.substring(i-3, i) + outString;
		}
		outString = clearText.substring(0,i) + outString;
		return outString;
	}
	
	private static String removeOnes(String clearText) {
		String outString = "";
		int i;
		for(i = clearText.length(); i >= 4; i -= 4) {
			outString = clearText.substring(i-3, i) + outString;
		}
		outString = clearText.substring(0,i) + outString;
		return outString;
	}
	
	private static int getCheckDigit(int msgId) {
		String numberStr = msgId+"";
		if (numberStr.length()>30)
			throw new NumberFormatException("Input number exceeded size limit: [" + numberStr + "]");
		//Calculate the check digit
		char multiplier[] = {7,1,3,7,1,3,7,1,3,7,1,3,7,1,3,7,1,3,7,1,3,7,1,3,7,1,3,7,1,3};
		char charstr[] = numberStr.toCharArray();
		int sum = 0;
		
		for (short i = 0; i < charstr.length; i++) {
			sum += multiplier[i] * (charstr[i] & 0x0f);
		}
		sum = (10 - (sum % 10)) % 10;
		return (int) sum;
	}
}
