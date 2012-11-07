package server.auction;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuctionServer
{
	private ExecutorService pool = null;
	protected Set<User> users = Collections.synchronizedSet(new HashSet<User>());
	protected Set<Auction> auctions = Collections.synchronizedSet(new HashSet<Auction>());
	private ServerSocket ssock = null;
	private int port = 0;
	private boolean shutdown = false;
	private int highestAuctionID = 0;
	private Cron tt = null;

	public AuctionServer(int port) {
		this.port = port;
		pool = Executors.newFixedThreadPool(10);
		try {
			ssock = new ServerSocket(this.port);
			ssock.setSoTimeout(500);
		} catch (IOException e) {
			System.err.println("Couldn't bind to specified port!");
			e.printStackTrace();
		}
	}

	public boolean getShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

	public void runServer() {
		pool.execute(new ConsoleListener(this));
		tt = new Cron(this);
		pool.execute(tt);
		while (!shutdown) {
			try {
				pool.execute(new CommandHandler(ssock.accept(), this));
			} catch (SocketTimeoutException e) {
				;
			} catch (IOException e) {
				System.err.println("Error starting or shutting down the server!");
				e.printStackTrace();
			}
		}
		shutdown();
	}

	public void shutdown() {
		System.out.println("Shutting down...");
		tt.cancel();
		shutdown = true;
		pool.shutdown();
		try {
			if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
				pool.shutdownNow();
			}
			ssock.close();
		} catch (InterruptedException e) {
			pool.shutdownNow();
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			System.err.println("Error starting or shutting down the server!");
			e.printStackTrace();
		}
	}

	public int getHighestAuctionID() {
		return highestAuctionID;
	}

	public void setHighestAuctionID(int highestAuctionID) {
		this.highestAuctionID = highestAuctionID;
	}

	public User getUser(String username) {
		User result = null;
		for (User u : users) {
			if (u.getUsername().equals(username)) {
				result = u;
				break;
			}
		}
		return result;
	}

	public Auction getAuction(int id) {
		Auction result = null;
		for (Auction a : auctions) {
			if (a.getId() == id) {
				result = a;
				break;
			}
		}
		return result;
	}

	public void sendNotification(User user, String message) {
		if (user.isLoggedIn()) {
			DatagramSocket datagramSocket;
			try {
				datagramSocket = new DatagramSocket();
				byte[] buffer = message.getBytes();
				InetAddress receiverAddress = user.getSocket().getInetAddress();
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, user.getUdpPort());
				datagramSocket.send(packet);
				datagramSocket.close();
			} catch (SocketException e) {
				System.err.println("Error while creating the Datagram Socket!");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Error sending the Datagram Socket!");
				e.printStackTrace();
			}
		} else {
			user.getDueNotifications().add(message);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Invalid arguments!");
			System.err.println("USAGE: java AuctionServer <tcpPort>");
			System.err.println("tcpPort must be numeric and <= 65535!");
		} else {
			try {
				int tcpPort = Integer.parseInt(args[0]); // check if tcpPort is numeric
				if (tcpPort <= 65535) {
					AuctionServer main = new AuctionServer(tcpPort);
					main.runServer();
				} else {
					System.err.println("tcpPort and udpPort must be numeric and <= 65535!");
				}
			}
			catch (NumberFormatException e) {
				System.err.println("tcpPort and udpPort must be numeric!");
			}
		}
	}
}
