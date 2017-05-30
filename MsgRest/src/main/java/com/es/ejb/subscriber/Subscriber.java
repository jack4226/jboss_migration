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
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import jpa.constant.Constants;
import jpa.model.EmailAddress;
import jpa.model.SubscriberData;
import jpa.model.Subscription;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SubscriberDataService;
import jpa.service.common.SubscriptionService;
import jpa.spring.util.SpringUtil;
import jpa.util.BeanCopyUtil;
import jpa.util.PrintUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.es.ejb.emailaddr.EmailAddrLocal;
import com.es.ejb.senderdata.SenderDataLocal;
import com.es.ejb.ws.vo.SubscriberDataVo;
import com.es.ejb.ws.vo.SubscriptionVo;
import jpa.tomee.util.TomeeCtxUtil;

/**
 * Session Bean implementation class Subscriber
 */
@Stateless (name="Subscriber", mappedName = "ejb/Subscriber")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(SubscriberRemote.class)
@Local(SubscriberLocal.class)
@LocalBean
@WebService (portName = "Subscriber", serviceName = "SubscriberService", targetNamespace = "http://com.es.ws.subscriber/wsdl")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public class Subscriber implements SubscriberRemote, SubscriberLocal, SubscriberWs {
	protected static final Logger logger = Logger.getLogger(Subscriber.class);
	@Resource
	SessionContext context;
	
	@javax.ejb.EJB
	private SenderDataLocal sender;
	@javax.ejb.EJB
	private EmailAddrLocal emailService;
	
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
		BeanCopyUtil.registerBeanUtilsConverters();
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
    	return subscriberDao.getAll();
    }
    
    @Override
    public SubscriberData getSubscriberById(String subrId) {
		SubscriberData sd = subscriberDao.getBySubscriberId(subrId);
		return sd;
	}

    @Override
	public SubscriberData getSubscriberByEmailAddress(String emailAddr) {
		EmailAddress emailAddrVo = emailAddrDao.findSertAddress(emailAddr);
		SubscriberData sd = subscriberDao.getByEmailAddress(emailAddrVo.getAddress());
		return sd;
	}

    @WebMethod
	@Override
	public SubscriberDataVo getSubscriberData(String emailAddr) {
    	logger.info("in getSubscriber() - emailAddr: " + emailAddr);
		jpa.model.SubscriberData sd = getSubscriberByEmailAddress(emailAddr);
		if (sd == null) {
			logger.info("Failed to find SubscriberData by emailAddr: " + emailAddr);
			return null;
		}
		SubscriberDataVo vo = subscriberDataToVo(sd);
		return vo;
	}

	@Override
	public List<SubscriptionVo> getSubscribedList(String emailAddr) {
		List<Subscription> sublist = subscriptionDao.getByAddress(emailAddr);
		List<SubscriptionVo> volist = new ArrayList<SubscriptionVo>();
		for (Subscription sub : sublist) {
			SubscriptionVo vo = subscriptionToVo(sub);
			volist.add(vo);
		}
		return volist;
	}

	@Override
	public Subscription subscribe(String emailAddr, String listId) {
		logger.info("in subscribe() - emailAddr/listId: " + emailAddr + "/" + listId);
		Subscription sub = subscriptionDao.subscribe(emailAddr, listId);
		return sub;
	}

	@Override
	public Subscription unSubscribe(String emailAddr, String listId) {
		logger.info("in unSubscribe() - emailAddr/listId: " + emailAddr + "/" + listId);
		Subscription sub = subscriptionDao.unsubscribe(emailAddr, listId);
		return sub;
	}

	@WebMethod
	@Override
	public SubscriptionVo addEmailToList(String emailAddr, String listId) {
		logger.info("in addEmailToList() - emailAddr/listId: " + emailAddr + "/" + listId);
		Subscription sub = subscriptionDao.subscribe(emailAddr, listId);
		SubscriptionVo vo = subscriptionToVo(sub);
		return vo;
	}

	@WebMethod
	@Override
	public SubscriptionVo removeEmailFromList(String emailAddr, String listId) {
		logger.info("in removeEmailFromList() - emailAddr/listId: " + emailAddr + "/" + listId);
		Subscription sub = subscriptionDao.unsubscribe(emailAddr, listId);
		SubscriptionVo vo = subscriptionToVo(sub);
		return vo;
	}

	@WebMethod
	@Override
	public Boolean addSubscriber(SubscriberDataVo vo) {
		if (StringUtils.isBlank(vo.getSenderId())) {
			vo.setSenderId(Constants.DEFAULT_SENDER_ID);
		}
		jpa.model.SenderData senderData = sender.findBySenderId(vo.getSenderId());
		if (senderData == null) {
			logger.info("Failed to find Sender by (" + vo.getSenderId() + "), exit.");
			return false;
		}
		jpa.model.SubscriberData sd = new jpa.model.SubscriberData();
		try {
			BeanUtils.copyProperties(sd, vo);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to copy properties", e);
		}
		sd.setSenderData(sender.findBySenderId(vo.getSenderId()));
		sd.setEmailAddress(emailService.findSertAddress(vo.getEmailAddress()));
		sd.setStartDate(new java.util.Date());
		insertSubscriber(sd);
		return true;
	}

	@WebMethod
	@Override
	public Boolean addAndSubscribe(SubscriberDataVo vo, String listId) {
		Boolean isSuccess = addSubscriber(vo);
		Subscription sub = null;
		if (isSuccess) {
			sub = subscribe(vo.getEmailAddress(), listId);
		}
		return (isSuccess && sub != null);
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

	SubscriberDataVo subscriberDataToVo(SubscriberData sd) {
		SubscriberDataVo vo = new SubscriberDataVo();
		try {
			BeanUtils.copyProperties(vo, sd);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to copy properties", e);
		}
		vo.setSenderId(sd.getSenderData().getSenderId());
		vo.setEmailAddress(sd.getEmailAddress().getAddress());
		return vo;
	}
	
	SubscriptionVo subscriptionToVo(Subscription sub) {
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
		return vo;
	}
}
