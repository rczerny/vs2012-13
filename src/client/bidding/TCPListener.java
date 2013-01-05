package client.bidding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.Signature;

import org.bouncycastle.util.encoders.Base64;

public class TCPListener implements Runnable {

	private ServerSocket serverSocket = null;
	private Socket socket = null;
	private String username = "";
	private int tcpPort = 0;
	private boolean shutdown = false;
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	private PrivateKey clientPrivKey = null;

	public TCPListener(int tcpPort, String username) throws SocketException{
		this.username = username;
		this.tcpPort = tcpPort;
		try {
			serverSocket = new ServerSocket(tcpPort);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setClientPrivKey(PrivateKey clientPrivKey) {
		this.clientPrivKey = clientPrivKey;
	}

	public void run() {
		String input = "";
		while(!shutdown) {
			try {
				socket = serverSocket.accept();
				System.out.println("TCPListener got connection: " + tcpPort);
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				input = br.readLine();
				if (input.trim().startsWith("!getTimeStamp")) {
					String[] commandParts = input.trim().split("\\s+");
					String bid = commandParts[1];
					double price = Double.parseDouble(commandParts[2]);
					String answer = "!timestamp " + bid + " " + price + " " + System.currentTimeMillis();
					System.out.println(answer);
					Signature sig = Signature.getInstance("SHA512withRSA");
					sig.initSign(clientPrivKey);
					sig.update(answer.getBytes());
					answer = answer + " " + new String(Base64.encode(sig.sign()));
					bw.write(answer);
					bw.newLine();
					bw.flush();
				}
			} catch (UnknownHostException e) {
				System.err.println("Host not found!");
			} catch(SocketTimeoutException e) {
				System.out.println("BiddingClient SocketTimeout!");
			} catch (IOException e) {
				System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
				e.printStackTrace();
				shutdown = true;
			} catch (NullPointerException e) {
				System.err.println("I/O Error! Shutting down! The server has probably been shut down.");
				e.printStackTrace();
				shutdown = true;
			} catch (Exception e) {
				System.err.println("Login failed");
				System.err.println(e.getMessage());
			}
		}
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}