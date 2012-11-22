package server.analytics;

import java.rmi.Remote;
import java.rmi.RemoteException;

import client.mgmt.ManagementClientInterface;

/**
 * Interface for ensuring RMI methods important for the analytics server.
 *
 */
public interface AnalyticsServerRMI extends Remote{
	String subscribe(ManagementClientInterface client, String filter) throws RemoteException;
	void processEvent(Event e) throws RemoteException;
	String unsubscribe(ManagementClientInterface client, int id) throws RemoteException;
	
	double getMin() throws RemoteException;
}
