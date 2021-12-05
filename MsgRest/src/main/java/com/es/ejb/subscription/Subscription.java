package com.es.ejb.subscription;

import java.util.List;
import java.util.Optional;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jpa.service.common.SubscriptionService;
import jpa.spring.util.SpringUtil;

@Stateless (name="Subscription", mappedName = "ejb/Subscription")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(SubscriptionRemote.class)
@Local(SubscriptionLocal.class)
@WebService (portName = "Subscription", serviceName = "SubscriptionService", targetNamespace = "http://com.es.ws.subscription/wsdl")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public class Subscription implements SubscriptionLocal, SubscriptionRemote, SubscriptionWs {

	protected static final Logger logger = LogManager.getLogger(Subscription.class);
	
	@Resource
	SessionContext context;
	
	private SubscriptionService subService;
	
	public Subscription() {
		subService = SpringUtil.getAppContext().getBean(SubscriptionService.class);
	}
	
	@WebMethod
	@Override
	public List<jpa.model.Subscription> getByListId(String listId) {
		logger.info("in getByListId() - listId: " + listId);
		return subService.getByListId(listId);
	}
	
	@WebMethod
	@Override
	public List<jpa.model.Subscription> getByAddress(String address) {
		logger.info("in getByAddress() - address: " + address);
		return subService.getByAddress(address);
	}
	
	@WebMethod
	@Override
	public jpa.model.Subscription getByUniqueKey(int emailAddrRowId, String listId) {
		logger.info("in getByUniqueKey() - emailAddrRowId/listId: " + emailAddrRowId + "/" + listId);
		return subService.getByUniqueKey(emailAddrRowId, listId);
	}
	
	@WebMethod
	@Override
	public jpa.model.Subscription getByAddressAndListId(String address, String listId) {
		logger.info("in getByAddressAndListId() - address/listId: " + address + "/" + listId);
		return subService.getByAddressAndListId(address, listId);
	}
	
	@WebMethod
	@Override
	public jpa.model.Subscription subscribe(String address, String listId) {
		logger.info("in subscribe() - address/listId: " + address + "/" + listId);
		return subService.subscribe(address, listId);
	}
	
	@WebMethod
	@Override
	public jpa.model.Subscription unsubscribe(String address, String listId) {
		logger.info("in unsubscribe() - address/listId: " + address + "/" + listId);
		return subService.unsubscribe(address, listId);
	}
	
	// local interface only
	@Override
	public jpa.model.Subscription getByRowId(int rowId) {
		logger.info("in getByRowId() - rowId: " + rowId);
		Optional<jpa.model.Subscription> sub = subService.getByRowId(rowId);
		return sub.isPresent()? sub.get() : null;
	}
	
	@Override
	public int updateOpenCount(int emailAddrRowId, String listId) {
		return subService.updateOpenCount(emailAddrRowId, listId);
	}

	@Override
	public int updateClickCount(int emailAddrRowId, String listId) {
		return subService.updateClickCount(emailAddrRowId, listId);
	}

}

