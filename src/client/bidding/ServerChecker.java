package client.bidding;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class ServerChecker implements Runnable 
{
	private BiddingClient main = null;
	private Timer timer = null;
	private Task task = null;
	private String host = "";
	private int port = 0;

	public ServerChecker(BiddingClient main, String host, int port) {
		this.main = main;
		this.host = host;
		this.port = port;
	}

	public void run() {
		timer = new Timer();
		task = new Task(main, host, port);
		timer.schedule(task, 0, 1000);
	}

	public void cancel() {
		task.cancel();
		timer.cancel();
	}

	public class Task extends TimerTask 
	{
		private Socket s = null;
		private String host = "";
		private int port = 0;
		private BiddingClient main = null;

		public Task(BiddingClient main, String host, int port) {
			this.host = host;
			this.port = port;
			this.main = main;
		}

		public void run() {
			if (main.isServerDown()) {
				try {
					s = new Socket(host, port);
					s.close();
					System.out.println("Server is back online!");
					main.setServerDown(false);
				} catch (UnknownHostException e) {
					;
				} catch (IOException e) {
					;
				}
			}
		}
	}
}
