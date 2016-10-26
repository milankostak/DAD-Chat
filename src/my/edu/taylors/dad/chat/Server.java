package my.edu.taylors.dad.chat;

import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.ClientInfo;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.gsa.GsaServer;

public class Server {
	static Auth[] users = {
			new Auth("omar", "123", 0),
			new Auth("test", "123", 0),
			new Auth("test2", "123", 0),
			new Auth("test3", "123", 0),
			new Auth("admin", "root", 1),
			new Auth("admin2", "root", 1),
			new Auth("admin3", "root", 1),
			new Auth("agent", "root", 1)
	};

	static ArrayBlockingQueue<ClientInfo> connectionQueue;

	// for ID purpose
	private int customerCount = 0;

	public static void main(String[] args) {
		// run server for listening for clients that want server ip address
		new GsaServer();
		// run regular messaging server
		new Server();
	}

	public Server() {
		customerCount = 0;
		connectionQueue = new ArrayBlockingQueue<ClientInfo>(5);
		ServerSocket server = null;
		try {
			try {
				server = new ServerSocket(9999);

				while (true) {
					Socket client = server.accept();
					System.out.println("Client connected with IP: " + client.getRemoteSocketAddress());
					new AuthenticationHandler(client);
				}
			} finally {
				if (server != null) server.close();
			}
		} catch (Exception e) {
			System.err.println("Server Error");
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
					PrintWriter pw = new PrintWriter(client.getOutputStream(), true);

					ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
					Auth receivedUser = (Auth) ois.readObject();
					Auth authenticatedUser = null;

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

						pw.println(Flags.AUTHENTICATICATION_ATTEMTPS);
					
					// check if the user matches any of our current users (Authentication)
					} else if (attemptsNumber <= MAX_ATTEMPTS && (authenticatedUser = receivedUser.authenticate(users)) != null) {
						authenticatedUser.setPassword("");
						failed = false;// stop AuthenticationHandler

						if (authenticatedUser.getType() == 0) {
							pw.println(Flags.CUSTOMER_AUTHENTICATED);
							authenticatedUser.setId(customerCount++);
							ClientInfo clientInfo = new ClientInfo(authenticatedUser, client);
							connectionQueue.put(clientInfo);
						} else {
							pw.println(Flags.AGENT_AUTHENTICATED);
							new ServerAgent(client, authenticatedUser);
						}

					// Wrong combination
					} else {
						pw.println(Flags.AUTHENTICATICATION_ERROR);
					}
				}
			} catch (Exception e) {
				System.err.println("Server Error");
				e.printStackTrace();
			}
		}
	}
}
