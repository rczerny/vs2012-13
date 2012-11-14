package server.analytics;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Subscription s = new Subscription(1, "(USER_*)|(BID_*)|(AUCTION_STARTED)");
		System.out.println(s.filter.toString());		
	}

}
