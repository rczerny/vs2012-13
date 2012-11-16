package server.auction;

import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import server.billing.BillingServerRMI;
import server.billing.BillingServerSecure;
import tools.PropertiesParser;

public class Cron implements Runnable
{
	private AuctionServer main = null;
	private Timer timer = null;
	private Task task = null;

	public Cron(AuctionServer main) {
		this.main = main;
	}

	public void run() {
		timer = new Timer();
		task = new Task();
		timer.schedule(task, 0, 500);
	}

	public void cancel() {
		task.cancel();
		timer.cancel();
	}

	public class Task extends TimerTask {
		public void run() {
			try {
				for (Auction a : main.auctions) {
					if (new Date().after(a.getDate())) {
						BillingServerRMI bs = null;
						BillingServerSecure bss = null;
						PropertiesParser ps = null;
						Registry reg = null;
						try {
							ps = new PropertiesParser("registry.properties");
							int portNr = Integer.parseInt(ps.getProperty("registry.port"));
							String host = ps.getProperty("registry.host");
							reg = LocateRegistry.getRegistry(host, portNr);
							bs = (BillingServerRMI) reg.lookup("RemoteBillingServer");
							bss = (BillingServerSecure) bs.login("auctionServer", "auctionServer");
						} catch (FileNotFoundException e) {
							System.err.println("properties file not found!");
						} catch (NumberFormatException e) {
							System.err.println("Port non-numeric!");
						} catch (RemoteException e) {
							System.err.println("Registry couln't be found!");
						} catch (NotBoundException e) {
							System.err.println("Remote object couldn't be found!");
							e.printStackTrace();
						}
						String username = "";
						if (a.getHighestBidder() == null)
							username = "noone";
						else
							username = a.getHighestBidder().getUsername();
						try {
							bss.billAuction(username, (long)a.getId(), a.getHighestBid());
						} catch (RemoteException e) {
							System.err.println("Error: Couldn't bill auction! BillingServer may be down!");
						}
						main.auctions.remove(a);
						/*********************
						 * no UDP in Lab 2
						 *********************
						if (a.getHighestBidder() != null) {
							String message = "!auction-ended " + a.getHighestBidder().getUsername() + "  " + a.getHighestBid() + " " + a.getDescription();
							main.sendNotification(a.getHighestBidder(), message);
							main.sendNotification(a.getOwner(), message);
						} else {
							String message = "!auction-ended noone " + a.getHighestBid() + " " + a.getDescription();
							main.sendNotification(a.getOwner(), message);
						}
						 */
					}
				} 
			} catch (ConcurrentModificationException e) {
				;
			}
		}
	}
}
