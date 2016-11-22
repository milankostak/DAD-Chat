package my.edu.taylors.dad.chat.saving;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

public class RmiServer {
	
	public RmiServer() {
		try {
			LocateRegistry.createRegistry(1099);
			ISaver saver = new Saver();
			Naming.rebind("saverObject", saver);
		} catch (ExportException e) {
			System.err.println("RMI service is already running on server on this machine.");
			e.printStackTrace();
		} catch (RemoteException | MalformedURLException e) {
			e.printStackTrace();
		}
	}

}
