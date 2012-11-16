package server.analytics;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for ensuring RMI methods important for the analytics server.
 *
 */
public interface AnalyticsServerRMI extends Remote{
	String subscribe(String client, String filter) throws RemoteException;
	void processEvent(Event e) throws RemoteException;
	String unsubscribe(String client, int id) throws RemoteException;
}
