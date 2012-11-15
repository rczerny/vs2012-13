package server.analytics;

import java.util.ArrayList;
import java.util.HashMap;

public class Client {
	String name;
	HashMap<Integer, Subscription> subscriptions;
	ArrayList<Event> buffer;

	public Client(String name) {
		this.name = name;
		subscriptions = new HashMap<Integer, Subscription>();
		buffer = new ArrayList<Event>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
