package server.analytics;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import tools.PropertiesParser;
import client.mgmt.ManagementClientInterface;

public class AnalyticsServer implements AnalyticsServerRMI{

	private long startTime;

	private List<Client> clients = Collections.synchronizedList(new ArrayList<Client>());
	private int highestSubscriptionId = 1;
	private List<Double> sessionTime = Collections.synchronizedList(new ArrayList<Double>());
	private double minSessionTime = 0;
	private double maxSessionTime = 0;
	private List<UserEvent> userEvents = Collections.synchronizedList(new ArrayList<UserEvent>());
	private double maxBidPrice = 0;
	private List<Double> auctionTime = Collections.synchronizedList(new ArrayList<Double>());
	private List<Boolean> auctionSucess = Collections.synchronizedList(new ArrayList<Boolean>());
	private int bids = 0;
	private String bindingName = "";

	public AnalyticsServer(String bindingName) {
		this.startTime = System.currentTimeMillis()/1000;
		this.bindingName = bindingName;
	}

	@Override
	public String subscribe(ManagementClientInterface mClient, String filter) throws RemoteException {
		Client client = null;
		for(Client c:clients) {
			if(c.getmClient().getId()==mClient.getId()) {
				client = c;
			}
		}
		if(client == null) {
			client = new Client(mClient);
			clients.add(client);
		}	

		Subscription sub = new Subscription(highestSubscriptionId, filter, client);
		String answer = sub.createFilter(filter);
		int id = highestSubscriptionId;

		if(!sub.filter.isEmpty()) {
			client.getSubscriptions().put(highestSubscriptionId, sub);
		}

		if(sub.filter.isEmpty()) {
			if(answer.equals("ALL")) {
				return "Creating subscription failed! - You were already subscribed for ALL of these events!";
			}

			return "Creating subscription failed!";
		}

		highestSubscriptionId++;
		if(answer.equals("SOME")) {
			return "Creating subscription failed for some events! - You were already subscribed for these events!";
		}

		return "Created subscription with ID " + id + " for events using filter " + filter;
	}

	public void processEvent(Event e) throws RemoteException, ConcurrentModificationException {
		if(e instanceof UserEvent) {
			createSessionTime(e);
		}

		if(e instanceof BidEvent) {
			if(e.type.equals("BID_PLACED")) {
				if(((BidEvent) e).getPrice()>maxBidPrice) {
					maxBidPrice = ((BidEvent) e).getPrice();
					StatisticsEvent se = new StatisticsEvent();
					se.setType("BID_PRICE_MAX");
					se.setTimestamp(System.currentTimeMillis());
					se.setValue(maxBidPrice);
					processEvent(se);					
				}

				bids=bids+1;
				long now = System.currentTimeMillis()/1000;
				long minutes = (now - startTime)/60;
				if(minutes<1) {
					minutes =1;
				}

				StatisticsEvent se = new StatisticsEvent();
				se.setType("BID_COUNT_PER_MINUTE");
				se.setTimestamp(System.currentTimeMillis());
				if (minutes < 1) {
					se.setValue(bids/1);
				} else {
					se.setValue(bids/minutes);
					
				}
				processEvent(se);
			}
		}

		if(e instanceof AuctionEvent) {
			if(e.type.equals("AUCTION_STARTED")) {
				auctionTime.add(((AuctionEvent) e).getDuration());
			}

			if(e.type.equals("AUCTION_ENDED")) {
				auctionSucess.add(((AuctionEvent) e).isSuccess());

				double suc = 0;
				for(boolean s:auctionSucess) {
					if(s==true) {
						suc = suc +1;
					}
				}

				StatisticsEvent se = new StatisticsEvent();
				se.setType("AUCTION_SUCESS_RATIO");
				se.setTimestamp(System.currentTimeMillis());
				se.setValue(suc/auctionSucess.size());
				processEvent(se);

				double sum = 0;
				for(double s:auctionTime) {
					sum = sum + s;
				}
				double avg = sum / auctionTime.size();

				StatisticsEvent se2 = new StatisticsEvent();
				se2.setTimestamp(System.currentTimeMillis());
				se2.setType("AUCTION_TIME_AVG");
				se2.setValue(avg);
				processEvent(se2);
			}
		}

		//process Event to subscribed clients
		try {
			for(Client c:clients) {
				Collection<Subscription> sub = c.getSubscriptions().values();
				for(Subscription s:sub) {
					if(s.getFilter().contains(e.type)) {
						try {
							c.getmClient().processEvent(e);
						} catch (RemoteException ex) {
							c.getSubscriptions().clear();
							clients.remove(c);
						}
					}
				}
			}
		} catch (ConcurrentModificationException ex) {
			;
		}
	}

