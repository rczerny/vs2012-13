package test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AuctionLoader implements Runnable 
{
	private LoadTestBidder ltb = null;

	public AuctionLoader(LoadTestBidder ltb) {
		this.ltb = ltb;
	}

	public void run() {
		ArrayList<String> answer = null;

		while(!ltb.isShutdown()) {
			answer = ltb.sendAndReceive("!list");
			for (String answerline : answer) {
				if (answerline.startsWith("0") ||
						answerline.startsWith("1") ||
						answerline.startsWith("2") ||
						answerline.startsWith("3") ||
						answerline.startsWith("4") ||
						answerline.startsWith("5") ||
						answerline.startsWith("6") ||
						answerline.startsWith("7") ||
						answerline.startsWith("8") ||
						answerline.startsWith("9")) {
					answerline = answerline.trim();
					String[] answerParts = answerline.split("\\s+");
					int id = Integer.parseInt(answerParts[0].substring(0, answerParts[0].length()-1));
					Date d = null;
					try {
						DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
						d = df.parse(answerParts[3] + " " + answerParts[4] + " " + answerParts[5] + " " +
								answerParts[6] + " "+ answerParts[7] + " " + answerParts[8]);
					} catch (ParseException e) {
						System.err.println("Error getting the auction date!");
						e.printStackTrace();
					}
					ltb.getAuctions().put(id, d);
				}
			}
			try {
				Thread.sleep(ltb.getUpdateIntervalSec()*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
