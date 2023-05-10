package org.eclipse.slm.common.keycloak.config;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class MyIpUtil.
 *
 * @author des
 */
public final class ConnectivityCheckUtil {

	private static final Logger LOG = Logger.getLogger(ConnectivityCheckUtil.class.getName());
	private static final int DEFAULT_TIMEOUT = 1000;

	/**
	 * Instantiates a new my ip util.
	 */
	private ConnectivityCheckUtil(){}

	/**
	 * Gets the ip address.
	 *
	 * @return the ip address
	 */
	public static String getIpAddress() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {

				// filters out 127.0.0.1 and inactive interfaces
				NetworkInterface iface = interfaces.nextElement();
				try {
					if (iface.isLoopback() || !iface.isUp()) {
						continue;
					}
				} catch (SocketException e) {
					LOG.log(Level.WARNING,e.getMessage(),e);
				}

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					String ip = addr.getHostAddress();

					if(addr instanceof Inet6Address) {
						// for ipv6 if network port is added
						ip = "["+ip.split("%")[0]+"]";
						continue;  // because ipv6 ain't working yet
					}

					LOG.fine("Local network interface found: " + iface.getDisplayName() + " your ip is " + ip);
					return ip;
				}
			}
		} catch (SocketException e) {
			LOG.log(Level.WARNING,e.getMessage(),e);
		}
		return null;
	}

	/**
	 * Gets all the ip address.
	 *
	 * @return the ip address
	 */
	public static String[] getAllIpAddress() {
		List<String> ips= new ArrayList<>();
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {

				// filters out 127.0.0.1 and inactive interfaces
				NetworkInterface iface = interfaces.nextElement();
				try {
					if (iface.isLoopback() || !iface.isUp()) {
						continue;
					}
				} catch (SocketException e) {
					LOG.log(Level.WARNING,e.getMessage(),e);
				}

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					String ip = addr.getHostAddress();

					if(addr instanceof Inet6Address) {
						// for ipv6 if network port is added
						ip = "["+ip.split("%")[0]+"]";
						continue;  // because ipv6 ain't working yet
					}

					LOG.fine("Local network interface found: " + iface.getDisplayName() + " your ip is " + ip);
					ips.add(ip);
				}
			}
		} catch (SocketException e) {
			LOG.log(Level.WARNING,e.getMessage(),e);
		}
		return ips.toArray(new String[]{});
	}

	public static String getFirstReachableIpAddress(int port) {
		return getFirstReachableIpAddress(getAllIpAddress(), port);
	}

	public static String getFirstReachableIpAddress(String[] ips, int port) {
		for (String ip : Arrays.asList(ips)) {
			if(isIpReachable(ip, port)){
				return ip;
			} else {
				LOG.fine("Ip " + ip + " is not reachable");
			}
		}
		return null;
	}

	/**
	 * Checks if is reachable.
	 *
	 * @param testUrl the test url
	 * @return true, if is reachable
	 */
	public static boolean isIpReachable(String testUrl) {
		return isIpReachable(testUrl, 80);
	}

	/**
	 * Tries to test the reachability of the ip given (as a url).
	 *
	 * @param testUrl which should contain an ip address
	 * @param port    the port
	 * @return boolean result
	 */
	public static boolean isIpReachable(String testUrl, int port) {
		if(testUrl == null || testUrl.isEmpty()){
			return false;
		}

		try (Socket socket = new Socket()){
			socket.connect(new InetSocketAddress(testUrl, port), DEFAULT_TIMEOUT);
		} catch (IOException e) {
			LOG.log(Level.WARNING,"Exception during "+testUrl+":"+port+" caused by "+e.getMessage(),e);
			return false;
		}
		return true;
	}

	/**
	 * Is url available boolean.
	 *
	 * @param url the url
	 * @return the boolean
	 */
	public static boolean isUrlAvailable(String url) {
		try {
			URL myUrl = new URL(url);
			URLConnection connection = myUrl.openConnection();
			connection.setConnectTimeout(DEFAULT_TIMEOUT);
			connection.connect();
			LOG.fine(url + " is available");
			return true;
		} catch (IOException e) {
			LOG.fine(url + " is not available");
			return false;
		}
	}

	/**
	 * Is url valid boolean.
	 *
	 * @param url the url
	 * @return the boolean
	 */
	public static boolean isUrlValid(String url) {
		if (url == null){
			return false;
		}
		String regex = "^(http://|https://)[a-z0-9]+([-.]{1}[a-z0-9]+)*(\\.[a-z]{2,5}|(:[0-9]{1,5})?)(/.*)?";
		//String regex = "^(http://|https://)(((localhost|[0-9]+([-.]{1}[0-9]+)*)(:[0-9]{1,5})?)|([a-z0-9]+([-.]{1}[a-z0-9]+)*(\\.[a-z]{2,5}(:[0-9]{1,5})?)))(/.*)?";
		try {
			Pattern patt = Pattern.compile(regex);
			Matcher matcher = patt.matcher(url.toLowerCase());
			return matcher.matches();
		} catch (RuntimeException e) {
			return false;
		}
	}



}
