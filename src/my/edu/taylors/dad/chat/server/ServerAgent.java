package my.edu.taylors.dad.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import my.edu.taylors.dad.chat.entity.Agent;
import my.edu.taylors.dad.chat.entity.Customer;
import my.edu.taylors.dad.chat.entity.CustomerInfo;
import my.edu.taylors.dad.chat.entity.Flags;

public class ServerAgent extends Thread {

	private Map<Integer, Socket> customersMap = new HashMap<>(2);
	private Agent agent;
	public volatile boolean isAgentConnected;
	private static int windowCount = 0;

	private ArrayBlockingQueue<Customer> queue = new ArrayBlockingQueue<>(1);

	public ServerAgent(Socket agentSocket, Agent authenticatedUser) {
		this.agent = authenticatedUser;
		this.isAgentConnected = true;

		for (int i = 0; i < 2; i++) {
			new CustomerToAgentHandler(agentSocket, i);
		}
		new AgentToCustomerHandler(agentSocket);
	}

	private class AgentToCustomerHandler extends Thread {
		private Socket agentSocket;

		public AgentToCustomerHandler(Socket agentSocket) {
			this.agentSocket = agentSocket;
			start();
		}

		@Override
		public void run() {
			// agent to customer
			try {
				BufferedReader brFromAgent = new BufferedReader(new InputStreamReader(agentSocket.getInputStream()));
				boolean keepRunning = true;
				while (keepRunning) {
					String clientId = brFromAgent.readLine();
					if (clientId == null) continue;

					if (clientId.equals(Flags.SENDING_CUSTOMER_TO_AGENT)) {
						// new customer connected to agent
						sendCustomerToAgent();

					// agent logged out, send to customer
					} else if (clientId.equals(Flags.AGENT_LOGGING_OUT)) {
						String clientIdString = brFromAgent.readLine();
						int clientIdInt = Integer.parseInt(clientIdString);
						Socket client = customersMap.get(clientIdInt);
						PrintWriter pwToCustomer = new PrintWriter(client.getOutputStream(), true);
						pwToCustomer.println(Flags.AGENT_LOGGING_OUT);

						// remove because here is reference, but close later, when received confirmation from customer
						customersMap.remove(clientIdInt);

					// customer logged out, agent sending finish to customer
					} else if (clientId.equals(Flags.CUSTOMER_LOGGING_OUT)) {
						String clientIdString = brFromAgent.readLine();
						int clientIdInt = Integer.parseInt(clientIdString);
						Socket client = customersMap.get(clientIdInt);
						PrintWriter pwToCustomer = new PrintWriter(client.getOutputStream(), true);
						pwToCustomer.println(Flags.CUSTOMER_LOGGING_OUT);

						client.close();
						customersMap.remove(clientIdString);

					// when TODO
					} else if (clientId.equals(Flags.VOICE_CAPTURE_FINISHED) || clientId.equals(Flags.VOICE_CAPTURE_CLEAR)) {
						String clientIdString = brFromAgent.readLine();
						int clientIdInt = Integer.parseInt(clientIdString);
						Socket client = customersMap.get(clientIdInt);
						PrintWriter pwToCustomer = new PrintWriter(client.getOutputStream(), true);
						pwToCustomer.println(clientId);

					} else {
						String receivedMsg = brFromAgent.readLine();

						Socket client = customersMap.get(Integer.parseInt(clientId));
						if (client != null && !client.isClosed()) {
							PrintWriter pwToCustomer = new PrintWriter(client.getOutputStream(), true);
							pwToCustomer.println(Flags.PLAIN_MESSAGE);
							pwToCustomer.println(receivedMsg);
						}
					}
				}
			} catch (SocketException e) {
				isAgentConnected = false;
				System.err.println("Agent disconnected");
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void sendCustomerToAgent() throws IOException, InterruptedException {
			// send customer to agent, it will trigger opening window
			Customer customer = queue.take();
			System.out.println("Sending customer to agent: " + customer.toString());
			ObjectOutputStream outputToAgent = new ObjectOutputStream(agentSocket.getOutputStream());
			outputToAgent.writeObject(customer);
		}
	}
	
	private class CustomerToAgentHandler extends Thread {
		private CustomerInfo customerInfo;
		private Socket agentSocket;
		private int tempWindowId;
		private Socket client = null;

		public CustomerToAgentHandler(Socket agentSocket, int i) {
			this.agentSocket = agentSocket;
			start();
		}

		@Override
		public void run() {
			try {
				try {
					while (isAgentConnected) {
						customerInfo = MessagingServer.connectionQueue.take();
						
						// when agent disconnects unexpectedly, this two thread keep running
						// stop them and re-put clients into queue
						if (!isAgentConnected) {
							MessagingServer.connectionQueue.put(customerInfo);
							break;
						}
						tempWindowId = windowCount++;
						client = customerInfo.getSocket();
						customersMap.put(customerInfo.getCustomer().getId(), client);

						// send agent to customer
						ObjectOutputStream outputToCustomer = new ObjectOutputStream(client.getOutputStream());
						Agent agentWId = new Agent(agent, tempWindowId, agentSocket.getInetAddress());
						outputToCustomer.writeObject(agentWId);

						BufferedReader brFromCustomer = new BufferedReader(new InputStreamReader(client.getInputStream()));
						PrintWriter pwToAgent = new PrintWriter(new OutputStreamWriter(agentSocket.getOutputStream()), true);
						pwToAgent.println(Flags.SENDING_CUSTOMER_TO_AGENT);
						queue.put(new Customer(customerInfo.getCustomer(), tempWindowId, client.getInetAddress()));

						// customer to agent
						boolean keepReceiving = true;
						while (keepReceiving) {
							String receivedId = brFromCustomer.readLine();
							if (receivedId != null) {

								// customer logged out, send to agent
								if (receivedId.equals(Flags.CUSTOMER_LOGGING_OUT)) {
									keepReceiving = false;
									pwToAgent.println(Flags.CUSTOMER_LOGGING_OUT);
									String id2 = brFromCustomer.readLine();
									pwToAgent.println(id2);

								// agent logged out, customer sending back for breaking the loop
								} else if (receivedId.equals(Flags.AGENT_LOGGING_OUT)) {
									keepReceiving = false;
									// no need to send back to agent, because he still listens to other customers
									client.close();

								// when TODO
								} else if (receivedId.equals(Flags.VOICE_CAPTURE_FINISHED) || receivedId.equals(Flags.VOICE_CAPTURE_CLEAR)) {
									String receivedId2 = brFromCustomer.readLine();
									pwToAgent.println(receivedId);
									pwToAgent.println(receivedId2);
									
								} else {
									String receivedMsg = brFromCustomer.readLine();
									pwToAgent.println(receivedId);
									pwToAgent.println(receivedMsg);
								}

							}
						}
					}// while (true)
				} finally {
					if (client != null) client.close();
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}
}
