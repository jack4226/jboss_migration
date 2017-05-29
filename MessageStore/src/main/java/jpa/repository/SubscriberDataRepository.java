package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.SubscriberData;

public interface SubscriberDataRepository extends JpaRepository<SubscriberData, Integer> {
	
	public SubscriberData findOneBySubscriberId(String subscriberId);

	public SubscriberData findOneByEmailAddress_Address(String address);
	
	public List<SubscriberData> findAllByOrderBySubscriberId();
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from SubscriberData t where t.subscriberId=?1", nativeQuery=false)
	public int deleteBySubscriberId(String subscriberId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from SubscriberData t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
}
