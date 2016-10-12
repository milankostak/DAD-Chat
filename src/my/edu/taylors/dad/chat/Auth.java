package my.edu.taylors.dad.chat;

import java.io.Serializable;

public class Auth implements Serializable{
	private String username;
	private String password;
	private int type;
	
	public Auth() {
		
	}
	
	public Auth(String username, String password, int type){
		this.username = username;
		this.password = password;
		this.type = type;
	}
	
	public Auth(String username, String password){
		this.username = username;
		this.password = password;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
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
	
	public boolean equals(Auth user){
		return (user.username.equals(username) && user.password.equals(password));
	}
	
	public boolean equals(Auth[] users){
		boolean found = false;
		for(Auth user : users){
			if(!found){
				if(user.username.equals(username) && user.password.equals(password))
					found = true;
			}
		}
		return found;
	}
}
