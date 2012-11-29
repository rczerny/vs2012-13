package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LoadTestBidder implements Runnable
{
	private String host = "";
	private int tcpPort = 0;
	private LoadTest lt = null;
	private Map<Integer, Date> auctions = Collections.synchronizedMap(new HashMap<Integer, Date>());
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	private BufferedReader keys = null;
	private Socket sock = null;

	public LoadTestBidder(String host, int tcpPort, LoadTest lt) {
		this.host = host;
		this.tcpPort = tcpPort;
		this.lt = lt;
		try {
			sock = new Socket(host, tcpPort);
			//sock.setSoTimeout(200);
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			sendAndReceive("!login hans" + new Date().getTime() + " 12345");
		} catch (UnknownHostException e) {
			System.err.println("Error! Couldn't find the given host!");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Error! Couldn't open socket properly!");
			e.printStackTrace();
			System.exit(1);
		}

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

	public Map<Integer, Date> getAuctions() {
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
	
	public String getAnalyticsBindingName() {
		return lt.getAnalyticsBindingName();
	}

	public synchronized ArrayList<String> sendAndReceive(String message) {
		String answerLine = "";
		ArrayList<String> temp = new ArrayList<String>();
		try {
			bw.write(message);
			bw.newLine();
			bw.flush();
		} catch (SocketTimeoutException e) {
			;
		} catch (IOException e) {
			System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
			e.printStackTrace();
			lt.setShutdown(true);
		}
		try {
			br.readLine();
			if (message.equals("!list")) {
				while (!(answerLine = br.readLine()).equals("ready")) {
					temp.add(answerLine);
				}
			}
		} catch (SocketTimeoutException e) {
			;
		} catch (IOException e) {
			System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
			e.printStackTrace();
			lt.setShutdown(true);
		}
		return temp;
	}

	public void run() {
		Thread al = new Thread(new AuctionLoader(this));
		al.start();
		Thread ac = new Thread(new AuctionCreator(this));
		ac.start();
		while(!lt.isShutdown()) {
			if (auctions.size() > 0) {
				int random = new Random().nextInt(auctions.size());
				int id = (Integer)auctions.keySet().toArray()[random];
				sendAndReceive("!bid " + id + " " + Math.abs(new Date().getTime() - auctions.get(id).getTime()));
				try {
					Thread.sleep((60/lt.getBidsPerMin())*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
