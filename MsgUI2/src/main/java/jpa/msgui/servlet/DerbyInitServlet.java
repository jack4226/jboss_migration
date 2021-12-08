package jpa.msgui.servlet;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import jpa.constant.Constants;
import jpa.dataloader.DataLoader;
import jpa.msgui.util.SpringUtil;
import jpa.service.common.SenderDataService;
import jpa.util.EnvUtil;
import jpa.util.JpaUtil;
import jpa.util.PrintUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.es.ejb.subscriber.Subscriber;
import com.es.ejb.subscriber.SubscriberLocal;
import com.es.ejb.subscriber.SubscriberRemote;
import com.es.tomee.util.TomeeCtxUtil;

/**
 * Servlet implementation class DerbyInitServlet
 */
@WebServlet(name="DerbyInitServlet", urlPatterns="/DerbyInit/*", loadOnStartup=8)
public class DerbyInitServlet extends HttpServlet {
	private static final long serialVersionUID = 1810496150486989387L;
	static final Logger logger = LogManager.getLogger(DerbyInitServlet.class);
	
	@javax.ejb.EJB
	private Subscriber subscriber;
	
	@Resource 
	private javax.sql.DataSource msgdb_pool;
	
	@Resource(name="msgdb_pool")
	private  javax.sql.DataSource myDS;
	
    @Override
	public void init() throws ServletException {
		ServletContext ctx = getServletContext();
		logger.info("init() - ServerInfo: " + ctx.getServerInfo() + ", Context Path: " + ctx.getContextPath());
		
		EnvUtil.displayAllThreads();
		
		// test
		logger.info("msgdb_pool DataSource 1: " + PrintUtil.prettyPrint(msgdb_pool));
		logger.info("msgdb_pool DataSource 2: " + PrintUtil.prettyPrint(myDS));
		subscriber.getResources();
		
		try {
			Context context = TomeeCtxUtil.getLocalContext();
			SubscriberRemote subr_rmt = (SubscriberRemote) context.lookup("ejb/SubscriberBean");
			logger.info("Context Subscriber 1: " + subr_rmt);
			
			SubscriberLocal subr_lcl = (SubscriberLocal) context.lookup("subscriberBeanLocal");
			logger.info("Context Subscriber 2: " + subr_lcl);

			TomeeCtxUtil.listContext(context, "");
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		// end test
		
		if (Constants.isDerbyDatabase(JpaUtil.getDBProductName())) {
			SenderDataService sender = SpringUtil.getWebAppContext(ctx).getBean(SenderDataService.class);
			if (sender.getAll().isEmpty()) {
				logger.warn("Initializing Derby database and load all the tables...");
				// load initial data to tables
				DataLoader loader = new DataLoader();
				try {
					loader.loadAllTables();
				}
				catch (Exception e) {
					logger.error("Failed to load data to tables", e);
				}
			}
		}
	}
}
