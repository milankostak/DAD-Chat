package my.edu.taylors.dad.chat.entity;

import java.net.InetAddress;

public class Agent extends Auth {
	private static final long serialVersionUID = 1L;

	public Agent(String username, String password) {
		super(username, password, ClientType.AGENT);
	}

	public Agent(Agent agent, int windowId, InetAddress inetAddress) {
		super(agent.getUsername(), agent.getPassword(), ClientType.AGENT, agent.getId(), windowId, inetAddress);
	}

	@Override
	public String toString() {
		return "Agent [getInetAddress()=" + getInetAddress() + ", getUsername()=" + getUsername() + ", getPassword()="
				+ getPassword() + ", getClientType()=" + getClientType() + ", getId()=" + getId() + ", getWindowId()="
				+ getWindowId() + "]";
	}

}
