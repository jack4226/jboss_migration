package jpa.dataloader;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import jpa.constant.Constants;
import jpa.constant.MobileCarrierEnum;
import jpa.constant.RuleCriteria;
import jpa.constant.StatusId;
import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.msgui.vo.PagingVo;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.common.SubscriberDataService;
import jpa.spring.util.SpringUtil;

import org.apache.log4j.Logger;

public class SubscriberDataLoader extends AbstractDataLoader {
	static final Logger logger = Logger.getLogger(SubscriberDataLoader.class);
	private SubscriberDataService service;
	private EmailAddressService emailAddrService;
	private SenderDataService senderService;

	public static void main(String[] args) {
		SubscriberDataLoader loader = new SubscriberDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(SubscriberDataService.class);
		emailAddrService = SpringUtil.getAppContext().getBean(EmailAddressService.class);
		senderService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		startTransaction();
		try {
			loadSubscriberData();
			loadByEmailAddresses();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadSubscriberData() {
		String sbsrId = getProperty("subscriber.id.1");
		SubscriberData sd = service.getBySubscriberId(sbsrId);
		if (sd != null) {
			logger.warn("Subscriber \"" + sbsrId + "\" already exists, exit!");
			return;
		}
		String addr = getProperty("subscriber.email.1");
		EmailAddress emailaddr = emailAddrService.findSertAddress(addr);
		SenderData cd = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		SubscriberData data = new SubscriberData();
		data.setSenderData(cd);
		data.setEmailAddress(emailaddr);
		data.setSubscriberId(sbsrId);
		data.setSsnNumber("123-45-6789");
		data.setTaxId(null);
		data.setProfession("Software Consultant");
		data.setFirstName("Joe");
		data.setLastName("Smith");
		data.setStreetAddress("123 Main St.");
		data.setCityName("Dublin");
		data.setStateCode("OH");
		data.setZipCode5("43071");
		data.setPostalCode("43071");
		data.setCountry("US");
		data.setDayPhone("614-234-5678");
		data.setEveningPhone("614-789-6543");
		data.setMobilePhone("614-264-4056");
		data.setMobileCarrier(MobileCarrierEnum.TMobile.getValue());
		data.setBirthDate(new java.sql.Date(new GregorianCalendar(1980,01,01).getTimeInMillis()));
		data.setStartDate(new java.sql.Date(new GregorianCalendar(2004,05,10).getTimeInMillis()));
		data.setEndDate(new java.sql.Date(new GregorianCalendar(2016,05,10).getTimeInMillis()));
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		data.setMsgHeader("Joe's Message Header");
		data.setMsgDetail("Dear Joe,");
		data.setMsgFooter("Have a nice day.");
		data.setTimeZone(TimeZone.getDefault().getID());
		data.setMemoText("E-Sphere Pilot subscriber");
		data.setStatusId(StatusId.ACTIVE.getValue());
		data.setSecurityQuestion("What is your favorite movie?");
		data.setSecurityAnswer("Rambo");
		data.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		service.insert(data);
		logger.info("EntityManager persisted the record.");
	}
	
	void loadByEmailAddresses() {
		PagingVo vo = new PagingVo();
		vo.setPageSize(100);
		vo.setSearchCriteria(PagingVo.Column.address, new PagingVo.Criteria(RuleCriteria.STARTS_WITH, "user"));
		List<EmailAddress> emailList = emailAddrService.getAddrListByPagingVo(vo);
		logger.info("Number of email addresses: " + emailList.size());
		for (EmailAddress emailaddr : emailList) {
			if (service.getBySubscriberId(emailaddr.getAddress()) != null) {
				logger.warn("Subscriber \"" + emailaddr.getAddress() + "\" already exists!");
				continue;
			}
			SenderData cd = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
			SubscriberData data = new SubscriberData();
			data.setSenderData(cd);
			data.setEmailAddress(emailaddr);
			data.setSubscriberId(emailaddr.getAddress());
			data.setSsnNumber("123-45-6789");
			data.setTaxId(null);
			data.setProfession("Software Consultant");
			data.setFirstName("Joe");
			data.setLastName("Smith");
			data.setStreetAddress("123 Main St.");
			data.setCityName("Dublin");
			data.setStateCode("OH");
			data.setZipCode5("43071");
			data.setPostalCode("43071");
			data.setCountry("US");
			data.setDayPhone("614-234-5678");
			data.setEveningPhone("614-789-6543");
			data.setMobilePhone("614-264-4056");
			data.setMobileCarrier(MobileCarrierEnum.TMobile.getValue());
			data.setBirthDate(new java.sql.Date(new GregorianCalendar(1980,01,01).getTimeInMillis()));
			data.setStartDate(new java.sql.Date(new GregorianCalendar(2004,05,10).getTimeInMillis()));
			data.setEndDate(new java.sql.Date(new GregorianCalendar(2016,05,10).getTimeInMillis()));
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			data.setMsgHeader("Joe's Message Header");
			data.setMsgDetail("Dear Joe,");
			data.setMsgFooter("Have a nice day.");
			data.setTimeZone(TimeZone.getDefault().getID());
			data.setMemoText("E-Sphere Pilot subscriber");
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setSecurityQuestion("What is your favorite movie?");
			data.setSecurityAnswer("Rambo");
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			
			service.insert(data);
		}
	}
}

