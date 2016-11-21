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
	
	private MessageType messageType;
	
	private transient byte[] voiceData;

	public Message(String message, ClientType clientType) {
		this(new Date(), message, clientType);
	}

	public Message(Date time, String message, ClientType clientType) {
		this.time = time;
		this.message = message;
		this.clientType = clientType;
		messageType = MessageType.TEXT;
	}

	public Message(byte[] voiceData, ClientType clientType) {
		this(new Date(), voiceData, clientType);
	}

	public Message(Date time, byte[] voiceData, ClientType clientType) {
		this.time = time;
		this.voiceData = voiceData;
		this.clientType = clientType;
		messageType = MessageType.VOICE;
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

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public byte[] getVoiceData() {
		return voiceData;
	}

	public void setVoiceData(byte[] voiceData) {
		this.voiceData = voiceData;
	}

	@Override
	public String toString() {
		return "Message [time=" + time + ", message=" + message + ", clientType=" + clientType + ", messageType="
				+ messageType + ", voiceData=" + voiceData + "]";
	}

	public String toStringForLog() {
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
