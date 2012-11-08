package server.billing;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import tools.PropertiesParser;

public class BillingServer implements BillingServerRMI
{
	public BillingServerSecure login(String username, String password) {
		BillingServerSecure result = null;
		try {
			PropertiesParser ps = new PropertiesParser("user.properties");
			MessageDigest md = MessageDigest.getInstance("MD5");
			String digest = new BigInteger(1, md.digest(password.getBytes())).toString(16); // make a hex-string out of the byte-array
			if (digest.equals(ps.getProperty(username))) {
				BillingServerSecure bss = new BillingServerSecureImpl();
				try {
					result = (BillingServerSecure) UnicastRemoteObject.exportObject(bss, 0);
				} catch (RemoteException e) {
					System.err.println("Error exporting the remote object!");
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Properties file couldn't be found!");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Error: MD5 not supported!");
			e.printStackTrace();
		}
		
		return result;
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Invalid arguments!");
			System.err.println("USAGE: java BillingServer <RMIBindingName>");
		} else {
			String bindingName = args[0];
			PropertiesParser ps;
			Registry registry = null;
			try {
				ps = new PropertiesParser("registry.properties");
				int registryPort = Integer.parseInt(ps.getProperty("registry.port"));
				registry = LocateRegistry.createRegistry(registryPort);
			} catch (FileNotFoundException e) {
				System.err.println("Properties file couldn't be found!");
				e.printStackTrace();
			} catch (RemoteException e) {
				System.err.println("Couldn't create Registry.");
				e.printStackTrace();
			}
			BillingServerRMI bs = new BillingServer();
			BillingServerRMI rbs = null;
			try {
				rbs = (BillingServerRMI) UnicastRemoteObject.exportObject(bs, 0);
			} catch (RemoteException e) {
				System.err.println("Error exporting the remote object!");
				e.printStackTrace();
			}
			try {
				registry.rebind(bindingName, rbs);
			} catch (AccessException e) {
				System.err.println("Access Error binding the remote object to registry!");
				e.printStackTrace();
			} catch (RemoteException e) {
				System.err.println("Error binding the remote object to registry!");
				e.printStackTrace();
			}
		}
	}
}