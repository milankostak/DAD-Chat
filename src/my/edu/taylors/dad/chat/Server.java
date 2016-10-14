package my.edu.taylors.dad.chat;

import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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

	static Socket client = null;
	static ArrayBlockingQueue<ClientInfo> connectionQueue;
	
	public static void main(String args[]) {
		connectionQueue = new ArrayBlockingQueue<ClientInfo>(5);
		ServerSocket server = null;
		ServerSocket server2 = null;
		int customerCount = 0;
		try {
			try {
				server = new ServerSocket(9999);
				server2 = new ServerSocket(9998);
				
				while (true) {
					client = server.accept();
					PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
					ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
					System.out.println("Client connected with IP: " + client.getRemoteSocketAddress());
					Auth user = (Auth) ois.readObject();
					Auth usr = null;
					// Check if the user matches any of our current users (Authentication)
					if ((usr = user.authenticate(users)) != null) {
						usr.setPassword("");
						// 1 - Agent, 0 - Guest
						if (usr.getType() == 0) {
							pw.println("0");
							usr.setId(customerCount++);
							ClientInfo clientInfo = new ClientInfo(usr, client);
							connectionQueue.put(clientInfo);
						} else {
							pw.println("1");
							new Agent(client, usr, server2);
						}
					} else {
						// Wrong combination
						pw.println("-1");
					}
				} 
			} finally {
				if (server != null) server.close();
				if (server2 != null) server2.close();
			}
		} catch (Exception e) {
			System.out.println("Server Error");
			e.printStackTrace();
		}
	}
}
