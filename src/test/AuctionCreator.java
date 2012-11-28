package test;

import java.util.Date;

public class AuctionCreator implements Runnable 
{
	private LoadTestBidder ltb = null;

	public AuctionCreator(LoadTestBidder ltb) {
		this.ltb = ltb;
	}

	public void run() {
		while(!ltb.isShutdown()) {
			ltb.sendAndReceive("!create " + ltb.getAuctionDuration() + " randomblub" + new Date().getTime());
			try {
				Thread.sleep((60/ltb.getAuctionsPerMin())*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
