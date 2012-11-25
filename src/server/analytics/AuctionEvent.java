package server.analytics;

public class AuctionEvent extends Event{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private enum Type {AUCTION_STARTED, AUCTION_ENDED;}
	private long auctionID;
	private double duration;
	private boolean success = false;

	public AuctionEvent(){

	}

	public void setType(String type) {
		if(type.equals(Type.AUCTION_STARTED.toString()) || type.equals(Type.AUCTION_ENDED.toString())) {
			this.type = type;
		}
	}

	public long getAuctionID() {
		return this.auctionID;
	}
	
	public void setAuctionID(long auctionID) {
		this.auctionID = auctionID;
	}
	
	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String toString() {
		String prefix = super.toString();
		String suffix ="";
		
		if(this.type.equals("AUCTION_STARTED")) {
			suffix = "auction " + auctionID + " started";
		}
		
		if(this.type.equals("AUCTION_ENDED")) {
			suffix = "auction " + auctionID + " ended";
		}		
		
		return prefix + suffix;
	}
}
