package server.analytics;

public class UserEvent extends Event{
	private enum Type {USER_LOGIN, USER_LOGOUT, USER_DISCONNECTED;}
	private String username;

	public UserEvent(){

	}

	public void setType(String type) {
		if(type.equals(Type.USER_LOGIN.toString()) || type.equals(Type.USER_LOGOUT.toString()) ||
				type.equals(Type.USER_DISCONNECTED.toString())) {
			this.type = type;
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
