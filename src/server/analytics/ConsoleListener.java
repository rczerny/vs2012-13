package server.analytics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleListener implements Runnable 
{
	private AnalyticsServer main = null;
	private BufferedReader br = null;

	public ConsoleListener(AnalyticsServerRMI bs) {
		this.main = (AnalyticsServer)bs;
		br = new BufferedReader(new InputStreamReader(System.in));
	}

	public void run() {
		try {
			while (!br.readLine().equals("!exit")) {
				;
			}
			main.shutdown();
		} catch (IOException e) {
			;
		}
	}
}
