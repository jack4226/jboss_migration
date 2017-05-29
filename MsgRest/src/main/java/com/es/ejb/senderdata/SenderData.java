package com.es.ejb.senderdata;

import java.util.ArrayList;
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
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import jpa.service.common.SenderDataService;
import jpa.spring.util.SpringUtil;
import jpa.util.BeanCopyUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.es.ejb.ws.vo.SenderDataVo;

/**
 * Session Bean implementation class EmailAddr
 */
@Stateless(name = "SenderData", mappedName = "ejb/SenderData")
@TransactionManagement(TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Resource(name = "msgdb_pool", mappedName = "jdbc/MessageDS", 
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@Remote(SenderDataRemote.class)
@Local(SenderDataLocal.class)

@WebService (portName = "SenderData", serviceName = "SenderDataService", targetNamespace = "http://com.es.ws.senderdata/wsdl")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public class SenderData implements SenderDataLocal, SenderDataRemote, SenderDataWs {
	protected static final Logger logger = Logger.getLogger(SenderData.class);
	
	@Resource
	SessionContext context;
	
	private SenderDataService senderDataDao;
	
	public SenderData() {
		senderDataDao = SpringUtil.getAppContext().getBean(SenderDataService.class);
		BeanCopyUtil.registerBeanUtilsConverters();
	}

	@Override
	public jpa.model.SenderData findBySenderId(String senderId) {
		jpa.model.SenderData sender = senderDataDao.getBySenderId(senderId);
		return sender;
	}

	@Override
	public List<jpa.model.SenderData> findAll() {
		return senderDataDao.getAll();
	}

	@Override
	public void insert(jpa.model.SenderData sender) {
		senderDataDao.insert(sender);
	}

	@Override
	public void update(jpa.model.SenderData sender) {
		senderDataDao.update(sender);
	}

	@Override
	public int deleteBySenderId(String senderId) {
		return senderDataDao.deleteBySenderId(senderId);
	}

	@WebMethod
	@Override
	public SenderDataVo getBySenderId(String senderId) {
		jpa.model.SenderData sender = senderDataDao.getBySenderId(senderId);
		if (sender == null) {
			try {
				SOAPFault soapFault = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL).createFault();
				QName faultName = new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Service");
				soapFault.setFaultCode(faultName);
				soapFault.setFaultString("SenderId (" + senderId + ") not found");
				throw new SOAPFaultException(soapFault);
			} catch (SOAPException e1) {
				throw new RuntimeException("Failed to create a SOAP Fault instance", e1);
			}
		}
		SenderDataVo vo = new SenderDataVo();
		try {
			BeanUtils.copyProperties(vo, sender);
			return vo;
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to copy properties", e);
		}
	}

	@WebMethod
	@Override
	public List<SenderDataVo> getAll() {
		List<jpa.model.SenderData> list = findAll();
		List<SenderDataVo> volist = new ArrayList<SenderDataVo>();
		for (jpa.model.SenderData sender : list) {
			SenderDataVo vo = new SenderDataVo();
			try {
				BeanUtils.copyProperties(vo, sender);
				volist.add(vo);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to copy properties", e);
			}
		}
		return volist;
	}

	@WebMethod
	@Override
	public void update(SenderDataVo vo) {
		jpa.model.SenderData sender = findBySenderId(vo.getSenderId());
		try {
			BeanUtils.copyProperties(sender, vo);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to copy properties", e);
		}
		update(sender);
	}

}
