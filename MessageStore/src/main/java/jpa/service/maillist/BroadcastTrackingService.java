package jpa.service.maillist;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.BroadcastTracking;
import jpa.repository.msg.BroadcastTrackingRepository;

@Component("broadcastTrackingService")
@Transactional(propagation=Propagation.REQUIRED)
public class BroadcastTrackingService implements java.io.Serializable {
	private static final long serialVersionUID = 3364188607980880964L;

	static Logger logger = Logger.getLogger(BroadcastTrackingService.class);
	
	@Autowired
	BroadcastTrackingRepository repository;
	
	
	public BroadcastTracking getByRowId(int rowId) {
		return repository.findOne(rowId);
	}
	
	public BroadcastTracking getByPrimaryKey(int emailAddrRowId, int broadcastMsgRowId) {
		return repository.findOneByEmailAddress_RowIdAndBroadcastMessage_RowId(emailAddrRowId, broadcastMsgRowId);
	}
	
	public List<BroadcastTracking> getByEmailAddress(String address) {
		return repository.findAllByEmailAddress_Address(address);
	}

	public List<BroadcastTracking> getByEmailAddrRowId(int emailAddrRowId) {
		return repository.findAllByEmailAddress_RowId(emailAddrRowId);
	}

	public List<BroadcastTracking> getByBroadcastMessageRowId(int bcstMsgRowId) {
		return repository.findAllByBroadcastMessage_RowId(bcstMsgRowId);
	}

	public int updateSentCount(int rowId) {
		return this.updateSentCount(rowId, 1);
	}

	public int updateSentCount(int rowId, int count) {
		return repository.updateSentCount(rowId, count, new java.sql.Timestamp(System.currentTimeMillis()));
	}
	
	public int updateOpenCount(int rowId) {
		return repository.updateOpenCount(rowId, new java.sql.Timestamp(System.currentTimeMillis()));
	}

	public int updateClickCount(int rowId) {
		return repository.updateClickCount(rowId, new java.sql.Timestamp(System.currentTimeMillis()));
	}

	public void delete(BroadcastTracking broadcast) {
		if (broadcast == null) return;
		repository.delete(broadcast);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(BroadcastTracking broadcast) {
		repository.saveAndFlush(broadcast);
	}

	public void update(BroadcastTracking broadcast) {
		repository.saveAndFlush(broadcast);
	}
}
