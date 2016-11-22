package my.edu.taylors.dad.chat.server;

import my.edu.taylors.dad.chat.gsa.GsaServer;
import my.edu.taylors.dad.chat.saving.RmiServer;

/**
 * Separates all server starters into this class
 */
public class ServerStarter {

	public static void main(String[] args) {
		// run server for listening for clients that want server IP address
		new GsaServer();
		// run RMI server for saving conversations
		new RmiServer();
		// run regular messaging server
		new MessagingServer();
	}

}
