package jpa.spring.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableScheduling
//@EnableWebMvc
@ComponentScan(basePackages = {"jpa.jbatch.mail"})
public class SpringTaskConfig {

	@Bean
	public TaskExecutor taskExecuter() {
		ThreadPoolTaskExecutor task = new ThreadPoolTaskExecutor();
		task.setCorePoolSize(5);
		task.setMaxPoolSize(10);
		task.setQueueCapacity(100);
		return task;
	}

}
