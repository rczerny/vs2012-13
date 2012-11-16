package server.analytics;

import java.io.FileNotFoundException;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import client.mgmt.ManagementClientInterface;

import tools.PropertiesParser;

public class AnalyticsServer implements AnalyticsServerRMI{

	private ArrayList<Client> clients = new ArrayList<Client>();
	int highestSubscriptionId = 1;

	@Override
	public String subscribe(ManagementClientInterface mClient, String filter) throws RemoteException {
		Subscription sub = new Subscription(highestSubscriptionId, filter);
		Client client = null;
		
		for(Client c:clients) {
			if(c.getmClient().equals(mClient)) {
				client = c;
			}
		}

		if(!sub.filter.isEmpty()) {
			if(client == null) {
				client = new Client(mClient);
				clients.add(client);
			}	
			client.getSubscriptions().put(highestSubscriptionId, sub);
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
	public String unsubscribe(ManagementClientInterface mClient, int id) throws RemoteException {
		for(Client c:clients) {
			if(c.getmClient().equals(mClient)) {
				if(c.getSubscriptions().containsKey(id)) {
					c.getSubscriptions().remove(id);
					return "subscription " + id + " terminated";
				}
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
