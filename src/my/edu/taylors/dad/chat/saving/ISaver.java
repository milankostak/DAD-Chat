package my.edu.taylors.dad.chat.saving;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import my.edu.taylors.dad.chat.entity.Message;

public interface ISaver extends Remote {

	public void saveConversation(List<Message> messages, String agentName, String customerName) throws RemoteException;

}
