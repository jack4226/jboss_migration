package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.EmailVariable;

public interface EmailVariableRepository extends JpaRepository<EmailVariable, Integer> {

	public EmailVariable findOneByVariableName(String variable);
	
	public List<EmailVariable> findAllByIsBuiltinOrderByRowId(boolean isBuiltin);
	
	public List<EmailVariable> findAllByOrderByRowId();
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from EmailVariable t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from EmailVariable t where t.variableName=?1", nativeQuery=false)
	public int deleteByVariableName(String variableName);
}
