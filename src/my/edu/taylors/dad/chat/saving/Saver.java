package my.edu.taylors.dad.chat.saving;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.List;

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;

/**
 * Implementations of saving message into log file
 */
public class Saver extends UnicastRemoteObject implements ISaver {

	private static final long serialVersionUID = 1L;

	public Saver() throws RemoteException { }

	public void saveConversation(List<Message> messages, String agentName, String customerName) throws RemoteException {

		long timestamp = new Date().getTime();
		File file = new File("logs/" + timestamp + ".txt");
		file.getParentFile().mkdirs();// make a log folder if it doesn't exist

		try (PrintWriter writer = new PrintWriter(file)) {
			writer.println("Created on " + new Date());
			writer.println();
			for (int i = 0; i < messages.size(); i++) {
				Message msg = messages.get(i);
				String who = msg.getClientType() == ClientType.ME ? "Agent " + agentName : "Customer " + customerName;
				writer.println(who + ": "  + msg.toStringForLog());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
