package jpa.util;

import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HostUtil {
	static final Logger logger = LogManager.getLogger(HostUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	/** host IP address and host name */
	private static String hostIPAddr = null;
	private static String hostName = null;

	/**
	 * @return IP address of the machine this program is running on
	 */
	public static String getHostIpAddress() {
		if (hostIPAddr == null) {
			try {
				hostIPAddr = java.net.InetAddress.getLocalHost().getHostAddress();
				if (isDebugEnabled) {
					logger.debug("Host IP Address: " + hostIPAddr);
				}
			}
			catch (UnknownHostException e) {
				logger.warn("UnknownHostException caught, use 127.0.0.1", e);
				hostIPAddr = "127.0.0.1";
			}
		}
		return hostIPAddr;
	}

	/**
	 * @return Host name of the machine this program is running on
	 */
	public static String getHostName() {
		if (hostName == null) {
			try {
				hostName = java.net.InetAddress.getLocalHost().getHostName();
				if (isDebugEnabled) {
					logger.debug("Host Name: " + hostName);
				}
			}
			catch (UnknownHostException e) {
				logger.warn("UnknownHostException caught, use localhost", e);
				hostName = "localhost";
			}
		}
		return hostName;
	}

}
