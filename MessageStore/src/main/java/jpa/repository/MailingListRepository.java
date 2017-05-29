package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.MailingList;

public interface MailingListRepository extends JpaRepository<MailingList, Integer> {

	public MailingList findOneByListId(String listId);
	
	public MailingList findOneByAcctUserNameAndSenderData_DomainName(String userName, String domainName);
	
	public List<MailingList> findAllByOrderByListId();
	
	public List<MailingList> findAllByStatusIdOrderByListId(String statusId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MailingList t where t.listId=?1", nativeQuery=false)
	public int deleteByListId(String listId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MailingList t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MailingList ml where ml.senderData in (select cd from SenderData cd where cd.senderId=?1)", nativeQuery=false)
	public int deleteBySenderId(String senderId);
}
