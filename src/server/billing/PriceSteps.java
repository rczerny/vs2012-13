package server.billing;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class PriceSteps implements Serializable
{
	private static final long serialVersionUID = 6285326241808704903L;
	private Set<PriceStep> priceSteps;
	
	public PriceSteps() {
		priceSteps = Collections.synchronizedSortedSet(new TreeSet<PriceStep>(new PriceStepComparator()));
	}

	public Set<PriceStep> getPriceSteps() {
		return priceSteps;
	}

	public void setPriceSteps(Set<PriceStep> priceSteps) {
		this.priceSteps = priceSteps;
	}
	
	public void add(PriceStep p) {
		priceSteps.add(p);
	}
	
	public int size() {
		return priceSteps.size();
	}
	
	public void delete(PriceStep p) {
		priceSteps.remove(p);
	}
}
