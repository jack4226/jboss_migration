package com.es.ejb.tracking;

import java.util.List;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;
import jpa.service.common.SubscriptionService;
import jpa.service.maillist.BroadcastMessageService;
import jpa.service.maillist.BroadcastTrackingService;
import jpa.spring.util.SpringUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Stateless (name="SbsrTracking", mappedName = "ejb/SbsrTracking")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(SbsrTrackingRemote.class)
@Local(SbsrTrackingLocal.class)
@WebService (portName = "SbsrTracking", serviceName = "SbsrTrackingService", targetNamespace = "http://com.es.ws.sbsrtracking/wsdl")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public class SbsrTracking implements SbsrTrackingLocal, SbsrTrackingRemote, SbsrTrackingWs {
	protected static final Logger logger = LogManager.getLogger(SbsrTracking.class);
	
	@Resource
	SessionContext context;
	
	private SubscriptionService subService;
	private BroadcastTrackingService bcstTrkService;
	private BroadcastMessageService bcstMsgService;
	
	public SbsrTracking() {
		subService = SpringUtil.getAppContext().getBean(SubscriptionService.class);
		bcstTrkService = SpringUtil.getAppContext().getBean(BroadcastTrackingService.class);
		bcstMsgService = SpringUtil.getAppContext().getBean(BroadcastMessageService.class);
	}
	
	@WebMethod
	@Override
	public int updateOpenCount(int trkRowId) {
		logger.info("in updateOpenCount() - trkRowId: " + trkRowId);
		return bcstTrkService.updateOpenCount(trkRowId);
	}

	@WebMethod
	@Override
	public int updateClickCount(int trkRowId) {
		logger.info("in updateClickCount() - trkRowId: " + trkRowId);
		return bcstTrkService.updateClickCount(trkRowId);
	}

	@Override
	public int updateSentCount(int trkRowId) {
		return bcstTrkService.updateSentCount(trkRowId);
	}
	
	@Override
	public int updateOpenCount(int emailAddrRowId, String listId) {
		return subService.updateOpenCount(emailAddrRowId, listId);
	}

	@Override
	public int updateClickCount(int emailAddrRowId, String listId) {
		return subService.updateClickCount(emailAddrRowId, listId);
	}

	@Override
	public List<BroadcastMessage> getByMailingListId(String listId) {
		return bcstMsgService.getByMailingListId(listId);
	}
	
	@Override
	public List<BroadcastTracking> getByBroadcastMessageRowId(int bcstMsgRowId) {
		return bcstTrkService.getByBroadcastMessageRowId(bcstMsgRowId);
	}

	@WebMethod
	@Override
	public int updateMsgOpenCount(int emailAddrRowId, String listId) {
		logger.info("in updateMsgOpenCount() - emailAddrId/listId: " + emailAddrRowId + "/" + listId);
		return updateOpenCount(emailAddrRowId, listId);
	}

	@WebMethod
	@Override
	public int updateMsgClickCount(int emailAddrRowId, String listId) {
		logger.info("in updateMsgClickCount() - emailAddrId/listId: " + emailAddrRowId + "/" + listId);
		return updateClickCount(emailAddrRowId, listId);
	}
}
