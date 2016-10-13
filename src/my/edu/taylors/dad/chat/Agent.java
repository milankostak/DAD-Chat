package my.edu.taylors.dad.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.ClientInfo;

public class Agent extends Thread {

	private Socket waitingAgent;
	private Map<Integer, Socket> customersMap = new HashMap<>();
	private Auth agent;
	
	public Agent(Socket agentSocket, Auth agent, ServerSocket server2) {
		this.agent = agent;

		try {
			// always is going to be there, because agent just logged in
			waitingAgent = server2.accept();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 2; i++) {
			new ConnectionHandler(agentSocket, i);
		}
	}
	
	private class ConnectionHandler extends Thread {
		private ClientInfo clientInfo;
		private Socket agentSocket;

		public ConnectionHandler(Socket agentSocket, int i) {
			System.out.println(agentSocket);
			this.agentSocket = agentSocket;
			start();
		}

		@Override
		public void run() {
			try {
				Socket client = null;
				try {
					clientInfo = Server.connectionQueue.take();

					ObjectOutputStream output = new ObjectOutputStream(waitingAgent.getOutputStream());
					output.writeObject(agent);
					
					client = clientInfo.getSocket();
					setReceivingThread(client);
					
					//customersMap.put(ID, client);

					// client to agent
					BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
					while (true) {
						String receivedMsg = br.readLine();
//						Socket agent = customerAgentMap.get(client);
						PrintWriter pw = new PrintWriter(agentSocket.getOutputStream(), true);
//						pw.println(ID);
						pw.println(receivedMsg);
					}
				} finally {
					if (client != null) client.close();
				}
			} catch (IOException | InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		}

		private void setReceivingThread(Socket client) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					// agent to client
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));
						while (true) {
							String clientId = br.readLine();
							String receivedMsg = br.readLine();

							Socket client = customersMap.get(Integer.parseInt(clientId));
							PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
							pw.println(receivedMsg);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			thread.start();
		}
	}
}
