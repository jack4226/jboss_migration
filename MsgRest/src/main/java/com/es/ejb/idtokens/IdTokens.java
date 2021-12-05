package com.es.ejb.idtokens;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import jpa.service.common.IdTokensService;
import jpa.spring.util.SpringUtil;
import jpa.util.BeanCopyUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.es.ejb.ws.vo.IdTokensVo;
import com.es.tomee.util.AccessTimeout;

/**
 * Session Bean implementation class IdTokens
 */
@Singleton(name = "IdTokens", mappedName = "ejb/IdTokens")
@Lock(LockType.READ) // allow concurrent access to the methods
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(IdTokensRemote.class)
@Local(IdTokensLocal.class)

@WebService (portName = "IdTokens", serviceName = "IdTokensService", targetNamespace = "http://com.es.ws.idtokens/wsdl")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public class IdTokens implements IdTokensRemote, IdTokensLocal, IdTokensWs {
	protected static final Logger logger = LogManager.getLogger(IdTokens.class);
	@Resource
	SessionContext context;
	private IdTokensService idTokensDao;
    /**
     * Default constructor. 
     */
    public IdTokens() {
    	idTokensDao = SpringUtil.getAppContext().getBean(IdTokensService.class);
    	BeanCopyUtil.registerBeanUtilsConverters();
    }

    @Asynchronous
    @AccessTimeout(-1)
    @Override
    public Future<?> stayBusy(CountDownLatch ready) {
    	long start = System.currentTimeMillis();
    	try {
    		//ready.await();
        	TimeUnit.MILLISECONDS.sleep(100);
    	}
    	catch (InterruptedException e) {
    		logger.error("InterruptedException caught: " + e.getMessage());
    		Thread.interrupted();
    	}
    	return new AsyncResult<Long>(System.currentTimeMillis() - start);
    }
    
    //@AccessTimeout(0)
    @AccessTimeout(value = 5, unit = TimeUnit.SECONDS)
    @Override
	public jpa.model.IdTokens findBySenderId(String senderId) {
		jpa.model.IdTokens idTokens = idTokensDao.getBySenderId(senderId);
		return idTokens;
	}

    @AccessTimeout(value = 10, unit = TimeUnit.SECONDS)
    @Override
	public List<jpa.model.IdTokens> findAll() {
		List<jpa.model.IdTokens> list = idTokensDao.getAll();
		return list;
	}
    
    @AccessTimeout(-1)
    @Override
    public void insert(jpa.model.IdTokens idTokens) {
    	idTokensDao.insert(idTokens);
    }
 
    @AccessTimeout(-1)
    @Override
    public void update(jpa.model.IdTokens idTokens) {
    	idTokensDao.update(idTokens);
    }
 
    @AccessTimeout(-1)
    @Override
    public int delete(String senderId) {
    	return idTokensDao.deleteBySenderId(senderId);
    }
 
    @WebMethod
    @Override
	public IdTokensVo getBySenderId(String senderId) {
		jpa.model.IdTokens idTokens = findBySenderId(senderId);
		if (idTokens == null) {
			try {
				SOAPFault soapFault = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createFault();
				QName faultName = new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Service");
				soapFault.setFaultCode(faultName);
				soapFault.setFaultString("SenderId (" + senderId + ") not found");
				throw new SOAPFaultException(soapFault);
			}
			catch (SOAPException e1) {
				throw new RuntimeException("Failed to create a SOAP Fault instance", e1);
			}
		}
		IdTokensVo idTokensVo = new IdTokensVo();
		try {
			BeanUtils.copyProperties(idTokensVo, idTokens);
			idTokensVo.setSenderId(idTokens.getSenderData().getSenderId());
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to copy properties", e);
		}
		return idTokensVo;
	}

    @WebMethod
    @Override
	public List<IdTokensVo> getAll() {
		List<jpa.model.IdTokens> list = findAll();
		List<IdTokensVo> volist = new ArrayList<IdTokensVo>();
		for (jpa.model.IdTokens idTokens : list) {
			IdTokensVo idTokensVo = new IdTokensVo();
			try {
				BeanUtils.copyProperties(idTokensVo, idTokens);
				idTokensVo.setSenderId(idTokens.getSenderData().getSenderId());
				volist.add(idTokensVo);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to copy properties", e);
			}
		}
		return volist;
	}

    @WebMethod
	@Override
	public void update(IdTokensVo vo) {
		jpa.model.IdTokens id = findBySenderId(vo.getSenderId());
		if (id == null) {
			try {
				SOAPFault soapFault = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createFault();
				QName faultName = new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Service");
				soapFault.setFaultCode(faultName);
				soapFault.setFaultString("SenderId (" + vo.getSenderId() + ") not found");
				throw new SOAPFaultException(soapFault);
			}
			catch (SOAPException e1) {
				throw new RuntimeException("Failed to create a SOAP Fault instance", e1);
			}
		}
		try {
			BeanUtils.copyProperties(id, vo);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to copy properties", e);
		}
		update(id);
	}

}
