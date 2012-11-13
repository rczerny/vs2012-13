package server.analytics;

public class AuctionEvent extends Event{
	private enum Type {AUCTION_STARTED, AUCTION_ENDED;}
	private long auctionID;

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
}
