package server.billing;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

import tools.PropertiesParser;

public class BillingServer implements BillingServerRMI, Unreferenced
{
	private String bindingName;
	private Registry registry;
	private BillingServerSecure billingServerSecure = null;
	public static PriceSteps s;
	public static ConcurrentHashMap<String, Bill> bills;

	public void setBindingName(String bindingName) {
		this.bindingName = bindingName;
	}

	public String getBindingName() {
		return bindingName;
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	public void unreferenced() {
		//shutdown();
	}

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
					billingServerSecure = bss;
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

	public void shutdown() {
		PropertiesParser ps = null;
		try {
			ps = new PropertiesParser("registry.properties");
		} catch (FileNotFoundException e1) {
			System.err.println("Error: Properties file not found!");
		}
		String host = ps.getProperty("registry.host");
		try {
			int portNr = Integer.parseInt(ps.getProperty("registry.port"));
			Naming.unbind("//" + host + ":" + portNr + "/" + bindingName);
			if (billingServerSecure != null) {
				((BillingServerSecureImpl) billingServerSecure).shutdown();
			}
			UnicastRemoteObject.unexportObject(this, true);
			UnicastRemoteObject.unexportObject(registry, true);
		} catch (RemoteException e) {
			;
		} catch (MalformedURLException e) {
			System.err.println("Error: Couldn't find registry!");
			e.printStackTrace();
		} catch (NotBoundException e) {
			;
		} catch (NumberFormatException e) {
			System.err.println("Port non-numeric!");
		}
		System.out.println("Shutting down!");
		System.exit(0);
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Invalid arguments!");
			System.err.println("USAGE: java BillingServer <RMIBindingName>");
		} else {
			String bindingName = args[0];
			PropertiesParser ps;
			Registry registry = null;
			String host = null;
			int registryPort = 0;
			try {
				ps = new PropertiesParser("registry.properties");
				host = ps.getProperty("registry.host");
				registryPort = Integer.parseInt(ps.getProperty("registry.port"));
				registry = LocateRegistry.createRegistry(registryPort);
			} catch (FileNotFoundException e) {
				System.err.println("Properties file couldn't be found!");
				e.printStackTrace();
			} catch (RemoteException e) {
				try {
					//registry = LocateRegistry.createRegistry(registryPort);
					registry = LocateRegistry.getRegistry(host, registryPort);
				} catch (RemoteException e1) {
					System.err.println("Couldn't find or create registry!");
					e1.printStackTrace();
				}
			}
			s = new PriceSteps();
			bills = new ConcurrentHashMap<String, Bill>();
			BillingServerRMI bs = new BillingServer();
			BillingServerRMI rbs = null;
			((BillingServer) bs).setBindingName(bindingName);
			((BillingServer) bs).setRegistry(registry);
			ConsoleListener cs = new ConsoleListener(bs);
			new Thread(cs).start();
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