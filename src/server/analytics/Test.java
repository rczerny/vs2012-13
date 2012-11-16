package server.analytics;

import java.rmi.RemoteException;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Test t = new Test();
		t.test1();
		t.test2();
		t.test3();
	}

	public void test1() {
		AnalyticsServerRMI as = new AnalyticsServer();
		
		String filter = "(USER_*)|(BID_*)|(AUCTION_STARTED)";
		try {
			System.out.println(as.subscribe("test", filter));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			System.out.println(as.unsubscribe("test", 2));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void test2() {
		UserEvent ue = new UserEvent();
		ue.setId("1");
		ue.setType("USER_LOGOUT");
		ue.setUsername("daniel");
		ue.setTimestamp(50000);
		
		System.out.println(ue.toString());
		
		ue.setType("USER_DISCONNECTED");
		
		System.out.println(ue.toString());
		
		ue.setType("Halo");
		System.out.println(ue.toString());
	}
	
	public void test3() {
		
	}
}
