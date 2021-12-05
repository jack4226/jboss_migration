package jpa.dataloader;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import jpa.constant.Constants;
import jpa.spring.util.SpringUtil;
import jpa.util.JpaUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateMysqlDatabase {
	static final Logger logger = LogManager.getLogger(CreateMysqlDatabase.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private static String DB_NAME = "emaildb";
	private String hostName = null;
	private String rootPassword = null;
	
	public static void main(String[] args) {
		CreateMysqlDatabase db = new CreateMysqlDatabase();
		AlterConstraints alter = new AlterConstraints();
		try {
			if (db.createDatabase()) {
				new DataLoader().loadAllTables();
				alter.executeQueries();
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			System.exit(1);
		}
		System.exit(0);
	}
	
	/**
	 * create "message" database and load initial data.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public boolean createDatabase() throws ClassNotFoundException, SQLException {
		logger.info("createDatabase() - Entering...");
		if (Constants.isDerbyDatabase(JpaUtil.getDBProductName())) {
			logger.info("Running with Derby database, Please run DataLoader instead...");
			return false;
		}
		else if (Constants.isPgSQLDatabase(JpaUtil.getDBProductName())) {
			logger.info("Running with PgSQL database, Please read postgresql.txt then run DataLoader...");
			return false;
		}
		boolean dropDb = false;
		if (isEmailDatabaseExist()) {
			if (overrideCurrentDB() == false) {
				logger.warn("createDatabase() - " + DB_NAME + " already exist, exiting...");
				return false;
			}
			else {
				dropDb = true;
			}
		}
		rootPassword = getRootPassword();
		Connection con = null;
		try {
			con = getInitConnection();
			if (dropDb) {
				dropDB(con);
			}
			createDB(con);
			return true;
		}
		finally {
			closeConnection(con);
		}
	}
	
	private String getHostName(String _url) {
		logger.info("MySQL URL from metadata: " + _url);
		int start = _url.indexOf("//");
		int end = _url.indexOf(":", start);
		if (end < 0) {
			end = _url.indexOf("/", start);
		}
		return _url.substring(start + 2, end);
	}
	
	/*
	 * prompt user to enter mysql root password
	 */
    private String getRootPassword() {
    	final JPasswordField jpf = new JPasswordField();
		JOptionPane jop = new JOptionPane(jpf, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		JDialog dialog = jop.createDialog(null, "Enter MySql root password:");
		dialog.requestFocus();
		dialog.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				jpf.requestFocusInWindow();
			}
		});
		dialog.setVisible(true);
		int result = (Integer) jop.getValue();
		dialog.dispose();
		char[] password = null;
		if (result == JOptionPane.OK_OPTION) {
			password = jpf.getPassword();
			return new String(password);
		}
		return null;
    }
    
    private boolean overrideCurrentDB() {
	    String message = "Database (" + DB_NAME + ") already created, override it?";
	    boolean debugDialog = false;
	    if (debugDialog) {
		    // Modal dialog with OK button (informational)
		    JOptionPane.showMessageDialog(null, message);
		    logger.info("overrideCurrentDB() - OK clicked");
	    }
	    // Modal dialog with yes/no button
	    int answer = JOptionPane.showConfirmDialog(null, message, "Emailsphere.com",
				JOptionPane.OK_CANCEL_OPTION);
	    if (answer == JOptionPane.YES_OPTION) {
	        // User clicked YES.
	    	logger.info("overrideCurrentDB() - Yes clicked");
	    	int answer2 = JOptionPane.showConfirmDialog(null,
					"The entire database will be erased, are you sure?", "Emailsphere.com",
					JOptionPane.OK_CANCEL_OPTION);
	    	if (answer2 == JOptionPane.YES_OPTION) {
	    		logger.info("overrideCurrentDB() - Override confirmed");
	    		return true;
	    	}
	    } else if (answer == JOptionPane.NO_OPTION) {
	        // User clicked NO.
	    	logger.info("overrideCurrentDB() - No clicked");
	    }
	    else if (answer == JOptionPane.CANCEL_OPTION) {
	    	logger.info("overrideCurrentDB() - Cancel clicked");
	    }
	    
	    if (debugDialog) {
		    // Modal dialog with OK/cancel and a text field
		    String text = JOptionPane.showInputDialog(null, message);
		    if (text == null) {
		        // User clicked cancel
		    	logger.info("overrideCurrentDB() - Cancel clicked");
		    	//return false;
		    }
		    else {
		    	logger.info("overrideCurrentDB() - OK clicked");
		    	return true;
		    }
	    }
	    return false;
    }
    
	private Connection getInitConnection() throws SQLException {
		DataSource ds = (DataSource) SpringUtil.getAppContext().getBean("initDataSource");
		Connection con = ds.getConnection("root", rootPassword);
		return con;
	}

	private boolean isEmailDatabaseExist() {
		DataSource ds = (DataSource) SpringUtil.getAppContext().getBean("msgDataSource");
		Connection con = null;
		try {
			con = ds.getConnection();
			String mysqlUrl = con.getMetaData().getURL();
			hostName = getHostName(mysqlUrl);
			logger.info("HostName retrieved from URL: " + hostName);
			return true;
		}
		catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
			// ignored
		}
		finally {
			closeConnection(con);
		}
		return false;
	}
	
	private void createDB(Connection con) throws SQLException {
		logger.info("createDB - Creating " + DB_NAME + " on " + hostName + "...");
		Statement stmt = con.createStatement();
		stmt.executeUpdate("CREATE DATABASE " + DB_NAME);
		stmt.executeUpdate(
				"GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,CREATE ROUTINE,DROP," +
				" CREATE TEMPORARY TABLES,REFERENCES,INDEX,ALTER,ALTER ROUTINE,EXECUTE " +
				" ON " + DB_NAME + ".* TO 'email'@'%' " +
				" IDENTIFIED BY 'email';");
		stmt.executeUpdate(
				"GRANT GRANT OPTION " +
				" ON " + DB_NAME + ".* TO 'email'@'%' " +
				" IDENTIFIED BY 'email';");
		stmt.executeUpdate(
				"GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,CREATE ROUTINE,DROP," +
				" CREATE TEMPORARY TABLES,REFERENCES,INDEX,ALTER,ALTER ROUTINE,EXECUTE " +
				" ON " + DB_NAME + ".* TO 'email'@'localhost' " +
				" IDENTIFIED BY 'email';");
		stmt.executeUpdate(
				"GRANT GRANT OPTION " +
				" ON " + DB_NAME + ".* TO 'email'@'localhost' " +
				" IDENTIFIED BY 'email';");
//		stmt.executeUpdate(
//	          "GRANT ALL " +
//	          "ON " + DB_NAME +".* TO 'email'@'%' " +
//	          "IDENTIFIED BY 'email';");
		// Received: Access denied for user 'root'@'%' to database 'message'
		stmt.executeUpdate("FLUSH PRIVILEGES");
		stmt.close();
		logger.info("createDB - " + DB_NAME + " created on " + hostName + " for email");
	}
	
	void dropDB(Connection con) throws SQLException {
		logger.info("dropDB() - Dropping " + DB_NAME + " on " + hostName + "...");
		Statement stmt = con.createStatement();
//	    stmt.executeUpdate(
//	              "REVOKE ALL PRIVILEGES ON *.* " +
//	              "FROM 'email'@'" + hostName + "'");
//	    stmt.executeUpdate(
//	              "REVOKE GRANT OPTION ON *.* " +
//	              "FROM 'email'@'" + hostName + "'");
//	    stmt.executeUpdate(
//	              "DELETE FROM mysql.user WHERE " +
//	              "User='email' and Host='" + hostName + "'");
//	    stmt.executeUpdate("FLUSH PRIVILEGES");
	    //Delete the database
        stmt.executeUpdate("DROP DATABASE " + DB_NAME);
        stmt.close();
        logger.info("dropDB() - " + DB_NAME + " dropped from " + hostName);
	}
	
	private void closeConnection(Connection con) {
		try {
			if (con != null)
				con.close();
		}
		catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
		}
	}

}
