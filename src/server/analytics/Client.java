package server.analytics;

import java.util.ArrayList;
import java.util.HashMap;

import client.mgmt.ManagementClientInterface;

public class Client {
	private ManagementClientInterface mClient;
	private HashMap<Integer, Subscription> subscriptions;
	private ArrayList<Event> buffer;

	public Client(ManagementClientInterface mClient) {
		this.mClient = mClient;
		subscriptions = new HashMap<Integer, Subscription>();
		buffer = new ArrayList<Event>();
	}
	
	public ManagementClientInterface getmClient() {
		return mClient;
	}



	public void setmClient(ManagementClientInterface mClient) {
		this.mClient = mClient;
	}

	public HashMap<Integer, Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(HashMap<Integer, Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public ArrayList<Event> getBuffer() {
		return buffer;
	}

	public void setBuffer(ArrayList<Event> buffer) {
		this.buffer = buffer;
	}
}
