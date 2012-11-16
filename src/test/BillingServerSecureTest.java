package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.Before;
import org.junit.Test;

import server.billing.AuctionCharging;
import server.billing.Bill;
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
			fail("Registry couldn't be found!");
		} catch (NotBoundException e) {
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
			exceptions++;
		}
		try {
			bss.createPriceStep(2.5, 3.5, 6, 2);
		} catch (RemoteException e) {
			exceptions++;
		}
		try {
			bss.createPriceStep(3.5, 5.5, 6, 2);
		} catch (RemoteException e) {
			exceptions++;
		}
		try {
			bss.createPriceStep(4.3, 4.8, 6, 2);
		} catch (RemoteException e) {
			exceptions++;
		}
		try {
			bss.createPriceStep(3, 4, 6, 2); // shouldn't throw an exception
		} catch (RemoteException e) {
			exceptions++;
		}
		assertEquals(4, exceptions);
	}
	
	@Test
	public void createPricestepInfinite() {
		int exceptions = 0;
		try {
			bss.createPriceStep(0, 10, 1, 1);
			bss.createPriceStep(20, 0, 1, 1);
		} catch (RemoteException e) {
			fail("Initial creation of initial price step failed!");
		}
		try {
			bss.createPriceStep(10, 23, 1, 1);
		} catch (RemoteException e) {
			exceptions++;
		}
		try {
			bss.createPriceStep(21, 23, 1, 1);
		} catch (RemoteException e) {
			exceptions++;
		}
		try {
			bss.createPriceStep(13, 0, 1, 1);
		} catch (RemoteException e) {
			exceptions++;
		}
		assertEquals(3, exceptions);
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

	@Test
	public void billAuctionNoPriceStep() {
		try {
			bss.getPriceSteps().deleteAll();
			bss.billAuction("peter", 1, 23.5);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		Bill bill = null;
		try {
			bill = bss.getBill("peter");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		long auctionID = 0;
		double strikePrice = 0;
		double fixedFee = 0;
		double variableFee = 0;
		for (AuctionCharging ac : bill.getAuctionChargings()) {
			System.out.println("for: variableFee" + ac.getVariableFee());
			auctionID = ac.getAuctionId();
			fixedFee = ac.getFixedFee();
			variableFee = ac.getVariableFee();
			strikePrice = ac.getStrikePrice();
			System.out.println("billAuctionNoPriceStep");
		}
		System.out.println("auctionID: " + auctionID + " should be 1");
		System.out.println("strikePrice: " + strikePrice + " should be 23.5");
		System.out.println("fixedFee: " + fixedFee + " should be 1");
		System.out.println("variableFee: " + variableFee + " should be 0.3525");
		assertEquals(1, auctionID);
		assertEquals(23.5, strikePrice, 0.0001);
		assertEquals(1, fixedFee, 0.0001);
		assertEquals(0, variableFee, 0.0001);
	}

	@Test
	public void billAuctionNoMatchingPriceStepDefaultToLowerPriceStep() {
		try {
			bss.getPriceSteps().deleteAll();
			bss.createPriceStep(5, 6, 5, 1.5);
			bss.billAuction("peter", 1, 23.5);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		Bill bill = null;
		try {
			bill = bss.getBill("peter");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		long auctionID = 0;
		double strikePrice = 0;
		double fixedFee = 0;
		double variableFee = 0;
		for (AuctionCharging ac : bill.getAuctionChargings()) {
			auctionID = ac.getAuctionId();
			fixedFee = ac.getFixedFee();
			variableFee = ac.getVariableFee();
			strikePrice = ac.getStrikePrice();
			System.out.println("billAuctionNoMatchingPriceStepDefaultToLower");
		}
		System.out.println("auctionID: " + auctionID + " should be 1");
		System.out.println("strikePrice: " + strikePrice + " should be 23.5");
		System.out.println("fixedFee: " + fixedFee + " should be 5");
		System.out.println("variableFee: " + variableFee + " should be 0.3525");
		assertEquals(1, auctionID);
		assertEquals(23.5, strikePrice, 0.0001);
		assertEquals(5, fixedFee, 0.0001);
		assertEquals(0.3525, variableFee, 0.0001);
	}

	@Test
	public void billAuctionNoMatchingLowerPriceStep() {
		try {
			bss.getPriceSteps().deleteAll();
			bss.createPriceStep(50, 60, 5, 1.5);
			bss.billAuction("peter", 1, 23.5);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		Bill bill = null;
		try {
			bill = bss.getBill("peter");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		long auctionID = 0;
		double strikePrice = 0;
		double fixedFee = 0;
		double variableFee = 0;
		for (AuctionCharging ac : bill.getAuctionChargings()) {
			auctionID = ac.getAuctionId();
			fixedFee = ac.getFixedFee();
			variableFee = ac.getVariableFee();
			strikePrice = ac.getStrikePrice();
		}
		System.out.println("billAuctionNoMatchingLowerPriceStep");
		System.out.println("auctionID: " + auctionID + " should be 1");
		System.out.println("strikePrice: " + strikePrice + " should be 23.5");
		System.out.println("fixedFee: " + fixedFee + " should be 1");
		System.out.println("variableFee: " + variableFee + " should be 0.3525");
		assertEquals(1, auctionID);
		assertEquals(23.5, strikePrice, 0.0001);
		assertEquals(1, fixedFee, 0.0001);
		assertEquals(0, variableFee, 0.0001);
	}

	@Test
	public void billAuctionNoBid() {
		try {
			bss.getPriceSteps().deleteAll();
			bss.billAuction("peter", 1, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		Bill bill = null;
		try {
			bill = bss.getBill("peter");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		long auctionID = 0;
		double strikePrice = 0;
		double fixedFee = 0;
		double variableFee = 0;
		for (AuctionCharging ac : bill.getAuctionChargings()) {
			auctionID = ac.getAuctionId();
			fixedFee = ac.getFixedFee();
			variableFee = ac.getVariableFee();
			strikePrice = ac.getStrikePrice();
			System.out.println("billAuctionNoBid");
		}
		System.out.println("auctionID: " + auctionID + " should be 1");
		System.out.println("strikePrice: " + strikePrice + " should be 0");
		System.out.println("fixedFee: " + fixedFee + " should be 1");
		System.out.println("variableFee: " + variableFee + " should be 0");
		assertEquals(1, auctionID);
		assertEquals(0, strikePrice, 0.0001);
		assertEquals(1, fixedFee, 0.0001);
		assertEquals(0, variableFee, 0.0001);
	}

	@Test
	public void billAuction() {
		try {
			bss.getPriceSteps().deleteAll();
			bss.createPriceStep(10, 50, 5, 4);
			bss.billAuction("peter", 1, 23.5);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		Bill bill = null;
		try {
			bill = bss.getBill("peter");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		long auctionID = 0;
		double strikePrice = 0;
		double fixedFee = 0;
		double variableFee = 0;
		for (AuctionCharging ac : bill.getAuctionChargings()) {
			auctionID = ac.getAuctionId();
			fixedFee = ac.getFixedFee();
			variableFee = ac.getVariableFee();
			strikePrice = ac.getStrikePrice();
			System.out.println("billAuction");
		}
		System.out.println("auctionID: " + auctionID + " should be 1");
		System.out.println("strikePrice: " + strikePrice + " should be 23.5");
		System.out.println("fixedFee: " + fixedFee + " should be 5");
		System.out.println("variableFee: " + variableFee + " should be 0.94");
		assertEquals(1, auctionID);
		assertEquals(23.5, strikePrice, 0.0001);
		assertEquals(5, fixedFee, 0.0001);
		assertEquals(0.94, variableFee, 0.0001);
	}
	
}
