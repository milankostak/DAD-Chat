package my.edu.taylors.dad.chat.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {

	private Date time;
	private String message;
	private ClientType clientType;

	public Message(String message, ClientType clientType) {
		this(new Date(), message, clientType);
	}

	public Message(Date time, String message, ClientType clientType) {
		this.time = time;
		this.message = message;
		this.clientType = clientType;
	}

	public Date getTime() {
		return time;
	}

	public String getFormattedTime() {
		return prepareTime();
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ClientType getClientType() {
		return clientType;
	}

	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}

	@Override
	public String toString() {
		return prepareTime() + "  " + message;
	}
	
	private String prepareTime() {
		return new SimpleDateFormat("HH:mm").format(time);
	}

}
