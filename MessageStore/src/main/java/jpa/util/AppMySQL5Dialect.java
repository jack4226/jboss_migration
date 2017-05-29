package jpa.util;

import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;

public class AppMySQL5Dialect extends MySQL5Dialect {
    public AppMySQL5Dialect() {
        super();
        /**
         * Function to evaluate regexp in MySQL
         */
        registerFunction("regexp_int", new SQLFunctionTemplate(org.hibernate.type.IntegerType.INSTANCE, "?1 REGEXP ?2"));
        registerFunction("regexp", new SQLFunctionTemplate(org.hibernate.type.StringType.INSTANCE, "?1 REGEXP ?2"));
        
        // For MySQL version 5.6
        registerColumnType(java.sql.Types.BOOLEAN, "bit");
        // MySQL types: TIME(3), DATETIME(3), TIMESTAMP(6)
        registerColumnType(java.sql.Types.TIMESTAMP, "timestamp(3)");
        registerColumnType(java.sql.Types.TIME, "time(3)");
        registerColumnType(java.sql.Types.DATE, "datetime(3)");
    }
}
