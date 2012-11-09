package server.analytics;

public interface Event {
	public String getId();
	//public Type getType();
	public long getTimestamp();
}
