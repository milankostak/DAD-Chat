package my.edu.taylors.dad.chat.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;

import my.edu.taylors.dad.chat.entity.Agent;
import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.AuthResult;
import my.edu.taylors.dad.chat.entity.CustomerInfo;
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Customer;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Ports;

public class MessagingServer {
	private static Auth[] users = {
			new Customer("test", "123"),
			new Customer("test2", "123"),
			new Customer("test3", "123"),
			new Customer("test4", "123"),
			// 224.0.0.0 - 239.255.255.255
			new Agent("admin", "root", "231.1.1.1"),
			new Agent("admin2", "root", "231.1.1.2"),
			new Agent("admin3", "root", "231.1.1.3"),
			new Agent("admin4", "root", "231.1.1.4")
	};

	static ArrayBlockingQueue<CustomerInfo> connectionQueue;

	// for ID purpose
	private int customerCount = 0;

	public MessagingServer() {
		customerCount = 0;
		connectionQueue = new ArrayBlockingQueue<CustomerInfo>(5);
		ServerSocket server = null;
		try {
			try {
				server = new ServerSocket(Ports.MSG_SERVER);

				while (true) {
					Socket client = server.accept();
					System.out.println("Client connected with IP: " + client.getRemoteSocketAddress());
					new AuthenticationHandler(client);
				}
			} finally {
				if (server != null) server.close();
			}
		} catch (BindException e) {
			System.err.println("There is already a server running on this port. Server is shuting down.");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Server Error. Server is shuting down.");
			e.printStackTrace();
		}
	}

	private class AuthenticationHandler extends Thread {

		private Socket client;
		private int attemptsNumber;
		private Calendar lastAttempt;

		private static final int MAX_ATTEMPTS = 3;
		private static final int BLOCKING_INTERVAL_MINS = 10;

		public AuthenticationHandler(Socket client) {
			this.client = client;
			attemptsNumber = 0;
			lastAttempt = Calendar.getInstance();
			start();
		}

		@Override
		public void run() {
			try {
				boolean failed = true;
				while (failed) {
					//PrintWriter pw = new PrintWriter(client.getOutputStream(), true);

					ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
					Auth receivedUser = (Auth) ois.readObject();
					Auth authenticatedUser = null;
					ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());

					// check attempts
					attemptsNumber++;
					Calendar tempCalendar = Calendar.getInstance();
					tempCalendar.add(Calendar.MINUTE, -BLOCKING_INTERVAL_MINS);
					if (attemptsNumber > MAX_ATTEMPTS && lastAttempt.before(tempCalendar)) {
						attemptsNumber = 1;
					}
					lastAttempt = Calendar.getInstance();

					// too many attempts
					if (attemptsNumber > MAX_ATTEMPTS) {

						AuthResult result = new AuthResult(null, Flags.AUTHENTICATICATION_ATTEMTPS);
						oos.writeObject(result);

					// check if the user matches any of our current users (Authentication)
					} else if (attemptsNumber <= MAX_ATTEMPTS && (authenticatedUser = receivedUser.authenticate(users)) != null) {
						authenticatedUser.setPassword("");
						failed = false;// stop AuthenticationHandler

						if (authenticatedUser.getClientType() == ClientType.CUSTOMER) {
							Customer customer = (Customer) authenticatedUser; 
							customer.setInetAddress(client.getInetAddress());
							customer.setId(customerCount++);

							AuthResult result = new AuthResult(customer, Flags.CUSTOMER_AUTHENTICATED);
							oos.writeObject(result);

							CustomerInfo customerInfo = new CustomerInfo(customer, client);
							connectionQueue.put(customerInfo);
						} else {
							Agent agent = (Agent) authenticatedUser;
							agent.setInetAddress(client.getInetAddress());

							AuthResult result = new AuthResult(agent, Flags.AGENT_AUTHENTICATED);
							oos.writeObject(result);

							new ServerAgent(client, agent);
						}

					// Wrong combination
					} else {
						AuthResult result = new AuthResult(null, Flags.AUTHENTICATICATION_ERROR);
						oos.writeObject(result);
					}
				}
			} catch (Exception e) {
				System.err.println("Server Error");
				e.printStackTrace();
			}
		}
	}
}
