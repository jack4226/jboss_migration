package jpa.service.common;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.StatusId;
import jpa.model.EmailAddress;
import jpa.msgui.vo.PagingVo;
import jpa.repository.EmailAddressRepository;
import jpa.repository.EmailAddressSpecs;
import jpa.util.EmailAddrUtil;
import jpa.util.JpaUtil;

@Component("emailAddressService")
@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
public class EmailAddressService implements java.io.Serializable {
	private static final long serialVersionUID = 4726327397885138151L;

	static Logger logger = Logger.getLogger(EmailAddressService.class);
	
	@Autowired
	private EmailAddressRepository repository;

	
	/* Search methods */
	
	public List<EmailAddress> getAddrListByPagingVo(PagingVo vo) {
		return repository.findAllByPagingVo(vo);
	}

	public Page<EmailAddress> getPageByPagingVo(PagingVo vo) {
		Pageable p = PageRequest.of(vo.getPageNumber(), vo.getPageSize());
		return repository.findAll(EmailAddressSpecs.buildSpecByPagingVo(vo), p);
	}

	public int getEmailAddressCount(PagingVo vo) {
		return repository.countAllByPagingVo(vo);
	}
	
	/* get methods */
	
	public EmailAddress getByAddress(String addr) {
		EmailAddress emailAddr = repository.findOneByAddress(EmailAddrUtil.removeDisplayName(addr));
		return emailAddr;
	}

	public EmailAddress getAllDataByAddress(String addr) {
		EmailAddress emailAddr = getByAddress(addr);
		if (emailAddr != null) {
			emailAddr.getSubscriberData();
			emailAddr.getSubscriptions().size();
		}
		return emailAddr;
	}
	
	public EmailAddress getByAddressWithCounts(String addr) {
		return repository.findByAddressWithCounts(addr);
	}
	
	public EmailAddress findSertAddress(String addr) {
		return repository.findSertAddress(addr);
	}
	
	public EmailAddress getByRowId(int rowId) {
		EmailAddress emailAddr = repository.findOneByRowId(rowId);
		return emailAddr;
	}

	public List<EmailAddress> getByAddressDomain(String domain) {
		return getByAddressPattern(domain + "$");
	}
	
	public List<EmailAddress> getByAddressUser(String user) {
		return getByAddressPattern("^" + user);
	}
	
	public List<EmailAddress> getByAddressPattern(String addressPattern) {
		return repository.findByAddressPattern(addressPattern);
	}
	
	/**
	 * get the first email address from EmailAddress table
	 * @return rowId of the record found
	 */
	public int getRowIdForPreview() {
		return repository.findRowIdForPreview();
	}

	public EmailAddress getFirstRecord() {
		return repository.findFirstByOrderByRowIdAsc();
	}
	
	/* insert, update, delete methods */
	
	public void delete(EmailAddress emailAddr) {
		if (emailAddr == null) {
			return;
		}
		repository.delete(emailAddr);
	}

	public int deleteByAddress(String addr) {
		return repository.deleteByAddress(EmailAddrUtil.removeDisplayName(addr));
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(EmailAddress emailAddr) {
		if (EmailAddrUtil.hasDisplayName(emailAddr.getAddress())) {
			emailAddr.setAddress(EmailAddrUtil.removeDisplayName(emailAddr.getAddress()));
		}
		repository.saveAndFlush(emailAddr);
	}
	
	public int updateStatus(String address, StatusId status) {
		if (status == null || StringUtils.isBlank(address)) {
			return 0;
		}
		return repository.updateStatus(address, status.getValue());
	}

	public void update(EmailAddress emailAddr) {
		if (EmailAddrUtil.hasDisplayName(emailAddr.getAddress())) {
			emailAddr.setAddress(EmailAddrUtil.removeDisplayName(emailAddr.getAddress()));
		}
		repository.saveAndFlush(emailAddr);
	}
	
	public int updateLastRcptTime(int rowId) {
		return repository.updateLastRcptTime(rowId, new java.sql.Timestamp(System.currentTimeMillis()));
	}

	public int updateLastSentTime(int rowId) {
		return repository.updateLastSentTime(rowId, new java.sql.Timestamp(System.currentTimeMillis()));
	}

	public int updateAcceptHtml(int rowId, boolean acceptHtml) {
		if (JpaUtil.isHibernate()) {
			return repository.updateAcceptHtml(rowId, acceptHtml);
		}
		else { // to work around the issue with EclipseLink
			return repository.updateAcceptHtmlNative(rowId, acceptHtml);
		}
	}

	public void updateBounceCount(EmailAddress emailAddr) {
		repository.updateBounceCount(emailAddr);
	}

}
