package server.analytics;

public class StatisticsEvent extends Event{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private enum Type {USER_SESSIONTIME_MIN, USER_SESSIONTIME_MAX, USER_SESSIONTIME_AVG, BID_PRICE_MAX, BID_COUNT_PER_MINUTE, AUCTION_TIME_AVG, AUCTION_SUCESS_RATIO;}

	private double value;
	
	public StatisticsEvent() {

	}

	public void setType(String type){
		if(type.equals(Type.AUCTION_SUCESS_RATIO.toString()) || type.equals(Type.AUCTION_TIME_AVG.toString()) ||
				type.equals(Type.BID_COUNT_PER_MINUTE.toString()) || type.equals(Type.BID_PRICE_MAX.toString()) || type.equals(Type.USER_SESSIONTIME_AVG.toString()) 
				|| type.equals(Type.USER_SESSIONTIME_MAX.toString()) || type.equals(Type.USER_SESSIONTIME_MIN.toString())) {
			this.type = type;
		}
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	public String toString() {
		String prefix = super.toString();
		String suffix ="";
		
		if(this.type.equals("USER_SESSIONTIME_MIN")) {
			suffix = "minimum session time is " + value + " seconds";
		}
		
		if(this.type.equals("USER_SESSIONTIME_MAX")) {
			suffix = "maximum session time is " + value + " seconds";
		}
		
		if(this.type.equals("USER_SESSIONTIME_AVG")) {
			suffix = "average session time is " + value + " seconds";
		}
		
		if(this.type.equals("BID_PRICE_MAX")) {
			suffix = "maximum bid price seen so far is " + value;
		}
		
		if(this.type.equals("BID_COUNT_PER_MINUTE")) {
			suffix = "current bids per minute is " + value;
		}
		
		if(this.type.equals("AUCTION_TIME_AVG")) {
			suffix = "average auction time is " + value + " seconds";
		}
		
		if(this.type.equals("AUCTION_SUCESS_RATIO")) {
			suffix = "auction sucess ratio is " + value;
		}
		
		return prefix + suffix;
	}
}
