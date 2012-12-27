package server.auction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ConcurrentModificationException;
import java.util.Date;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

import server.analytics.AnalyticsServerRMI;
import server.analytics.AuctionEvent;
import server.analytics.BidEvent;
import server.analytics.UserEvent;
import tools.PropertiesParser;
import tools.SuperSecureSocket;

public class CommandHandler implements Runnable
{
	private User u = null;
	private Socket sock = null;
	private SuperSecureSocket ssock = null;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	private AuctionServer main = null;
	private boolean localShutdown = false;

	//RMI Analytics Server
	private AnalyticsServerRMI as = null;
	private PropertiesParser ps = null;
	private Registry reg = null;

	public CommandHandler(Socket s, AuctionServer main) {
		this.sock = s;
		ssock = new SuperSecureSocket(sock, main.getServerPrivKey(), main.getClientsKeyDir());
		this.main = main;
		br = new BufferedReader(new InputStreamReader(ssock.getInputStream()));
		bw = new BufferedWriter(new OutputStreamWriter(ssock.getOutputStream()));

		try {
			ps = new PropertiesParser("registry.properties");
			int portNr = Integer.parseInt(ps.getProperty("registry.port"));
			String host = ps.getProperty("registry.host");
			reg = LocateRegistry.getRegistry(host, portNr);
			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
		} catch (FileNotFoundException e) {
			System.out.println("properties file not found!");
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println("Port non-numeric!");
			e.printStackTrace();
		} catch (RemoteException e) {
			System.out.println("Registry couln't be found!");
			e.printStackTrace();
		} catch (NotBoundException e) {
			System.out.println("Specified remote object couldn't be found in registry!");
			e.printStackTrace();
		}
	}

