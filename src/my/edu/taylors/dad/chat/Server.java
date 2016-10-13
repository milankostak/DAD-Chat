package my.edu.taylors.dad.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.ClientType;

public class Server {
	static Auth[] users = {new Auth("omar", "123", 0),
			new Auth("test", "123", 0),
			new Auth("admin", "root", 1),
			new Auth("agent", "root", 1)};
	
	static Socket client = null;
	static ArrayBlockingQueue<Socket> connectionQueue;
	
	public static void main(String args[]){
		connectionQueue = new ArrayBlockingQueue<Socket>(5);
		ServerSocket server = null;
		try{
			server = new ServerSocket(9999);
			
			while(true){
				client = server.accept();
				PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
				ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
				System.out.println("Client connected with IP: " + client.getRemoteSocketAddress());
				Auth user = (Auth)ois.readObject();
				Auth usr = null;
				// Check if the user matches any of our current users (Authentication)
				if((usr = user.authenticate(users)) != null){
					// 1 - Agent, 0 - Guest
					if(usr.getType() == 0){
//						pw.println("Welcome " + usr.getUsername() + ", you are our guest now !");
						pw.println("0");
						connectionQueue.put(client);
					}else{
//						pw.println("Welcome " + usr.getUsername() + ", you are an agent !");
						pw.println("1");
						new Agent(client, usr);
					}
				}else{
//					pw.println("Wrong combination");
					pw.println("-1");
				}
			}
			
		}catch(Exception e){
			System.out.println("Server Error");
			e.printStackTrace();
		} finally{
			try {
				server.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
