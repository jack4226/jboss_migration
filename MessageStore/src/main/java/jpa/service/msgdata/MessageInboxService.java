package jpa.service.msgdata;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.MsgDirectionCode;
import jpa.constant.MsgStatusCode;
import jpa.model.msg.MessageInbox;
import jpa.msgui.vo.PagingVo;
import jpa.msgui.vo.SearchFieldsVo;
import jpa.repository.msg.MessageInboxRepository;
import jpa.repository.msg.MessageInboxSpecs;
import jpa.util.JpaUtil;

@Component("messageInboxService")
@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
public class MessageInboxService implements java.io.Serializable {
	private static final long serialVersionUID = -2776130351245699784L;

	static Logger logger = Logger.getLogger(MessageInboxService.class);
	
	@Autowired
	private MessageInboxRepository repository;

	public MessageInbox getByRowId(int rowId) {
		// TODO fix optimistic locking issue with Hibernate when running MailProcessorBoTest
		if (JpaUtil.isHibernate()) {
			return repository.findOneByRowIdNoLock(rowId);
		}
		MessageInbox record = repository.findOneByRowId(rowId);
		return record;
	}

	/*
	 * Consider modifying model to support join and lazy loading
	 */
	public MessageInbox getAllDataByPrimaryKey(int rowId) {
		//em.clear(); // to force fetching from database
		MessageInbox mi = getByRowId(rowId);
		if (mi != null) {
			mi.getMessageHeaderList().size();
			mi.getMessageAddressList().size();
			mi.getMessageStream();
			mi.getMessageRfcFieldList().size();
			mi.getMessageAttachmentList().size();
			mi.getMessageUnsubComment();
			mi.getMessageActionLogList().size();
			mi.getMessageDeliveryStatusList().size();
			
			Integer refRowId = mi.getReferringMessageRowId();
			if (refRowId != null) {
				MessageInbox refMsg = getByRowId(refRowId);
				mi.setReferringMessage(refMsg);
			}
			
			Integer leadRowId = mi.getLeadMessageRowId();
			if (leadRowId != null) {
				if (rowId == leadRowId) {
					mi.setLeadMessage(mi);
				}
				else {
					MessageInbox leadMsg = getByRowId(leadRowId);
					mi.setLeadMessage(leadMsg);
				}
			}
		}
		//
		return mi;
	}

	public MessageInbox getFirstRecord() {
		MessageInbox record =  repository.findFirstByOrderByRowId();
		return record;
	}

	public MessageInbox getLastRecord() {
		MessageInbox record =  repository.findFirstByOrderByRowIdDesc();
		return record;
	}

	public MessageInbox getPrevoiusRecord(int rowId) {
		return repository.findTop1ByRowIdLessThanOrderByRowIdDesc(rowId);
	}
	
	public MessageInbox getNextRecord(int rowId) {
		return repository.findTop1ByRowIdGreaterThanOrderByRowIdAsc(rowId);
	}
	
	public MessageInbox getPrevoiusRecord(MessageInbox inbox) {
		MessageInbox record = getPrevoiusRecord(inbox.getRowId());
		return record;
	}

	public MessageInbox getNextRecord(MessageInbox inbox) {
		MessageInbox record = getNextRecord(inbox.getRowId());
		return record;
	}
	
	public List<MessageInbox> getByNotExistsDeliveryStatus(PagingVo pagingVo) {
		return repository.findAllByNotExistsDeliveryStatus(pagingVo);
	}

	/*
	 * define methods primarily used by UI components. 
	 */

	public List<MessageInbox> getByLeadMsgId(int leadMsgId) {
		return repository.findByLeadMessageRowIdOrderByRowId(leadMsgId);
	}

	public List<MessageInbox> getByReferringMsgId(int referredMsgId) {
		return repository.findByReferringMessageRowIdOrderByRowId(referredMsgId);
	}

