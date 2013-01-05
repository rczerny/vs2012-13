package server.auction;

import java.util.HashSet;
import java.util.Set;

public class GroupBid {
	private boolean confirmed = false;
	private String user = "";
	private double amount = 0;
	private int auctionId = 0;
	private Set<String> groupUser = new HashSet<String>();
	
	public GroupBid(String user, int auctionId, double amount) {
		this.user = user;
		this.auctionId = auctionId;
		this.amount = amount;
		groupUser.add(user);
	}
	
	public boolean isConfirmed() {
		return confirmed;
	}
	
	public String getUser() {
		return user;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public int getAuctionId() {
		return auctionId;
	}
	
	public void confirm(String u){
		if(!groupUser.contains(u)) {
			groupUser.add(u);
		}
		
		if(groupUser.size()>=3) {
			confirmed = true;
		}
	}
	
	public void unConfirm(String u){
		if(groupUser.contains(u)) {
			groupUser.remove(u);
		}
	}
	
	public String toString() {
		return "AuctionID: " + auctionId + " Amount: " + amount + " Bidder: " + user;
	}
}
