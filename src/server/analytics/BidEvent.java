package server.analytics;

public class BidEvent implements Event{
	private String id;
	private enum Type {BID_PLACED, BID_OVERBID, BID_WON;}
	private Type type;
	private long timestamp;
	private long auctionId;
	private String username;
	private double price;

	public BidEvent(){

	}

	@Override
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public long getAuctionId() {
		return auctionId;
	}

	public void setAuctionId(long auctionId) {
		this.auctionId = auctionId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
