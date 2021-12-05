package com.es.ejb.subscriber;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import jpa.model.EmailAddress;
import jpa.model.SubscriberData;
import jpa.model.Subscription;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriberDataService;
import jpa.service.common.SubscriptionService;
import jpa.spring.util.SpringUtil;
import jpa.util.PrintUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.es.ejb.ws.vo.SubscriptionVo;
import com.es.tomee.util.TomeeCtxUtil;

/**
 * Session Bean implementation class Subscriber
 */
@Stateless (name="subscriberBean", mappedName = "ejb/SubscriberBean")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(SubscriberRemote.class)
@Local(SubscriberLocal.class)
@LocalBean
public class Subscriber implements SubscriberRemote, SubscriberLocal {
	protected static final Logger logger = LogManager.getLogger(Subscriber.class);
	@Resource
	SessionContext context;
	private SubscriberDataService subscriberDao;
	private SubscriptionService subscriptionDao;
	private EmailAddressService emailAddrDao;

	/**
     * Default constructor. 
     */
    public Subscriber() {
		subscriberDao = SpringUtil.getAppContext().getBean(SubscriberDataService.class);
		subscriptionDao = SpringUtil.getAppContext().getBean(SubscriptionService.class);
		emailAddrDao = SpringUtil.getAppContext().getBean(EmailAddressService.class);
    }

    public void getResources() {
		try {
			InitialContext initialContext = new InitialContext();
			DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/msgdb_pool");
			logger.info("in EJB - msgdb_pool 1: " + PrintUtil.prettyPrint(dataSource));
			TomeeCtxUtil.listContext(initialContext, "java:comp");
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		
		if (context != null) {
			// EJBContext will prepend "java:comp/env/" to the lookup name.
			DataSource dataSource = (DataSource) context.lookup("msgdb_pool");
			logger.info("in EJB - msgdb_pool 2: " + PrintUtil.prettyPrint(dataSource));
		}
    }
	
    @Override
    public List<SubscriberData> getAllSubscribers() {
    	logger.info("getAllSubscribers() - Entering...");
    	return subscriberDao.getAll();
    }
    
    @Override
    public SubscriberData getSubscriberById(String subrId) {
		SubscriberData vo = subscriberDao.getBySubscriberId(subrId);
		return vo;
	}

    @Override
	public SubscriberData getSubscriberByEmailAddress(String emailAddr) {
		EmailAddress emailAddrVo = emailAddrDao.findSertAddress(emailAddr);
		SubscriberData sbsrVo = subscriberDao.getByEmailAddress(emailAddrVo.getAddress());
		return sbsrVo;
	}

	@Override
	public List<SubscriptionVo> getSubscribedList(String emailAddr) {
		List<Subscription> sublist = subscriptionDao.getByAddress(emailAddr);
		List<SubscriptionVo> volist = new ArrayList<SubscriptionVo>();
		for (Subscription sub : sublist) {
			SubscriptionVo vo = new SubscriptionVo();
			try {
				BeanUtils.copyProperties(vo, sub);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to copy properties", e);
			}
			vo.setListId(sub.getMailingList().getListId());
			vo.setDescription(sub.getMailingList().getDescription());
			vo.setAddress(sub.getEmailAddress().getAddress());
			volist.add(vo);
		}
		return volist;
	}

	@Override
	public Subscription subscribe(String emailAddr, String listId) {
		Subscription emailAdded = subscriptionDao.subscribe(emailAddr, listId);
		return emailAdded;
	}

	@Override
	public Subscription unSubscriber(String emailAddr, String listId) {
		Subscription emailRemoved = subscriptionDao.unsubscribe(emailAddr, listId);
		return emailRemoved;
	}

	@Override
	public void insertSubscriber(SubscriberData vo) {
		subscriberDao.insert(vo);
	}

	@Override
	public void updateSubscriber(SubscriberData vo) {
		subscriberDao.update(vo);
	}

	@Override
	public void deleteSubscriber(SubscriberData vo) {
		subscriberDao.delete(vo);
	}
	
	@Override
	public Subscription optInRequest(String emailAddr, String listId) {
		Subscription emailOptIned = subscriptionDao.optInRequest(emailAddr, listId);
		return emailOptIned;
	}

	@Override
	public Subscription optInConfirm(String emailAddr, String listId) {
		Subscription emailOptIned = subscriptionDao.optInConfirm(emailAddr, listId);
		return emailOptIned;
	}

	@Override
	public Subscription addToList(String sbsrEmailAddr, String listEmailAddr) {
		Subscription sub = subscriptionDao.addToList(sbsrEmailAddr, listEmailAddr);
		return sub;
	}

	@Override
	public Subscription removeFromList(String sbsrEmailAddr, String listEmailAddr) {
		Subscription sub = subscriptionDao.removeFromList(sbsrEmailAddr, listEmailAddr);
		return sub;
	}

}
