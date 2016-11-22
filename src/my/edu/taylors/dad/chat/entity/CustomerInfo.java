package my.edu.taylors.dad.chat.entity;

import java.net.Socket;

public class CustomerInfo {
	private Customer customer;
	private Socket socket;
	
	public CustomerInfo(Customer customer, Socket socket) {
		this.customer = customer;
		this.socket = socket;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	@Override
	public String toString() {
		return "ClientInfo [customer=" + customer + ", socket=" + socket + "]";
	}

}
