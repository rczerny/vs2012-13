package server.billing;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.server.Unreferenced;
import java.util.concurrent.ConcurrentHashMap;

public class BillingServerSecureImpl implements BillingServerSecure, Unreferenced 
{

	public BillingServerSecureImpl() {
	}

	public PriceSteps getPriceSteps() throws RemoteException {
		return BillingServer.s;
	}

	public void unreferenced() {
		shutdown();
	}

	public void shutdown() {
		try {
			UnicastRemoteObject.unexportObject(this, true);
		} catch (NoSuchObjectException e) {
			System.err.println("Object BillingServerSecure couldn't be unexported!");
			e.printStackTrace();
		}
	}

	public void createPriceStep(double startPrice, double endPrice,	double fixedPrice, double variablePricePercent) throws RemoteException {
		if (startPrice < 0 || endPrice < 0 || fixedPrice < 0 || variablePricePercent < 0) {
			throw new RemoteException("Error: No negative values allowed!");
		} else if (endPrice == 0) {
			for (PriceStep p : BillingServer.s.getPriceSteps()) {
				if (p.getStartPrice() > startPrice || p.getEndPrice() > startPrice || p.getEndPrice() == 0) {
					throw new RemoteException("Error: Couldn't create price step! Overlapping steps would occur! Delete conflicting step first!");
				}
			}

		}
		else if (startPrice > endPrice) {
			throw new RemoteException("Error: Start price mustn't be higher than end price!");
		} else {
			for (PriceStep p : BillingServer.s.getPriceSteps()) {
				if (endPrice > p.getStartPrice() && endPrice <= p.getEndPrice() ||
						startPrice >= p.getStartPrice() && startPrice < p.getEndPrice() ||
						startPrice < p.getStartPrice() && endPrice >= p.getEndPrice() ||
						endPrice > p.getStartPrice() && p.getEndPrice() == 0) {
					throw new RemoteException("Error: Couldn't create price step! Overlapping steps would occur! Delete conflicting step first!");
				}
			}
		}
		BillingServer.s.add(new PriceStep(startPrice, endPrice, fixedPrice, variablePricePercent));
	}

	public void deletePriceStep(double startPrice, double endPrice) throws RemoteException {
		boolean exists = false;
		for (PriceStep p : BillingServer.s.getPriceSteps()) {
			if (p.getStartPrice() == startPrice && p.getEndPrice() == endPrice) {
				exists = true;
				BillingServer.s.delete(p);
			}
		}
		if (!exists) {
			throw new RemoteException("Error: Price step doesn't exist!");
		}		
	}

	public void billAuction(String user, long auctionID, double price) throws RemoteException {
		PriceStep ps = BillingServer.s.getPriceStepForPrice(price);
		AuctionCharging ac = new AuctionCharging(auctionID, price, ps.getFixedPrice(), ps.getVariablePricePercent());
		Bill b = BillingServer.bills.get(user);
		if (b == null) {
			b = new Bill();
			BillingServer.bills.put(user, b);
		}
		System.out.println(BillingServer.bills.size() + ", " + b.getAuctionChargings().size());
		System.out.println("billAuction: " + user + " " + auctionID + " " + price);
		System.out.println("Auction: " + ac.getAuctionId() + " " + ac.getFixedFee() + " " + ac.getVariableFee() + " " + ac.getStrikePrice());
		b.addAuctionCharging(ac);
	}

	public Bill getBill(String user) throws RemoteException {
		Bill b = BillingServer.bills.get(user);
		System.out.println(BillingServer.bills.size());
		if (b == null) {
			throw new RemoteException("Error: Unknown user!");
		}
		return b;
	}
}
