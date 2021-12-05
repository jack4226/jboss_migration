package jpa.service.maillist;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.model.BroadcastMessage;
import jpa.msgui.vo.PagingVo;
import jpa.repository.msg.BroadcastMessageRepository;

@Component("broadcastMessageService")
@Transactional(propagation=Propagation.REQUIRED)
public class BroadcastMessageService implements java.io.Serializable {
	private static final long serialVersionUID = 3364188607980880964L;

	static Logger logger = LogManager.getLogger(BroadcastMessageService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	BroadcastMessageRepository repository;
	
	public Optional<BroadcastMessage> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public List<BroadcastMessage> getByMailingListId(String listId) {
		return repository.findAllByMailingList_ListId(listId);
	}

	public List<BroadcastMessage> getByEmailTemplateId(String templateId) {
		return repository.findAllByEmailTemplate_TemplateId(templateId);
	}

	public List<BroadcastMessage> getByListIdAndTemplateId(String listId, String templateId) {
		return repository.findAllByMailingList_ListIdAndEmailTemplate_TemplateId(listId, templateId);
	}

	/*
	 * returns up to 100 more recent messages. 
	 */
	public List<BroadcastMessage> getTop100() {
		return repository.findTop100ByOrderByRowIdDesc();
	}
	
	public int updateSentCount(int rowId) {
		return this.updateSentCount(rowId, 1);
	}
	
	public int updateSentCount(int rowId, int count) {
		return repository.updateSentCount(rowId, count, new java.sql.Timestamp(System.currentTimeMillis()));
	}
	
	public int updateOpenCount(int rowId) {
		return repository.updateOpenCount(rowId, Constants.DEFAULT_USER_ID, new java.sql.Timestamp(System.currentTimeMillis()));
	}
	
	public int updateClickCount(int rowId) {
		return repository.updateClickCount(rowId, Constants.DEFAULT_USER_ID, new java.sql.Timestamp(System.currentTimeMillis()));
	}
	
	public int updateReferalCount(int rowId) {
		return repository.updateReferalCount(rowId, new java.sql.Timestamp(System.currentTimeMillis()));
	}
	
	public int updateUnsubscribeCount(int rowId) {
		return repository.updateUnsubscribeCount(rowId, new java.sql.Timestamp(System.currentTimeMillis()));
	}
	
	public int getMessageCountForWeb() {
		return repository.countBySentCountGreaterThanAndStartTimeNotNull(0);
	}
	
	public Page<BroadcastMessage> getMessageListForWeb(PagingVo vo) {
		Direction dir = Direction.DESC;
		Boolean isAscending = vo.getOrderBy().getIsAscending(PagingVo.Column.rowId);
		if (isAscending != null && isAscending == true) {
			dir = Direction.ASC;
		}
		Sort sort = Sort.by(dir, "rowId");
		Pageable paging = PageRequest.of(vo.getPageNumber(), vo.getPageSize(), sort);
		return repository.findAllBySentCountGreaterThanAndStartTimeNotNull(0, paging);
	}
	
	public void delete(BroadcastMessage broadcast) {
		if (broadcast == null) return;
		repository.delete(broadcast);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(BroadcastMessage broadcast) {
		repository.saveAndFlush(broadcast);
	}

	public void update(BroadcastMessage broadcast) {
		repository.saveAndFlush(broadcast);
	}
}
