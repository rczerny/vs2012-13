package server.billing;

import java.io.IOException;

public class ConsoleListener implements Runnable 
{
	private BillingServer main = null;

	public ConsoleListener(BillingServerRMI bs) {
		this.main = (BillingServer)bs;
	}

	public void run() {
		try {
			while (System.in.read() != '\n') {
				;
			}
			main.shutdown();
		} catch (IOException e) {
			System.err.println("Console I/O Error!");
			e.printStackTrace();
		}
	}
}
