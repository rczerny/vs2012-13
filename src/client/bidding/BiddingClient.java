package client.bidding;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class BiddingClient implements Runnable
{	
	private String host = "";
	private int tcpPort = 0;
	private int udpPort = 0;
	private ExecutorService pool = null;
	private boolean shutdown = false;
	private static String PROMPT = "> ";
	private String username = "";

	public BiddingClient() {

	}

	public BiddingClient(String host, int tcpPort, int udpPort) {
		this.host = host;
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		pool = Executors.newSingleThreadExecutor();
	}

	public void run() {
		/**********************
		 * No UDP in Lab 2
		 **********************
		UDPListener udpListener = null; 
		try {
			udpListener = new UDPListener(udpPort, username);
		} catch (SocketException e) {
			System.err.println("Error opening the datagram socket! Port is probably already used!");
			return;
		}
		pool.execute(udpListener);
		*/
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
			keys = new BufferedReader(new InputStreamReader(System.in));
			//sock.setSoTimeout(1000);
		} catch (IOException e) {
			System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
			//e.printStackTrace();
			shutdown = true;
		}
		while(!shutdown) {
			try {
				System.out.print(PROMPT);
				input = keys.readLine();
				if (input.trim().startsWith("!login")) {
					input += " " + udpPort;
				} else {
					if (input.trim().startsWith("!end")) {
						System.out.println("Shutting down...");
						shutdown = true;
						break;
					}
				}
				bw.write(input);
				bw.newLine();
				bw.flush();
				while (!(answer = br.readLine()).equals("ready")) {
					if (input.trim().startsWith("!login") && answer.startsWith("Successfully logged in as")) {
						username = input.trim().split("\\s+")[1];
						PROMPT =  username + PROMPT;
						//udpListener.setUsername(username);
					}
					if (input.trim().startsWith("!logout") && answer.startsWith("Successfully logged out as")) {
						username = "";
						PROMPT =  username + "> ";
					}
					System.out.println(answer);
				}
			} catch (UnknownHostException e) {
				System.err.println("Host not found!");
			} catch(SocketTimeoutException e) {
				System.out.println("BiddingClient SocketTimeout!");
			} catch (IOException e) {
				System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
				//e.printStackTrace();
				shutdown = true;
			}
		}
		try {
			sock.close();
			//udpListener.setShutdown(true);
			pool.shutdown();
		} catch (IOException e) {
			System.err.println("Couldn't close socket!");
			e.printStackTrace();
		} catch (NullPointerException e) {
			//udpListener.setShutdown(true);
			pool.shutdown();
		}
	}

	public static void printPROMPT() {
		System.out.print(PROMPT);
	}

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Invalid arguments!");
			System.err.println("USAGE: java ClientMain <hostname> <tcpPort> <udpPort>");
			System.err.println("tcpPort and udpPort must be numeric and <= 65535!");
		} else {
			String host = args[0];
			try {
				int tcpPort = Integer.parseInt(args[1]); // check if tcpPort is numeric
				int udpPort = Integer.parseInt(args[2]); // check if udpPort is numeric
				if (tcpPort <= 65535 && udpPort <= 65535) {
					BiddingClient cm = new BiddingClient(host, tcpPort, udpPort);
					cm.run();
				} else {
					System.err.println("tcpPort and udpPort must be numeric and <= 65535!");
				}
			}
			catch (NumberFormatException e) {
				System.err.println("tcpPort and udpPort must be numeric!");
			}
		}

		/*try { // Part 1 A
			Socket sock = new Socket("stockholm.vitalab.tuwien.ac.at", 9000);
			String answer = "";
			BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			bw.write("!login dslab	xxx password\n");
			bw.flush();
			if ((answer = br.readLine()) != null) {
				System.out.println(answer);
			}
			bw.close();
			br.close();
		} catch (IOException e) {
			System.out.println("Unable to connect to server. Host or Port might be invalid!");
			e.printStackTrace();
		}*/
	}
}
