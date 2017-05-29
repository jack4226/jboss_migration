package com.es.ejb.emailaddr;

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

import jpa.constant.StatusId;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.spring.util.SpringUtil;
import jpa.util.BeanCopyUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.es.ejb.ws.vo.EmailAddrVo;

/**
 * Session Bean implementation class EmailAddr
 */
@Stateless(name = "EmailAddr", mappedName = "ejb/EmailAddr")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(EmailAddrRemote.class)
@Local(EmailAddrLocal.class)

@WebService (portName = "EmailAddr", serviceName = "EmailAddrService", targetNamespace = "http://com.es.ws.emailaddr/wsdl")
		//,endpointInterface = "com.es.ejb.emailaddr.EmailAddrWs")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT) //Style.RPC
public class EmailAddr implements EmailAddrRemote, EmailAddrLocal, EmailAddrWs {
	protected static final Logger logger = Logger.getLogger(EmailAddr.class);

	@Resource
	SessionContext context;

	private EmailAddressService emailAddrDao;
    /**
     * Default constructor.
     */
    public EmailAddr() {
		emailAddrDao = SpringUtil.getAppContext().getBean(EmailAddressService.class);
		// setup for BeanUtils.copyProperties() to handle null value
		BeanCopyUtil.registerBeanUtilsConverters();
    }

    @Override
    public EmailAddress findSertAddress(String address) {
    	EmailAddress addr = emailAddrDao.findSertAddress(address);
    	return addr;
    }
 
    @Override
    public void updateStatus(String address, StatusId status) {
    	emailAddrDao.updateStatus(address, status);
    }
    
    @WebMethod
    @Override
	public EmailAddrVo getOrAddAddress(String address) {
    	EmailAddress addr = findSertAddress(address);
    	EmailAddrVo addr_vo = new EmailAddrVo();
    	try {
			BeanUtils.copyProperties(addr_vo, addr);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to copy properties", e);
		}
		return addr_vo;
	}

    @WebMethod
    @Override
	public int delete(String address) {
		int rowsDeleted = emailAddrDao.deleteByAddress(address);
		return rowsDeleted;
	}
}
