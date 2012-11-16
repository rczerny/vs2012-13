package server.analytics;

import java.io.FileNotFoundException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import tools.PropertiesParser;

public class AnalyticsServer implements AnalyticsServerRMI{

	HashMap<String, Client> clients = new HashMap<String, Client>();
	int highestSubscriptionId = 1;

	@Override
	public String subscribe(String c, String filter) throws RemoteException {
		Subscription sub = new Subscription(highestSubscriptionId, filter);

		Client client = new Client(c);
		if(!clients.containsKey(c)) {
			clients.put(c, client);
		}

		if(!sub.filter.isEmpty()) {
			client.subscriptions.put(highestSubscriptionId, sub);
		}

		int id = highestSubscriptionId;

		if(sub.filter.isEmpty()) {
			return "Creating subscription failed!";
		}

		highestSubscriptionId++;
		return "Created subscription with ID " + id + " for events using filter " + filter;
	}

	@Override
	public void processEvent(Event e) throws RemoteException {

	}

	@Override
	public String unsubscribe(String c, int id) throws RemoteException {
		if(clients.containsKey(c)) {
			Client client = clients.get(c);
					if(client.getSubscriptions().containsKey(id)) {
						client.getSubscriptions().remove(id);
						return "subscription " + id + " terminated";
					} 
		}
		return "unsubscribe failed";
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
			try {
				ps = new PropertiesParser("registry.properties");
				int registryPort = Integer.parseInt(ps.getProperty("registry.port"));
				registry = LocateRegistry.createRegistry(registryPort);
			} catch (FileNotFoundException e) {
				System.err.println("Properties file couldn't be found!");
				e.printStackTrace();
			} catch (RemoteException e) {
				System.err.println("Couldn't create Registry.");
				e.printStackTrace();
			}
			AnalyticsServerRMI as = new AnalyticsServer();

			AnalyticsServerRMI ras = null;
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
