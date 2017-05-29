package jpa.test.msgdata;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import jpa.message.BounceAddressFinder;
import jpa.spring.util.BoTestBase;

public class BounceAddressFinderTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(BounceAddressFinderTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	static final String LF = System.getProperty("line.separator", "\n");

	@Test
	public void findBounceAddr1() {
		findBounceAddress("samples/bounceBodySamples.txt");
	}
	
	@Test
	public void findBounceAddr2() {
		findBounceAddress("samples/bounceBodySamples2.txt");
	}

	private void findBounceAddress(String filePath) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream(filePath);
		if (is == null) {
			logger.warn("InputStream not found.");
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			StringBuilder sb = new StringBuilder();
			String line = null;
			int count = 0;
			int matched = 0;
			while ((line = br.readLine()) != null) {
				if ("--- EOM ---".equals(line)) {
					count++;
					String body = sb.toString();
					sb = new StringBuilder();
					String addr = BounceAddressFinder.getInstance().find(body);
					if (StringUtils.isBlank(addr)) {
						logger.info("(" + count + ") - not matched ##########.");
					}
					else {
						logger.info("(" + count + ") - matched: " + addr);
						matched++;
					}
				}
				else {
					sb.append(line + LF);
				}
			}
			br.close();
			logger.info("Count: " + count + ", Matched: " + matched);
			assertEquals(count, matched);
		}
		catch (IOException e) {
			logger.error("Exception", e);
		}
	}

}
