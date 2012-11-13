package test;

import static org.junit.Assert.*;
import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.junit.Before;
import org.junit.Test;

import server.billing.BillingServerRMI;
import server.billing.BillingServerSecure;
import tools.PropertiesParser;

public class BillingServerSecureTest 
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
			bs = (BillingServerRMI) reg.lookup("RemoteBillingServer");
			bss = (BillingServerSecure) bs.login("franz", "franz");
		} catch (FileNotFoundException e) {
			fail ("properties file not found!");
		} catch (NumberFormatException e) {
			fail("Port non-numeric!");
		} catch (RemoteException e) {
			fail("Registry couln't be found!");
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void createPriceStepNegativeValue() {
		int exceptions = 0;
		try {
			bss.createPriceStep(-1, 1, 5, 1.5);
		} catch (RemoteException e) {
			exceptions++;
		}
		try {
			bss.createPriceStep(0, -1, 5, 1.5);
		} catch (RemoteException e) {
			exceptions++;
		}
		try {
			bss.createPriceStep(0, 1, -5, 1.5);
		} catch (RemoteException e) {
			exceptions++;
		}
		try {
			bss.createPriceStep(0, 1, 5, -1.5);
		} catch (RemoteException e) {
			exceptions++;
		}
		assertEquals(4, exceptions);
	}

	@Test(expected = RemoteException.class)
	public void createPriceStepStartHigherThanEnd() throws RemoteException {
		bss.createPriceStep(2, 1, 5, 1.5);
	}

	@Test
	public void createPriceStep() {
		try {
			bss.createPriceStep(0, 1, 5, 1.5);
			bss.createPriceStep(1, 2, 6, 2);
			bss.createPriceStep(2, 3, 7, 2.5);
			bss.createPriceStep(4, 5, 8, 3);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		try {
			assertEquals(4, bss.getPriceSteps().size());
		} catch (RemoteException e) {
			e.printStackTrace();
			fail("Couldn't determine number of pricesteps");
		}
	}

	@Test
	public void createPriceStepOverlapping() {
		int exceptions = 0;
		try {
			bss.createPriceStep(0, 1, 5, 1.5);
			bss.createPriceStep(1, 2, 6, 2);
			bss.createPriceStep(2, 3, 7, 2.5);
			bss.createPriceStep(4, 5, 8, 3);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		try {
			bss.createPriceStep(3.5, 4.5, 6, 2);
		} catch (RemoteException e) {
			System.out.println("First");
			exceptions++;
		}
		try {
			bss.createPriceStep(2.5, 3.5, 6, 2);
		} catch (RemoteException e) {
			exceptions++;
			System.out.println("Second");
		}
		try {
			bss.createPriceStep(3.5, 5.5, 6, 2);
		} catch (RemoteException e) {
			exceptions++;
			System.out.println("Third");
		}
		try {
			bss.createPriceStep(4.3, 4.8, 6, 2);
		} catch (RemoteException e) {
			System.out.println("Fourth");
			exceptions++;
		}
		try {
			bss.createPriceStep(3, 4, 6, 2); // shouldn't throw an exception
		} catch (RemoteException e) {
			System.out.println("Fifth");
			exceptions++;
		}
		assertEquals(4, exceptions);
	}

	@Test
	public void deletePriceStep() {
		try {
			bss.createPriceStep(5, 6, 5, 1.5);
			bss.deletePriceStep(5, 6);
			assertEquals(0, bss.getPriceSteps().size());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test(expected = RemoteException.class)
	public void deletePriceStepNonExisting() throws RemoteException {
		bss.deletePriceStep(7, 8);
	}

	//TODO: Test case where there is no PriceStep for this price
	@Test
	public void billAuction() {
		try {
			bss.createPriceStep(10, 50, 5, 4);
			bss.billAuction("peter", 1, 23.5);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		String bill = "";
		try {
			bill = bss.getBill("peter").toString();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		assertEquals(bill, "auction_ID	strike_price	fee_fixed	fee_variable	fee_total\n1 23.5 5 0.92 5.92");
	}
}
