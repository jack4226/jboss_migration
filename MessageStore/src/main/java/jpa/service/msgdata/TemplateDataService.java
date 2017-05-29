package jpa.service.msgdata;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.StatusId;
import jpa.model.msg.TemplateData;
import jpa.model.msg.TemplateDataPK;
import jpa.repository.msg.TemplateDataRepository;

@Component("templateDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class TemplateDataService implements java.io.Serializable {
	private static final long serialVersionUID = 8608904621919420136L;

	static Logger logger = Logger.getLogger(TemplateDataService.class);
	
	@Autowired
	TemplateDataRepository repository;

	public TemplateData getByRowId(int rowId) {
		return repository.findOne(rowId);
	}

	public TemplateData getByPrimaryKey(TemplateDataPK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		if (pk.getStartTime()!=null) {
			return repository.findOneByPrimaryKey(pk.getSenderData().getSenderId(), pk.getTemplateId(), pk.getStartTime());
		}
		else {
			return repository.findOneByPrimaryKey(pk.getSenderData().getSenderId(), pk.getTemplateId());
		}
	}

	public TemplateData getByBestMatch(TemplateDataPK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		if (pk.getStartTime()==null) {
			pk.setStartTime(new Timestamp(System.currentTimeMillis()));
		}
		List<TemplateData> list = repository.findAllByBestMatch(pk.getSenderData().getSenderId(), pk.getTemplateId(), pk.getStartTime());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public List<TemplateData> getByTemplateId(String templateId) {
		return repository.findAllByTemplateId(templateId);
	}

	public List<TemplateData> getCurrentBySenderId(String senderId) {
		return repository.findAllCurrentBySenderId(StatusId.ACTIVE.getValue(), new Timestamp(System.currentTimeMillis()), senderId);
	}

	public void delete(TemplateData template) {
		if (template == null) return;
		repository.delete(template);
	}

	public int deleteByPrimaryKey(TemplateDataPK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimaryKey(pk.getTemplateId(), pk.getStartTime(), pk.getSenderData().getSenderId());
	}

	public int deleteByTemplateId(String templateId) {
		return repository.deleteByTemplateId(templateId);
	}

	public int deleteBySenderId(String senderId) {
		return repository.deleteBySenderId(senderId);
	}

	public void update(TemplateData template) {
		repository.saveAndFlush(template);
	}

	public void insert(TemplateData template) {
		repository.saveAndFlush(template);
	}

}
