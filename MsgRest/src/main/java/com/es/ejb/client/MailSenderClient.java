package com.es.ejb.client;

import javax.naming.Context;
import javax.naming.NamingException;

import jpa.constant.Constants;
import jpa.message.MessageBean;
import jpa.message.MessageBeanUtil;
import jpa.model.EmailAddress;
import jpa.util.FileUtil;
import jpa.util.PrintUtil;

import org.apache.log4j.Logger;

import com.es.ejb.mailsender.MailSenderRemote;
import com.es.tomee.util.TomeeCtxUtil;

public class MailSenderClient {
	static Logger logger = Logger.getLogger(MailSenderClient.class);
	
	public static void main(String[] args) {
		try {
			MailSenderClient client = new MailSenderClient();
			client.testMailSender();
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
	
	void testMailSender() {
		MailSenderRemote sender = null;
		Context ctx = null;
		try {
			ctx = TomeeCtxUtil.getRemoteContext();
			TomeeCtxUtil.listContext(ctx, "");
			sender = (MailSenderRemote) ctx.lookup("MailSenderRemote");
		}
		catch (NamingException e) {
			logger.error("NamingException", e);
			return;
		}

		// test EJB remote access
		logger.info("MailSenderRemote instance: " + sender);
		EmailAddress ea = sender.findByAddress("test@test.com");
		logger.info(PrintUtil.prettyPrint(ea, 1));
		
		String filePath = "bouncedmails";
		String fileName = "BouncedMail_1.txt";
		try {
			byte[] mailStream = FileUtil.loadFromFile(filePath, fileName);
			MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
			msgBean.setSenderId(Constants.DEFAULT_SENDER_ID);
			sender.send(msgBean);
		}
		catch (Exception te) {
			logger.error("Exception caught", te);
		}
	}
}
