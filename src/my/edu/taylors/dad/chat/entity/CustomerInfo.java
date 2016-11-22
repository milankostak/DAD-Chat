package my.edu.taylors.dad.chat.entity;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Class for storing {@link Customer} information together with his socket inside {@link ArrayBlockingQueue}
 */
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
		return "CustomerInfo [customer=" + customer + ", socket=" + socket + "]";
	}

}
