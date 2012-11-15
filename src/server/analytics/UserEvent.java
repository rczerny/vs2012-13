package server.analytics;

public class UserEvent extends Event{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	
	public String toString() {
		String prefix = super.toString();
		String suffix ="";
		
		if(this.type.equals("USER_LOGIN")) {
			suffix = "user " + username + " logged in";
		}
		
		if(this.type.equals("USER_LOGOUT")) {
			suffix = "user " + username + " logged out";
		}
		
		if(this.type.equals("USER_DISCONNECTED")) {
			suffix = "user " + username + " disconnected";
		}
		
		return prefix + suffix;
	}
}
