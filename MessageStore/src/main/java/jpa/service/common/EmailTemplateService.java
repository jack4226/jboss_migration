package jpa.service.common;

import java.util.List;
import java.util.Optional;

import jpa.model.EmailTemplate;
import jpa.repository.EmailTemplateRepository;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("emailTemplateService")
@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
public class EmailTemplateService implements java.io.Serializable {
	private static final long serialVersionUID = 248665617609102612L;

	static Logger logger = Logger.getLogger(EmailTemplateService.class);
	
	@Autowired
	EmailTemplateRepository repository;

	public EmailTemplate getByTemplateId(String templateId) {
		return repository.findOneByTemplateId(templateId);
	}
	
	public Optional<EmailTemplate> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public List<EmailTemplate> getAll() {
		return repository.findAllByOrderByTemplateId();
	}
	
	public List<EmailTemplate> getByMailingListId(String listId) {
		return repository.findAllByMailingList_ListId(listId);
	}
	
	public void delete(EmailTemplate template) {
		if (template==null) return;
		repository.delete(template);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByTemplateId(String templateId) {
		return repository.deleteByTemplateId(templateId);
	}

	public void insert(EmailTemplate template) {
		if (template == null) return;
		repository.saveAndFlush(template);
	}
	
	public void update(EmailTemplate template) {
		if (template == null) return;
		repository.saveAndFlush(template);
	}
	
}
