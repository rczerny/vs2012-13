package server.auction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleListener implements Runnable
{
	AuctionServer main = null;
	BufferedReader br = null;

	public ConsoleListener(AuctionServer main) {
		this.main = main;
	}

	public void run() {
		br = new BufferedReader(new InputStreamReader(System.in));
		String input = "";
		while (!main.getShutdown()) {
			try {
				input = br.readLine().trim();
				if (input.equals("!end") || input.equals("!exit")) {
					main.setShutdown(true);
					break;
				} else if (input.equals("!stop")) {
					main.setStopped(true);
					for (User u : main.users) {
						u.getSocket().close();
						u.setLoggedIn(false);
					}
					System.out.println("Server stopped.");
				} else if (input.equals("!reconnect")) {
					main.setStopped(false);
					System.out.println("Server back online.");
				}
			} catch (IOException e) {
				System.err.println("Console I/O error!");
				//e.printStackTrace();
			}
		}
	}
}
