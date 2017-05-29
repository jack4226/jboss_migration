package jpa.tomee.util;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class TomeeCtxUtil {
	protected final static Logger logger = Logger.getLogger(TomeeCtxUtil.class);

	public static javax.naming.Context getLocalContext() throws NamingException {
		Properties p = new Properties();
		p.put("java.naming.factory.initial", "org.apache.openejb.client.LocalInitialContextFactory");
		try {
			InitialContext context = new InitialContext(p);
			return context;
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw e;
		}
	}
	
	public static javax.naming.Context getRemoteContext() throws NamingException {
		Properties p = new Properties();
		p.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
		//p.put("java.naming.provider.url", "ejbd://localhost:4201"); // OpenEjb
		int port = findHttpPort(new int[] { 8181, 8080 });
		p.put("java.naming.provider.url", "http://127.0.0.1:" + port + "/tomee/ejb"); // TomEE
		// user and pass optional
		p.put("java.naming.security.principal", "tomee");
		p.put("java.naming.security.credentials", "tomee");
		try {
			InitialContext context = new InitialContext(p);
			return context;
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw e;
		}
	}

	public static javax.naming.Context getActiveMQContext(String... queueNames) throws NamingException {
		Properties props = new Properties();
		props.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
				"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		props.setProperty(javax.naming.Context.PROVIDER_URL, "tcp://127.0.0.1:61616");
		
		// trust all packages for ActiveMQ
		props.setProperty("trustAllPackages", "true");
		
		//specify queue property name as queue.jndiname
		if (queueNames != null && queueNames.length > 0) {
			for (String queueName : queueNames) {
				props.setProperty("queue."+ queueName, queueName);
			}
		}
		
		try {
			javax.naming.Context context = new InitialContext(props);
			return context;
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw e;
		}
	}

	public static void listContext(javax.naming.Context context, String listName) {
    	try {
			NamingEnumeration<NameClassPair> list = context.list(listName);
			while (list!=null && list.hasMore()) {
				NameClassPair pair = list.next();
				String name = pair.getName();
				logger.info("Name: " + listName + "/" + name);
				if (StringUtils.isNotBlank(name)) {
					listContext(context, listName + "/" + name);
				}
			}
		} catch (NamingException e) {
			logger.error("NamingException caught: " + e.getMessage());
		}
    }

	private static Integer tomcat_port = null;
	
	public static int findHttpPort(int... ports) {
		if (tomcat_port != null) {
			return tomcat_port;
		}
		String hostIP = "127.0.0.1";
		Socket socket = null;
		long start = System.currentTimeMillis();
		for (int port : ports) {
			socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(hostIP, port), 1000);
				logger.info("Port (" + port + ") reachable.");
				tomcat_port = port;
				return tomcat_port;
			}
			catch (java.io.IOException e) {
				logger.info("Port (" + port + ") unreachable, time spent: " + (System.currentTimeMillis() - start));
			}
			finally {
				if (socket != null) {
					try {
						socket.close();
					}
					catch (java.io.IOException e) {
						logger.error("IOException caught: " + e.getMessage());
					}
				}
			}
		}
		throw new RuntimeException("Tomcat or TomEE is down or not listening to one of the ports: " + Arrays.toString(ports));
	}

	public static boolean isActiveMQUp() {
		String hostIP = "127.0.0.1";
		int port = 61616;
		return isHostPortUp(hostIP, port, 1000);
	}
	
	static boolean isHostPortUp(String hostIP, int port, int timeout) {
		long start = System.currentTimeMillis();
		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(hostIP, port), timeout);
			logger.info("Port (" + port + ") reachable.");
			return true;
		}
		catch (java.io.IOException e) {
			logger.info("Port (" + port + ") unreachable, time spent: " + (System.currentTimeMillis() - start));
		}
		finally {
			if (socket != null) {
				try {
					socket.close();
				}
				catch (java.io.IOException e) {
					logger.error("IOException caught: " + e.getMessage());
				}
			}
		}
		return false;
	}


	public static void main(String[] args) {
		try {
			logger.info("port found: " + findHttpPort(new int[] {8181,8080,8161}));
			// test JMS remote access
			Context ctx = getActiveMQContext((String[])null);
			listContext(ctx, "");
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
}
