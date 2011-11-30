package mazeoblig;

import java.rmi.*;

import simulator.PositionInMaze;
import simulator.User;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public interface BoxMazeInterface extends Remote {
    public Box [][] getMaze() throws RemoteException;

	public Integer join(User virtualUser) throws RemoteException;

	public void update(Integer id, PositionInMaze positionInMaze) throws RemoteException;
}
