package server.analytics;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import tools.PropertiesParser;

public class AnalyticsServer implements AnalyticsServerRMI{

	int highestSubscriptionId = 1;
	ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();

	@Override
	public String subcribe(String filter) throws RemoteException {
		Subscription sub = new Subscription(highestSubscriptionId, filter);
		subscriptions.add(sub);

		int id = highestSubscriptionId;

		if(sub.filter.isEmpty()) {
			return "Creating subscription failed!";
		}
		highestSubscriptionId++;
		return "Created subscription with ID " + id + " for events using filter " + filter;
	}

	@Override
	public void processEvent(Event e) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void unsubcribe(int id) throws RemoteException {
		// TODO Auto-generated method stub

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

		/*AnalyticsServer as = new AnalyticsServer();
		try {
			System.out.println(as.subcribe("(USER_*)|(BID_WON)"));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		try {
			System.out.println(as.subcribe("(USER_*)"));
		} catch (RemoteException e) {
			e.printStackTrace();
		}*/
	}
}
