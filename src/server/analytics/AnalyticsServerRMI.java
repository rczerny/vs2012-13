package server.analytics;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interface for ensuring RMI methods important for the analytics server.
 *
 */
public interface AnalyticsServerRMI extends Remote{
	String subcribe(String filter) throws RemoteException;
	void processEvent(Event e) throws RemoteException;
	void unsubcribe(int id) throws RemoteException;
}
