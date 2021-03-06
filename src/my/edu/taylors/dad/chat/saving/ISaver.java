package my.edu.taylors.dad.chat.saving;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import my.edu.taylors.dad.chat.entity.Message;

/**
 * Interface for RMI saving
 */
public interface ISaver extends Remote {

	/**
	 * Save {@link List} of {@link Message}s into a log file
	 */
	public void saveConversation(List<Message> messages, String agentName, String customerName) throws RemoteException;

}
