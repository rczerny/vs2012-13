package server.analytics;

import java.util.ArrayList;

public class Subscription {
	String[] a = {"AUCTION_STARTED", "AUCTION_ENDED", "BID_PLACED", "BID_OVERBID", "BID_WON", "USER_SESSIONTIME_MIN", "USER_SESSIONTIME_MAX", "USER_SESSIONTIME_AVG", "BID_PRICE_MAX", "BID_COUNT_PER_MINUTE", "AUCTION_TIME_AVG", "AUCTION_SUCESS_RATIO", "USER_LOGIN", "USER_LOGOUT", "USER_DISCONNECTED"};
	final ArrayList<String> types = new ArrayList<String>();
	int id;
	ArrayList<String> filter;

	public Subscription(int id, String f) {
		this.id = id;	
		for(int i = 0;i<a.length;i++) {
			types.add(a[i]);
		}

		filter = new ArrayList<String>();
		createFilter(f);
	}

	private void createFilter(String f) {
		String[] filterParts = f.split("\\|");
		for(int i = 0;i<filterParts.length;i++){
			//remove ( and )
			if(filterParts[i].charAt(0) == '(') {
				filterParts[i] = filterParts[i].substring(1, filterParts[i].length()-1);

				if(!filterParts[i].contains("*")) {
					if(types.contains(filterParts[i])) {
						filter.add(filterParts[i]);
					} else {
						System.out.println("Filter does not exist!");
					}
				} else {

				}

			} else {
				System.out.println("Filter muss in Runden Klammern stehen!");
			}
		}
	}

	public String toString(){
		return id + " " + filter.toString();
	}
}
