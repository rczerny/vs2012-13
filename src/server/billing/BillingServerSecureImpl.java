package server.billing;

import java.rmi.RemoteException;

public class BillingServerSecureImpl implements BillingServerSecure 
{
	private PriceSteps s;
	
	public BillingServerSecureImpl() {
		s = new PriceSteps();
	}
	
	@Override
	public PriceSteps getPriceSteps() throws RemoteException {
		return s;
	}

	// TODO special case where endPrice is 0 and therefore infinite
	public void createPriceStep(double startPrice, double endPrice,	double fixedPrice, double variablePricePercent) throws RemoteException {
		if (startPrice < 0 || endPrice < 0 || fixedPrice < 0 || variablePricePercent < 0) {
			throw new RemoteException("Error: No negative values allowed!");
		} else if (startPrice > endPrice) {
			throw new RemoteException("Error: Start price mustn't be higher than end price!");
		} else {
			for (PriceStep p : s.getPriceSteps()) {
				if (endPrice > p.getStartPrice() && endPrice <= p.getEndPrice() ||
					startPrice >= p.getStartPrice() && startPrice < p.getEndPrice() ||
					startPrice < p.getStartPrice() && endPrice >= p.getEndPrice()) {
					throw new RemoteException("Error: Couldn't create price step! Overlapping steps would occur! Delete conflicting step first!");
				}
			}
			s.add(new PriceStep(startPrice, endPrice, fixedPrice, variablePricePercent));
		}
	}

	@Override
	public void deletePriceStep(double startPrice, double endPrice) throws RemoteException {
		boolean exists = false;
		for (PriceStep p : s.getPriceSteps()) {
			if (p.getStartPrice() == startPrice && p.getEndPrice() == endPrice) {
				exists = true;
				s.delete(p);
			}
		}
		if (!exists) {
			throw new RemoteException("Error: Price step doesn't exist!");
		}		
	}

	@Override
	public void billAuction(String user, long auctionID, double price)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Bill getBill(String user) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
}
