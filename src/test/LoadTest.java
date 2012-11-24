package test;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tools.PropertiesParser;

public class LoadTest 
{
	private int clients = 0;
	private int auctionsPerMin = 0;
	private int auctionDuration = 0;
	private int updateIntervalSec = 0;
	private int bidsPerMin = 0;
	private boolean shutdown = false;
	private int tcpPort = 0;
	private String host = null;

	public LoadTest(String host, int tcpPort, int clients, int auctionsPerMin, int auctionDuration, int updateIntervalSec, int bidsPerMin) {
		this.clients = clients;
		this.auctionsPerMin = auctionsPerMin;
		this.auctionDuration = auctionDuration;
		this.updateIntervalSec = updateIntervalSec;
		this.bidsPerMin = bidsPerMin;
		this.host  = host;
		this.tcpPort = tcpPort;
	}

	public void run() {
		ExecutorService pool = Executors.newFixedThreadPool(clients);
		for (int i = 0; i < clients; i++) {
			pool.execute(new LoadTestBidder(host, tcpPort, this));
		}
	}

	public int getClients() {
		return clients;
	}

	public void setClients(int clients) {
		this.clients = clients;
	}

	public int getAuctionsPerMin() {
		return auctionsPerMin;
	}

	public void setAuctionsPerMin(int auctionsPerMin) {
		this.auctionsPerMin = auctionsPerMin;
	}

	public int getAuctionDuration() {
		return auctionDuration;
	}

	public void setAuctionDuration(int auctionDuration) {
		this.auctionDuration = auctionDuration;
	}

	public int getUpdateIntervalSec() {
		return updateIntervalSec;
	}

	public void setUpdateIntervalSec(int updateIntervalSec) {
		this.updateIntervalSec = updateIntervalSec;
	}

	public int getBidsPerMin() {
		return bidsPerMin;
	}

	public void setBidsPerMin(int bidsPerMin) {
		this.bidsPerMin = bidsPerMin;
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Invalid arguments!");
			System.err.println("USAGE: java ClientMain <hostname> <tcpPort> <analyticsBindingName>");
			System.err.println("tcpPort must be numeric and <= 65535!");
		} else {
			PropertiesParser ps = null;
			try {
				ps = new PropertiesParser("loadtest.properties");
				int tcpPort = Integer.parseInt(args[1]);
				String host = args[0];
				int clients = Integer.parseInt(ps.getProperty("clients"));
				int auctionsPerMin = Integer.parseInt(ps.getProperty("auctionsPerMin"));
				int auctionDuration = Integer.parseInt(ps.getProperty("auctionDuration"));
				int updateIntervalSec = Integer.parseInt(ps.getProperty("updateIntervalSec"));
				int bidsPerMin = Integer.parseInt(ps.getProperty("bidsPerMin"));
				LoadTest lt = new LoadTest(host, tcpPort, clients, auctionsPerMin, auctionDuration, updateIntervalSec, bidsPerMin);
				lt.run();
			} catch (FileNotFoundException e) {
				System.err.println("Properties file couldn't be found!");
				System.exit(1);
			} catch (NumberFormatException e) {
				System.err.println("Argument not numeric!");
				System.exit(1);
			}
		}
	}
}
