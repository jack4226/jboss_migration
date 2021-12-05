package jpa.service.common;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.repository.SenderDataRepository;

@Component("senderDataService")
@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
public class SenderDataService implements java.io.Serializable {
	private static final long serialVersionUID = -5718921335820858248L;

	static Logger logger = Logger.getLogger(SenderDataService.class);
	
	@Autowired
	SenderDataRepository repository;

	public SenderData getBySenderId(String senderId) {
		return repository.findOneBySenderId(senderId);
	}
	
	public Optional<SenderData> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public SenderData getByDomainName(String domainName) {
		return repository.findOneByDomainName(domainName);
	}

	public List<SenderData> getAll() {
		return repository.findAllByOrderBySenderId();
	}
	
	public String getSystemId() {
		return repository.findSystemIdBySenderId(Constants.DEFAULT_SENDER_ID);
	}

	public String getSystemKey() {
		return repository.findSystemKeyBySenderId(Constants.DEFAULT_SENDER_ID);
	}

	public void delete(SenderData sender) {
		if (sender==null) return;
		if (Constants.DEFAULT_SENDER_ID.equals(sender.getSenderId())) {
			throw new IllegalArgumentException("Can not delete system sender!");
		}
		repository.delete(sender);
	}

	public int deleteBySenderId(String senderId) {
		if (Constants.DEFAULT_SENDER_ID.equals(senderId)) {
			throw new IllegalArgumentException("Can not delete system sender!");
		}
		return repository.deleteBySenderId(senderId);
	}

	public int deleteByRowId(int rowId) {
		Optional<SenderData> sender = getByRowId(rowId);
		if (sender.isPresent() && Constants.DEFAULT_SENDER_ID.equals(sender.get().getSenderId())) {
			throw new IllegalArgumentException("Can not delete system sender!");
		}
		return repository.deleteByRowId(rowId);
	}

	@Autowired
	private ReloadFlagsService reloadFlagsService;

	public void insert(SenderData sender) {
		validateSender(sender);
		repository.saveAndFlush(sender);
		reloadFlagsService.updateSenderReloadFlag();
	}
	
	public void update(SenderData sender) {
		validateSender(sender);
		repository.saveAndFlush(sender);
		reloadFlagsService.updateSenderReloadFlag();
	}
	
	private void validateSender(SenderData sender) {
		if (sender.isUseTestAddr()) {
			if (StringUtils.isBlank(sender.getTestToAddr())) {
				throw new IllegalStateException("Test TO Address was null");
			}
		}
		if (sender.isVerpEnabled()) {
			if (StringUtils.isBlank(sender.getVerpInboxName())) {
				throw new IllegalStateException("VERP bounce inbox name was null");
			}
			if (StringUtils.isBlank(sender.getVerpRemoveInbox())) {
				throw new IllegalStateException("VERP remove inbox name was null");
			}
		}
	}

}
