package jpa.repository;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;

import jpa.model.EmailAddress;
import jpa.msgui.vo.PagingVo;

public interface EmailAddressRepositoryCustom {

	@Lock(LockModeType.PESSIMISTIC_READ)
	public EmailAddress findSertAddress(String addr);
	
	public EmailAddress findByAddressWithCounts(String addr);
	
	public void updateBounceCount(EmailAddress emailAddr);
	
	public int countAllByPagingVo(PagingVo vo);
	
	public int findRowIdForPreview();
	
	public List<EmailAddress> findAllByPagingVo(PagingVo vo);
	
	public List<EmailAddress> findByAddressPattern(String addressPattern);
}
