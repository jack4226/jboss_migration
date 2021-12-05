package com.es.ejb.mailinglist;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import jpa.service.maillist.BroadcastTrackingBo;
import jpa.service.maillist.MailingListBo;
import jpa.service.maillist.MailingListService;
import jpa.spring.util.SpringUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Session Bean implementation class MailingList
 */
@Stateless(mappedName = "ejb/MailingList")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(MailingListRemote.class)
@Local(MailingListLocal.class)
public class MailingList implements MailingListRemote, MailingListLocal {
	protected static final Logger logger = LogManager.getLogger(MailingList.class);
	@Resource
	SessionContext context;
	private MailingListBo mailingListBo;
	private MailingListService mlistService;
	private BroadcastTrackingBo bcstTrackingBo;
    /**
     * Default constructor. 
     */
    public MailingList() {
    	mailingListBo = SpringUtil.getAppContext().getBean(MailingListBo.class);
    	mlistService = SpringUtil.getAppContext().getBean(MailingListService.class);
    	bcstTrackingBo = SpringUtil.getAppContext().getBean(BroadcastTrackingBo.class);
    }
    
    @Override
    public List<jpa.model.MailingList> getActiveLists() {
    	List<jpa.model.MailingList> list = mlistService.getAll(true);
    	return list;
    }

	@Override
	public jpa.model.MailingList getByListId(String listId) {
		jpa.model.MailingList ml = mlistService.getByListId(listId);
		return ml;
	}
	
	@Override
	public jpa.model.MailingList getByListAddress(String address) {
		jpa.model.MailingList ml = mlistService.getByListAddress(address);
		return ml;
	}

	@Override
	public void update(jpa.model.MailingList mailingList) {
		mlistService.update(mailingList);
	}

	@Override
	public int sendMail(String toAddr, Map<String, String> variables, String templateId) {
    	try {
			int mailsSent = mailingListBo.send(toAddr, variables, templateId);
			return mailsSent;
    	}
    	catch (Exception e) {
    		throw new EJBException("in sendMail(...) method", e);
    	}
	}

    @Override
	public int broadcast(String templateId) {
    	try {
			int mailsSent = mailingListBo.broadcast(templateId);
			return mailsSent;
	    }
    	catch (Exception e) {
    		throw new EJBException("in broadcast(templateId) method", e);
    	}
    }

    @Override
	public int broadcast(String templateId, String listId) {
    	try {
			int mailsSent = mailingListBo.broadcast(templateId, listId);
			return mailsSent;
	    }
    	catch (Exception e) {
    		throw new EJBException("in broadcast(templateId, listId) method", e);
    	}
	}

    @Override
    public void removeFromList(int bcstTrkRowId) {
    	bcstTrackingBo.removeFromList(bcstTrkRowId);
    }

}
