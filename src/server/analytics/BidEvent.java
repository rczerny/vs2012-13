package server.analytics;

public class BidEvent extends Event{
	private enum Type {BID_PLACED, BID_OVERBID, BID_WON;}
	private long auctionId;
	private String username;
	private double price;

	public BidEvent(){

	}

	public void setType(String type) {
		if(type.equals(Type.BID_PLACED.toString()) || type.equals(Type.BID_OVERBID.toString()) || type.equals(Type.BID_WON.toString())) {
			this.type = type;
		}
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
