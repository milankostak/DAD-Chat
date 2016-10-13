package my.edu.taylors.dad.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import my.edu.taylors.dad.chat.entity.Auth;

public class Agent{
	private static Socket agentSocket = null;
	private static Auth userInfo = null;
	
	public Agent(Socket agentSocket){
		try {
			PrintWriter pw = new PrintWriter(agentSocket.getOutputStream(), true);
			pw.println("Welcome agent !");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i = 0; i < 2; i++){
			new ConnectionHandler();
		}
		this.agentSocket = agentSocket;
	}
	
	private static class ConnectionHandler extends Thread {
		Socket client;
		BufferedReader br = null;
		public ConnectionHandler() {
			start();
		}
		
		public void run(){
			while(true){
				try{
					client = Server.connectionQueue.take();
					try{
						// Send a test message to the client when connecting
						PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
						pw.println("Hello there, I'm " + userInfo.getUsername());
//						ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
						br = new BufferedReader(new InputStreamReader(client.getInputStream()));
						System.out.println("Client connected with IP: " + client.getRemoteSocketAddress());
						//Handle the messages here
						pw.flush();
						client.close();
					}catch(Exception e){
						System.out.println("Connection error ...");
					}
				}catch(InterruptedException e){
					System.out.println("Error interrupted");
				}
			}
		}
	}
}
