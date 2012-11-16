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

import server.analytics.AnalyticsServerRMI;
import tools.PropertiesParser;

public class AnalyticsServerTest {

	private AnalyticsServerRMI as = null;
	private PropertiesParser ps = null;
	private Registry reg = null;

	@Before
	public void setUp() {

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
			String test = as.subscribe("test", "(USER_*)|(BID_WON)");
			assertEquals(test, a);
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
			String test = as.subscribe("test", "(USER_WON");
			assertEquals(test, a);
		} catch (RemoteException e) {
			fail("Remote Error executing subscribe function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}
	}

	@Test
	public void testValidUnsubscribe() {
		assertNotNull(reg);
		String a = "subscription 1 terminated";
		String test = "";
		try {
			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
			as.subscribe("test", "(USER_*)|(BID_WON)");
		} catch (RemoteException e) {
			fail("Remote Error executing subscribe function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}
		
		try {
			test = as.unsubscribe("test", 1);
		} catch (RemoteException e) {
			fail("Remote Error executing unsubscribe function!");
		}

		assertEquals(test, a);
	}

	@Test
	public void testInvalidUnsubscripe() {
		assertNotNull(reg);
		String a = "unsubscribe failed";
		String test = "";
		try {
			as = (AnalyticsServerRMI) reg.lookup("RemoteAnalyticsServer");
			as.subscribe("test", "(USER_*)|(BID_WON)");
		} catch (RemoteException e) {
			fail("Remote Error executing subscribe function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}
		
		try {
			test = as.unsubscribe("test", 100);
		} catch (RemoteException e) {
			fail("Remote Error executing unsubscribe function!");
		}

		assertEquals(test, a);
	}
}
