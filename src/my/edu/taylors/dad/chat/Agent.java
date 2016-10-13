package my.edu.taylors.dad.chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

import my.edu.taylors.dad.chat.entity.Auth;

public class Agent{
	private Socket agentSocket = null;
	
	public Agent(Socket agentSocket){
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
						PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
						ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
						br = new BufferedReader(new InputStreamReader(client.getInputStream()));
						System.out.println("Client connected with IP: " + client.getRemoteSocketAddress());
						Auth user = (Auth)ois.readObject();
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
