package server.billing;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class Bill implements Serializable {

	private static final long serialVersionUID = -4980948864848742035L;
	private Set<AuctionCharging> auctionChargings;
	
	public Bill() {
		auctionChargings = Collections.synchronizedSortedSet(new TreeSet<AuctionCharging>(new AuctionChargingComparator()));
	}
	
	public void addAuctionCharging(AuctionCharging ac) {
		auctionChargings.add(ac);
	}
	
	public Set<AuctionCharging> getAuctionChargings() {
		return auctionChargings;
	}
}
