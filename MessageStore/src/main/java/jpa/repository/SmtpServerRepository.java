package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.SmtpServer;

public interface SmtpServerRepository extends JpaRepository<SmtpServer, Integer> {
	
	public SmtpServer findOneByServerName(String serverName); 
	
	public List<SmtpServer> findAllByServerType(String serverType);

	public List<SmtpServer> findAllByServerTypeAndStatusId(String serverType, String statusId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from SmtpServer t where t.rowId=?1")
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from SmtpServer t where t.serverName=:serverName")
	public int deleteByServerName(@Param("serverName")String serverName);
}
