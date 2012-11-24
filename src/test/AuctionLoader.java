package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuctionLoader implements Runnable 
{
	private LoadTestBidder ltb = null;
	
	public AuctionLoader(LoadTestBidder ltb) {
		this.ltb = ltb;
	}
	
	public void run() {
		BufferedReader br = null;
		BufferedWriter bw = null;
		String answer = null;
		Socket sock = null;
		try {
			sock = new Socket(ltb.getHost(), ltb.getTcpPort());
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			sock.setSoTimeout(500);
		} catch (IOException e) {
			System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
			ltb.setShutdown(true);
		}
		while(!ltb.isShutdown()) {
			try {
				bw.write("!list");
				bw.newLine();
				bw.flush();
				br.readLine();
				while ((answer = br.readLine()) != null) {
					answer = answer.trim(); // remove leading and trailing whitespaces
					String[] answerParts = answer.split("\\s+"); // 1. 'Apple I' wozniak 10.10.2012 21:00 CET 10000.00 gates
					int id = Integer.parseInt(answerParts[0].substring(0, answerParts[0].length()-1));
					Date d = null;
					try {
						DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy"); //Sat Nov 24 17:11:33 CET 2012
						d = df.parse(answerParts[3] + " " + answerParts[4] + " " + answerParts[5] + " " +
								answerParts[6] + " "+ answerParts[7] + " " + answerParts[8]);
					} catch (ParseException e) {
						System.err.println("Error getting the auction date!");
						e.printStackTrace();
					}
					ltb.getAuctions().put(id, d);
					System.out.println("!list");
				}
			} catch (UnknownHostException e) {
				System.err.println("Host not found!");
			} catch(SocketTimeoutException e) {
				;		
			} catch (IOException e) {
				System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
				ltb.setShutdown(true);
			}
			System.out.println("Before timer auction loader");
			try {
				Thread.sleep(ltb.getUpdateIntervalSec()*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("After timer auction loader");
		}
	}
}
