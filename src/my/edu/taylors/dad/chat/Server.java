package my.edu.taylors.dad.chat;

import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.ClientInfo;

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
	private int customerCount = 0;

	public static void main(String[] args) {
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
					Auth user = (Auth) ois.readObject();
					Auth usr = null;

					// check attempts
					attemptsNumber++;
					Calendar tempCalendar = Calendar.getInstance();
					tempCalendar.add(Calendar.MINUTE, -BLOCKING_INTERVAL_MINS);
					if (attemptsNumber > MAX_ATTEMPTS && lastAttempt.before(tempCalendar)) {
						attemptsNumber = 1;
					}
					lastAttempt = Calendar.getInstance();
					// Check if the user matches any of our current users (Authentication)
					if (attemptsNumber <= MAX_ATTEMPTS && (usr = user.authenticate(users)) != null) {
						usr.setPassword("");
						failed = false;
						// 1 - Agent, 0 - Guest
						if (usr.getType() == 0) {
							pw.println("0");
							usr.setId(customerCount++);
							ClientInfo clientInfo = new ClientInfo(usr, client);
							connectionQueue.put(clientInfo);
						} else {
							pw.println("1");
							new ServerAgent(client, usr);
						}
					} else if (attemptsNumber > MAX_ATTEMPTS) {
						// too many attempts
						pw.println("-2");
					} else {
						// Wrong combination
						pw.println("-1");
					}
				}
			} catch (Exception e) {
				System.err.println("Server Error");
				e.printStackTrace();
			}
		}
	}
}
