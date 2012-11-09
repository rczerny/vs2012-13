package server.analytics;

public class StatisticsEvent implements Event{
	private String id;
	private enum Type {USER_SESSIONTIME_MIN, USER_SESSIONTIME_MAX, USER_SESSIONTIME_AVG, BID_PRICE_MAX, BID_COUNT_PER_MINUTE, AUCTION_TIME_AVG, AUCTION_SUCESS_RATIO;}
	private Type type;
	private long timestamp;
	private double value;
	
	public StatisticsEvent() {
		
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

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}	
}
