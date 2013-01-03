package server.auction;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

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
	private PrivateKey serverPrivKey = null;
	private String clientsKeyDir = "";
	private boolean stopped = false;

	public AuctionServer(int port, String serverPrivKey, String clientsKeyDir) {
		this.port = port;
		pool = Executors.newCachedThreadPool();
		PEMReader in;
		PrivateKey privateKey = null;
		try {
			in = new PEMReader(new FileReader(serverPrivKey), new PasswordFinder() {
				public char[] getPassword() {
					char[] privK = null;
					System.out.println("Enter pass phrase:");
					try {
						privK = new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray() ;
					} catch (IOException e) {
						System.err.println("Couldn't read password!");
					}
					return privK;
				}
			});
			KeyPair keyPair = (KeyPair) in.readObject(); 
			privateKey = keyPair.getPrivate();
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't find private key!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Couldn't read Private Key!");
			e.printStackTrace();
		}
		try {
			ssock = new ServerSocket(this.port);
			ssock.setSoTimeout(500);
		} catch (IOException e) {
			System.err.println("ERROR: Couldn't bind to specified port! It is probably already in use!");
		}
		this.serverPrivKey = privateKey;
		this.clientsKeyDir = clientsKeyDir;
	}

	public boolean getShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

	public void runServer(String billingBindingName) {
		pool.execute(new ConsoleListener(this));
		tt = new Cron(this, billingBindingName);
		pool.execute(tt);
		while (!shutdown) {
			try {
				pool.execute(new CommandHandler(ssock.accept(), this));
				while (stopped) {
					Thread.sleep(1000);
				}
			} catch (SocketTimeoutException e) {
				;
			} catch (IOException e) {
				System.err.println("Error starting or shutting down the server!");
				e.printStackTrace();
			} catch (InterruptedException e) {
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

	public PrivateKey getServerPrivKey() {
		return serverPrivKey;
	}

	public void setServerPrivKey(PrivateKey serverPrivKey) {
		this.serverPrivKey = serverPrivKey;
	}

	public String getClientsKeyDir() {
		return clientsKeyDir;
	}

	public void setClientsKeyDir(String clientsKeyDir) {
		this.clientsKeyDir = clientsKeyDir;
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

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
		try {
			Thread.sleep(1000);
			if (stopped == false) {
				ssock = new ServerSocket(this.port);
				ssock.setSoTimeout(500);
			} else {
				ssock.close();
			}	
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error setting stop-state of server!");
			e.printStackTrace();
		}
	}

	public void sendNotification(User user, String message) {
		;
		/**********************
		 * no UDP functionality in Lab 2
		 **********************
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
		 */
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 5) {
			System.err.println("Invalid arguments!");
			System.err.println("USAGE: java AuctionServer <tcpPort> <analyticsBindingName> <billingBindingName>");
			System.err.println("tcpPort must be numeric and <= 65535!");
		} else {
			try {
				int tcpPort = Integer.parseInt(args[0]); // check if tcpPort is numeric
				if (tcpPort <= 65535) {
					AuctionServer main = new AuctionServer(tcpPort, args[3], args[4]);
					main.runServer(args[2]);
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
