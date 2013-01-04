package client.bidding;

import java.net.InetAddress;

public class User 
{
	private String username = "";
	private int tcpPort = 0;
	private InetAddress host = null;
	
	public User(InetAddress host, int tcpPort, String username) {
		this.host = host;
		this.tcpPort = tcpPort;
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public InetAddress getHost() {
		return host;
	}

	public void setHost(InetAddress host) {
		this.host = host;
	}
}
