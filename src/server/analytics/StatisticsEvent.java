package server.analytics;

public class StatisticsEvent extends Event{
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
}
