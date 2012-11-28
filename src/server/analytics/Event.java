package server.analytics;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;

abstract public class Event implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String id;
	String type;
	long timestamp;
	
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	
	public long getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getEndString() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm z");
		Date out = new Date(timestamp);
		return simpleDateFormat.format(out);
	}
	
	public String toString(){
		return type + ": " + getEndString() + " - ";
	}
}
