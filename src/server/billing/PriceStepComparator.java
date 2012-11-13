package server.billing;

import java.io.Serializable;
import java.util.Comparator;

public class PriceStepComparator implements Comparator<PriceStep>, Serializable
{
	private static final long serialVersionUID = -6595923729210437474L;

	public int compare(PriceStep first, PriceStep second) {
		int result = 0;
		if (first.getStartPrice() < second.getStartPrice())
			result = -1;
		else if (first.getStartPrice() > second.getStartPrice())
			result = 1;
		else if (first.getStartPrice() == second.getStartPrice())
			result = 0;
		return result;
	}
}
