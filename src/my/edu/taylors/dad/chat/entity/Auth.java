package my.edu.taylors.dad.chat.entity;

import java.io.Serializable;
import java.net.InetAddress;

public class Auth implements Serializable {
	private static final long serialVersionUID = 1L;

	private String username;
	private String password;
	private ClientType clientType;
	private int id;
	private int windowId;
	private InetAddress inetAddress;

	public Auth(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public Auth(String username, String password, ClientType clientType) {
		this.username = username;
		this.password = password;
		this.clientType = clientType;
	}

	public Auth(String username, String password, ClientType clientType, int id, int windowId,
			InetAddress inetAddress) {
		super();
		this.username = username;
		this.password = password;
		this.clientType = clientType;
		this.id = id;
		this.windowId = windowId;
		this.inetAddress = inetAddress;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ClientType getClientType() {
		return clientType;
	}

	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getWindowId() {
		return windowId;
	}

	public void setWindowId(int windowId) {
		this.windowId = windowId;
	}

	public boolean equals(Auth user) {
		return (user.username.toLowerCase().equals(username.toLowerCase()) && user.password.equals(password));
	}

	public boolean equals(Auth[] users) {
		boolean found = false;
		for (Auth user : users) {
			if (!found) {
				if (user.username.toLowerCase().equals(username.toLowerCase()) && user.password.equals(password)) {
					found = true;
					break;
				}
			}
		}
		return found;
	}

	public Auth authenticate(Auth[] users) {
		Auth found = null;
		for (Auth user : users) {
			if (found == null) {
				if (user.username.toLowerCase().equals(username.toLowerCase()) && user.password.equals(password)) {
					// because of reference to password
					if (user.clientType == ClientType.CUSTOMER) {
						found = new Customer(user.username, user.password);
					} else {
						found = new Agent(user.username, user.password);
					}
					break;
				}
			}
		}
		return found;
	}

	@Override
	public String toString() {
		return "Auth [username=" + username + ", password=" + password + ", clientType=" + clientType + ", id=" + id
				+ ", windowId=" + windowId + ", inetAddress=" + inetAddress + "]";
	}

}
