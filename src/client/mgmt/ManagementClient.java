package client.mgmt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import server.analytics.AnalyticsServer;
import server.analytics.AnalyticsServerRMI;
import server.analytics.Event;
import server.billing.BillingServerRMI;
import server.billing.BillingServerSecure;
import tools.PropertiesParser;

public class ManagementClient extends UnicastRemoteObject implements ManagementClientInterface{
	private String analyticsBindingName = "";
	private String billingBindingName = "";
	private BillingServerRMI bs = null;
	private BillingServerSecure bss = null;
	private AnalyticsServerRMI as = null;
	private PropertiesParser ps = null;
	private Registry reg = null;
	private BufferedReader keys = null;
	private int id = 0;
	private ArrayList<String> buffer = null;
	private boolean auto;

	public ManagementClient(String analyticsBindingName, String billingBindingName) throws RemoteException{		
		keys = new BufferedReader(new InputStreamReader(System.in));
		this.analyticsBindingName = analyticsBindingName;
		this.billingBindingName = billingBindingName;
		try {
			ps = new PropertiesParser("registry.properties");
			int portNr = Integer.parseInt(ps.getProperty("registry.port"));
			String host = ps.getProperty("registry.host");
			reg = LocateRegistry.getRegistry(host, portNr);
			//bs = (BillingServerRMI) reg.lookup(billingBindingName);
			as = (AnalyticsServerRMI) reg.lookup(analyticsBindingName);
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
		id = 1;
		buffer = new ArrayList<String>();
		auto = true;
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
						//PriceSteps
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

	public String subscribe(String filter) {
		String answer = "";
		try {
			answer = as.subscribe(this, filter);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		if(answer.equals("")) {
			answer = "Failed";
		}
		return answer;
	}

	public String unsubscribe(int id) {
		String answer = "";
		try {
			answer = as.unsubscribe(this, id);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		if(answer.equals("")) {
			answer = "Failed";
		}
		return answer;
	}

	@Override
	public void updateEvents(Event e) throws RemoteException {
		if(auto) {
			System.out.println(buffer.toString());
		} else {
			buffer.add(e.toString());
		}
	}

	public void printBuffer() {
		if(!buffer.isEmpty()) {
			for(String s:buffer) {
				System.out.println(s);
			}
			buffer.clear();
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public boolean getAuto() {
		return auto;
	}

	public void setAuto(boolean a) {
		auto = a;
	}
	
	public ArrayList<String> getBuffer() {
		return buffer;
	}

	public void setBuffer(ArrayList<String> buffer) {
		this.buffer = buffer;
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Invalid arguments!");
			System.err.println("USAGE: java ManagementClient <AnalyticsBindingname> <BillingBindingName>");
		} else {
			try{
				ManagementClient mc = new ManagementClient(args[0], args[1]);
			}catch(RemoteException e){
				e.printStackTrace();
			}
			//mc.listen();
		}
	}
}