	private synchronized void createSessionTime(Event e) {
		//create SessionTimeEvents

		if(e.type.equals("USER_LOGIN")) {
			userEvents.add((UserEvent) e);
		}
		if(e.type.equals("USER_LOGOUT") || e.type.equals("USER_DISCONNECTED")) {
			for(int i = 0;i<userEvents.size();i++){					
				if(((UserEvent) e).getUsername().equals(userEvents.get(i).getUsername())) {
					double session = e.getTimestamp() - userEvents.get(i).getTimestamp();

					if(session < minSessionTime || minSessionTime == 0) {
						minSessionTime = session;
						try{
							StatisticsEvent se = new StatisticsEvent();
							se.setType("USER_SESSIONTIME_MIN");
							se.setTimestamp(System.currentTimeMillis());
							se.setValue(session/1000);
							processEvent(se);
						} catch (RemoteException ex) {
							System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
							ex.printStackTrace();
						}
					}

					if(session > maxSessionTime) {
						maxSessionTime = session;
						try{
							StatisticsEvent se = new StatisticsEvent();
							se.setType("USER_SESSIONTIME_MAX");
							se.setValue(session/1000);
							se.setTimestamp(System.currentTimeMillis());
							processEvent(se);
						} catch (RemoteException ex) {
							System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
							ex.printStackTrace();
						}
					}

					sessionTime.add(session);
					userEvents.remove(i);
					double sum = 0;
					for(double s:sessionTime) {
						sum = sum + s;
					}

					double avg = sum / sessionTime.size();
					try{
						StatisticsEvent se = new StatisticsEvent();
						se.setType("USER_SESSIONTIME_AVG");
						se.setValue(avg/1000);
						se.setTimestamp(System.currentTimeMillis());
						processEvent(se);
					} catch (RemoteException ex) {
						System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
						ex.printStackTrace();
					}
					return;
				}
			}
		}
	}

	@Override
	public String unsubscribe(ManagementClientInterface mClient, int id) throws RemoteException {
		for(Client c:clients) {
			System.out.println(c.getmClient().getId());
			if(c.getmClient().getId()==mClient.getId()) {
				if(c.getSubscriptions().containsKey(id)) {
					c.getSubscriptions().remove(id);
					System.out.println("Remove");
					return "subscription " + id + " terminated";
				}
			}
		}

		return "unsubscribe failed";
	}

	public void shutdown() {
		PropertiesParser ps = null;
		try {
			ps = new PropertiesParser("registry.properties");
		} catch (FileNotFoundException e1) {
			System.err.println("Error: Properties file not found!");
		}
		String host = ps.getProperty("registry.host");
		try {
			int portNr = Integer.parseInt(ps.getProperty("registry.port"));
			Naming.unbind("//" + host + ":" + portNr + "/" + bindingName);
			UnicastRemoteObject.unexportObject(this, true);
		} catch (RemoteException e) {
			;
		} catch (MalformedURLException e) {
			System.err.println("Error: Couldn't find registry!");
			e.printStackTrace();
		} catch (NotBoundException e) {
			;
		} catch (NumberFormatException e) {
			System.err.println("Port non-numeric!");
		}
		System.out.println("Shutting down!");
		System.exit(0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 1) {
			System.err.println("Invalid arguments!");
			System.err.println("USAGE: java AnalyticsServer <RMIBindingName>");
		} else {
			String bindingName = args[0];
			PropertiesParser ps;
			Registry registry = null;
			String host = null;
			int registryPort = 0;
			try {
				ps = new PropertiesParser("registry.properties");
				host = ps.getProperty("registry.host");
				registryPort = Integer.parseInt(ps.getProperty("registry.port"));
				registry = LocateRegistry.createRegistry(registryPort);
			} catch (FileNotFoundException e) {
				System.err.println("Properties file couldn't be found!");
				e.printStackTrace();
			} catch (RemoteException e) {
				try {
					registry = LocateRegistry.getRegistry(host, registryPort);
				} catch (RemoteException e1) {
					System.err.println("Couldn't find or create registry!");
					e1.printStackTrace();
				}
			}
			AnalyticsServerRMI as = new AnalyticsServer(bindingName);
			AnalyticsServerRMI ras = null;
			Thread cl = new Thread(new ConsoleListener(as));
			cl.start();
			try {
				ras = (AnalyticsServerRMI) UnicastRemoteObject.exportObject(as, 0);
			} catch (RemoteException e) {
				System.err.println("Error exporting the remote object!");
				e.printStackTrace();
			}
			try {
				registry.rebind(bindingName, ras);
			} catch (AccessException e) {
				System.err.println("Access Error binding the remote object to registry!");
				e.printStackTrace();
			} catch (RemoteException e) {
				System.err.println("Error binding the remote object to registry!");
				e.printStackTrace();
			}
		}
	}
}
