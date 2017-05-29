package jpa.test.msgin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

import jpa.constant.MsgStatusCode;
import jpa.data.preload.MailInboxEnum;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.MessageBeanUtil;
import jpa.message.MessageContext;
import jpa.model.MailInbox;
import jpa.model.MailInboxPK;
import jpa.model.msg.MessageInbox;
import jpa.service.common.EmailAddressService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgin.MailInboxService;
import jpa.service.msgin.MailProcessorBo;
import jpa.spring.util.BoTestBase;
import jpa.util.JpaUtil;
import jpa.util.TestUtil;

public class MailProcessorBoTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = Logger.getLogger(MailProcessorBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailProcessorBo mailProcBo;
	@Autowired
	private MailInboxService mailboxService;
	@Autowired
	private MessageInboxService inboxService;
	@Autowired
	private EmailAddressService emailService;
	
	@Autowired
	EntityManager em;
	
	private static List<Integer> rowIds_1 = null;
	private static List<Integer> rowIds_2 = null;
	
	@BeforeTransaction
	public void prepare() {
		if (rowIds_1 == null) {
			try {
				List<Integer> rowids = persistRecord("BouncedMail_1.txt");
				assertTrue(rowids.size()>0);
				rowIds_1 = rowids;
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				fail();
			}
		}
		if (rowIds_2 == null) {
			try {
				List<Integer> rowids = persistRecord("BouncedMail_2.txt");
				assertTrue(rowids.size()>0);
				rowIds_2 = rowids;
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				fail();
			}
		}
		assertNotNull(rowIds_1);
		assertNotNull(rowIds_2);
		assertFalse(rowIds_1.get(0).equals(rowIds_2.get(0)));
	}
	
	private List<Integer> persistRecord(String... fileNames)
			throws DataValidationException, MessagingException, IOException,
			TemplateException {
		javax.mail.Message[] messages = new javax.mail.Message[fileNames.length];
		for (int i=0; i<fileNames.length; i++) {
			String fileName = fileNames[i];
			javax.mail.Message message = readFromFile(fileName);
			messages[i] = message;
		}
		
		MailInboxPK pk = new MailInboxPK(MailInboxEnum.BOUNCE.getUserId(), MailInboxEnum.BOUNCE.getHostName());
		MailInbox mailbox = mailboxService.getByPrimaryKey(pk);

		MessageContext ctx = new MessageContext(messages, mailbox);
		try {
			mailProcBo.process(ctx);
		}
		catch (Exception e) {
			if (JpaUtil.isOptimisticLockException(e)) {
				logger.error("OptimisticLockException caught", e);
			}
			throw e;
		}
		return ctx.getRowIds();
	}
	
	@Test
	public void testMailProcessorBo1() throws MessagingException, IOException {
		testBouncedMail(rowIds_1, 1);
	}

	@Test
	//@org.junit.Ignore
	public void testMailProcessorBo2() throws MessagingException, IOException {
		testBouncedMail(rowIds_2, 2);
	}

	@AfterTransaction
	public void cleanup() {
	}

	private void testBouncedMail(List<Integer> rowIds, int fileNbr) throws MessagingException, IOException {
		try {
			if (fileNbr == 1) {
				logger.info("row_id_1 = " + rowIds.get(0));
				MessageInbox inbox = TestUtil.verifyBouncedMail_1(rowIds.get(0), inboxService, emailService);
				assertTrue(MsgStatusCode.CLOSED.getValue().equals(inbox.getStatusId()));
			}
			else if (fileNbr == 2) {
				logger.info("row_id_2 = " + rowIds.get(0));
				MessageInbox inbox = TestUtil.verifyBouncedMail_2(rowIds.get(0), inboxService, emailService);
				assertTrue(MsgStatusCode.CLOSED.getValue().equals(inbox.getStatusId()));
				if (rowIds.size()==2) { // Message Delivery Status
					logger.info("row_id_2 for DeliveryStatus = " + rowIds.get(1));
					TestUtil.verifyDeliveryStatus4BounceMail_2(rowIds.get(1), inboxService);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}

	private javax.mail.Message readFromFile(String fileName) throws MessagingException {
		byte[] mailStream = TestUtil.loadFromSamples(fileName);
		javax.mail.Message message = MessageBeanUtil.createMimeMessage(mailStream);
		return message;
	}	
}
