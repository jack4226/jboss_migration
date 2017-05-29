package jpa.test.msgdata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import jpa.constant.RuleDataName;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.spring.util.BoTestBase;
import jpa.util.TestUtil;

public class MessageBeanUtilTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MessageBeanUtilTest.class);
	static final String LF = System.getProperty("line.separator", "\n");

	@BeforeClass
	public static void MessageBeanutilPrepare() {
	}

	@Test
	public void testMessageBeanUtil() throws MessagingException, IOException {
		String fileName = "BouncedMail_1.txt";
		MessageBean msgBean = testReadFromFile(fileName);
		assertNotNull(msgBean);
		
		Message msg1 = MessageBeanUtil.createMimeMessage("samples/" + fileName);
		Message msg2 = MessageBeanUtil.createMimeMessage(msgBean);
		assertTrue(msg1.getSubject().equals(msg2.getSubject()));
		assertTrue(msg1.getFrom()[0].equals(msg2.getFrom()[0]));
		assertTrue("jackwng@gmail.com".equals(msg1.getAllRecipients()[0].toString()));
		assertTrue("support.hotline@jbatch.com".equals(msg2.getAllRecipients()[0].toString()));
		assertTrue(msg1.getContentType().startsWith("multipart/report;"));
		assertTrue(msg2.getContentType().startsWith("multipart/report;"));
		
		List<String> methodNameList = MessageBeanUtil.getMessageBeanMethodNames();
		StringBuffer sb = new StringBuffer();
		sb.append("========= MessageBean method name list ==========" + LF);
		for (int i=0; i<methodNameList.size(); i++) {
			sb.append(methodNameList.get(i) + LF);
		}
		sb.append("=========== End of method name list =============" + LF);
		logger.info(sb.toString());

		for (RuleDataName name : RuleDataName.values()) {
			Object obj = MessageBeanUtil.invokeMethod(msgBean, name.getValue());
			//System.out.println("Name: " + name.getValue());
			//if (obj!=null) {
			//	System.out.println("class: " + obj.getClass().getName() + ", value: " + obj);
			//}
			if ("From".equals(name.getValue())) {
				assertTrue("postmaster@synnex.com.au".equals(obj));
			}
			else if ("To".equals(name.getValue())) {
				assertTrue("support.hotline@jbatch.com".equals(obj));
			}
			else if ("Subject".equals(name.getValue())) {
				assertTrue("Delivery Status Notification (Failure)".equals(obj));
			}
			else if ("MimeType".equals(name.getValue())) {
				assertTrue("multipart/report".equals(obj));
			}
		}
	}

	private MessageBean testReadFromFile(String fileName) throws MessagingException, IOException {
		byte[] mailStream = TestUtil.loadFromSamples(fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
}
