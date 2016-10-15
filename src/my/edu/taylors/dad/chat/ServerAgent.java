package my.edu.taylors.dad.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.AuthWithWindowId;
import my.edu.taylors.dad.chat.entity.ClientInfo;
import my.edu.taylors.dad.chat.entity.Flags;

public class ServerAgent extends Thread {

	private Map<Integer, Socket> customersMap = new HashMap<>(2);
	private Auth agent;
	private static int windowCount = 0;

	private ArrayBlockingQueue<AuthWithWindowId> queue = new ArrayBlockingQueue<AuthWithWindowId>(1);
	
	public ServerAgent(Socket agentSocket, Auth agent) {
		this.agent = agent;

		for (int i = 0; i < 2; i++) {
			new ConnectionHandler(agentSocket, i);
		}
		new CustomerToAgentHandler(agentSocket);
	}
	
	private class CustomerToAgentHandler extends Thread {
		private Socket agentSocket;

		public CustomerToAgentHandler(Socket agentSocket) {
			this.agentSocket = agentSocket;
			start();
		}

		@Override
		public void run() {
			// agent to customer
			try {
				BufferedReader brFromAgent = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));
				while (true) {
					String clientId = brFromAgent.readLine();
					if (clientId.equals(Flags.SENDING_CUSTOMER_TO_AGENT)) {
						// new customer connected to agent
						sendCustomerToAgent();
					} else {
						String receivedMsg = brFromAgent.readLine();

						Socket client = customersMap.get(Integer.parseInt(clientId));
						System.out.println("Map size1: " + customersMap.size());
						if (client != null && !client.isClosed()) {
							PrintWriter pwToCustomer = new PrintWriter(client.getOutputStream(), true);
							pwToCustomer.println(receivedMsg);
						}
					}
				}
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void sendCustomerToAgent() throws IOException, InterruptedException {
			// send customer to agent, it will trigger opening window
			AuthWithWindowId customer = queue.take();
			System.out.println("Sending customer to agent: " + customer.toString());
			ObjectOutputStream outputToAgent = new ObjectOutputStream(agentSocket.getOutputStream());
			outputToAgent.writeObject(customer);
		}
	}
	
	private class ConnectionHandler extends Thread {
		private ClientInfo clientInfo;
		private Socket agentSocket;
		private int tempWindowId;
		private Socket client = null;

		public ConnectionHandler(Socket agentSocket, int i) {
			this.agentSocket = agentSocket;
			start();
		}

		@Override
		public void run() {
			try {
				try {
					while (true) {
						tempWindowId = windowCount++;
						clientInfo = Server.connectionQueue.take();
						client = clientInfo.getSocket();
						customersMap.put(clientInfo.getAuth().getId(), client);
						System.out.println("Map size2: " + customersMap.size());

						// send agent to customer
						ObjectOutputStream outputToCustomer = new ObjectOutputStream(client.getOutputStream());
						AuthWithWindowId agentWId = new AuthWithWindowId(agent, tempWindowId);
						outputToCustomer.writeObject(agentWId);

						BufferedReader brFromCustomer = new BufferedReader(new InputStreamReader(client.getInputStream()));
						PrintWriter pwToAgent = new PrintWriter(new OutputStreamWriter(agentSocket.getOutputStream()), true);
						pwToAgent.println(Flags.SENDING_CUSTOMER_TO_AGENT);
						queue.put(new AuthWithWindowId(clientInfo.getAuth(), tempWindowId));

						// customer to agent
						boolean keepReceiving = true;
						while (keepReceiving) {
							String receivedId = brFromCustomer.readLine();
							if (receivedId.equals(Flags.LOGOUT)) {
								keepReceiving = false;
								pwToAgent.println(Flags.LOGOUT);
								String id2 = brFromCustomer.readLine();
								pwToAgent.println(id2);
								client.close();
								customersMap.remove(clientInfo.getAuth().getId());
								System.out.println("Map size3: " + customersMap.size());
							} else {
								String receivedMsg = brFromCustomer.readLine();
								pwToAgent.println(receivedId);
								pwToAgent.println(receivedMsg);
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
	}
}
