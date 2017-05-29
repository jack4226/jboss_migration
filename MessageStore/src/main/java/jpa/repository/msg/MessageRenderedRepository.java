package jpa.repository.msg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.MessageRendered;

public interface MessageRenderedRepository extends JpaRepository<MessageRendered, Integer> {

	// find the first record
	public MessageRendered findFirstByOrderByRowId();

	// find the last record
	public MessageRendered findFirstByOrderByRowIdDesc();
	
	// find next record
	public MessageRendered findTop1ByRowIdGreaterThanOrderByRowId(Integer rowId);
	
	// find previous record
	public MessageRendered findTop1ByRowIdLessThanOrderByRowIdDesc(Integer rowId);

	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageRendered t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
}
