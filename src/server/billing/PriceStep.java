package server.billing;

import java.io.Serializable;

public class PriceStep implements Serializable
{
	private static final long serialVersionUID = -6795125171787154729L;
	private double startPrice;
	private double endPrice;
	private double fixedPrice;
	private double variablePricePercent;

	public PriceStep(double startPrice, double endPrice, double fixedPrice, double variablePricePercent) {
		this.startPrice = startPrice;
		this.endPrice = endPrice;
		this.fixedPrice = fixedPrice;
		this.variablePricePercent = variablePricePercent;
	}

	public double getStartPrice() {
		return startPrice;
	}

	public void setStartPrice(double startPrice) {
		this.startPrice = startPrice;
	}

	public double getEndPrice() {
		return endPrice;
	}

	public void setEndPrice(double endPrice) {
		this.endPrice = endPrice;
	}

	public double getFixedPrice() {
		return fixedPrice;
	}

	public void setFixedPrice(double fixedPrice) {
		this.fixedPrice = fixedPrice;
	}

	public double getVariablePricePercent() {
		return variablePricePercent;
	}

	public void setVariablePricePercent(double variablePricePercent) {
		this.variablePricePercent = variablePricePercent;
	}
}

	