	public void run() {
		while(!main.getShutdown() && !localShutdown) {
			try {
				//sock.setSoTimeout(1000);
				String command = "";
				command = ssock.readLine();
				System.out.println("Got " + command);
				if (command == null)
					command = "!end";
				command = command.trim(); // remove leading and trailing whitespaces
				String[] commandParts = command.split("\\s+");
				if (commandParts[0].equals("!list")) {
					listAuctions();
					////////////////////////////////////////////
					// !login - Client logs in
					////////////////////////////////////////////
				} else if(commandParts[0].equals("!login")) {
					byte[] clientChallenge = Base64.decode(commandParts[3]);
					System.out.println(command);
					System.out.println(commandParts[3]);
					byte[] clientChallengeB64 = ssock.decrypt(clientChallenge, "RSA/NONE/OAEPWithSHA256AndMGF1Padding", ssock.getPEMPrivateKey(main.getServerPrivKey()));
					clientChallenge = Base64.decode(clientChallengeB64);
					if (commandParts.length != 4) {
						ssock.sendLine("Invalid command! Should be !login <username>");
					} else {
						if (u != null && u.isLoggedIn()) { // already logged in?
							ssock.sendLine("You are already logged in as " + u.getUsername() + "\nPlease logout first!");
						} else {
							String username = commandParts[1];
							if (username.length() > 50) { // check if username is too long
								ssock.sendLine("Username is too long! Limit is 50 characters!");
							} else {
								int udpPort = Integer.parseInt(commandParts[2]);
								u = main.getUser(username);
								if (u != null && u.isLoggedIn()) { // is user already logged in at another session?
									u = null;
									ssock.sendLine(username + " is already logged in at another session! Logout first!");
								} else {
									System.out.println("Key generation!");
									byte[] secretKey = ssock.generateSecureRandomNumber(32);
									byte[] serverChallenge = ssock.generateSecureRandomNumber(32);
									byte[] iv = ssock.generateSecureRandomNumber(16);
									String message2 = new String(clientChallengeB64) + " " + new String(Base64.encode(serverChallenge)) +
											" " + new String(Base64.encode(secretKey)) + " " + new String(Base64.encode(iv));
									byte[] message2B = ssock.encrypt(message2.getBytes(), "RSA/NONE/OAEPWithSHA256AndMGF1Padding", ssock.getPEMPublicKey(main.getClientsKeyDir() + username + ".pub.pem")); 
									message2 = "!ok " + new String(Base64.encode(message2B));
									System.out.println("iv-length: " +iv.length);
									System.out.println("Just before sending message 2");
									ssock.sendLine(message2);
									System.out.println("Just after sending message 2");
									ssock.setSecretKey(new SecretKeySpec(secretKey, "AES"));
									ssock.setIv(new IvParameterSpec(iv));
									String message3 = ssock.readLine();
									System.out.println("message3: " + message3);
									if (message3.equals(new String(serverChallenge))) {
										System.out.println("serverChallenges match!");
										if (u == null) { // new user?
											u = new User(sock);
											u.setUsername(username);
											u.setUdpPort(udpPort);
											u.setLoggedIn(true);
											main.users.add(u);
										} else { // user is known to the system
											u.setUdpPort(udpPort);
											u.setLoggedIn(true);
											u.setSocket(sock);
											/*********************
											 * no UDP in Lab 2
											 *********************
										if (u.getDueNotifications() != null && u.getDueNotifications().size() > 0) { // any notifications due?
											for (String message : u.getDueNotifications()) {
												main.sendNotification(u, message);
												u.getDueNotifications().remove(message);
											}
										}
											 */
										}
										System.out.println("Successfully logged in as " + u.getUsername());
										ssock.sendLine(new String("Successfully logged in as " + u.getUsername()));
										/*bw.write("ready");
										bw.newLine();
										bw.flush();*/
										System.out.println("Ready to roll!");
										/*try{
											UserEvent ue = new UserEvent();
											ue.setType("USER_LOGIN");
											ue.setUsername(u.getUsername());
											ue.setTimestamp(System.currentTimeMillis());
											as.processEvent(ue);
										} catch (RemoteException e) {
											System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
											//e.printStackTrace();
										}*/
									}
								}
							}
						}
					}
					////////////////////////////////////////////
					// !logout - Client logs out
					////////////////////////////////////////////
				} else if(commandParts[0].equals("!logout")) {
					if (u == null || !u.isLoggedIn()) {
						ssock.sendLine("You have to login first!");
					} else {
						String username = u.getUsername();
						u.setLoggedIn(false);
						u.setUdpPort(0);
						ssock.setIv(null);
						ssock.setSecretKey(null);
						ssock.sendLine("Successfully logged out as " + username);
						/*try{
							UserEvent ue = new UserEvent();
							ue.setType("USER_LOGOUT");
							ue.setUsername(u.getUsername());
							ue.setTimestamp(System.currentTimeMillis());
							as.processEvent(ue);
						} catch (RemoteException e) {
							System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
							//e.printStackTrace();
						}*/
					}
					////////////////////////////////////////////
					// !create - Client creates an auction
					////////////////////////////////////////////
				} else if(commandParts[0].equals("!create")) {
					if (u != null && u.isLoggedIn()) {
						if (command.split("\\s+", 3).length < 3) {
							ssock.sendLine("Invalid command! Should be !create <duration> <description>");
						} else {
							String description = command.split("\\s+", 3)[2];
							if (description.length() > 1000) {
								ssock.sendLine("Description is too long! Limit is 1000 characters!");
							} else {
								long duration = Long.parseLong(command.split("\\s+", 3)[1]);
								main.setHighestAuctionID(main.getHighestAuctionID()+1);
								Auction a = new Auction(main.getHighestAuctionID(), description, u);
								Date date = new Date(new Date().getTime() + (duration*1000));
								a.setDate(date);
								main.auctions.add(a);
								ssock.sendLine("An auction '" + description + "' with id " + a.getId() + " has been created and will end on " 
										+ date.toString() + ".");
								try{
									AuctionEvent ae = new AuctionEvent();
									ae.setType("AUCTION_STARTED");
									ae.setAuctionID(a.getId());
									ae.setTimestamp(System.currentTimeMillis());
									ae.setDuration(duration);
									as.processEvent(ae);
								} catch (RemoteException e) {
									System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
									//e.printStackTrace();
								} catch (ConcurrentModificationException e) {
									;
								}
							}
						}
					} else {
						ssock.sendLine("You have to login first!");
					}
					////////////////////////////////////////////
					// !bid - Client bids on an auction
					////////////////////////////////////////////
				} else if(commandParts[0].equals("!bid")) {
					if (commandParts.length != 3) {
						ssock.sendLine("Invalid command! Should be !bid <auction-id> <amount>");
					} else if (u != null && u.isLoggedIn()) {
						int id = Integer.parseInt(commandParts[1]);
						double amount = Double.parseDouble(commandParts[2]);
						//DecimalFormat f = new DecimalFormat("#0.00");
						//String amount_string = f.format(amount);
						//amount = Double.parseDouble(amount_string); 
						Auction a = main.getAuction(id);
						if (a == null) {
							ssock.sendLine("Error! Auction not found!");
						} else {
							if (amount > a.getHighestBid()) {
								/*********************
								 * no UDP in Lab 2
								 *********************
								if (a.getHighestBidder() != null && !a.getHighestBidder().getUsername().equals(u.getUsername())) {
									main.sendNotification(a.getHighestBidder(), "!new-bid " + a.getDescription());
								}
								 */
								if (a.getHighestBidder() != null) {
									try {
										BidEvent be = new BidEvent();
										be.setType("BID_OVERBID");
										be.setUsername(a.getHighestBidder().getUsername());
										be.setAuctionId(a.getId());
										be.setPrice(amount);
										be.setTimestamp(System.currentTimeMillis());
										as.processEvent(be);
									} catch (RemoteException e) {
										System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
										//e.printStackTrace();
									}
								}

								a.setHighestBid(amount);
								a.setHighestBidder(u);
								ssock.sendLine("You successfully bid with " + amount + " on '" + a.getDescription() + "'.");
								try{
									BidEvent be = new BidEvent();
									be.setType("BID_PLACED");
									be.setUsername(u.getUsername());
									be.setAuctionId(a.getId());
									be.setPrice(amount);
									be.setTimestamp(System.currentTimeMillis());
									as.processEvent(be);
								} catch (RemoteException e) {
									System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
									//e.printStackTrace();
								}

							} else {
								ssock.sendLine("You unsuccessfully bid with " + amount + " on '" + a.getDescription() + "'. Current highest bid is " + (a.getHighestBid()));
							}
						}
					} else {
						ssock.sendLine("You have to login first!");
					}
					////////////////////////////////////////////
					// !end - Client requests connection closing
					////////////////////////////////////////////
				} else if(commandParts[0].equals("!end")) {
					localShutdown = true;
					if (u != null && u.isLoggedIn()) {
						try{
							UserEvent ue = new UserEvent();
							ue.setType("USER_LOGOUT");
							ue.setUsername(u.getUsername());
							ue.setTimestamp(System.currentTimeMillis());
							as.processEvent(ue);
						} catch (RemoteException e) {
							System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
							//e.printStackTrace();
						}
						u.setLoggedIn(false);
						//u.setSocket(null);
						u.setUdpPort(0);
					}
				} else {
					ssock.sendLine("Unknown command!");
				}
			} catch (SocketTimeoutException e) {
				;
			} catch (NumberFormatException e) {
				try {
					ssock.sendLine("Invalid format: Found non-number where number was expected!");
				} catch (IOException ex) {
					u.setLoggedIn(false);
				}
			} catch (IOException e) {
				//try{
				/*UserEvent ue = new UserEvent();
					ue.setType("USER_DISCONNECTED");
					ue.setUsername(u.getUsername());
					ue.setTimestamp(System.currentTimeMillis());
					as.processEvent(ue);*/
				/*} catch (RemoteException e1) {
					System.err.println("Error: Couldn't create event! AnalyticsServer may be down!");
					//e1.printStackTrace();
				}*/
				System.err.println("Error while communicating with the client!");
				e.printStackTrace();
				u.setLoggedIn(false);
				break;
			}
		}
		///////////////////////////
		// SHUTDOWN THIS CONNECTION
		///////////////////////////
		try {
			sock.close();
		} catch (IOException e) {
			System.err.println("Error closing the connection!");
			e.printStackTrace();
		}
	}

