package my.edu.taylors.dad.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

import my.edu.taylors.dad.chat.entity.Auth;

public class Server {
	static Auth[] users = {new Auth("omar", "123", 0),
			new Auth("test", "123", 0),
			new Auth("admin", "root", 1),
			new Auth("agent", "root", 1)};
	
	static Socket client = null;
	static ArrayBlockingQueue<Socket> connectionQueue;
	
	public static void main(String args[]){
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
						pw.println("Welcome " + usr.getUsername() + ", you are our guest now !");
						connectionQueue.put(client);
					}else{
						pw.println("Welcome " + usr.getUsername() + ", you are an agent !");
						new Agent(client);
					}
				}else{
					pw.println("Wrong combination");
				}
			}
			
		}catch(Exception e){
			System.out.println("Server Error");
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
