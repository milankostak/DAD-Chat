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

public class ServerAgent extends Thread {

	private Socket waitingAgent;
	private Map<Integer, Socket> customersMap = new HashMap<>();
	private Auth agent;
	private static int windowCount = 0;
	
	public ServerAgent(Socket agentSocket, Auth agent, ServerSocket server2) {
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
			this.agentSocket = agentSocket;
			start();
		}

		@Override
		public void run() {
			try {
				Socket client = null;
				try {
					clientInfo = Server.connectionQueue.take();
					client = clientInfo.getSocket();
					customersMap.put(clientInfo.getAuth().getId(), client);

					// send customer to agent
					ObjectOutputStream output = new ObjectOutputStream(waitingAgent.getOutputStream());
					output.writeObject(clientInfo.getAuth());
					PrintWriter pw2 = new PrintWriter(waitingAgent.getOutputStream(), true);
					pw2.println(windowCount);
					// send agent to customer
					agent.setId(windowCount);// window count, for both windows different
					windowCount++;
					ObjectOutputStream output2 = new ObjectOutputStream(client.getOutputStream());
					output2.writeObject(agent);

					setReceivingThread(client);

					// client to agent
					BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
					PrintWriter pw = new PrintWriter(agentSocket.getOutputStream(), true);
					while (true) {
						String receivedId = br.readLine();
						String receivedMsg = br.readLine();

						pw.println(receivedId);
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
							if (client != null) {
								PrintWriter pw = new PrintWriter(client.getOutputStream(), true);
								pw.println(receivedMsg);
							}
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
