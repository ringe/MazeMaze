package mazeoblig;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;

import simulator.VirtualUser;

/**
 * Tegner opp maze i en applet, basert på definisjon som man finner på
 * {@link RMIServer} RMIServer på sin side henter størrelsen fra definisjonen i
 * Maze.
 * 
 * <P>
 * Allows for simulation of {@link #n} number of users, by default 1500. The
 * first user is called {@link #self} and has a connection back to the
 * {@link Maze} applet for updates.
 * 
 * @author asd
 * @author runar
 * 
 */
public class Maze extends Applet {

	private static final long serialVersionUID = -4427531806078172174L;

	private BoxMazeInterface bm;
	private Box[][] maze;
	/** Used as the default size for the {@link BoxMaze} constructor. */
	public static int DIM = 30;
	private int dim = DIM;

	static int xp;
	static int yp;
	static boolean found = false;

	private String server_hostname;
	private int server_portnumber;

	/** The {@link VirtualUser} related to this {@link Maze} client. */
	private VirtualUser self;

	/** The {@link #n} number of {@link VirtualUser} {@link Worker}s to create. */
	private int n = 1500;

	/**
	 * Initiate the {@link Maze} applet by simulating {@link #n} number of
	 * {@link VirtualUser}s.
	 */
	@Override
	public void init() {
		LotsOfPlayers pl = new LotsOfPlayers(n, this);
		pl.setDaemon(true);
		pl.start();
	}

	/**
	 * Establish an RMI connection to the {@link RMIServer} {@link BoxMaze}
	 * object and return the {@link BoxMazeInterface} for further reference.
	 * 
	 * @return BoxMazeInterface
	 */
	public BoxMazeInterface connect() {
		/*
		 * * Kobler opp mot RMIServer, under forutsetning av at disse* kjører på
		 * samme maskin. Hvis ikke må oppkoblingen* skrives om slik at dette
		 * passer med virkeligheten.
		 */
		if (server_hostname == null)
			server_hostname = RMIServer.getHostName();
		if (server_portnumber == 0)
			server_portnumber = RMIServer.getRMIPort();
		try {
			java.rmi.registry.Registry r = java.rmi.registry.LocateRegistry
					.getRegistry(server_hostname, server_portnumber);

			/*
			 * * Henter inn referansen til Labyrinten (ROR)
			 */
			BoxMazeInterface boxmaze = (BoxMazeInterface) r
					.lookup(RMIServer.MazeName);
			if (maze == null)
				maze = boxmaze.getMaze();
			if (bm == null)
				bm = boxmaze;

			return boxmaze;
		} catch (RemoteException e) {
			System.err.println("Remote Exception: " + e.getMessage());
			System.exit(0);
		} catch (NotBoundException f) {
			/*
			 * * En exception her er en indikasjon på at man ved oppslag
			 * (lookup())* ikke finner det objektet som man søker.* Årsaken til
			 * at dette skjer kan være mange, men vær oppmerksom på* at hvis
			 * hostname ikke er OK (RMIServer gir da feilmelding under*
			 * oppstart) kan være en årsak.
			 */
			System.err.println("Not Bound Exception: " + f.getMessage());
			System.exit(0);
		}
		return null;
	}

	/**
	 * The {@link LotsOfPlayers} Thread will create {@link #n} number of
	 * {@link Worker} instances when started, and give each a reference to the
	 * given Maze applet instance.
	 * 
	 * @author runar
	 */
	class LotsOfPlayers extends Thread {
		/** The n number of {@link Worker} Threads to start */
		private int n;
		/** The client Maze applet for these {@link Worker}s. */
		private Maze mz;

		/**
		 * Prepare a {@link LotsOfPlayers} Thread to start c number of
		 * {@link Worker}s running behind the given m {@link Maze} applet
		 * instance.
		 * 
		 * @param c
		 *            number of players to create
		 * @param m
		 *            client Maze to connect to
		 */
		LotsOfPlayers(int c, Maze m) {
			n = c;
			mz = m;
		}

