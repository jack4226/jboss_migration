package jpa.repository.msg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.MessageFolder;

public interface MessageFolderRepository extends JpaRepository<MessageFolder, Integer> {
	
	public MessageFolder findOneByFolderName(String folderName);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageFolder t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);

}
