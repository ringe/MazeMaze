/**
 * 
 */
package simulator;

/**
 * <p>
 * Title: User
 * </p>
 * 
 * <p>
 * Description: The interface towards all User instances.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2011 runar
 * </p>
 * 
 * <p>
 * Company: Han Runar
 * </p>
 * 
 * @author runar
 * @version 1.0
 */
import java.awt.Color;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * 
 * The {@link User} interface is here to let the {@link mazeoblig.BoxMaze
 * BoxMaze} send RMI callbacks to the {@link VirtualUser} instances running in
 * Worker Threads in the {@link mazeoblig.Maze Maze} applet.
 * 
 * @author runar
 * 
 */
public interface User extends Remote {
	/**
	 * Receive and updated map of all other {@link User}'s positions.
	 * 
	 * @param map
	 * @throws RemoteException
	 */
	public void updateMap(HashMap<String, Color> map) throws RemoteException;

	/**
	 * Return this {@link User}'s id.
	 * 
	 * @return the User id.
	 * @throws RemoteException
	 */
	public Integer getId() throws RemoteException;
}
