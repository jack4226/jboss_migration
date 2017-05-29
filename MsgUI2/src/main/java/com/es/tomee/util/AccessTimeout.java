package com.es.tomee.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessTimeout {
	/*
	 * A value of 
	 *  > 0 indicates a timeout value in the units specified by the unit element.
	 *  0 means concurrent access is not permitted.
	 *  -1 indicates that the request will block indefinitely until forward progress it can proceed.
	 */
	long value();
	TimeUnit unit() default TimeUnit.MILLISECONDS;
}
