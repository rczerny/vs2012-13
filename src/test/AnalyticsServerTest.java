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
import server.billing.BillingServerRMI;
import server.billing.BillingServerSecure;
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
}
