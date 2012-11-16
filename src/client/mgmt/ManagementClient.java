package client.mgmt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.billing.BillingServerRMI;
import server.billing.BillingServerSecure;
import tools.PropertiesParser;

public class ManagementClient 
{
	private String analyticsBindingName = "";
	private String billingBindingName = "";
	private BillingServerRMI bs = null;
	private BillingServerSecure bss = null;
	private PropertiesParser ps = null;
	private Registry reg = null;
	private BufferedReader keys = null;

	public ManagementClient(String analyticsBindingName, String billingBindingName) {
		keys = new BufferedReader(new InputStreamReader(System.in));
		this.analyticsBindingName = analyticsBindingName;
		this.billingBindingName = billingBindingName;
		try {
			ps = new PropertiesParser("registry.properties");
			int portNr = Integer.parseInt(ps.getProperty("registry.port"));
			String host = ps.getProperty("registry.host");
			reg = LocateRegistry.getRegistry(host, portNr);
			bs = (BillingServerRMI) reg.lookup(billingBindingName);
		} catch (FileNotFoundException e) {
			System.err.println("properties file not found!");
		} catch (NumberFormatException e) {
			System.err.println("Port non-numeric!");
		} catch (RemoteException e) {
			System.err.println("Registry couldn't be found!");
		} catch (NotBoundException e) {
			System.err.println("Object couldn't be found");
			e.printStackTrace();
		}
	}
	
	public void listen() {
		try {
			String command = "";
			String[] commandParts;
			while ((command = keys.readLine()) != "\n") {
				command = command.trim(); // remove leading and trailing whitespaces
				commandParts = command.split("\\s+");
				if (commandParts[0].equals("!login")) {
					if (commandParts.length < 3) {
						System.err.println("Invalid command! Must be !login <username> <password>");
					} else {
						bss = (BillingServerSecure) bs.login(commandParts[1], commandParts[2]);
						if (bss == null) {
							System.err.println("Login failed!");
						} else {
							System.out.println(commandParts[1] + " successfully logged in");
						}
					}
				} else if (commandParts[0].equals("!steps")) {
					if (bss == null) {
						System.err.println("You need to login first!");
					} else {
						PriceSteps
					}
				} else if (commandParts[0].equals("!addStep")) {
					if (bss == null) {
						System.err.println("You need to login first!");
					} else {
						
					}
				} else if (commandParts[0].equals("!removeStep")) {
					if (bss == null) {
						System.err.println("You need to login first!");
					} else {
						
					}
				} else if (commandParts[0].equals("!bill")) {
					if (bss == null) {
						System.err.println("You need to login first!");
					} else {
						
					}
				} else if (commandParts[0].equals("!logout")) {
					if (bss == null) {
						System.err.println("You need to login first!");
					} else {
						
					}
				} else {
					System.err.println("Unknown command!");
				}
			}
		} catch (IOException e) {
			System.err.println("Console I/O Error!");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Invalid arguments!");
			System.err.println("USAGE: java ManagementClient <AnalyticsBindingname> <BillingBindingName>");
		} else {
			ManagementClient mc = new ManagementClient(args[0], args[1]);
			mc.listen();
		}
	}
}
