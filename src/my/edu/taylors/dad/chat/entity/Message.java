package my.edu.taylors.dad.chat.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	// time of message, just for showing in GUI
	private Date time;
	// content of the message
	private String message;
	// who sent it, used for showing on either left or right side of chat window
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
	
	/**
	 * Format for showing date, want just hour and minute
	 * @return formatted time
	 */
	private String prepareTime() {
		return new SimpleDateFormat("HH:mm").format(time);
	}

}
