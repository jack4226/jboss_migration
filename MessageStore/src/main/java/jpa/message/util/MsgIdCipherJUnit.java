package jpa.message.util;

import static org.junit.Assert.*;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class MsgIdCipherJUnit {
	static final Logger logger = LogManager.getLogger(MsgIdCipherJUnit.class);
	@Test 
	public void testMsgIdCipher() {
		Random random = new Random(System.currentTimeMillis());
		msgIdCipher(random);
		random = new Random();
		msgIdCipher(random);
	}
	
	void msgIdCipher(Random random) {
		long startTime =System.currentTimeMillis();
		int count = 0;
		int maxlen = 0;
		for (int i = 0; i < 20000; i++) {
			int msgId = Math.abs(random.nextInt(Integer.MAX_VALUE));
			String encoded1 = MsgIdCipher.encode(msgId);
			String encoded2 = MsgIdCipher.encode(msgId);
			if (encoded1 != null && !encoded1.equals(encoded2)) count++;
			int decoded1 = MsgIdCipher.decode(encoded1);
			int decoded2 = MsgIdCipher.decode(encoded2);
			assertEquals(msgId, decoded1);
			assertEquals(msgId, decoded2);
			if (encoded1.length()>maxlen) maxlen=encoded1.length();
			if (encoded2.length()>maxlen) maxlen=encoded2.length();
		}
		logger.info("Test completed, time taken: "
				+ (System.currentTimeMillis() - startTime)
				+ " ms, number of unequal encoding: " + count + ", maxlen="+maxlen);

	}
}
