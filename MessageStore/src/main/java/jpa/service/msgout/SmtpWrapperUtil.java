package jpa.service.msgout;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jpa.model.SmtpServer;
import jpa.spring.util.SpringUtil;

public class SmtpWrapperUtil implements java.io.Serializable {
	private static final long serialVersionUID = -5036775609137065316L;

	static final Logger logger = LogManager.getLogger(SmtpWrapperUtil.class);
	
	private static SmtpServerService smtpServerDao = null;
	private static java.util.Date lastGetTime = new java.util.Date();
	private static NamedPool smtpPools = null;
	private static NamedPool secuPools = null;
	
	public static void main(String[] args) {
		NamedPool pools = getSmtpNamedPool();
		for (String name :pools.getNames()) {
			logger.info("Pool name: " + name);
		}
		List<ObjectPool> objPools = pools.getPools();
		int _size = 0;
		for (int i = 0; i < objPools.size(); i++) {
			ObjectPool pool = objPools.get(i);
			_size += pool.getSize();
		}
		logger.info("Total Connections: " + _size);
		SmtpConnection[] conns = new SmtpConnection[_size];
		try {
			for (int i = 0; i < _size; i++) {
				conns[i] = (SmtpConnection) pools.getConnection();
				conns[i].testConnection(true);
			}
			for (int i = 0; i < _size; i++) {
				pools.returnConnection(conns[i]);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public synchronized static NamedPool getSmtpNamedPool() {
		refreshPool();
		return smtpPools;
	}
	
	public synchronized static NamedPool getSecuNamedPool() {
		refreshPool();
		return secuPools;
	}

	public synchronized static void clearSmtpNamedPool() {
		if (smtpPools != null) {
			if (!smtpPools.isEmpty()) {
				smtpPools.close();
			}
			smtpPools = null;
		}
	}

	public synchronized static void clearSecuNamedPool() {
		if (secuPools != null) {
			if (!secuPools.isEmpty()) {
				secuPools.close();
			}
			secuPools = null;
		}
	}

	private static void refreshPool() {
		java.util.Date currTime = new java.util.Date();
		if (smtpPools == null || secuPools == null || (smtpPools.isEmpty() && secuPools.isEmpty())
				|| (currTime.getTime() - lastGetTime.getTime()) > (15 * 60 * 1000)) {
			clearSmtpNamedPool();
			clearSecuNamedPool();
			List<SmtpServer> smtpConnVos = getSmtpServers(false);
			smtpPools =getNamedPool(smtpConnVos);
			List<SmtpServer> secuConnVos = getSmtpServers(true);
			secuPools =getNamedPool(secuConnVos);
			lastGetTime = currTime;
		}
	}

	/*
	 * returns a list of named pools or an empty list
	 */
	private static NamedPool getNamedPool(List<SmtpServer> smtpConnVos) {
		List<ObjectPool> objPools = new ArrayList<ObjectPool>();
		for (int i = 0; i < smtpConnVos.size(); i++) {
			SmtpServer smtpConnVo = smtpConnVos.get(i);
			ObjectPool smtpPool = new ObjectPool(smtpConnVo);
			objPools.add(smtpPool);
		}
		NamedPool pools = new NamedPool(objPools);
		return pools;
	}

	/*
	 * returns a list or SmtpServer's or an empty list 
	 */
	private static List<SmtpServer> getSmtpServers(boolean isSecure) {
		List<SmtpServer> list = null;
		list = getSmtpServerService().getAll(true, isSecure);
		return list;
	}
	
	private static SmtpServerService getSmtpServerService() {
		if (smtpServerDao == null) {
			smtpServerDao = SpringUtil.getAppContext().getBean(SmtpServerService.class);
		}
		return smtpServerDao;
	}
}
