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
import my.edu.taylors.dad.chat.entity.AuthWithWindowId;
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
					while (true) {
						int tempWindowId = windowCount++;
						clientInfo = Server.connectionQueue.take();
						client = clientInfo.getSocket();
						customersMap.put(clientInfo.getAuth().getId(), client);
	
						// send customer to agent
						ObjectOutputStream output = new ObjectOutputStream(waitingAgent.getOutputStream());
						AuthWithWindowId customerWId = new AuthWithWindowId(clientInfo.getAuth(), tempWindowId);
						output.writeObject(customerWId);
	
						// send agent to customer
						ObjectOutputStream output2 = new ObjectOutputStream(client.getOutputStream());
						AuthWithWindowId agentWId = new AuthWithWindowId(agent, tempWindowId);
						output2.writeObject(agentWId);
	
						setReceivingThread(client);
	
						// client to agent
						BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
						PrintWriter pw = new PrintWriter(agentSocket.getOutputStream(), true);

						// customer to agent
						boolean keepReceiving = true;
						while (keepReceiving) {
							String receivedId = br.readLine();
							String receivedMsg = br.readLine();
							if (receivedId.equals("-1")) {
								keepReceiving = false;
								client.close();
							} else {
								pw.println(receivedId);
								pw.println(receivedMsg);
							}
						}
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
					// agent to customer
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));
						while (true && !client.isClosed()) {
							String clientId = br.readLine();
							String receivedMsg = br.readLine();

							Socket client = customersMap.get(Integer.parseInt(clientId));
							if (client != null && !client.isClosed()) {
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
