package client.mgmt;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.billing.AuctionCharging;
import server.billing.Bill;
import server.billing.BillingServerRMI;
import server.billing.BillingServerSecure;
import server.billing.PriceStep;
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
	private String PROMPT = "> ";

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
					if (bss != null) {
						System.err.println("You are already logged in! Logout first!");
					} else {
						if (commandParts.length != 3) {
							System.err.println("Invalid command! Must be !login <username> <password>");
						} else {
							bss = (BillingServerSecure) bs.login(commandParts[1], commandParts[2]);
							if (bss == null) {
								System.err.println("Login failed!");
							} else {
								System.out.println(commandParts[1] + " successfully logged in");
								PROMPT = commandParts[1] + "> ";
							}
						}
					}
				} else if (commandParts[0].equals("!steps")) {
					if (bss == null) {
						System.err.println("You need to login first!");
					} else {
						System.out.printf("%-9s %-9s %-9s %-9s%n", "Min_Price", "Max_Price", "Fee_Fixed", "Fee_Variable");
						for (PriceStep p : bss.getPriceSteps().getPriceSteps()) {
							System.out.printf("%-9.0f %-9.0f %-9.1f %-9.1f%n", p.getStartPrice(), p.getEndPrice(), p.getFixedPrice(), p.getVariablePricePercent());
						}
					}
				} else if (commandParts[0].equals("!addStep")) {
					if (bss == null) {
						System.err.println("You need to login first!");
					} else {
						if (commandParts.length != 5) {
							System.err.println("Invalid command! Must be !addStep <startPrice> <endPrice> <fixedPrice> <variablePricePercent>");
						} else {
							try {
								double startPrice = Double.parseDouble(commandParts[1]);
								double endPrice = Double.parseDouble(commandParts[2]);
								double fixedPrice = Double.parseDouble(commandParts[3]);
								double variablePricePercent = Double.parseDouble(commandParts[4]);
								bss.createPriceStep(startPrice, endPrice, fixedPrice, variablePricePercent);
							} catch (NumberFormatException e) {
								System.err.println("Error! Non-numeric argument, where numeric argument expected!");
							} catch (RemoteException e) {
								System.err.println("Couldn't create price step! Please check if price steps are overlapping!");
							}
						}
					}
				} else if (commandParts[0].equals("!removeStep")) {
					if (bss == null) {
						System.err.println("You need to login first!");
					} else {
						if (commandParts.length != 3) {
							System.err.println("Invalid command! Must be !removeStep <startPrice> <endPrice>");
						} else {
							try {
								double startPrice = Double.parseDouble(commandParts[1]);
								double endPrice = Double.parseDouble(commandParts[2]);
								bss.deletePriceStep(startPrice, endPrice);
							} catch (NumberFormatException e) {
								System.err.println("Error! Non-numeric argument, where numeric argument expected!");
							} catch (RemoteException e) {
								System.err.println("Couldn't delete price step! Please make sure the given price step exists!");
							}
						}
					}
				} else if (commandParts[0].equals("!bill")) {
					if (bss == null) {
						System.err.println("You need to login first!");
					} else {
						if (commandParts.length != 2) {
							System.err.println("Invalid command! Must be !bill <username>");
						} else {
							try {
								Bill bill = bss.getBill(commandParts[1]);
								System.out.printf("%-12s %-12s %-12s %-12s %-12s%n", "auction_ID", "strike_price", "fee_fixed", "fee_variable", "fee_total");
								for (AuctionCharging ac : bill.getAuctionChargings()) {
									System.out.printf("%-12d %-12.0f %-12.0f %-12.1f %-12.1f%n", ac.getAuctionId(), ac.getStrikePrice(), ac.getFixedFee(), 
																			   ac.getStrikePrice() + ac.getFixedFee());
								}
							} catch (RemoteException e) {
								System.err.println("Couldn't bill user! Please make sure the given user exists!");
								e.printStackTrace();
							}
						}
					}
				} else if (commandParts[0].equals("!logout")) {
					if (bss == null) {
						System.err.println("You need to login first!");
					} else {

					}
				} else {
					System.err.println("Unknown command!");
				}
				System.out.print(PROMPT);
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
