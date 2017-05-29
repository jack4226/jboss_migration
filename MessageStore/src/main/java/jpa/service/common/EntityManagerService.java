package jpa.service.common;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("entityManagerService")
@Transactional(propagation=Propagation.REQUIRED)
public class EntityManagerService implements java.io.Serializable {
	private static final long serialVersionUID = -2718125813478203516L;

	static Logger logger = Logger.getLogger(EntityManagerService.class);
	
	@Autowired
	EntityManager em;
	
	public void clearEM() {
		em.clear();
	}

	public void detach(Object entity) {
		try {
			if (em.contains(entity)) {
				em.detach(entity);
			}
		}
		finally {
		}
	}
}
