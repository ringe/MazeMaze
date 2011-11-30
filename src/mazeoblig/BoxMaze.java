package mazeoblig;

/************************************************************************
 * Denne koden har blitt rørt.
 ***********************************************************************/

/**
 * <p>Title: BoxMaze</p>
 *
 * <p>Description: En 20 x 20 labyrint som er bygget opp av 20x20 bokser som er
 * satt ved siden av hverandre og under hverandre. </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.awt.Color;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import simulator.PositionInMaze;
import simulator.User;

/**
 * The {@link BoxMaze} is the server object of this Java RMI Maze game
 * simulator.
 * 
 * @author asd
 * @author runar
 * 
 */
public class BoxMaze extends UnicastRemoteObject implements BoxMazeInterface {
	private static final long serialVersionUID = -2163647278903288857L;

	private int maze[][];
	/** The famous maze of this Java RMI game */
	protected Box boxmaze[][];
	private int size = 20;

	/** All registered {@link User}s. */
	private HashMap<Integer, User> users = new HashMap<Integer, User>();
	/** All know {@link PositionInMaze positions} from registered {@link User}s. */
	private HashMap<Integer, PositionInMaze> positions = new HashMap<Integer, PositionInMaze>();
	/** All registered {@link User}s set for removal */
	private ArrayList<Integer> removables = new ArrayList<Integer>();
	/** The next {@link User} id */
	private int nextId = 0;

	/**
	 * A Thread to periodically update {@link User}s about the current
	 * {@link #positions} map and drop {@link #removables} {@link User}s.
	 * 
	 * @author runar
	 */
	private class Sender extends Thread {
		/** Perform periodic tasks */
		@Override
		public void run() {
			try {
				while (true) {
					sleep(75);
					removeUsers();
					if (users.size() > 0)
						sendUpdate();
				}
			} catch (InterruptedException ie) {
				// In case the sleep blows up.
				System.out.println("Interrupted. What now?");
			}
		}
	}

	/**
	 * Start the {@link Sender} Thread
	 */
	private void startSender() {
		Sender s = new Sender();
		s.setDaemon(true);
		s.start();
	}

	/**
	 * The {@link BoxMaze} constructor randomizes a maze, by default at 20 x 20
	 * {@link Box}es, where the walls between the {@link Box} are "removed", to
	 * make a maze.
	 * 
	 * <P>
	 * Starts the {@link Sender} Thread.
	 * 
	 * @param newSize
	 *            Give a number or null (for default size)
	 * @throws RemoteException
	 */
	public BoxMaze(Integer newSize) throws RemoteException {
		if (newSize != null)
			size = newSize;
		init(size);
		startSender();
	}