	public void listAuctions() {
		System.out.println("listAuctions()");
		try{			
			Key secretKey = ssock.getPEMPrivateKey(main.getClientsKeyDir());

			// create a MAC and initialize with the above key
			Mac mac = Mac.getInstance(secretKey.getAlgorithm());
			mac.init(secretKey);
			String message = "This is a confidential message";

			// get the string as UTF-8 bytes
			byte[] b = message.getBytes("UTF-8");

			// create a digest from the byte array
			byte[] digest = mac.doFinal(b);
		}catch (NoSuchAlgorithmException e) {
			System.out.println("No Such Algorithm:" + e.getMessage());
			return;
		}
		catch (UnsupportedEncodingException e) {
			System.out.println("Unsupported Encoding:" + e.getMessage());
			return;
		}
		catch (InvalidKeyException e) {
			System.out.println("Invalid Key:" + e.getMessage());
			return;
		}

		try {
			if (main.auctions.size() > 0) {
				for (Auction a : main.auctions) {
					
					ssock.sendLine(a.toString());
				}
				ssock.sendLine("ready");
			} else {
				ssock.sendLine("No auctions available at the moment!");
				ssock.sendLine("ready");
			}
		} catch (IOException e) {
			System.err.println("Error while returning an auction list!");
			e.printStackTrace();
		} catch (ConcurrentModificationException e) {
			;
		}
	}
}
