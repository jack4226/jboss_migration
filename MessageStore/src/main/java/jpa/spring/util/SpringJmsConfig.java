package jpa.spring.util;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import jpa.jbatch.queue.MailSenderListener;
import jpa.jbatch.queue.RuleEngineListener;
import jpa.tomee.util.TomeeCtxUtil;

@Configuration
@ComponentScan(basePackages = {"jpa.jbatch.queue"})
@EnableJms
public class SpringJmsConfig implements JmsListenerConfigurer {
	protected final static Logger logger = Logger.getLogger(SpringJmsConfig.class);

	private @Value("${test.Queue}") String listenerQueueName;
	
	private @Value("${listener.Queues}") String[] listenerQueues;
	
	private @Value("${mailReaderOutput.Queue}") String mailReaderOutputQueueName;
	private @Value("${ruleEngineError.Queue}") String ruleEngineErrorQueueName;
	private @Value("${mailSenderInput.Queue}") String mailSenderInputQueueName;
	private @Value("${customerCareInput.Queue}") String customerCareInputQueueName;
	private @Value("${rmaRequestInput.Queue}") String rmaRequestInputQueueName;
	
	@Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        try {
			factory.setConnectionFactory(connectionFactory());
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw new java.lang.IllegalStateException("NamingException caught", e);
		}
        factory.setDestinationResolver(destinationResolver());
        factory.setConcurrency("3-10");
        return factory;
    }
	
	@Bean
	@Scope(value="prototype")
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        //jmsTemplate.setDefaultDestination(new ActiveMQQueue("jms.queue"));
        try {
			jmsTemplate.setConnectionFactory(connectionFactory());
			jmsTemplate.setDestinationResolver(destinationResolver());
			jmsTemplate.setReceiveTimeout(10000L); // 10 seconds
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw new java.lang.IllegalStateException("NamingException caught", e);
		}
        return jmsTemplate;
    }
	
	@Bean
	public jpa.jbatch.queue.JmsListener jmsListener() {
		return new jpa.jbatch.queue.JmsListener();
	}
	
	@Bean(initMethod="initialize", destroyMethod="destroy")
    public DefaultMessageListenerContainer jmsListenerContainer() {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        try {
			container.setConnectionFactory(connectionFactory());
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw new java.lang.IllegalStateException("NamingException caught", e);
		}
        String queueName = "testQueue";
        if (StringUtils.isNotBlank(listenerQueueName)) {
        	queueName = listenerQueueName;
        }
        container.setDestination(new ActiveMQQueue(queueName));
        //container.setDestinationResolver(destinationResolver());
        // set Listener properties
        container.setAutoStartup(false);
        container.setConcurrency("1-4");
        container.setMaxConcurrentConsumers(4);
        //container.setErrorHandler(null); // TODO add error handler
        //container.setMessageSelector(null); // XXX implement
        container.setSessionTransacted(true);
        // end of properties
        container.setMessageListener(jmsListener());
        return container;
    }
	
	@Bean
	//@org.springframework.context.annotation.Scope("prototype")
	public ConnectionFactory connectionFactory() throws NamingException {
		Context ctx = TomeeCtxUtil.getActiveMQContext(new String[] {});
		ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		logger.info("ConnectionFactory instance: " + cf);
		return cf;
	}
	
	DestinationResolver destinationResolver() {
		DestinationResolver resolver = new DynamicDestinationResolver();
		//resolver = new JndiDestinationResolver();
		return resolver;
	}
	
	@Bean
	public MailSenderListener mailSenderListener() {
		return new MailSenderListener();
	}
	
	@Bean
	public RuleEngineListener ruleEngineListener() {
		return new RuleEngineListener();
	}
	
	@Bean(initMethod="initialize", destroyMethod="destroy")
    public DefaultMessageListenerContainer ruleEngineJmsListener() {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        try {
			container.setConnectionFactory(connectionFactory());
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw new java.lang.IllegalStateException("NamingException caught", e);
		}
        container.setDestination(new ActiveMQQueue(mailReaderOutputQueueName));
        // set Listener properties
        container.setAutoStartup(true);
        container.setConcurrency("1-2");
        container.setMaxConcurrentConsumers(2);
        //container.setErrorHandler(null); // TODO add error handler
        //container.setMessageSelector(null); // XXX implement
        container.setSessionTransacted(true);
        container.setSessionAcknowledgeMode(Session.SESSION_TRANSACTED);
        // end of properties
        container.setMessageListener(ruleEngineListener());
        return container;
    }
	
	@Bean(initMethod="initialize", destroyMethod="destroy")
    public DefaultMessageListenerContainer mailSenderJmsListener() {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        try {
			container.setConnectionFactory(connectionFactory());
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw new java.lang.IllegalStateException("NamingException caught", e);
		}
        container.setDestination(new ActiveMQQueue(mailSenderInputQueueName));
        // set Listener properties
        container.setAutoStartup(true);
        container.setConcurrency("1-4");
        container.setMaxConcurrentConsumers(4);
        //container.setErrorHandler(null); // TODO add error handler
        container.setSessionTransacted(true);
        // end of properties
        container.setMessageListener(mailSenderListener());
        return container;
    }
	
    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
    	registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    	if (listenerQueues != null && listenerQueues.length > 0) {
	    	for (String queueName : listenerQueues) {
	    		logger.info("Register JMS endpoint for queue: " + queueName);
	            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
	            endpoint.setId(queueName + "_id");
	            endpoint.setDestination(queueName);
	            endpoint.setConcurrency("1-4");
	            if (StringUtils.equals(queueName, ruleEngineErrorQueueName)) {
	            	endpoint.setMessageListener(message -> {
		                // TODO implement
	            		logger.info("Received from " + queueName + ": " + message);
		            });
	            }
	            else if (StringUtils.equals(queueName, customerCareInputQueueName)) {
	            	endpoint.setMessageListener(message -> {
		                // TODO implement
		            	logger.info("Received from " + queueName + ": " + message);
		            });
	            }
	            else if (StringUtils.equals(queueName, rmaRequestInputQueueName)) {
	            	endpoint.setMessageListener(message -> {
		                // TODO implement
	            		logger.info("Received from " + queueName + ": " + message);
		            });
	            }
	            else {
		            endpoint.setMessageListener(message -> {
		            	// default 
		            	logger.info("Received from " + queueName + ": " + message);
		            });
	            }
	            registrar.registerEndpoint(endpoint);
	    	}
    	}
    }
    
    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        //factory.setValidator(myValidator()); // TODO implement
        //factory.setMessageConverter(null); // TODO implement
        return factory;
    }
    
}
