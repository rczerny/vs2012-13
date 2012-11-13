package client.mgmt;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import server.analytics.AnalyticsServer;

public class ManagementClient {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AnalyticsServer server = null;

		try {
			Remote remoteObject = Naming.lookup("Analytics");
			server = (AnalyticsServer)remoteObject;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if(server!= null) {
			try {
				System.out.println(server.subcribe("(USER_LOGIN)"));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
