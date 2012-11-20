package test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.Before;
import org.junit.Test;

import client.mgmt.ManagementClient;
import client.mgmt.ManagementClientInterface;

import server.analytics.AnalyticsServerRMI;
import server.analytics.AuctionEvent;
import server.analytics.BidEvent;
import server.billing.BillingServerRMI;
import tools.PropertiesParser;

public class AnalyticsServerTest {

	private AnalyticsServerRMI as = null;
	private PropertiesParser ps = null;
	private Registry reg = null;
	private ManagementClientInterface mClient = null;

	@Before
	public void setUp() {

		try {
			ps = new PropertiesParser("registry.properties");
			int portNr = Integer.parseInt(ps.getProperty("registry.port"));
			String host = ps.getProperty("registry.host");
			reg = LocateRegistry.getRegistry(host, portNr);
			mClient = new ManagementClient("RemoteAnalyticsServer", "RemoteBillingServer");
		} catch (FileNotFoundException e) {
			fail ("properties file not found!");
		} catch (NumberFormatException e) {
			fail("Port non-numeric!");
		} catch (RemoteException e) {
			fail("Registry couln't be found!");
		}
	}


	@Test
	public void connect2registry() {
		try {
			ps = new PropertiesParser("registry.properties");
			int portNr = Integer.parseInt(ps.getProperty("registry.port"));
			String host = ps.getProperty("registry.host");
			reg = LocateRegistry.getRegistry(host, portNr);
		} catch (FileNotFoundException e) {
			fail ("properties file not found!");
		} catch (NumberFormatException e) {
			fail("Port non-numeric!");
		} catch (RemoteException e) {
			fail("Registry couln't be found!");
		}
		assertNotNull(reg);
	}


	@Test
	public void getRemoteObject() {
		assertNotNull(reg);
		try {
			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
		} catch (AccessException e) {
			fail("Insufficient access rights to get remote object from registry");
		} catch (RemoteException e) {
			fail("Error obtaining the remote object!");
		} catch (NotBoundException e) {
			fail("Specified remote object couldn't be found in registry!");
		}
	}

	@Test
	public void testValidSubscribe() {
		assertNotNull(reg);
		String a = "Created subscription with ID 1 for events using filter (USER_*)|(BID_WON)";
		try {
			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
			String test = as.subscribe(mClient, "(USER_*)|(BID_WON)");
			assertEquals(a, test);
		} catch (RemoteException e) {
			fail("Remote Error executing subscribe function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}

	}

	@Test
	public void testInvalidSubscripe() {
		assertNotNull(reg);
		String a = "Creating subscription failed!";
		try {
			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
			String test = as.subscribe(mClient, "(USER_WON");
			assertEquals(a, test);
		} catch (RemoteException e) {
			fail("Remote Error executing subscribe function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}
	}

	@Test
	public void testValidUnsubscribe() {
		assertNotNull(reg);
		String a = "subscription 2 terminated";
		String b = "Created subscription with ID 2 for events using filter (BID_PLACED)";
		String test = "";
		try {
			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
			String test2 = as.subscribe(mClient, "(BID_PLACED)");
			assertEquals(b, test2);
		} catch (RemoteException e) {
			fail("Remote Error executing subscribe function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}
		
		try {
			test = as.unsubscribe(mClient, 2);
		} catch (RemoteException e) {
			fail("Remote Error executing unsubscribe function!");
		}

		assertEquals(a, test);
	}

	@Test
	public void testInvalidUnsubscripe() {
		assertNotNull(reg);
		String a = "unsubscribe failed";
		String test = "";
		try {
			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
			as.subscribe(mClient, "(USER_*)|(BID_WON)");
		} catch (RemoteException e) {
			fail("Remote Error executing subscribe function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}
		
		try {
			test = as.unsubscribe(mClient, 100);
		} catch (RemoteException e) {
			fail("Remote Error executing unsubscribe function!");
		}

		assertEquals(a, test);
	}
	
	@Test
	public void testValidProcessEvent() {
		assertNotNull(reg);
		
		try {
			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
		} catch (RemoteException e) {
			fail("Remote Error executing subscribe function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}
		
		BidEvent be = new BidEvent();
		be.setType("BID_WON");
		be.setId("2");
		be.setPrice(2.0);
		
		try {
			as.processEvent(be);
		} catch (RemoteException e) {
			fail("Remote Error executing processEvent function!");
			e.printStackTrace();
		}

		try {
			assertTrue(!mClient.getBuffer().isEmpty());
		} catch (RemoteException e) {
			fail("Remote Error executing getBuffer function!");
			e.printStackTrace();
		}
	}
	
	@Test
	public void testInvalidProcessEvent() {
		assertNotNull(reg);
		
		try {
			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
		} catch (RemoteException e) {
			fail("Remote Error executing subscribe function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}
		
		AuctionEvent ae = new AuctionEvent();
		ae.setType("AUCTION_STARTED");
		ae.setId("1");
		ae.setAuctionID(50000);
		try {
			mClient.getBuffer().clear();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			as.processEvent(ae);
		} catch (RemoteException e) {
			fail("Remote Error executing processEvent function!");
			e.printStackTrace();
		}

		try {
			assertTrue(mClient.getBuffer().isEmpty());
		} catch (RemoteException e) {
			fail("Remote Error executing getBuffer function!");
			e.printStackTrace();
		}
	}
}
