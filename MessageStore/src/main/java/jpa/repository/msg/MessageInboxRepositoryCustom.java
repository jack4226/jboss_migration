package jpa.repository.msg;

import java.util.List;

import jpa.model.msg.MessageInbox;
import jpa.msgui.vo.PagingVo;
import jpa.msgui.vo.SearchFieldsVo;

public interface MessageInboxRepositoryCustom {

	/**
	 * XXX - NOT WORKING under Hibernate, see impl for details.
	 * @deprecated
	 */
	public MessageInbox findOneByRowIdWithAllData(Integer rowId);
	
	public Long findRowCountForWeb(SearchFieldsVo vo);
	
	public List<MessageInbox> findListForWeb(SearchFieldsVo vo, long rowCount);
	
	public List<MessageInbox> findAllByNotExistsDeliveryStatus(PagingVo vo);

}
