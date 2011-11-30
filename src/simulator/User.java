/**
 * 
 */
package simulator;

import java.awt.Color;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * @author runar
 *
 */
public interface User extends Remote {
	public void updateMap(HashMap<String, Color> map) throws RemoteException;

	public Integer getId() throws RemoteException;
}
