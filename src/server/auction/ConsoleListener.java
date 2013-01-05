package server.auction;

import java.io.IOException;

public class ConsoleListener implements Runnable
{
	AuctionServer main = null;

	public ConsoleListener(AuctionServer main) {
		this.main = main;
	}

	public void run() {
		while (!main.getShutdown()) {
			try {
				if (System.in.read() == 'e') {
					if (System.in.read() == '\n') {
						main.setShutdown(true);
						break;
					}
				}
			} catch (IOException e) {
				System.err.println("Console I/O error!");
				e.printStackTrace();
			}
		}
	}
}
