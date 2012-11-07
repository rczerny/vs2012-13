package client.bidding;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class UDPListener implements Runnable {

	private DatagramSocket datagramSocket = null;
	private String username = "";
	private boolean shutdown = false;

	public UDPListener(int udpPort, String username) throws SocketException{
		this.username = username;
		datagramSocket = new DatagramSocket(udpPort);
		datagramSocket.setSoTimeout(500);
	}

	public void run() {
		byte[] buffer = new byte[1400];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		String data = "";
		while (!shutdown) {
			try {
				datagramSocket.receive(packet);
				data = new String(packet.getData());
				if (data.startsWith("!auction-ended")) {
					String[] data_parts = data.split("\\s+", 4);
					if (data_parts[1].equals(username)) {
						System.out.println("The auction '" + data_parts[3] + "' has ended. You won with " 
								+ data_parts[2] + "!");
					} else {
						if (data_parts[1].equals("noone")) {
							System.out.println("The auction '" + data_parts[3] + "' has ended. Nobody bid.");
						} else {
							System.out.println("The auction '" + data_parts[3] + "' has ended. " + data_parts[1] +
									" won with " + data_parts[2] + "!");
						}
					}
				} else if (data.startsWith("!new-bid")) {
					String[] data_parts = data.split("\\s+", 2);
					System.out.println("You have been overbid on '" + data_parts[1] + "'");
				}
				BiddingClient.printPROMPT();
			} catch (SocketTimeoutException e) {
				;
			} catch (IOException e) {
				System.err.println("Error receiving UDP data!");
				e.printStackTrace();
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
