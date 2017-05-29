package jpa.service.msgdata;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageFolder;
import jpa.repository.msg.MessageFolderRepository;

@Component("messageFolderService")
@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
public class MessageFolderService implements java.io.Serializable {
	private static final long serialVersionUID = -5551491267170494876L;
	
	static Logger logger = Logger.getLogger(MessageFolderService.class);

	@Autowired
	private MessageFolderRepository repository;
	
	public MessageFolder getByRowId(int rowId) {
		return repository.findOne(rowId);
	}

	public MessageFolder getOneByFolderName(String folderName) {
		return repository.findOneByFolderName(folderName);
	}
	
	public List<MessageFolder> getAll() {
		return repository.findAll();
	}
	
	public void insert(MessageFolder folder) {
		repository.saveAndFlush(folder);
	}
	
	public void update(MessageFolder folder) {
		repository.saveAndFlush(folder);
	}
	
	public void delete(MessageFolder folder) {
		repository.delete(folder);
	}
	
	public int deleteByRowId(Integer rowId) {
		return repository.deleteByRowId(rowId);
	}
	
}
