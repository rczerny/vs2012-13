package server.analytics;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class AnalyticsServer implements AnalyticsServerRMI{
	int highestSubscriptionId = 1;
	ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AnalyticsServer as = new AnalyticsServer();
		try {
			System.out.println(as.subcribe("(USER_*)|(BID_WON)"));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		try {
			System.out.println(as.subcribe("(USER_*)"));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

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

}
