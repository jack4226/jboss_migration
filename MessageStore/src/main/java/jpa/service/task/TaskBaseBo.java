package jpa.service.task;

import java.io.IOException;

import javax.mail.MessagingException;

import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.MessageContext;

public interface TaskBaseBo {

	static final String LF = System.getProperty("line.separator", "\n");
	
	public Object process(MessageContext messageCtx)
			throws DataValidationException, MessagingException, IOException, TemplateException;
}
