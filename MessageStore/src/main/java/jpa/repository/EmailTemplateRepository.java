package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jpa.model.EmailTemplate;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Integer> {
	// CrudRepository<EmailTemplate, Integer>

	public List<EmailTemplate> findAllByMailingList_ListId(String listId);
	
	public EmailTemplate findOneByTemplateId(String templateId);
	
	public List<EmailTemplate> findAllByOrderByTemplateId();
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from EmailTemplate t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from EmailTemplate t where t.templateId=?1", nativeQuery=false)
	public int deleteByTemplateId(String templateId);
	
}
