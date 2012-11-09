package server.analytics;

public class UserEvent implements Event{
	private String id;
	private enum Type {USER_LOGIN, USER_LOGOUT, USER_DISCONNECTED;}
	private Type type;
	private long timestamp;
	private String username;

	public UserEvent(){

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
