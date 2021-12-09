package jpa.test.common;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jpa.util.JpaUtil;
import jpa.constant.Constants;
import jpa.model.SenderData;
import jpa.model.IdTokens;
import jpa.service.common.IdTokensService;
import jpa.service.common.SenderDataService;
import jpa.spring.util.SpringUtil;

public class IdTokens2Test {
	static final Logger logger = LogManager.getLogger(IdTokens2Test.class);

	private static final String PERSISTENCE_UNIT_NAME = "MessageDB";

	private static PlatformTransactionManager txmgr;
	private static TransactionStatus status;
	
	@BeforeClass
	public static void IdTokensPrepare() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("idtokens_service");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		txmgr = (PlatformTransactionManager) SpringUtil.getAppContext().getBean("msgTransactionManager");
		status = txmgr.getTransaction(def);
	}

	@AfterClass
	public static void IdTokenTeardown() {
		txmgr.rollback(status);
	}

	/*
	 * load entity manager factory by spring as a spring bean
	 */
	@Test
	public void springEntityManager() {
		if (JpaUtil.isDerbyDatabase()) {
			// Error 40XL1 (org.hibernate.exception.LockAcquisitionException) A lock could not be obtained within the time requested
			// caught when running with Derby
			return;
		}
		EntityManagerFactory emf = SpringUtil.getAppContext().getBean(LocalContainerEntityManagerFactoryBean.class).getObject();
		EntityManager entityManager = emf.createEntityManager();
		// Read the existing entries and write to console
		Query q = entityManager.createQuery("select t from IdTokens t");
		@SuppressWarnings("unchecked")
		List<IdTokens> tokens = q.getResultList();
		try {
			for (IdTokens token : tokens) {
				logger.info(token);
				// update record
				entityManager.getTransaction().begin();
				if ("SysAdmin".equalsIgnoreCase(token.getUpdtUserId())) {
					token.setUpdtUserId("admin");
				}
				else {
					token.setUpdtUserId("SysAdmin");
				}
				token.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
				entityManager.persist(token);
				entityManager.getTransaction().commit();
			}
			logger.info("Size: " + tokens.size());
		}
		finally {
			entityManager.close();
		}
	}

	@Ignore // ignored due to dead lock when running pom test using derby.
	public void idTokensService1() {
		IdTokensService service = SpringUtil.getAppContext().getBean(IdTokensService.class);
		SenderDataService cdService = SpringUtil.getAppContext().getBean(SenderDataService.class);;

		List<IdTokens> list = service.getAll();
		assertFalse(list.isEmpty());
		
		IdTokens tkn0 = service.getBySenderId(Constants.DEFAULT_SENDER_ID);
		assertNotNull(tkn0);
		
		// test update - it should not create a new record
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		Optional<IdTokens> tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue(tkn1.isPresent());
		assertTrue("JpaTest".equals(tkn1.get().getUpdtUserId()));
		// end of test update
		
		// test insert - a new record should be created
		SenderData cd2 = cdService.getBySenderId("JBatchCorp");
		IdTokens tkn2 = new IdTokens();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn2.setSenderData(cd2);
		service.insert(tkn2);
		
		IdTokens tkn3 = service.getBySenderId("JBatchCorp");
		assertNotNull(tkn3);
		assertTrue(tkn1.get().getRowId()!=tkn3.getRowId());
		// end of test insert
		
		service.delete(tkn3);
		assertNull(service.getByRowId(tkn3.getRowId()));
		
		assertTrue(0==service.deleteBySenderId(tkn3.getSenderData().getSenderId()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
	
	@Test //(expected=javax.persistence.NoResultException.class)
	public void idTokensService2() {
		IdTokensService service = SpringUtil.getAppContext().getBean(IdTokensService.class);
		SenderDataService cdService = SpringUtil.getAppContext().getBean(SenderDataService.class);;

		IdTokens tkn0 = service.getBySenderId(Constants.DEFAULT_SENDER_ID);
		assertNotNull(tkn0);
		
		SenderData cd2 = cdService.getBySenderId("JBatchCorp");
		IdTokens tkn1 = new IdTokens();
		try {
			BeanUtils.copyProperties(tkn1, tkn0);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		tkn1.setSenderData(cd2);
		service.insert(tkn1);
		
		IdTokens tkn2 = service.getBySenderId(tkn1.getSenderData().getSenderId());
		assertNotNull(tkn2);
		
		service.delete(tkn2);
		service.getBySenderId(tkn2.getSenderData().getSenderId());
	}

	/* 
	 * !!! load entity manager factory by EclipseLink from persistence.xml
	 */
	@Ignore
	public void persistenceXmlfile() {
		java.util.Map<Object,Object> properties = new HashMap<Object,Object>();
		properties.put(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, "META-INF/jpa-persistence.xml");
		//properties.put(PersistenceUnitProperties.CLASSLOADER, this.getClass().getClassLoader());
		
		EntityManagerFactory emf = new PersistenceProvider().createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
		EntityManager entityManager = emf.createEntityManager();
		// Read the existing entries and write to console
		Query q = entityManager.createQuery("select t from IdTokens t");
		@SuppressWarnings("unchecked")
		List<IdTokens> tokens = q.getResultList();
		for (IdTokens token : tokens) {
			logger.info(token);
			// update record
			entityManager.getTransaction().begin();
			if ("SysAdmin".equalsIgnoreCase(token.getUpdtUserId())) {
				token.setUpdtUserId("admin");
			}
			else {
				token.setUpdtUserId("SysAdmin");
			}
			token.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
			entityManager.persist(token);
			entityManager.getTransaction().commit();
		}
		logger.info("Size: " + tokens.size());

		entityManager.close();
	}

}
