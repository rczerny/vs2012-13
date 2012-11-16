package server.analytics;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for ensuring RMI methods important for the analytics server.
 *
 */
public interface AnalyticsServerRMI extends Remote{
	String subscribe(Client client, String filter) throws RemoteException;
	void processEvent(Event e) throws RemoteException;
	String unsubscribe(Client client, int id) throws RemoteException;
}
