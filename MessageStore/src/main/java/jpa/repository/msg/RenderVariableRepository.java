package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.RenderVariable;

public interface RenderVariableRepository extends JpaRepository<RenderVariable, Integer> {

	@Query("select t from RenderVariable t, MessageRendered mi where " +
			" mi=t.renderVariablePK.messageRendered and mi.rowId=?1 and t.renderVariablePK.variableName=?2")
	public RenderVariable findOneByPrimaryKey(Integer renderedRowId, String variableName);
	
	public List<RenderVariable> findAllByRenderVariablePK_MessageRendered_RowId(Integer renderRowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RenderVariable t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);

	@Modifying(clearAutomatically = true)
	@Query("delete from RenderVariable rv where " +
			" rv.renderVariablePK.messageRendered.rowId=?1 and rv.renderVariablePK.variableName=?2")
	public int deleteByPrimaryKey(Integer renderedRowId, String variableName);

	@Modifying(clearAutomatically = true)
	@Query("delete from RenderVariable t where t.renderVariablePK.messageRendered.rowId=?1")
	public int deleteByMessageRenderedRowId(Integer renderRowId);

}
