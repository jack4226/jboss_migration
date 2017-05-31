package jpa.repository.msg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.MsgUnreadCount;

public interface MsgUnreadCountRepository extends JpaRepository<MsgUnreadCount, Integer> {

	@Modifying(clearAutomatically = true)
	@Query(value="delete from MsgUnreadCount t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
}
