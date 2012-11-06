package server;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Cron implements Runnable
{
	private ServerMain main = null;
	private Timer timer = null;
	private Task task = null;

	public Cron(ServerMain main) {
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

						main.auctions.remove(a);
						if (a.getHighestBidder() != null) {
							String message = "!auction-ended " + a.getHighestBidder().getUsername() + "  " + a.getHighestBid() + " " + a.getDescription();
							main.sendNotification(a.getHighestBidder(), message);
							main.sendNotification(a.getOwner(), message);
						} else {
							String message = "!auction-ended noone " + a.getHighestBid() + " " + a.getDescription();
							main.sendNotification(a.getOwner(), message);
						}

					}
				} 
			} catch (ConcurrentModificationException e) {
				;
			}
		}
	}
}
