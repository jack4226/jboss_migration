package jpa.spring.util;

import java.util.Properties;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import jpa.message.SimpleEmailSender;
import jpa.service.msgout.SmtpConnection;

@Configuration
@ComponentScan(basePackages = {"jpa.service"})
//@org.springframework.context.annotation.Import(SpringJmsConfig.class)
@ImportResource({"classpath:/properties-config.xml", "classpath:/spring-jpa-config.xml"})
public class SpringAppConfig {
	protected final static Logger logger = LogManager.getLogger(SpringAppConfig.class);
	
	@Bean
	public SimpleEmailSender simpleEmailSender() {
		Properties smtpProps = new Properties();
		smtpProps.setProperty("smtphost","localhost");
		smtpProps.setProperty("smtpport", "-1");
		smtpProps.setProperty("userid", "jwang");
		smtpProps.setProperty("password", "jwang");
		smtpProps.setProperty("persistent", "no");
		try {
			return new SimpleEmailSender(smtpProps);
		} catch (NumberFormatException | MessagingException e) {
			throw new java.lang.IllegalStateException(e);
		}
	}
	
	@Bean
	@Scope(value="thread") // custom "thread" scope
	public LobHandler lobHandler() {
		return new DefaultLobHandler();
	}
	
	@Bean
	@Scope(value="prototype")
	public SmtpConnection smtpConnection() {
		Properties props = new Properties();
		props.setProperty("smtphost", "localhost");
		props.setProperty("smtpport", "-1");
		props.setProperty("default", "localhost");
		props.setProperty("use_ssl", "no");
		props.setProperty("userid", "jwang");
		props.setProperty("password", "jwang");
		props.setProperty("persistence", "yes");
		props.setProperty("server_type", "smtp");
		props.setProperty("threads", "2");
		props.setProperty("retry", "10");
		props.setProperty("freq", "5");
		props.setProperty("alert_after", "15");
		props.setProperty("alert_level", "error");
		props.setProperty("message_count", "0");
		return new SmtpConnection(props);
	}

}
