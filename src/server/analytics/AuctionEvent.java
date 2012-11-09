package server.analytics;

public class AuctionEvent implements Event{
	private String id;
	private enum Type {AUCTION_STARTED, AUCTION_ENDED;}
	private Type type;
	private long timestamp;
	private long auctionID;

	public AuctionEvent(){

	}

	@Override
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	//@Override
	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public long getAuctionID() {
		return this.auctionID;
	}
	
	public void setAuctionID(long auctionID) {
		this.auctionID = auctionID;
	}
}
