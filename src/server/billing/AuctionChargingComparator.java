package server.billing;

import java.io.Serializable;
import java.util.Comparator;

public class AuctionChargingComparator implements Comparator<AuctionCharging>, Serializable {

	private static final long serialVersionUID = -4245076733060441862L;

	public int compare(AuctionCharging ac1, AuctionCharging ac2) {
		int result = 0;
		if (ac1.getAuctionId() < ac2.getAuctionId())
			result = -1;
		else if (ac1.getAuctionId() > ac2.getAuctionId())
			result = 1;
		else if (ac1.getAuctionId() == ac2.getAuctionId())
			result = 0;
		return result;
	}
}
