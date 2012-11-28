package server.analytics;

import java.util.ArrayList;

public class Subscription {
	String[] a = {"AUCTION_STARTED", "AUCTION_ENDED", "BID_PLACED", "BID_OVERBID", "BID_WON", "USER_SESSIONTIME_MIN", "USER_SESSIONTIME_MAX", "USER_SESSIONTIME_AVG", "BID_PRICE_MAX", "BID_COUNT_PER_MINUTE", "AUCTION_TIME_AVG", "AUCTION_SUCESS_RATIO", "USER_LOGIN", "USER_LOGOUT", "USER_DISCONNECTED"};
	final ArrayList<String> types = new ArrayList<String>();
	int id;
	ArrayList<String> filter;
	Client c;

	public Subscription(int id, String f, Client c) {
		this.id = id;	
		for(int i = 0;i<a.length;i++) {
			types.add(a[i]);
		}

		this.c = c;
		filter = new ArrayList<String>();
	}

	public String createFilter(String f) {
		String answer = "";
		boolean dublicates = false;
		
		if(f.charAt(0) == '\'' && f.charAt(f.length()-1) == '\'') {
			f = f.substring(1, f.length()-1);
		}

		String[] filterParts = f.split("\\|");
		for(int i = 0;i<filterParts.length;i++){
			//remove ( and )
			if(filterParts[i].charAt(0) == '(') {
				filterParts[i] = filterParts[i].substring(1, filterParts[i].length()-1);
			}
			
			if(filterParts[i].contains("*")) {
				filterParts[i] = filterParts[i].replace("*", ".*");
			}

			
			//for each type of types check if matches with pattern
			for(String type:types) {
				if(type.matches(filterParts[i])) {
					//add type to filter
					if(!filter.contains(type)) {
						boolean alreadyExists = false;
						for(Subscription s:c.getSubscriptions().values()) {
							if(s.getFilter().contains(type)) {
								alreadyExists = true;
								dublicates = true;
							}
						}
						if(!alreadyExists) {
							filter.add(type);
						}
					}
				}
			}



		}
		
		if(dublicates) {
			if(filter.isEmpty()) {
				answer = "ALL";
			} else {
				answer = "SOME";
			}			
		} else {
			answer = "ELSE";
		}
		return answer;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<String> getFilter() {
		return filter;
	}

	public void setFilter(ArrayList<String> filter) {
		this.filter = filter;
	}

	public ArrayList<String> getTypes() {
		return types;
	}

	public String toString(){
		return id + " " + filter.toString();
	}
}
