package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.SessionUpload;

public interface SessionUploadRepository extends JpaRepository<SessionUpload, Integer> {

	public List<SessionUpload> findAllBySessionUploadPK_SessionId(String sessionId);
	
	public SessionUpload findOneBySessionUploadPK_SessionIdAndSessionUploadPK_SessionSequence(String sessionId,
			Integer sessionSequence);
	
	public List<SessionUpload> findAllByUserData_UserId(String userId);
	
	@Query("select max(t.sessionUploadPK.sessionSequence) from SessionUpload t where t.sessionUploadPK.sessionId=:sessionId")
	public Integer findLastSessionSequence(@Param("sessionId") String sessionId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from SessionUpload t")
	public int deleteAllRecords();
	
	@Modifying(clearAutomatically = true)
	@Query("delete from SessionUpload t where t.updtTime<:rollback")
	public int deleteExpired(@Param("rollback") java.sql.Timestamp rollback);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from SessionUpload t where t.sessionUploadPK.sessionId=:sessionId")
	public int deleteBySessionId(@Param("sessionId") String sessionId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from SessionUpload t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from SessionUpload t where " +
			"t.sessionUploadPK.sessionId=:sessionId and t.sessionUploadPK.sessionSequence=:sessionSequence ")
	public int deleteByPrimaryKey(@Param("sessionId") String sessionId, @Param("sessionSequence") Integer sessionSequence);
}
