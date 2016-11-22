package my.edu.taylors.dad.chat.entity;

import java.io.Serializable;

public class Auth implements Serializable {
	private static final long serialVersionUID = 1L;

	private String username;
	private String password;
	private int clientType;
	private int id;
	
	public Auth(String username, String password) {
		this(username, password, 0);
	}

	public Auth(String username, String password, int clientType) {
		this(username, password, clientType, 0);
	}

	public Auth(String username, String password, int clientType, int id) {
		this.username = username;
		this.password = password;
		this.clientType = clientType;
		this.id = id;
	}

	public int getClientType() {
		return clientType;
	}

	public void setClientType(int clientType) {
		this.clientType = clientType;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
					found = new Auth(user.username, user.password, user.clientType);
					break;
				}
			}
		}
		return found;
	}

	@Override
	public String toString() {
		return "Auth [username=" + username + ", password=" + password + ", clientType=" + clientType + ", id=" + id + "]";
	}

}
