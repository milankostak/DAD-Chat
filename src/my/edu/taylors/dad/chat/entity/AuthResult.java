package my.edu.taylors.dad.chat.entity;

import java.io.Serializable;

public class AuthResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private Auth auth;
	private String result;

	public AuthResult(Auth auth, String result) {
		this.auth = auth;
		this.result = result;
	}
	
	public Auth getAuth() {
		return auth;
	}

	public void setAuth(Auth auth) {
		this.auth = auth;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "AuthResult [auth=" + auth + ", result=" + result + "]";
	}

}