	public List<MessageInbox> getByFromAddress(String address) {
		return repository.findAllByFromAddress_AddressOrderByRowId(address);
	}

	public List<MessageInbox> getByToAddress(String address) {
		return repository.findAllByToAddress_AddressOrderByRowId(address);
	}

	public List<MessageInbox> getRecentByDays(int days) {
		if (days < 0) days = 30; // default to last 30 days
		long millis = TimeUnit.DAYS.toMillis(days);
		return getRecentByDate(new java.sql.Date(System.currentTimeMillis() - millis));
	}
	
	/**
	 * results are limited to the last 100 records.
	 * @param date
	 * @return
	 */
	public List<MessageInbox> getRecentByDate(java.sql.Date date) {
		return repository.findTop100ByReceivedTimeGreaterThanEqualOrderByReceivedTimeDesc(date);
	}

	public void delete(MessageInbox msgInbox) {
		repository.delete(msgInbox);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void update(MessageInbox msgInbox) {
		repository.saveAndFlush(msgInbox);
	}

	public void insert(MessageInbox msgInbox) {
		repository.saveAndFlush(msgInbox);
		if (msgInbox.getLeadMessageRowId() == null) {
			msgInbox.setLeadMessageRowId(msgInbox.getRowId());
		}
	}

	public int getUnreadCountByFolderName(String folderName) {
		return repository.countByMessageFolder_FolderNameAndReadCountEquals(folderName, 0);
	}
	
	public int getReadCountByFolderName(String folderName) {
		return repository.countByMessageFolder_FolderNameAndReadCountGreaterThan(folderName, 0);
	}
	
	public int getMessageCountByFolderName(String folderName) {
		return repository.countByMessageFolder_FolderNameEquals(folderName);
	}
	
	public int getReceivedUnreadCount() {
		return getUnreadCount(MsgDirectionCode.RECEIVED);
	}

	public int getSentUnreadCount() {
		return getUnreadCount(MsgDirectionCode.SENT);
	}

	public int getAllUnreadCount() {
		return getUnreadCount(null);
	}

	private int getUnreadCount(MsgDirectionCode msgDirection) {
		if (msgDirection == null) {
			return repository.countByReadCountAndStatusIdNot(0, MsgStatusCode.CLOSED.getValue());
		}
		else {
			return repository.countByReadCountAndMsgDirectionAndStatusIdNot(0, msgDirection.getValue(), MsgStatusCode.CLOSED.getValue());
		}
	}

	public int updateStatusIdByLeadMsgId(MessageInbox msgInbox) {
		java.sql.Timestamp time = new java.sql.Timestamp(System.currentTimeMillis());
		return repository.updateStatusIdByLeadMsgId(msgInbox.getStatusId(), msgInbox.getUpdtUserId(), msgInbox.getLeadMessageRowId(), time);
	}

	public int updateReadCount(MessageInbox msgInbox) {
		java.sql.Timestamp time = new java.sql.Timestamp(System.currentTimeMillis());
		return repository.updateReadCount(msgInbox.getReadCount(), msgInbox.getUpdtUserId(), msgInbox.getRowId(), time);
	}

	public int updateIsFlagged(MessageInbox msgInbox) {
		java.sql.Timestamp time = new java.sql.Timestamp(System.currentTimeMillis());
		return repository.updateIsFlagged(msgInbox.isFlagged(), msgInbox.getUpdtUserId(), msgInbox.getRowId(), time);
	}

	public long getRowCountForWeb(SearchFieldsVo vo) {
		return repository.findRowCountForWeb(vo);
	}

	public long getRowCountForWeb(PagingVo vo) {
		return repository.count(MessageInboxSpecs.buildSpecForRowCount(vo));
	}

	public List<MessageInbox> getListForWeb(SearchFieldsVo vo) {
		long rowCount = repository.findRowCountForWeb(vo);
		return repository.findListForWeb(vo, rowCount);
	}
	
}
