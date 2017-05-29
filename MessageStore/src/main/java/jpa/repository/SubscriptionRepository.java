package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
	
	public List<Subscription> findAllByMailingList_ListId(String listId);
	
	public List<Subscription> findAllByEmailAddress_Address(String address);
	
	public Subscription findOneByMailingList_ListIdAndEmailAddress_Address(String listId, String address);

	public Subscription findOneByEmailAddress_RowIdAndMailingList_ListId(Integer emailAddrRowId, String listId);
	
	@Query("select t from Subscription t, MailingList l, SubscriberData sub, EmailAddress ea " +
			" where l=t.mailingList and l.listId = :listId and ea=t.emailAddress and sub=ea.subscriberData and sub is not null")
	public List<Subscription> findAllByListIdSubscribersOnly(@Param("listId") String listId);
	
	@Query("select t from Subscription t, MailingList l, EmailAddress ea " +
			" where l=t.mailingList and l.listId = :listId and ea=t.emailAddress " +
			" and not exists (select sub from SubscriberData sub where sub=ea.subscriberData)")
	public List<Subscription> findAllByListIdProsperctsOnly(@Param("listId") String listId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from Subscription t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from Subscription su where su.mailingList in (select ml from MailingList ml where ml.listId=?1)")
	public int deleteByListId(String listId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from Subscription su where su.emailAddress in (select ea from EmailAddress ea where ea.address=?1)")
	public int deleteBySubscriberEmailAddress(String address);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from Subscription su where su.emailAddress in (select ea from EmailAddress ea where ea.rowId=?1) " +
			" and su.mailingList in (select ml from MailingList ml where ml.listId=?2 )", nativeQuery=false)
	public int deleteByUniqueKey(int emailAddrRowId, String listId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from Subscription su where su.emailAddress in (select ea from EmailAddress ea where ea.address=?1) " +
			" and su.mailingList in (select ml from MailingList ml where ml.listId=?2 )", nativeQuery=false)
	public int deleteByAddressAndListId(String address, String listId);
	
	@Modifying
	@Query("update Subscription t set t.sentCount = (t.sentCount + :mailsSent), t.updtUserId = :user, t.updtTime = :time where t.rowId=:rowId")
	public int updateSentCount(@Param("rowId")Integer rowId, @Param("mailsSent")Integer mailsSent, @Param("user")String user, @Param("time")java.sql.Timestamp time);
	
	@Modifying
	@Query("update Subscription t set t.clickCount = (t.clickCount + 1), t.updtUserId = ?3, t.updtTime = ?4 " +
			"where t.emailAddress.rowId = ?1 and t.mailingList in (select ml from MailingList ml where ml.listId = ?2)")
	public int updateClickCount(Integer emailAddrRowId, String listId, String user, java.sql.Timestamp time);
	
	@Modifying
	@Query("update Subscription t set t.openCount = (t.openCount + 1), t.updtUserId = ?3, t.updtTime = ?4 " +
			"where t.emailAddress.rowId = ?1 and t.mailingList in (select ml from MailingList ml where ml.listId = ?2)")
	public int updateOpenCount(Integer emailAddrRowId, String listId, String user, java.sql.Timestamp time);
}
