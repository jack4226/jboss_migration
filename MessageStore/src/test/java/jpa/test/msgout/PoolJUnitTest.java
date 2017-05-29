package jpa.test.msgout;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import jpa.constant.MailServerType;
import jpa.model.SmtpServer;
import jpa.service.msgout.NamedPool;
import jpa.service.msgout.ObjectPool;
import jpa.service.msgout.SmtpConnection;
import jpa.service.msgout.SmtpServerService;
import jpa.service.msgout.SmtpWrapperUtil;
import jpa.spring.util.SpringUtil;

public class PoolJUnitTest {
	static final Logger logger = Logger.getLogger(PoolJUnitTest.class);
	static int init_count = 0;

	final static String LF = System.getProperty("line.separator", "\n");
	private SmtpServer smtpServer = null;
	private SmtpConnection smtpConnection = null;

	private SmtpServerService smtpService;
	
	@Before
	public void prepare() {
		smtpService = SpringUtil.getAppContext().getBean(SmtpServerService.class);
		List<SmtpServer> servers = smtpService.getAll(true, null);
		assertFalse(servers.isEmpty());
		smtpServer = servers.get(0);
		smtpConnection = new SmtpConnection(smtpServer);
	}
	
	@Test
	public void testDistribution() throws Exception {
		logger.info(LF + "********** Starting testDistribution **********");

		List<ObjectPool> poolItems = new ArrayList<ObjectPool> ();
		poolItems.add(new ObjectPool(smtpConnection, 2, "smtp1", MailServerType.SMTP, 50));
		// two servers with equal distribution
		NamedPool pools = new NamedPool(poolItems);
		distribution(pools);

		// two servers with unequal distribution
		poolItems.add(new ObjectPool(smtpConnection, 2, "smtp2", MailServerType.SMTP, 50));
		pools = new NamedPool(poolItems);
		List<String> names = pools.getNames();
		Iterator<String> it = names.iterator();
		int row = 70;
		while (it.hasNext()) {
			String name = it.next();
			pools.setDistribution(name, row);
			row += 90;
		}
		distribution(pools);

		// one server has zero value of distribution
		pools.setDistribution("smtp1", 0);
		distribution(pools);

		// only one server
		Object obj = pools.remove("smtp1");
		assertNotNull(obj);
		distribution(pools);
		pools.close();
	}

	@Test
	public void testSmtpConnection() throws Exception {
		logger.info(LF + "********** Starting testSmtpConnection **********");
		{
			NamedPool pools = SmtpWrapperUtil.getSmtpNamedPool();
			assertNotNull(pools);
			if (pools.size() > 0) {
				ObjectPool pool = pools.getPools().get(0);
				int size = pool.getSize();
				//assertEquals(size, 2);
				SmtpConnection[] conn = new SmtpConnection[size];
				for (int i = 0; i < size; i++) {
					conn[i] = (SmtpConnection) pool.getItem();
					assertNotNull(conn[i]);
					conn[i].testConnection(true);
				}
				assertEquals(pool.getNumberOfFreeItems(), 0);
				for (int i = 0; i < size; i++) {
					pool.returnItem(conn[i]);
				}
				assertEquals(pool.getNumberOfFreeItems(), size);
			}
			pools.close();
		}
		
		{
			NamedPool pools = SmtpWrapperUtil.getSecuNamedPool();
			assertNotNull(pools);
			if (pools.size() > 0) {
				ObjectPool pool = pools.getPools().get(0);
				int size = pool.getSize();
				//assertEquals(size, 2);
				SmtpConnection[] conn = new SmtpConnection[size];
				for (int i = 0; i < size; i++) {
					conn[i] = (SmtpConnection) pool.getItem();
					assertNotNull(conn[i]);
					conn[i].testConnection(true);
				}
				assertEquals(pool.getNumberOfFreeItems(), 0);
				for (int i = 0; i < size; i++) {
					pool.returnItem(conn[i]);
				}
				assertEquals(pool.getNumberOfFreeItems(), size);
			}
			pools.close();
		}
	}

