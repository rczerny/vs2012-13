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

import server.billing.BillingServerRMI;
import server.billing.BillingServerSecure;
import tools.PropertiesParser;

public class BillingServerTest 
{
	private BillingServerRMI bs = null;
	private BillingServerSecure bss = null;
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
			bs = (BillingServerRMI) reg.lookup("RemoteBillingServer");
		} catch (AccessException e) {
			fail("Insufficient access rights to get remote object from registry");
		} catch (RemoteException e) {
			fail("Error obtaining the remote object!");
		} catch (NotBoundException e) {
			fail("Specified remote object couldn't be found in registry!");
		}
	}
	
	@Test
	public void testValidLogin() {
		assertNotNull(reg);
		try {
			bs = (BillingServerRMI) reg.lookup("RemoteBillingServer");
			bss = (BillingServerSecure) bs.login("franz", "franz");
			assertNotNull(bs);
		} catch (RemoteException e) {
			fail("Remote Error executing login function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}
		assertNotNull(bss);
	}
	
	@Test
	public void testInvalidLogin() {
		assertNotNull(reg);
		try {
			bs = (BillingServerRMI) reg.lookup("RemoteBillingServer");
			bss = (BillingServerSecure) bs.login("franz", "peter");
			assertNotNull(bs);
		} catch (RemoteException e) {
			fail("Remote Error executing login function!");
		} catch (NotBoundException e) {
			fail("Remote object couldn't be found!");
		}
		assertNull(bss);
	}
}
