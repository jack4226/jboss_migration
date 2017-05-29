package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.UserData;

public interface UserDataRepository extends JpaRepository<UserData, Integer> {

	public UserData findOneByUserId(String userId);
	
	public UserData findOneByUserIdAndPassword(String userId, String password);
	
	public List<UserData> findAllByOrderByUserId(); 
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from UserData t where t.userId=?1", nativeQuery=false)
	public int deleteByUserId(String userId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from UserData t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying
	@Query("update UserData t set t.sessionId=:sessionId, t.lastVisitTime=:lastVisitTime, t.hits=:hits where t.rowId=:rowId")
	public int updateUserDataForWeb(@Param("sessionId") String sessionId,
			@Param("lastVisitTime") java.sql.Timestamp lastVisitTime, @Param("hits") Integer hits,
			@Param("rowId") Integer rowId);
}
