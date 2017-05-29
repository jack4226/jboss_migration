package jpa.dataloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import jpa.constant.Constants;
import jpa.spring.util.SpringUtil;
import jpa.util.FileUtil;
import jpa.util.JpaUtil;
import jpa.util.StringUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

public class AlterConstraints {
	static final Logger logger = Logger.getLogger(AlterConstraints.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	static final String MySQLFileName = "CascadeOnDeleteMySQL.sql";
	static final String PgSQLFileName = "CascadeOnDeletePgSQL.sql";
	static final String DerbyFileName = "CascadeOnDeleteDerby.sql";

	public static void main(String[] args) {
		AlterConstraints alter = new AlterConstraints();
		try {
			alter.executeQueries();
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			System.exit(1);
		}
		System.exit(0);
	}
	
	void executeQueries() {
		String SQLFileName;
		String db_name = JpaUtil.getDBProductName();
		if (Constants.isPgSQLDatabase(db_name)) {
			SQLFileName = PgSQLFileName;
		}
		else if (Constants.isMySQLDatabase(db_name)) {
			SQLFileName = MySQLFileName;
		}
		else if (Constants.isDerbyDatabase(db_name)) {
			SQLFileName = DerbyFileName;
		}
		else {
			throw new RuntimeException("Unsupported Database product: " + db_name);
		}
		List<String> queries = loadQueries(SQLFileName);
		DataSource ds = (DataSource) SpringUtil.getAppContext().getBean("msgDataSource");
		JdbcTemplate jdbc = new JdbcTemplate(ds);
		int[] results = jdbc.batchUpdate(queries.toArray(new String[] {}));
		logger.info("Queries executed: " + Arrays.asList(ArrayUtils.toObject(results)));
	}
	
	private List<String> loadQueries(String sqlFileName) {
		List<String> queries = FileUtil.loadFromTextFile("META-INF", sqlFileName);
		for (String query : queries) {
			logger.info("Query loaded: "+ query);
		}
		List<String> queryList = new ArrayList<String>();
		for (String query : queries) {
			query = StringUtils.trim(query);
			if (StringUtils.isBlank(query)) {
				continue;
			}
			if (StringUtils.startsWith(query, "--")) {
				continue;
			}
			if (StringUtils.endsWith(query, "--hibernate")) {
				if (JpaUtil.isHibernate()) {
					query = StringUtil.removeStringLast(query, "--hibernate").trim();
				}
				else {
					continue;
				}
			}
			if (StringUtils.endsWith(query, "--eclipselink")) {
				if (JpaUtil.isEclipseLink()) {
					query = StringUtil.removeStringLast(query, "--eclipselink").trim();
				}
				else {
					continue;
				}
			}
			if (StringUtils.endsWith(query, ";")) {
				query = StringUtil.removeStringLast(query, ";");
			}
			queryList.add(query);
			logger.info("Query to execute: "+ query);
		}
		return queryList;
	}
}
