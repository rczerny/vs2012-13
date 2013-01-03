package client.bidding;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bouncycastle.openssl.PEMReader;

import tools.SuperSecureSocket;

public class BiddingClient implements Runnable
{	
	private String host = "";
	private int tcpPort = 0;
	private int clientTCPPort = 0;
	private String serverPubKey = "";
	private String clientsKeyDir = "";
	private ExecutorService pool = null;
	private boolean shutdown = false;
	private boolean serverDown = false;
	private static String PROMPT = "> ";
	private String username = "";
	private ArrayList<User> activeUsers;
	private Socket sock;
	private SuperSecureSocket s;
	private ArrayList<String> signedBids = new ArrayList<String>();
	private BufferedReader br = null;
	private BufferedWriter bw = null;

	public BiddingClient() {

	}

	public BiddingClient(String host, int tcpPort, int clientTCPPort, String serverPubKey, String clientsKeyDir) {
		this.host = host;
		this.tcpPort = tcpPort;
		this.clientTCPPort = clientTCPPort;
		this.serverPubKey = serverPubKey;
		this.clientsKeyDir = clientsKeyDir;
		pool = Executors.newCachedThreadPool();
	}

	public void run() {
		TCPListener tcpListener = null; 
		try {
			tcpListener = new TCPListener(clientTCPPort, username);
		} catch (SocketException e) {
			System.err.println("Error opening the datagram socket! Port is probably already used!");
			return;
		}
		pool.execute(tcpListener);
		ServerChecker sc = new ServerChecker(this, host, tcpPort);
		pool.execute(sc);
		BufferedReader keys = null;
		String input = null;
		String answer = null;
		sock = null;
		try {
			sock = new Socket(host, tcpPort);
			PEMReader in;
			PublicKey publicKey = null;
			in = new PEMReader(new FileReader(serverPubKey));
			publicKey = (PublicKey) in.readObject();
			s = new SuperSecureSocket(sock, publicKey, clientsKeyDir);
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
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
				if (serverDown) {
					if (input.trim().startsWith("!bid") && activeUsers != null) {
						if (activeUsers.size() < 2) {
							System.err.println("Can't create signedBid, too few other clients!");
						} else {
							int random1 = 0;
							int random2 = 0;
							while (random1 == random2) {
								random1 = new Random().nextInt(activeUsers.size());
								random2 = new Random().nextInt(activeUsers.size());
							}
							String[] commandParts = input.trim().split("\\s+");
							String bid = commandParts[1];
							double amount = Double.parseDouble(commandParts[2]);
							User user1 = activeUsers.get(random1);
							User user2 = activeUsers.get(random2);
							System.out.println("User1: " + user1.getHost() + " - " + user1.getTcpPort());
							System.out.println("User2: " + user2.getHost() + " - " + user2.getTcpPort());
							Socket s1 = new Socket(user1.getHost(), user1.getTcpPort());
							BufferedReader brTemp = new BufferedReader(new InputStreamReader(s1.getInputStream()));
							BufferedWriter bwTemp = new BufferedWriter(new OutputStreamWriter(s1.getOutputStream()));
							bwTemp.write("!getTimeStamp " + bid + " " + amount);
							bwTemp.newLine();
							bwTemp.flush();
							String result1 = brTemp.readLine();
							String[] result1Parts = result1.trim().split("\\s+");
							Socket s2 = new Socket(user2.getHost(), user2.getTcpPort());
							brTemp = new BufferedReader(new InputStreamReader(s2.getInputStream()));
							bwTemp = new BufferedWriter(new OutputStreamWriter(s2.getOutputStream()));
							bwTemp.write("!getTimeStamp " + bid + " " + amount);
							bwTemp.newLine();
							bwTemp.flush();
							String result2 = brTemp.readLine();
							String[] result2Parts = result2.trim().split("\\s+");
							signedBids.add("!signedBid " + result1Parts[1] + " " + result1Parts[2] + " " + user1.getUsername() + ":" +
									result1Parts[3] + ":" + result1Parts[4] + " " + user2.getUsername() + ":" + result2Parts[3] + ":" + result2Parts[4]);	
						}
					} else {
						System.out.println("Only bidding allowed right now, and only when logged in!");
					}
				} else {
					if (input.trim().startsWith("!login")) {
						if (s.getIv() == null && s.getSecretKey() == null) {
							input += " " + clientTCPPort;
							answer = s.login(input);
							tcpListener.setClientPrivKey(s.getClientPrivKey());
							if (!signedBids.isEmpty()) {
								for (String signedBid : signedBids) {
									System.out.println(s.sendAndReceive(signedBid));
								}
								signedBids.clear();
							}
						}
						else {
							answer = "Already logged in! Logout first!";
						}
					} else if (input.trim().startsWith("!end")) {
						System.out.println("Shutting down...");
						shutdown = true;
						break;
					} else if (input.trim().startsWith("!list")) {
						s.sendLine(input);
						String temp = "";
						while (!(temp = s.readLine()).equals("ready")) {
							answer += "\n" + temp;
						}
					} else if ((input.trim().startsWith("!getClientList"))) {
						answer = getActiveClients();
					} else if (input.trim().startsWith("!logout")) {
						answer = s.sendAndReceive(input);
						s.setIv(null);
						s.setSecretKey(null);
					} else {
						answer = s.sendAndReceive(input);
					}
					if (input.trim().startsWith("!login") && answer.startsWith("Successfully logged in as")) {
						System.out.println("logged in!");
						username = input.trim().split("\\s+")[1];
						PROMPT =  username + PROMPT;
						tcpListener.setUsername(username);
						getActiveClients();
					}
					if (input.trim().startsWith("!logout") && answer.startsWith("Successfully logged out as")) {
						username = "";
						PROMPT =  username + "> ";
					}
					System.out.println(answer);
					answer = "";
				}
			} catch (UnknownHostException e) {
				System.err.println("Host not found!");
			} catch(SocketTimeoutException e) {
				System.out.println("BiddingClient SocketTimeout!");
			} catch (IOException e) {
				System.out.println("Server down! Bidding still possible!");
				serverDown = true;
				//System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
				e.printStackTrace();
				//shutdown = true;
			} catch (NullPointerException e) {
				System.out.println("Server down! Bidding still possible!");
				serverDown = true;
				//System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
				e.printStackTrace();
				//shutdown = true;
			} catch (Exception e) {
				System.err.println("Login failed");
				System.err.println(e.getMessage());
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

	public String getActiveClients() throws IOException {
		s.sendLine("!getClientList");
		activeUsers = new ArrayList<User>();
		String temp = "";
		String result = "";
		while (!(temp = s.readLine()).equals("ready")) {
			result += "\n" + temp;
			if (!temp.startsWith("You") && !temp.startsWith("No")) {
				String host = temp.trim().split(":")[0];
				int port = Integer.parseInt(temp.trim().split(":")[1]);
				String username = temp.trim().split(":")[2];
				User tempUser = new User(InetAddress.getByName(host), port, username);
				activeUsers.add(tempUser);
			}
		}
		System.out.println("Active Users:");
		return result;
	}

	public boolean isServerDown() {
		return serverDown;
	}

	public void setServerDown(boolean serverDown) {
		this.serverDown = serverDown;
		if (!serverDown) {
			try {
				sock = new Socket(host, tcpPort);
				PEMReader in;
				PublicKey publicKey = null;
				in = new PEMReader(new FileReader(serverPubKey));
				publicKey = (PublicKey) in.readObject();
				s = new SuperSecureSocket(sock, publicKey, clientsKeyDir);
			} catch (UnknownHostException e) {
				System.err.println("Host not found!");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Couldn't create socket!");
				e.printStackTrace();
			}
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		}
	}

	public static void main(String[] args) {
		if (args.length != 5) {
			System.err.println("Invalid arguments!");
			System.err.println("USAGE: java ClientMain <serverHostname> <serverPort> <clientPort> <serverPubKey> <clientsKeyDir>");
			System.err.println("serverPort and clientPort must be numeric and <= 65535!");
		} else {
			String host = args[0];
			try {
				int serverPort = Integer.parseInt(args[1]);
				int clientPort = Integer.parseInt(args[2]);
				String serverPubKey = args[3];
				String clientsKeyDir = args[4];
				if (serverPort <= 65535 && clientPort <= 65535) {
					BiddingClient cm = new BiddingClient(host, serverPort, clientPort, serverPubKey, clientsKeyDir);
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
