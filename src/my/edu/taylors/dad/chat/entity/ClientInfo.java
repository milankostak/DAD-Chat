package my.edu.taylors.dad.chat.entity;

import java.net.Socket;

public class ClientInfo {
	private Auth auth;
	private Socket socket;
	
	public ClientInfo(Auth auth, Socket socket) {
		this.auth = auth;
		this.socket = socket;
	}
	
	public Auth getAuth() {
		return auth;
	}
	public void setAuth(Auth auth) {
		this.auth = auth;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
}
