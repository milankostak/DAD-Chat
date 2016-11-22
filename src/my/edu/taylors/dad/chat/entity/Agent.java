package my.edu.taylors.dad.chat.entity;

import java.net.InetAddress;

public class Agent extends Auth {
	private static final long serialVersionUID = 1L;

	public Agent(String username, String password, String multicastAddress) {
		super(username, password, ClientType.AGENT, multicastAddress);
	}

	public Agent(Agent agent, int windowId, InetAddress inetAddress) {
		super(agent.getUsername(), agent.getPassword(), ClientType.AGENT, agent.getId(), windowId, inetAddress, agent.getMulticastAddress());
	}

	@Override
	public String toString() {
		return "Agent [getMulticastAddress()=" + getMulticastAddress() + ", getInetAddress()=" + getInetAddress()
				+ ", getUsername()=" + getUsername() + ", getPassword()=" + getPassword() + ", getClientType()="
				+ getClientType() + ", getId()=" + getId() + ", getWindowId()=" + getWindowId() + "]";
	}

}
