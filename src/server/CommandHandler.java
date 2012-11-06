package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class CommandHandler implements Runnable
{
	private User u = null;
	private Socket sock = null;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	private ServerMain main = null;
	private boolean localShutdown = false;

	public CommandHandler(Socket s, ServerMain main) {
		this.sock = s;
		this.main = main;
		try {
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
		} catch (IOException e) {
			System.err.println("Error when opening I/O streams!");
			e.printStackTrace();
		}
	}

	public synchronized void run() {
		while(!main.getShutdown() && !localShutdown) {
			try {
				sock.setSoTimeout(1000);
				String command = "";
				command = br.readLine();
				if (command == null)
					command = "!end";
				command = command.trim(); // remove leading and trailing whitespaces
				String[] commandParts = command.split("\\s+");
				if (commandParts[0].equals("!list")) {
					bw.newLine();
					bw.flush();
					listAuctions();
					////////////////////////////////////////////
					// !login - Client logs in
					////////////////////////////////////////////
				} else if(commandParts[0].equals("!login")) {
					if (commandParts.length != 3) {
						bw.write("Invalid command! Should be !login <username>");
						bw.newLine();
						bw.flush();
					} else {
						if (u != null && u.isLoggedIn()) { // already logged in?
							bw.write("You are already logged in as " + u.getUsername());
							bw.newLine();
							bw.write("Please logout first!");
							bw.newLine();
							bw.flush();
						} else {
							String username = commandParts[1];
							if (username.length() > 50) { // check if username is too long
								bw.write("Username is too long! Limit is 50 characters!");
								bw.newLine();
								bw.flush();
							} else {
								int udpPort = Integer.parseInt(commandParts[2]);
								u = main.getUser(username);
								if (u != null && u.isLoggedIn()) { // is user already logged in at another session?
									u = null;
									bw.write(username + " is already logged in at another session! Logout first!");
									bw.newLine();
									bw.flush();
								} else { 
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
										if (u.getDueNotifications() != null && u.getDueNotifications().size() > 0) { // any notifications due?
											for (String message : u.getDueNotifications()) {
												main.sendNotification(u, message);
												u.getDueNotifications().remove(message);
											}
										}
									}
									bw.write("Successfully logged in as " + u.getUsername());
									bw.newLine();
									bw.flush();
								}
							}
						}
					}
					////////////////////////////////////////////
					// !logout - Client logs out
					////////////////////////////////////////////
				} else if(commandParts[0].equals("!logout")) {
					if (u == null || !u.isLoggedIn()) {
						bw.write("You have to login first!");
						bw.newLine();
						bw.flush();
					} else {
						String username = u.getUsername();
						u.setLoggedIn(false);
						u.setUdpPort(0);
						bw.write("Successfully logged out as " + username);
						bw.newLine();
						bw.flush();
					}
					////////////////////////////////////////////
					// !create - Client creates an auction
					////////////////////////////////////////////
				} else if(commandParts[0].equals("!create")) {
					if (u != null && u.isLoggedIn()) {
						if (command.split("\\s+", 3).length < 3) {
							bw.write("Invalid command! Should be !create <duration> <description>");
							bw.newLine();
							bw.flush();
						} else {
							String description = command.split("\\s+", 3)[2];
							if (description.length() > 1000) {
								bw.write("Description is too long! Limit is 1000 characters!");
								bw.newLine();
								bw.flush();
							} else {
								long duration = Long.parseLong(command.split("\\s+", 3)[1]);
								main.setHighestAuctionID(main.getHighestAuctionID()+1);
								Auction a = new Auction(main.getHighestAuctionID(), description, u);
								Date date = new Date(new Date().getTime() + (duration*1000));
								a.setDate(date);
								main.auctions.add(a);
								bw.write("An auction '" + description + "' with id " + a.getId() + " has been created and will end on " 
										+ date.toString() + ".");
								bw.newLine();
								bw.flush();
							}
						}
					} else {
						bw.write("You have to login first!");
						bw.newLine();
						bw.flush();
					}
					////////////////////////////////////////////
					// !bid - Client bids on an auction
					////////////////////////////////////////////
				} else if(commandParts[0].equals("!bid")) {
					if (commandParts.length != 3) {
						bw.write("Invalid command! Should be !bid <auction-id> <amount>");
						bw.newLine();
						bw.flush();
					} else if (u != null && u.isLoggedIn()) {
						int id = Integer.parseInt(commandParts[1]);
						double amount = Double.parseDouble(commandParts[2]);
						DecimalFormat f = new DecimalFormat("#0.00");
						String amount_string = f.format(amount);
				        amount = Double.parseDouble(amount_string); 
				        Auction a = main.getAuction(id);
						if (a == null) {
							bw.write("Error! Auction not found!");
							bw.newLine();
							bw.flush();
						} else {
							if (amount > a.getHighestBid()) {
								if (a.getHighestBidder() != null && !a.getHighestBidder().getUsername().equals(u.getUsername())) {
									main.sendNotification(a.getHighestBidder(), "!new-bid " + a.getDescription());
								}
								a.setHighestBid(amount);
								a.setHighestBidder(u);
								bw.write("You successfully bid with " + amount_string + " on '" + a.getDescription() + "'.");
								bw.newLine();
								bw.flush();
							} else {
								bw.write("You unsuccessfully bid with " + amount_string + " on '" + a.getDescription() + "'. ");
								bw.write("Current highest bid is " + f.format(a.getHighestBid()));
								bw.newLine();
								bw.flush();
							}
						}
					} else {
						bw.write("You have to login first!");
						bw.newLine();
						bw.flush();
					}
					////////////////////////////////////////////
					// !end - Client requests connection closing
					////////////////////////////////////////////
				} else if(commandParts[0].equals("!end")) {
					localShutdown = true;
					if (u != null && u.isLoggedIn()) {
						u.setLoggedIn(false);
						//u.setSocket(null);
						u.setUdpPort(0);
					}
				} else {
					bw.write("Unknown command!");
					bw.newLine();
					bw.flush();
				}
			} catch (SocketTimeoutException e) {
				;
			} catch (NumberFormatException e) {
				sendMessage("Invalid format: Found non-number where number was expected!");
			} catch (IOException e) {
				System.err.println("Error while communicating with the client!");
				e.printStackTrace();
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
		if (main.auctions.size() > 0) {
			for (Auction a : main.auctions) {
				try {
					bw.write(a.toString());
					bw.newLine();
					bw.flush();
				} catch (IOException e) {
					System.err.println("Error while returning an auction list!");
					e.printStackTrace();
				}
			}
		} else {
			try {
				bw.write("No auctions available at the moment!");
				bw.newLine();
				bw.flush();
			} catch (IOException e) {
				System.err.println("Error while returning an auction list!");
				e.printStackTrace();
			}
		}
	}

	public void sendMessage(String message) {
		try {
			bw.write(message);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			System.err.println("Error while communicating with the client!");
			e.printStackTrace();
		}
	}
}
