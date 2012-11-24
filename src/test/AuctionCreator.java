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

public class AuctionCreator implements Runnable 
{
	private LoadTestBidder ltb = null;
	
	public AuctionCreator(LoadTestBidder ltb) {
		this.ltb = ltb;
	}
	
	public void run() {
		BufferedWriter bw = null;
		Socket sock = null;
		try {
			sock = new Socket(ltb.getHost(), ltb.getTcpPort());
			bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			sock.setSoTimeout(500);
		} catch (IOException e) {
			System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
			ltb.setShutdown(true);
		}
		while(!ltb.isShutdown()) {
			try {
				bw.write("!create " + ltb.getAuctionDuration() + " randomblub" + new Date().getTime());
				System.out.println("!create " + ltb.getAuctionDuration() + " randomblub" + new Date().getTime());
				bw.newLine();
				bw.flush();
			} catch (UnknownHostException e) {
				System.err.println("Host not found!");
			} catch(SocketTimeoutException e) {
				;		
			} catch (IOException e) {
				System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
				ltb.setShutdown(true);
			}
			try {
				Thread.sleep((60/ltb.getAuctionsPerMin())*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
