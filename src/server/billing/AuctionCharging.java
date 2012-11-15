package server.billing;

import java.io.Serializable;

public class AuctionCharging implements Serializable {

	private static final long serialVersionUID = 9216861958598593491L;
	private long auctionID = 0;
	private double strikePrice = 0;
	private double fixedFee = 0;
	private double variableFee = 0;
	
	public AuctionCharging(long auctionID, double strikePrice, double fixedFee, double variablePricePercent) {
		super();
		this.auctionID = auctionID;
		this.strikePrice = strikePrice;
		this.fixedFee = fixedFee;
		this.variableFee = (variablePricePercent/100)*strikePrice;
	}
	
	public long getAuctionId() {
		return auctionID;
	}
	
	public void setAuctionId(long auctionID) {
		this.auctionID = auctionID;
	}
	
	public double getStrikePrice() {
		return strikePrice;
	}
	
	public void setStrikePrice(double strikePrice) {
		this.strikePrice = strikePrice;
	}
	
	public double getFixedFee() {
		return fixedFee;
	}
	
	public void setFixedFee(double fixedFee) {
		this.fixedFee = fixedFee;
	}
	
	public double getVariableFee() {
		return variableFee;
	}
	
	public void setVariableFee(double variableFee) {
		this.variableFee = variableFee;
	}
}
