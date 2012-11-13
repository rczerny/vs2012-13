package server.analytics;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Notify extends Remote{
	public void finished() throws RemoteException;
}
