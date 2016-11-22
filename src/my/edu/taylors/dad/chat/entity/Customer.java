package my.edu.taylors.dad.chat.entity;

import java.net.InetAddress;

public class Customer extends Auth {
	private static final long serialVersionUID = 1L;

	public Customer(String username, String password) {
		super(username, password, ClientType.CUSTOMER);
	}

	public Customer(Customer customer, int windowId, InetAddress inetAddress) {
		super(customer.getUsername(), customer.getPassword(), ClientType.AGENT, customer.getId(), windowId, inetAddress);
	}

	@Override
	public String toString() {
		return "Customer [getInetAddress()=" + getInetAddress() + ", getUsername()=" + getUsername()
				+ ", getPassword()=" + getPassword() + ", getClientType()=" + getClientType() + ", getId()=" + getId()
				+ ", getWindowId()=" + getWindowId() + "]";
	}

}
