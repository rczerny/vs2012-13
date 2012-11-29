package client.mgmt;

import java.rmi.Remote;
import java.rmi.RemoteException;

import server.analytics.Event;

public interface ManagementClientInterface extends Remote {
	public void processEvent(Event e) throws RemoteException;
	public long getId() throws RemoteException;
}
