package client.mgmt;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ManagementClientInterface extends Remote {
	public void updateEvents() throws RemoteException;
}
