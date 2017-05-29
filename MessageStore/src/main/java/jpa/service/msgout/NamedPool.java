package jpa.service.msgout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * create a named pool. A pool contains sub-pools, and the sub-pool could be
 * accessed by name, or accessed in round-robin fashion.
 * <p>
 * multiple sub-pools of same type could be used to serve as a bigger single
 * pool, the load of each sub-pool can be controlled by "distribution". For
 * example:
 * <pre>
 * assume two sub-pools with distribution ratios of:
 * sub-pool 1 : 40
 * sub-pool 2 : 60
 * the service load applied to them will be:
 * sub-pool 1 : 40% of the load
 * sub-pool 2 : 60% of the load
 * </pre>
 */
public final class NamedPool implements java.io.Serializable {
	private static final long serialVersionUID = -447731141642759939L;
	static final Logger logger = Logger.getLogger(NamedPool.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private final LinkedHashMap<String, ObjectPool> pools;
	private ArrayList<String> nameList;
	private Set<String> nameSet;
	private final Hashtable<Object, String> inUseConns;
	private int position;
	private int[] distArray;
	private int distSum;
	private boolean useDist = false;
	private Random random;

	/**
	 * constructor, create a named pool instance
	 */
	public NamedPool() {
		pools = new LinkedHashMap<String, ObjectPool>();
		nameSet = pools.keySet();
		nameList = new ArrayList<String>(nameSet);
		inUseConns = new Hashtable<Object, String>();
		position = 0;
		random = new Random(new Date().getTime());
	}

	/**
	 * constructor, create a named pool instance
	 * 
	 * @param objPools -
	 *            a list of ObjectPool instances
	 */
	public NamedPool(List<ObjectPool> objPools) {
		pools = new LinkedHashMap<String, ObjectPool>();
		for (Iterator<ObjectPool> it = objPools.iterator(); it.hasNext();) {
			ObjectPool pool = it.next();
			if (pools.containsKey(pool.getName())) {
				logger.warn("Overriding a named pool: " + pool.getName());
			}
			add(pool.getName(), pool);
		}
		nameSet = pools.keySet();
		nameList = new ArrayList<String>(nameSet);
		inUseConns = new Hashtable<Object, String>();
		position = 0;
		random = new Random(new Date().getTime());
		logger.info("Named Pools Created for: " + nameList);
	}

	/**
	 * retrieve a connection object from a named sub-pool
	 * 
	 * @param name -
	 *            sub-pool name
	 * @return a connection object from the sub-pool
	 * @throws IllegalArgumentException
	 *             if the sub-pool does not exist
	 */
	public synchronized Object getConnection(String name) {
		if (isEmpty()) {
			throw new IllegalStateException("The NamedPool are empty, either not initialized or have been closed.");
		}
		ObjectPool pool = (ObjectPool) pools.get(name);
		if (pool == null) {
			throw new IllegalArgumentException("ObjectPool " + name + " does not exist.");
		}
		else {
			return pool.getItem();
		}
	}

	/**
	 * return a connection object back to the named sub-pool
	 * 
	 * @param name -
	 *            sub-pool name
	 * @param conn -
	 *            connection object to be returned
	 * @throws IllegalArgumentException
	 *             if the sub-pool does not exist
	 */
	public synchronized void returnConnection(String name, Object conn) {
		if (inUseConns.containsKey(conn)) { // in case of user error
			logger.warn("Connection returned was from getConnection(), returns it anyway.");
			returnConnection(conn);
		}
		else {
			ObjectPool pool = (ObjectPool) pools.get(name);
			if (pool == null)
				throw new IllegalArgumentException("ObjectPool " + name + " does not exist.");
			else
				pool.returnItem(conn);
		}
	}

	/**
	 * retrieve a connection object from next available sub-pool.
	 * 
	 * @return a connection object from the sub-pool
	 * @throws IllegalArgumentException
	 *             if the next sub-pool does not exist
	 */
	public synchronized Object getConnection() {
		if (isEmpty()) {
			throw new IllegalStateException("The NamedPool are empty, either not initialized or have been closed.");
		}
		for (int i=0; i<size(); i++) {
			ObjectPool pool = getNextPool();
			if (pool == null) {
				throw new IllegalArgumentException("ObjectPool does not exist.");
			}
			else {
				if (pool.getNumberOfFreeItems() > 0) {
					Object conn = pool.getItem();
					inUseConns.put(conn, pool.getName());
					return conn;
				}
			}
		}
		
		if (isDebugEnabled)
			logger.debug("getConnection() - All pools are empty, entering wait()...");
		try {
			wait();
			if (isDebugEnabled)
				logger.debug("getConnection() - Notified by return(), exiting the wait()...");
		}
		catch (InterruptedException e) {
			logger.error("getConnection() - InterruptedException caught. " + new java.util.Date());
		}

		return getConnection();
	}

	/**
	 * return a connection object back to sub-pool. The connection object
	 * returned must be obtained from getConnection() method.
	 * 
	 * @param conn -
	 *            the connection object to be returned
	 * @throws IllegalArgumentException
	 *             if the named pool does not exist
	 */
	public synchronized void returnConnection(Object conn) {
		if (!inUseConns.containsKey(conn)) {
			throw new IllegalArgumentException("Connection returned was not from getConnection() method.");
		}
		else {
			String name = (String) inUseConns.get(conn);
			ObjectPool pool = getPool(name);
			inUseConns.remove(conn);
			pool.returnItem(conn);
			notifyAll();
		}
	}

	/**
	 * see if a named sub-pool exist
	 * 
	 * @param name -
	 *            sub-pool name
	 * @return true if the named sub-pool exist
	 */
	public boolean containsName(String name) {
		return (pools.get(name) != null);
	}

	/**
	 * @return the number of sub-pools contained in the pool
	 */
	public int size() {
		return pools.size();
	}

	/**
	 * return a named ObjectPool instance (a sub-pool)
	 * 
	 * @param name -
	 *            the pool name
	 * @return an pool instance
	 */
	public ObjectPool getPool(String name) {
		return (ObjectPool) pools.get(name);
	}

	/**
	 * retrieve all sub-pool names into an ArrayList
	 * 
	 * @return sub-pool names as an arrayNode
	 */
	public List<String> getNames() {
		return nameList;
	}

	/**
	 * retrieve all sub-pools into an ArrayList
	 * 
	 * @return sub-pools as an arrayNode list
	 */
	public List<ObjectPool> getPools() {
		Collection<ObjectPool> c = pools.values();
		return new ArrayList<ObjectPool>(c);
	}

	/**
	 * Specify how next sub-pool is retrieved. If true, use distribution
	 * algorithm, otherwise use round robin.
	 * 
	 * @param use -
	 *            true to use distribution
	 */
	public void setUseDistribution(boolean use) {
		useDist = use;
	}

	/**
	 * set a sub-pool's distribution
	 * 
	 * @param name -
	 *            sub-pool name
	 * @param distribution -
	 *            ratio
	 */
	public void setDistribution(String name, int distribution) {
		ObjectPool pool = getPool(name);
		if (pool != null) {
			pool.setDistribution(distribution);
			calculateDistributions();
		}
	}

	/**
	 * get a sub-pool's distribution
	 * 
	 * @param name -
	 *            sub-pool name
	 * @return the distribution ratio, -1 if the named sub-pool does not exist
	 */
	public int getDistribution(String name) {
		ObjectPool pool = getPool(name);
		if (pool != null)
			return pool.getDistribution();
		else
			return -1;
	}

	/**
	 * get the sum of all distributions
	 * 
	 * @return the sum of all distributions
	 */
	public int getTotalDistributions() {
		return distSum;
	}

	/**
	 * add a sub-pool to the object
	 * 
	 * @param name -
	 *            sub-pool name
	 * @param pool -
	 *            sub-pool object
	 */
	public synchronized void add(String name, ObjectPool pool) {
		pools.put(name, pool);
		pool.setName(name);
		nameSet = pools.keySet();
		nameList = new ArrayList<String>(nameSet);
		calculateDistributions();
	}

	/**
	 * remove the named sub-pool from the object
	 * 
	 * @param name -
	 *            sub-pool name to be removed
	 * @return pool removed
	 */
	public synchronized ObjectPool remove(String name) {
		ObjectPool pool = (ObjectPool) pools.remove(name);
		nameSet = pools.keySet();
		nameList = new ArrayList<String>(nameSet);
		calculateDistributions();
		return pool;
	}

	/**
	 * remove all sub-pools from the pool object
	 */
	public synchronized void close() {
		// return "in use" connections to their pools
		Enumeration<?> enu = inUseConns.keys();
		while (enu.hasMoreElements()) {
			try {
				Object conn = enu.nextElement();
				returnConnection(conn);
			}
			catch (Exception e) { // just for safety
				logger.error("Exception caught", e);
			}
		}
		inUseConns.clear();
		// close all ObjectPools
		for (Iterator<ObjectPool> it=pools.values().iterator(); it.hasNext(); ) {
			ObjectPool pool = it.next();
			pool.close();
		}
		pools.clear();
		nameList.clear();
		logger.info("close() - NamedPool has been closed.");
	}
	
	public boolean isEmpty() {
		return (pools.isEmpty() && inUseConns.isEmpty());
	}

	/*
	 * get the first sub-pool that matches the type @throws Exception if any
	 * error
	 */
	private ObjectPool getNextPool() {
		String name = null;
		if ((name = getNextName()) != null) {
			return getPool(name);
		}
		else
			return null;
	}

	/**
	 * get the next sub-pool name
	 * 
	 * @return sub-pool name
	 */
	public String getNextName() {
		if (nameList.size() > 0) {
			if (useDist) {
				int range = random.nextInt(distSum);
				for (int i = 0; i < distArray.length; i++) {
					if (range < distArray[i])
						return (String) nameList.get(i);
				}
				throw new RuntimeException("Internal error, contact programming");
			}
			else {
				position %= nameList.size();
				return (String) nameList.get(position++);
			}
		}
		return null;
	}

	/*
	 * calculate distributions and save it to a internal arrayNode
	 */
	private void calculateDistributions() {
		distSum = 0;
		distArray = new int[nameList.size()];
		for (int i = 0; i < nameList.size(); i++) {
			String name = (String) nameList.get(i);
			ObjectPool pool = (ObjectPool) pools.get(name);
			distArray[i] = pool.getDistribution() + distSum;
			distSum += pool.getDistribution();
		}
	}
}
