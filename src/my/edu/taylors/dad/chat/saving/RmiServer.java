package my.edu.taylors.dad.chat.saving;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RmiServer {
	
	public RmiServer() {
		try {
			LocateRegistry.createRegistry(1099);
			ISaver saver = new Saver();
			Naming.rebind("saverObject", saver);
		} catch (RemoteException | MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
