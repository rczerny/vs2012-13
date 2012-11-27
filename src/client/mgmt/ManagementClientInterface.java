package client.mgmt;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import server.analytics.Event;

public interface ManagementClientInterface extends Remote {
	public void processEvent(Event e) throws RemoteException;
	public int getId() throws RemoteException;
}
