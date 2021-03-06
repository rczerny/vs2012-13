package server.billing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleListener implements Runnable 
{
	private BillingServer main = null;
	private BufferedReader br = null;

	public ConsoleListener(BillingServerRMI bs) {
		this.main = (BillingServer)bs;
		br = new BufferedReader(new InputStreamReader(System.in));
	}

	public void run() {
		try {
			while (!br.readLine().equals("!exit")) {
				;
			}
			main.shutdown();
		} catch (IOException e) {
			System.err.println("ERROR: Console I/O Error!");
			e.printStackTrace();
		}
	}
}
