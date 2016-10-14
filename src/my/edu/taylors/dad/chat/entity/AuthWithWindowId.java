package my.edu.taylors.dad.chat.entity;

import java.io.Serializable;

public class AuthWithWindowId extends Auth implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int windowId;

	public AuthWithWindowId(Auth auth, int windowId) {
		super(auth.getUsername(), auth.getPassword(), auth.getType(), auth.getId());
		this.windowId = windowId;
	}

	public int getWindowId() {
		return windowId;
	}

	public void setWindowId(int windowId) {
		this.windowId = windowId;
	}


	@Override
	public String toString() {
		return "AuthWithWindowId [username=" + getUsername()
							+ ", password=" + getPassword()
							+ ", type=" + getType()
							+ ", id=" + getId()
							+ ", windowId=" + windowId + "]";
	}
}
