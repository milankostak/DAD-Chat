package my.edu.taylors.dad.chat.entity;

import java.io.Serializable;
import java.net.InetAddress;

public class AuthIdIp extends Auth implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int windowId;
	private InetAddress inetAddress;

	public AuthIdIp(Auth auth, int windowId, InetAddress inetAddress) {
		super(auth.getUsername(), auth.getPassword(), auth.getType(), auth.getId());
		this.windowId = windowId;
		this.inetAddress = inetAddress;
	}

	public int getWindowId() {
		return windowId;
	}

	public void setWindowId(int windowId) {
		this.windowId = windowId;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	@Override
	public String toString() {
		return "Agent [windowId=" + windowId + ", inetAddress=" + inetAddress + ", getType()=" + getType()
				+ ", getUsername()=" + getUsername() + ", getPassword()=" + getPassword() + ", getId()=" + getId()
				+ "]";
	}

}
