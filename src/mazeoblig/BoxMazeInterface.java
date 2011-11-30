package mazeoblig;

import java.rmi.Remote;
import java.rmi.RemoteException;

import simulator.PositionInMaze;
import simulator.User;

/**
 * <p>
 * Title: BoxMazeInterface
 * </p>
 * 
 * <p>
 * Description: The interface towards all BoxMaze server instances.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006-2011 diverse people
 * </p>
 * 
 * <p>
 * Company: Han Runar
 * </p>
 * 
 * @author runar & co
 * @version 1.0
 */
public interface BoxMazeInterface extends Remote {
	/**
	 * Return the {@link BoxMaze#boxmaze maze} from this {@link BoxMaze}.
	 * 
	 * @return Box[][]
	 * @throws RemoteException
	 */
	public Box[][] getMaze() throws RemoteException;

	/**
	 * Join the given {@link User} to this {@link BoxMaze}.
	 * 
	 * @param user
	 *            The {@link User} to join in.
	 * @return The resulting {@link User}#id
	 * @throws RemoteException
	 */
	public Integer join(User user) throws RemoteException;

	/**
	 * Tell the {@link BoxMaze} that the {@link User} identified by the given id
	 * is now positioned at the given {@link PositionInMaze position}.
	 * 
	 * @param id
	 *            The id of the {@link User} sending the update.
	 * @param positionInMaze
	 *            The new {@link PositionInMaze} for the id'd {@link User}.
	 * @throws RemoteException
	 */
	public void update(Integer id, PositionInMaze positionInMaze)
			throws RemoteException;
}
