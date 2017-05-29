package jpa.service.external;

import jpa.constant.Constants;
import jpa.service.common.EmailVariableService;
import jpa.spring.util.SpringUtil;
import jpa.util.JpaUtil;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("subscriberNameResolver")
@Transactional(propagation=Propagation.REQUIRED)
public class SubscriberNameResolver implements VariableResolver,java.io.Serializable {
	private static final long serialVersionUID = 2958446223435763676L;
	static final Logger logger = Logger.getLogger(SubscriberNameResolver.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailVariableService dao;
	
	public String process(int addrId) {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		String query = "SELECT CONCAT(c.firstName,' ',c.lastName) as ResultStr ";
		if (Constants.isDerbyDatabase(JpaUtil.getDBProductName())) {
			query = "SELECT (c.firstName || ' ' || c.lastName) as ResultStr ";
		}
		
		query += " FROM subscriber_data c, email_address e " +
				" where e.row_id=c.EmailAddrRowId and e.row_id=?1";
		
		String result = dao.getByQuery(query, addrId);
		
		return result;
	}
	
	public static void main(String[] args) {
		VariableResolver resolver =SpringUtil.getAppContext().getBean(SubscriberNameResolver.class);
		try {
			String name = resolver.process(2);
			System.err.println("Subscriber name: " + name);
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}
}
