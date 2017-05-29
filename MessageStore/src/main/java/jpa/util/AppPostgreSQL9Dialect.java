package jpa.util;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;

public class AppPostgreSQL9Dialect extends PostgreSQL9Dialect {

	public AppPostgreSQL9Dialect() {
		super();
		
		/**
         * Function to evaluate regexp in PostgreSQL
         */
		// case insensitive
        registerFunction("regexp", new SQLFunctionTemplate(org.hibernate.type.BooleanType.INSTANCE, "?1 ~* ?2"));
        // case sensitive
        registerFunction("regexp_cs", new SQLFunctionTemplate(org.hibernate.type.BooleanType.INSTANCE, "?1 ~ ?2"));
	}
}