		/**
		 * Run the {@link LotsOfPlayers} Thread to create new {@link Worker}s.
		 */
		@Override
		public void run() {
			for (int i = n; i != 0; i--) {
				try {
					// Prepare Worker for background job, then start it.
					Worker w = new Worker(mz);
					w.setDaemon(true);
					w.start();
					sleep(75);
				} catch (InterruptedException e) {
					// In case the sleep blows up.
					System.out.println("Interrupted. What now?");
				}
			}
		}
	}

	/**
	 * Start a {@link Worker} Thread for each {@link VirtualUser}.
	 * 
	 * @author runar
	 */
	private class Worker extends Thread {

		private Maze mz;

		/**
		 * Prepare one {@link Worker} Thread to enable movements for the
		 * {@link VirtualUser} object created per {@link Worker} Thread.
		 * 
		 * @param m
		 *            a Maze Applet instance
		 */
		public Worker(Maze m) {
			mz = m;
		}

		/**
		 * Run the {@link Worker} Thread, performing {@link VirtualUser} moves
		 * eternally. The Hero is yellow.
		 */
		@Override
		public void run() {
			try {
				/** Create a new user for this maze. The hero is yellow. */
				VirtualUser vu = new VirtualUser(connect(), (self == null) ? mz
						: null, (self == null) ? Color.yellow : null);
				if (self == null)
					self = vu;

				/** Move VirtualUser eternally */
				while (true) {
					sleep(150);
					vu.move();
				}
			} catch (InterruptedException ex) {
				// In case the sleep blows up.
				System.out.println("Interrupted. What now?");
			} catch (RemoteException e) {
				// In case of a RemoteException, we have a problem with the
				// connection.
				System.out
						.println("Connection to server failed. Try to restart the server and the Maze applet.");
			}
		}
	}

	/** Get a parameter value */
	public String getParameter(String key, String def) {
		return getParameter(key) != null ? getParameter(key) : def;
	}

	/** Get Applet information */
	@Override
	public String getAppletInfo() {
		return "Applet Information";
	}

	/** Get parameter info */
	@Override
	public String[][] getParameterInfo() {
		java.lang.String[][] pinfo = { { "Size", "int", "" }, };
		return pinfo;
	}

	/**
	 * Draw the {@link Maze} ({@link #maze}) and the map of all
	 * {@link VirtualUser}'s positions, if the Maze applet have a {@link #self}
	 * defined.
	 * 
	 * @param g
	 *            Graphics
	 */
	@Override
	public void paint(Graphics g) {
		int x, y;

		// Draw the maze based on the Box definitions
		if (maze != null)
			for (x = 1; x < (dim - 1); ++x)
				for (y = 1; y < (dim - 1); ++y) {
					if (maze[x][y].getUp() == null)
						g.drawLine(x * 10, y * 10, x * 10 + 10, y * 10);
					if (maze[x][y].getDown() == null)
						g.drawLine(x * 10, y * 10 + 10, x * 10 + 10,
								y * 10 + 10);
					if (maze[x][y].getLeft() == null)
						g.drawLine(x * 10, y * 10, x * 10, y * 10 + 10);
					if (maze[x][y].getRight() == null)
						g.drawLine(x * 10 + 10, y * 10, x * 10 + 10,
								y * 10 + 10);
				}

		// If self exists, drawMap
		if (self != null)
			drawMap(g);
	}

	/**
	 * Draw the map of all {@link VirtualUser} positions known to {@link #self}.
	 * 
	 * @param g
	 *            The AWT Graphics object
	 */
	private void drawMap(Graphics g) {
		// Get map from self.
		HashMap<String, Color> map = self.getMap();

		// Iterate over keys in the given map.
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();

			// Parse the x and y positions.
			String[] pos = key.split(",");
			int x = new Integer(pos[0]);
			int y = new Integer(pos[1]);

			// Set the correct Color.
			g.setColor(map.get(key));

			// Draw the VirtualUser position.
			g.fillOval((x * 10) + 2, (y * 10) + 2, 7, 7);
		}

		// Reset the default Color.
		g.setColor(Color.black);
	}

	/**
	 * Check to see if this Maze applet belongs to the {@link VirtualUser} with
	 * the given id. (See if id is same as {@link #self}'s id).
	 * 
	 * @param id
	 * @return true or false
	 */
	public boolean belongsToUser(Integer id) {
		return self.getId() == id;
	}
}
