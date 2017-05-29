package jpa.service.msgout;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import jpa.constant.MailServerType;
import jpa.model.SmtpServer;
import jpa.spring.util.SpringUtil;

import org.apache.log4j.Logger;

/**
 * a common object pool.
 */
public class ObjectPool implements java.io.Serializable {
	private static final long serialVersionUID = 2272863504438513367L;
	protected static final Logger logger = Logger.getLogger(ObjectPool.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();

	private final Vector<Object> freeConns;
	private final Hashtable<Object, java.util.Date> inUse;
		// where the in-use connections are kept
	private final String poolItemName;
	private final int maxConns;
	private final int initialItems;
	private final int increment;
	private int freeItemsLastCycle = 0;
	// used to keep the minimum free connections
	// available during a refresh cycle. to be used
	// by the adjustPoolSize() to release free connections.

	private final SmtpServer smtpConnVo;
	
	private String name;
	private MailServerType type;
	private int distribution;
	private static int name_seq=0;

	/**
	 * create a pool instance.<br>
	 * 
	 * @param pool_item -
	 *            spring bean id of pool item
	 * @param max_conns -
	 *            the maximum number of connections allowed
	 */
	public ObjectPool(SmtpConnection pool_item, int max_conns) {
		this(pool_item, max_conns, "pool-" + getNextNumber(), MailServerType.SMTP, 100);
	}

	/**
	 * create a pool instance.<br>
	 * 
	 * @param pool_item -
	 *            spring bean id of pool item
	 * @param max_conns -
	 *            the maximum number of connections allowed
	 * @param _name -
	 *            the name of the pool, used by NamedPool class
	 * @param _type -
	 *            the pool type, used by NamedPool class
	 * @param _dist -
	 *            the distribution ratio, used by NamedPool class
	 */
	public ObjectPool(SmtpConnection pool_item,
			int max_conns,
			String _name,
			MailServerType _type,
			int _dist) {
		this.poolItemName = pool_item.getSmtpServer().getServerName();
		if (_name==null) {
			name = "pool-"+getNextNumber();
		}
		else {
			name = _name;
		}
		type = _type;
		distribution = _dist;
		maxConns = max_conns < 1 ? 1 : max_conns;

		int _initialItems = maxConns / 5;
		if (_initialItems < 1) {
			_initialItems = 1;
		}
		initialItems = _initialItems;
		int _increment = initialItems / 2;
		if (_increment < 1) {
			_increment = 1;
		}
		increment = _increment;
		freeConns = new Vector<Object>();
		inUse = new Hashtable<Object, java.util.Date>();

		smtpConnVo = pool_item.getSmtpServer();
		// Put our pool of Connections in the Vector
		for (int i = 0; i < initialItems; i++) {
			Object obj = new SmtpConnection(smtpConnVo);
			// Class cls = obj.getClass();
			freeConns.addElement(obj);
		}

		freeItemsLastCycle = initialItems;
	}

	/**
	 * create a SMTP connection pool instance.<br>
	 * 
	 * @param smtpConnVo -
	 *            SMTP connection properties
	 */
	public ObjectPool(SmtpServer vo) {
		this(vo,100);
	}
	
	/**
	 * create a SMTP connection pool instance.<br>
	 * 
	 * @param smtpConnVo -
	 *            SMTP connection properties
	 * @param _dist -
	 *            the distribution ratio, used by NamedPool class
	 */
	public ObjectPool(SmtpServer vo, int _dist) {
		this.poolItemName = vo.getServerName();
		name = poolItemName;
		type = MailServerType.getByValue(vo.getServerType());
		distribution = _dist;
		maxConns = vo.getNumberOfThreads();

		int _initialItems = maxConns / 5;
		if (_initialItems < 1) {
			_initialItems = 1;
		}
		initialItems = _initialItems;
		int _increment = initialItems / 2;
		if (_increment < 1) {
			_increment = 1;
		}
		increment = _increment;
		freeConns = new Vector<Object>();
		inUse = new Hashtable<Object, java.util.Date>();

		// Put our pool of Connections in the Vector
		for (int i = 0; i < initialItems; i++) {
			Object obj = new SmtpConnection(vo);
			// Class cls = obj.getClass();
			freeConns.addElement(obj);
		}

		freeItemsLastCycle = initialItems;
		smtpConnVo = vo;
	}

	/**
	 * retrieve a connection object from the pool
	 * 
	 * @return a connection object
	 */
	public synchronized Object getItem() {
		if (isEmpty()) {
			throw new IllegalStateException("The ObjectPool is empty, it may have been closed.");
		}
		if (freeConns.size() > 0) {
			Object obj = freeConns.firstElement();

			// remember the minimum free connections
			freeItemsLastCycle = Math.min(freeItemsLastCycle, freeConns.size());

			freeConns.removeElementAt(0);

			// Update the Hash table to show this one is taken
			inUse.put(obj, new java.util.Date());
			
			if (isDebugEnabled)
				logger.debug(name + "/" + type + ": " + "Found a free pool item, pool size: "
					+ (freeConns.size() + inUse.size()) + ", in use: " + inUse.size());
			// Return the item
			return obj;
		}

		// If we get here, there were no free connections.
		// We've got to make more if the maximum size hasn't been reached.
		if (freeConns.size() + inUse.size() < maxConns) {
			for (int i = 0; i < increment; i++) {
				if (smtpConnVo == null) {
					freeConns.addElement(SpringUtil.getAppContext().getBean(poolItemName));
				}
				else {
					freeConns.addElement(new SmtpConnection(smtpConnVo));
				}
			}
		}
		else { // wait for next available connection
			if (isDebugEnabled)
				logger.debug("Allocated items has reached the limit, entering wait()...");
			try {
				wait();
				if (isDebugEnabled)
					logger.debug("Notified by returnItem(), exiting from the wait()...");
			}
			catch (InterruptedException e) {
				logger.error("ObjectPool.getItem() - InterruptedException caught. "
						+ new java.util.Date());
			}
		}

		// Recurse to get one of the new connections.
		return getItem();
	}

	/**
	 * return a connection object back to the pool
	 * 
	 * @param returned
	 *            the connection object to be returned
	 */
	public synchronized void returnItem(Object returned) {
		returnItem(returned, false);
	}

	/**
	 * return a connection object back to the pool.
	 * 
	 * @param returned -
	 *            connection object to be returned
	 * @param remove -
	 *            if true, remove the connection from the pool
	 */
	public synchronized void returnItem(Object returned, boolean remove) {
		if (inUse.containsKey(returned)) { // first remove it from inUse
			inUse.remove(returned);
		}
		else {
			logger.error("Error: Returned item not matching any pool item.");
		}
		if (!remove) {
			freeConns.addElement(returned);
		}
		if (isDebugEnabled)
			logger.debug(name + "/" + type + ": " + "Returned a pool item, pool size: "
				+ (freeConns.size() + inUse.size()) + ", in use: " + inUse.size());

		// wake up the threads that are waiting for available connections
		notifyAll();
	}

	/*
	 * refresh connection to prevent idled connections from timeout 
	 * @throws Exception if any error
	 */
	synchronized void refreshPool() throws Exception {
		if (freeConns.size() > 0) {
			if (isDebugEnabled)
				logger.debug("ObjectPool.refreshPool(): Refresh the oldest free item.");
			Object obj = getItem();
			returnItem(obj);
		}
	}

	/*
	 * reduce the pool size when there are too many idled connections
	 */
	synchronized void adjustPoolSize() {
		if (isDebugEnabled)
			logger.debug("freeItemsLastCycle/initialItems: " + freeItemsLastCycle + "/" + initialItems);
		if (freeItemsLastCycle > initialItems) {
			int number2keep;
			if (freeItemsLastCycle / 2 > initialItems) {
				number2keep = freeItemsLastCycle / 2;
			}
			else {
				number2keep = initialItems;
			}
			// release free connections gradually until it reaches the minimum.
			for (int i = freeItemsLastCycle; i > number2keep; i--) {
				freeConns.removeElementAt(0);
			}
		}

		freeItemsLastCycle = freeConns.size() + inUse.size();
		if (isDebugEnabled)
			logger.debug("ObjectPool: adjusted the pool size to: " + freeItemsLastCycle);
	}
	
	/**
	 * clear the pool, close and remove all connections from the pool.
	 */
	public synchronized void close() {
		if (freeConns.size() > 0) {
			// close connections that are idle
			try {
				// test if a close() method is present
				freeConns.get(0).getClass().getMethod("close", (Class[])null);
				for (int i=0; i<freeConns.size(); i++) {
					Object obj = freeConns.get(i);
					Method method = obj.getClass().getMethod("close", (Class[])null);
					try {
						method.invoke(obj, (Object[])null);
					}
					catch (Exception e) {
						logger.error("Exception caught: " + e.getMessage());
					}
				}
			}
			catch (Exception e) {
				// close() method not found
				logger.info("close() method not found in: " + freeConns.get(0).getClass().getName());
			}
		}
		freeConns.clear();
		if (inUse.size() > 0) {
			// close connections that are still in use
			Enumeration<?> enu = inUse.keys();
			while (enu.hasMoreElements()) {
				Object obj = enu.nextElement();
				try {
					Method method = obj.getClass().getMethod("close", (Class[])null);
					method.invoke(obj, (Object[])null);
				}
				catch (Exception e) {
					logger.error("Exception caught: " + e.getMessage());
				}
			}
		}
		inUse.clear();
		logger.info("close() - ObjectPool " + getName() + " has been closed.");
	}
	
	public boolean isEmpty() {
		return (freeConns.isEmpty() && inUse.isEmpty());
	}

	/**
	 * return the maximum connections allowed
	 * 
	 * @return the maximum connections allowed
	 */
	public int getSize() {
		return maxConns;
	}

	public int getNumberOfFreeItems() {
		//return freeConns.size();
		return maxConns - inUse.size();
	}
	
	/**
	 * set the pool name
	 * 
	 * @param _name -
	 *            pool name
	 */
	void setName(String _name) {
		name = _name;
	}

	/**
	 * set the pool type
	 * 
	 * @param _type -
	 *            pool type
	 */
	void setType(MailServerType _type) {
		type = _type;
	}

	/**
	 * set the pool distribution
	 * 
	 * @param _dist -
	 *            distribution ratio
	 */
	void setDistribution(int _dist) {
		distribution = _dist;
	}

	/**
	 * get the pool name
	 * 
	 * @return pool name
	 */
	public String getName() {
		return name;
	}

	/**
	 * get the pool type
	 * 
	 * @return pool type
	 */
	public MailServerType getType() {
		return type;
	}

	/**
	 * get the pool distribution
	 * 
	 * @return pool distribution ratio
	 */
	public int getDistribution() {
		return distribution;
	}

	/*
	 * get next sequence number, it is used to name the pool if the pool name is
	 * not given at time of its creation.
	 * @return the next sequence number
	 */
	private synchronized static int getNextNumber() {
		name_seq++;
		return name_seq;
	}

	public Vector<?> getFreeItems() {
		return freeConns;
	}
}