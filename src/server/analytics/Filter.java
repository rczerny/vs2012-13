package server.analytics;

import java.util.ArrayList;

public class Filter {
	enum Type{AUCTION_STARTED, AUCTION_ENDED, BID_PLACED, BID_OVERBID, BID_WON, USER_SESSIONTIME_MIN, USER_SESSIONTIME_MAX, USER_SESSIONTIME_AVG, BID_PRICE_MAX, BID_COUNT_PER_MINUTE, AUCTION_TIME_AVG, AUCTION_SUCESS_RATIO, USER_LOGIN, USER_LOGOUT, USER_DISCONNECTED;}
	private ArrayList<String> types;

	public Filter(String s) {
		types = new ArrayList<String>();
		createFilter(s);
	}

	private void createFilter(String s) {
		String[] filterParts = s.split(" ");
		for(int i = 0;i<filterParts.length;i++){
			//remove ( and )
			if(filterParts[i].charAt(0) == '(') {
				filterParts[i] = filterParts[i].substring(1, filterParts[i].length()-1);
			} else {
				System.out.println("Filter muss in Runden Klammern stehen!");
			}

			//check for wildcards
			if(filterParts[i].contains("*")) {
				if(filterParts[i].equals("USER_*")) {
					types.add("USER_LOGIN");
					types.add("USER_LOGOUT");
					types.add("USER_DISCONNECTED");
				}
			}else {
				types.add(filterParts[i]);
			}
		}
	}

	public ArrayList<String> getTypes() {
		return types;
	}

	public void setTypes(ArrayList<String> types) {
		this.types = types;
	}
}