	@Test
	public void testAllSmtpPools() throws Exception {
		logger.info(LF + "********** Starting testAllSmtpPools **********");
		NamedPool pools = SmtpWrapperUtil.getSmtpNamedPool();
		assertNotNull(pools);
		for (int i=0; i<pools.size(); i++) {
			ObjectPool pool = pools.getPools().get(i);
			smtpPools(pools, pool.getName());
		}
		logger.info("++++++++++ Now Testing Anonymous getConnection()");
		for (int i = 0; pools.size()> 0 && i < 4; i++) {
			smtpPools(pools);
		}
		SmtpWrapperUtil.clearSmtpNamedPool();
	}

	@Test
	public void testPostSmtpPools() throws Exception {
		logger.info(LF + "********** Starting testPostSmtpPools **********");
		List<ObjectPool> poolItems = new ArrayList<ObjectPool>();
		poolItems.add(new ObjectPool(smtpConnection, 2, "postfix1", MailServerType.SMTP, 50));
		poolItems.add(new ObjectPool(smtpConnection, 2, "smtpsvr1", MailServerType.SMTP, 50));
		NamedPool pools = new NamedPool(poolItems);
		assertNotNull(pools);
		assertTrue(pools.size() > 0);
		smtpPools(pools, "postfix1");
		smtpPools(pools, "smtpsvr1");
		logger.info("++++++++++ Now Testing POST Anonymous getConnection()");
		for (int i = 0; i < 4; i++) {
			smtpPools(pools);
		}
		pools.close();
	}

	@Test
	public void testExchSmtpPools() throws Exception {
		logger.info(LF + "********** Starting testExchSmtpPools **********");
		List<ObjectPool> poolItems = new ArrayList<ObjectPool>();
		poolItems.add(new ObjectPool(smtpConnection, 2, "postexch", MailServerType.SMTP, 50));
		poolItems.add(new ObjectPool(smtpConnection, 2, "exchsvr1", MailServerType.SMTP, 50));
		NamedPool pools = new NamedPool(poolItems);
		assertNotNull(pools);
		assertTrue(pools.size() > 0);
		Object obj = pools.remove("postexch");
		assertNotNull(obj);
		smtpPools(pools, "exchsvr1");
		logger.info("++++++++++ Now Testing EXCH Anonymous getConnection()");
		for (int i = 0; i < 4; i++) {
			smtpPools(pools);
		}
		pools.close();
	}

	private void distribution(NamedPool pools) {
		pools.setUseDistribution(false);
		List<String> names = pools.getNames();
		for (int j = 0; j < 10; j++) {
			for (int i = 0; i < pools.size(); i++) {
				String name = pools.getNextName();
				assertEquals(name, names.get(i));
			}
		}

		pools.setUseDistribution(true);
		Hashtable<String, Integer> map = new Hashtable<String, Integer>();
		for (int i = 0; i < pools.getTotalDistributions(); i++) {
			String name = pools.getNextName();
			if (map.get(name) == null) {
				map.put(name, Integer.valueOf(1));
			}
			else {
				Integer cnt = (Integer) map.get(name);
				map.put(name, Integer.valueOf(cnt.intValue() + 1));
			}
		}
		Enumeration<?> enu = map.keys();
		while (enu.hasMoreElements()) {
			String name = (String) enu.nextElement();
			Integer count = (Integer) map.get(name);
			int dist = pools.getDistribution(name);
			logger.info("name=" + name + ", count=" + count.intValue() + ", dist=" + dist);
			if (dist == 0)
				assertTrue(count.intValue() == 0);
			else {
				assertTrue(((float) count.intValue() / dist) > 0.7);
				assertTrue(((float) count.intValue() / dist) < 1.3);
			}
		}
		logger.info("");
	}

	private void smtpPools(NamedPool pools, String name) throws Exception {
		ObjectPool pool = pools.getPool(name);
		SmtpConnection[] conn = new SmtpConnection[pool.getSize()];
		for (int i = 0; i < pool.getSize(); i++) {
			conn[i] = (SmtpConnection) pools.getConnection(name);
			assertNotNull(conn[i]);
			conn[i].testConnection(true);
		}
		for (int i = 0; i < pool.getSize(); i++) {
			pools.returnConnection(name, conn[i]);
		}
	}

	private void smtpPools(NamedPool pools) throws Exception {
		SmtpConnection conn = (SmtpConnection) pools.getConnection();
		assertNotNull(conn);
		conn.testConnection(true);
		pools.returnConnection(conn);
	}
}
