package jpa.service.msgdata;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.EmailAddress;
import jpa.model.msg.MessageDeliveryStatus;
import jpa.model.msg.MessageDeliveryStatusPK;
import jpa.msgui.vo.PagingVo;
import jpa.repository.EmailAddressRepository;
import jpa.repository.msg.MessageDeliveryStatusRepository;
import jpa.util.BeanCopyUtil;
import jpa.util.PrintUtil;

@Component("messageDeliveryStatusService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageDeliveryStatusService implements java.io.Serializable {
	private static final long serialVersionUID = 5561974880688407087L;

	static Logger logger = Logger.getLogger(MessageDeliveryStatusService.class);
	
	@Autowired
	MessageDeliveryStatusRepository repository;
	
	@Autowired
	EmailAddressRepository emailRepository;

	public MessageDeliveryStatus getByRowId(int rowId) {
		return repository.findOne(rowId);

	}

	public MessageDeliveryStatus getByPrimaryKey(MessageDeliveryStatusPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		return repository.findOneByPrimaryKey(pk.getMessageInbox().getRowId(), pk.getFinalRcptAddrRowId());
	}

	public List<MessageDeliveryStatus> getByMsgInboxId(int msgId) {
		return repository.findAllByMessageDeliveryStatusPK_MessageInbox_RowId(msgId);
	}
	
	public Page<MessageDeliveryStatus> findAllByFinalRcptAddress(String rcptAddr, PagingVo pagingVo) {
		EmailAddress addr = emailRepository.findSertAddress(rcptAddr);
		Direction dir = Direction.DESC;
		Boolean isAscending = pagingVo.getOrderBy().getIsAscending(PagingVo.Column.updtTime);
		if (isAscending != null && isAscending == true) {
			dir = Direction.ASC;
		}
		Sort sort = new Sort(dir, "updtTime");
		Pageable pageable = new PageRequest(pagingVo.getPageNumber(), pagingVo.getPageSize(), sort);
		return repository.findTop20ByMessageDeliveryStatusPK_FinalRcptAddrRowId(addr.getRowId(), pageable);
	}

	public void delete(MessageDeliveryStatus dlvrStatus) {
		if (dlvrStatus == null) return;
		repository.delete(dlvrStatus);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowid(rowId);
	}

	public int deleteByPrimaryKey(MessageDeliveryStatusPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimaryKey(pk.getMessageInbox().getRowId(), pk.getFinalRcptAddrRowId());
	}

	public int deleteByMsgInboxId(int msgId) {
		return repository.deleteByMsgInboxId(msgId);
	}

	public void update(MessageDeliveryStatus dlvrStatus) {
		repository.saveAndFlush(dlvrStatus);
	}

	public void insert(MessageDeliveryStatus dlvrStatus) {
		MessageDeliveryStatus status = getByPrimaryKey(dlvrStatus.getMessageDeliveryStatusPK());
		if (status == null) {
			repository.saveAndFlush(dlvrStatus);
		}
		else {
			int receivedCount = status.getReceivedCount();
			BeanCopyUtil.registerBeanUtilsConverters();
			try {
				BeanUtils.copyProperties(status, dlvrStatus);
			}
			catch (Exception e) {
				logger.error("Failed to copy bean: " + PrintUtil.prettyPrint(dlvrStatus));
			}
			status.setReceivedCount(++receivedCount);
			update(status);
			dlvrStatus.setReceivedCount(status.getReceivedCount());
		}
	}
}
