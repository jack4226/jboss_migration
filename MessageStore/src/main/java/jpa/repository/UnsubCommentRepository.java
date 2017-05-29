package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.UnsubComment;

public interface UnsubCommentRepository extends JpaRepository<UnsubComment, Integer> {

	public List<UnsubComment> findAllByEmailAddress_Address(String address);
	
	public List<UnsubComment> findAllByMailingList_ListId(String listId);
	
	public List<UnsubComment> findAllByEmailAddress_AddressAndMailingList_ListId(String address, String listId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from UnsubComment t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from UnsubComment uc where uc.emailAddress in " +
			"(select ea from EmailAddress ea where ea.address = ?1)", nativeQuery=false)
	public int deleteByAddress(String address);
}
