package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class LoadTestBidder implements Runnable
{
	private String host = "";
	private int tcpPort = 0;
	private LoadTest lt = null;
	private HashMap<Integer, Date> auctions = new HashMap<Integer, Date>();

	public LoadTestBidder(String host, int tcpPort, LoadTest lt) {
		this.host = host;
		this.tcpPort = tcpPort;
		this.lt = lt;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public HashMap<Integer, Date> getAuctions() {
		return auctions;
	}

	public void setAuctions(HashMap<Integer, Date> auctions) {
		this.auctions = auctions;
	}

	public boolean isShutdown() {
		return lt.isShutdown();
	}

	public void setShutdown(boolean shutdown) {
		lt.setShutdown(shutdown);
	}

	public int getUpdateIntervalSec() {
		return lt.getUpdateIntervalSec();
	}

	public int getBidsPerMin() {
		return lt.getBidsPerMin();
	}

	public int getAuctionsPerMin() {
		return lt.getAuctionsPerMin();
	}

	public int getAuctionDuration() {
		return lt.getAuctionDuration();
	}

	public void run() {
		BufferedReader br = null;
		BufferedWriter bw = null;
		BufferedReader keys = null;
		String input = null;
		String answer = null;
		Socket sock = null;
		try {
			sock = new Socket(host, tcpPort);
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			//keys = new BufferedReader(new InputStreamReader(System.in));
			//sock.setSoTimeout(500);
		} catch (IOException e) {
			System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
			lt.setShutdown(true);
		}
		Thread al = new Thread(new AuctionLoader(this));
		al.start();
		Thread ac = new Thread(new AuctionCreator(this));
		ac.start();
		while(!lt.isShutdown()) {
			try {
				if (auctions.size() > 0) {
					int random = new Random().nextInt(auctions.size());
					System.out.println(auctions.size());
					int id = (Integer)auctions.keySet().toArray()[random];
					bw.write("!bid " + id + " " + (new Date().getTime() - auctions.get(id).getTime()));
					System.out.println("!bid " + id + " " + (new Date().getTime() - auctions.get(id).getTime()));
					bw.newLine();
					bw.flush();
				}
			} catch (UnknownHostException e) {
				System.err.println("Host not found!");
			} catch(SocketTimeoutException e) {
				;		
			} catch (IOException e) {
				System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
				lt.setShutdown(true);
			}
			try {
				Thread.sleep((60/lt.getBidsPerMin())*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			sock.close();
		} catch (IOException e) {
			System.err.println("Couldn't close socket!");
			e.printStackTrace();
		} catch (NullPointerException e) {
			;
		}
	}
}
