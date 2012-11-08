package server.billing;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for a billing server to implement Login functionalities via RMI
 *
 */
public interface BillingServerRMI extends Remote
{
	BillingServerSecure login(String username, String password) throws RemoteException;
}
