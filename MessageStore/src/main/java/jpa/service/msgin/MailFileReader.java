package jpa.service.msgin;

import java.io.IOException;

import javax.mail.MessagingException;

import jpa.constant.CarrierCode;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.message.MessageContext;
import jpa.service.task.TaskSchedulerBo;
import jpa.spring.util.SpringUtil;
import jpa.util.FileUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailFileReader implements java.io.Serializable {
	private static final long serialVersionUID = -7542897465313801472L;
	final static String LF = System.getProperty("line.separator","\n");
	static final Logger logger = LogManager.getLogger(MailFileReader.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	public static void main(String[] args){
		String filePath = "bouncedmails";
		String fileName = "BouncedMail_1.txt";
		SpringUtil.beginTransaction();
		try {
			MailFileReader fReader = new MailFileReader();
			MessageBean msgBean = fReader.read(filePath, fileName);
			logger.info("Number of Attachments: " + msgBean.getAttachCount());
			logger.info("******************************");
			logger.info("MessageBean created:" + LF + msgBean);
			SpringUtil.commitTransaction();
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			SpringUtil.clearTransaction();
		}
		System.exit(0);
	}
	
	public MessageBean read(String filePath, String fileName) throws MessagingException,
			IOException, DataValidationException, TemplateException {
		MessageBean msgBean = readIntoMessageBean(filePath, fileName);
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		MessageParserBo parser = SpringUtil.getAppContext().getBean(MessageParserBo.class);
		msgBean.setRuleName(parser.parse(msgBean));
		TaskSchedulerBo taskBo = SpringUtil.getAppContext().getBean(TaskSchedulerBo.class);
		taskBo.scheduleTasks(new MessageContext(msgBean));
		return msgBean;
	}

	private MessageBean readIntoMessageBean(String filePath, String fileName)
			throws MessagingException, IOException {
		byte[] mailStream = FileUtil.loadFromFile(filePath, fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
}