	/**
	 * Genererer labyrinten. Koden er i all vesentlig grad hentet fra en enkel
	 * algoritme som er publisert på http://en.wikipedia.org/wiki/Image:MAZE.png
	 * 
	 * <P>
	 * Men det er jo bare et bilde (som hadde feil URL). Mente du
	 * http://en.wikipedia.org/wiki/Maze_generation_algorithm ?
	 * 
	 * <P>
	 * Algoritmen er skrevet om til å håndtere boksene
	 */
	private void init(int size) {
		int x, y, n, d;
		int dx[] = { 0, 0, -1, 1 };
		int dy[] = { -1, 1, 0, 0 };
		int todo[] = new int[size * size], todonum = 0;

		/* We want to create a maze on a grid. */
		maze = new int[size][size];

		/* We start with a grid full of walls. */
		for (x = 0; x < size; ++x)
			for (y = 0; y < size; ++y) {
				if (x == 0 || x == (size - 1) || y == 0 || y == (size - 1)) {
					maze[x][y] = 32;
				} else {
					maze[x][y] = 63;
				}
			}
		/* Select any square of the grid, to start with. */
		x = (int) (1 + Math.random() * (size - 2));
		y = (int) (1 + Math.random() * (size - 2));

		/* Mark this square as connected to the maze. */
		maze[x][y] &= ~48;

		/* Remember the surrounding squares, as we will */
		for (d = 0; d < 4; ++d)
			if ((maze[x + dx[d]][y + dy[d]] & 16) != 0) {
				/* want to connect them to the maze. */
				todo[todonum++] = ((x + dx[d]) << 16) | (y + dy[d]);
				maze[x + dx[d]][y + dy[d]] &= ~16;
			}

		/* We won't be finished until all is connected. */
		while (todonum > 0) {
			/* We select one of the squares next to the maze. */
			n = (int) (Math.random() * todonum);
			x = todo[n] >> 16;
			y = todo[n] & 65535;

			/* We will connect it, so remove it from the queue. */
			todo[n] = todo[--todonum];

			/* Select a direction, which leads to the maze. */
			do
				d = (int) (Math.random() * 4);
			while ((maze[x + dx[d]][y + dy[d]] & 32) != 0);

			/* Connect this square to the maze. */
			maze[x][y] &= ~((1 << d) | 32);
			maze[x + dx[d]][y + dy[d]] &= ~(1 << (d ^ 1));

			/* Remember the surrounding squares, which aren't */
			for (d = 0; d < 4; ++d)
				if ((maze[x + dx[d]][y + dy[d]] & 16) != 0) {

					/* connected to the maze, and aren't yet queued to be. */
					todo[todonum++] = ((x + dx[d]) << 16) | (y + dy[d]);
					maze[x + dx[d]][y + dy[d]] &= ~16;
				}
			/* Repeat until finished. */
		}

		/* One may want to add an entrance and exit. */
		maze[1][1] &= ~1;
		maze[size - 2][size - 2] &= ~2;

		// Oppdatterer boksene, og antar at alle er forbundet med hverandre
		boxmaze = new Box[size][size];
		for (x = 0; x < boxmaze.length; x++) {
			for (y = 0; y < boxmaze[x].length; y++) {
				boxmaze[x][y] = new Box(maze[x][y]);
			}
		}
		for (x = 0; x < boxmaze.length; x++) {
			for (y = 0; y < boxmaze[x].length; y++) {
				boxmaze[x][y].setLeft((x > 0 ? boxmaze[x - 1][y] : null));
				boxmaze[x][y]
						.setRight((x < boxmaze[x].length - 1 ? boxmaze[x + 1][y]
								: null));
				boxmaze[x][y].setUp((y > 0 ? boxmaze[x][y - 1] : null));
				boxmaze[x][y]
						.setDown((y < boxmaze[y].length - 1 ? boxmaze[x][y + 1]
								: null));
				boxmaze[x][y].setValue(maze[x][y]);
			}
		}
		// Fjerner forbindelsene slik at vi kan representere en vegg
		for (x = 1; x < (size - 1); ++x)
			for (y = 1; y < (size - 1); ++y) {
				if ((boxmaze[x][y].getValue() & 1) != 0) {
					boxmaze[x][y].setUp(null);
					boxmaze[x][y - 1].setDown(null);
				}
				if ((boxmaze[x][y].getValue() & 2) != 0) {
					boxmaze[x][y].setDown(null);
					boxmaze[x][y + 1].setUp(null);
				}
				if ((boxmaze[x][y].getValue() & 4) != 0) {
					boxmaze[x][y].setLeft(null);
					boxmaze[x - 1][y].setRight(null);
				}
				if ((boxmaze[x][y].getValue() & 8) != 0) {
					boxmaze[x][y].setRight(null);
					boxmaze[x + 1][y].setLeft(null);
				}
			}
	}

	/**
	 * Henter hele det aktuelle Maze
	 * 
	 * @return Box[][]
	 * @throws RemoteException
	 *             ved kommunikasjonsfeil
	 */
	@Override
	public Box[][] getMaze() throws RemoteException {
		return boxmaze;
	}

	@Override
	public Integer join(User user) throws RemoteException {
		synchronized (users) {
			int id = nextId++;
			users.put(id, user);
			System.out.println(id);
			return id;
		}
	}

	@Override
	public void update(Integer id, PositionInMaze position) {
		synchronized (positions) {
			positions.put(id, position);
		}
	}

	/**
	 * Perform removal of all {@link User}s deemed {@link #removables}.
	 */
	public void removeUsers() {
		synchronized (users) {
			synchronized (positions) {
				for (int i = 0; i < removables.size(); i++) {
					users.remove(removables.get(i));
					positions.remove(removables.get(i));
				}
				removables = new ArrayList<Integer>();
			}
		}
	}

	/**
	 * Prepare the updated list of all {@link User}'s {@link #positions} into a
	 * HashMap, revealing the current position and the Color of the {@link User}
	 * last added to the relevant spot.
	 * 
	 * <P>
	 * Thereafter, send the map to all {@link User}s so far joined to this maze.
	 */
	public synchronized void sendUpdate() {
		synchronized (users) {
			synchronized (positions) {
				HashMap<String, Color> map = new HashMap<String, Color>();

				// Prepare our position/color map.
				Set<Integer> keys = positions.keySet();
				Iterator<Integer> it = keys.iterator();
				while (it.hasNext()) {
					PositionInMaze pos = positions.get(it.next());
					map.put(pos.getXpos() + "," + pos.getYpos(), pos.getColor());
				}

				// Send map to all current Users.
				Set<Integer> ids = users.keySet();
				Iterator<Integer> usr = ids.iterator();
				while (usr.hasNext()) {
					int id = usr.next();
					User user = users.get(id);
					try {
						user.updateMap(map);
					} catch (RemoteException e) {
						removables.add(id);
					}
				}

			}
		}
	}

}