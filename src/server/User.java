package server;

import java.net.Socket;
import java.util.ArrayList;

public class User 
{
	private String username = "";
	private Socket socket = null;
	private int udpPort = 0;
	private boolean isLoggedIn = false;
	private ArrayList<String> dueNotifications = new ArrayList<String>();
	
	public User(Socket socket) {
		this.socket = socket;
	}
	
	public User(String username, Socket socket, int udpPort) {
		this.username = username;
		this.socket = socket;
		this.udpPort = udpPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public int getUdpPort() {
		return udpPort;
	}

	public void setUdpPort(int udpPort) {
		this.udpPort = udpPort;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public ArrayList<String> getDueNotifications() {
		return dueNotifications;
	}

	public void setDueNotifications(ArrayList<String> dueNotifications) {
		this.dueNotifications = dueNotifications;
	}
	
	
}
