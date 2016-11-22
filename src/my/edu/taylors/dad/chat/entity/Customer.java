package my.edu.taylors.dad.chat.entity;

import java.net.InetAddress;

public class Customer extends Auth {
	private static final long serialVersionUID = 1L;

	private Agent agent;

	public Customer(String username, String password) {
		super(username, password, ClientType.CUSTOMER);
	}

	public Customer(Customer customer, int windowId, InetAddress inetAddress) {
		super(customer.getUsername(), customer.getPassword(), ClientType.AGENT, customer.getId(), windowId, inetAddress, null);
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	@Override
	public String toString() {
		return "Customer [getInetAddress()=" + getInetAddress() + ", getUsername()=" + getUsername()
				+ ", getPassword()=" + getPassword() + ", getClientType()=" + getClientType() + ", getId()=" + getId()
				+ ", getWindowId()=" + getWindowId() + "]";
	}

}
