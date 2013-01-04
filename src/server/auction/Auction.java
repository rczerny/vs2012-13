package server.auction;

import java.text.DecimalFormat;
import java.util.Date;

public class Auction 
{
	private int id = 0;
	private String description = "";
	private User owner = null;
	private double highestBid = 0.00;
	private User highestBidder = null;
	private Date date = null;

	public Auction(int id, String description, User owner) {
		this.id = id;
		this.description = description;
		this.owner = owner;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public double getHighestBid() {
		return highestBid;
	}

	public void setHighestBid(double highestBid) {
		this.highestBid = highestBid;
	}

	public User getHighestBidder() {
		return highestBidder;
	}

	public void setHighestBidder(User highestBidder) {
		this.highestBidder = highestBidder;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String toString() {
		String text = getId() + ". '" + getDescription() + "' " + getOwner().getUsername() 
					  + " " + date.toString() + " " + getHighestBid() + " ";
		if (getHighestBidder() == null) {
			text += "none";
		} else {
			text += getHighestBidder().getUsername();
		}
		return text;
	}
}
