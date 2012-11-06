package server;

import java.io.IOException;

public class ConsoleListener implements Runnable
{
	ServerMain main = null;
	
	public ConsoleListener(ServerMain main) {
		this.main = main;
	}
	
	public void run() {
		while (!main.getShutdown()) {
			try {
				if (System.in.read() == '\n') {
					main.setShutdown(true);
					break;
				}
			} catch (IOException e) {
				System.err.println("Console I/O error!");
				e.printStackTrace();
			}
		}
	}
}
