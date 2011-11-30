package mazeoblig;

import java.awt.*;
import java.applet.*;

import simulator.*;

/**
 *
 * <p>Title: Maze</p>
 *
 * <p>Description: En enkel applet som viser den randomiserte labyrinten</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
/**
 * Tegner opp maze i en applet, basert p� definisjon som man finner p� RMIServer
 * RMIServer p� sin side  henter st�rrelsen fra definisjonen i Maze
 * @author asd
 *
 */
public class Maze extends Applet {

	private static final long serialVersionUID = -4427531806078172174L;
	
	private BoxMazeInterface bm;
	private Box[][] maze;
	public static int DIM = 30;
	private int dim = DIM;

	static int xp;
	static int yp;
	static boolean found = false;

	private String server_hostname;
	private int server_portnumber;

	// The VirtualUser related to this Maze client.
	private VirtualUser self;
	
	// n number of users to create
	private int n = 100;

	/**
	 * Initiate a simulation of n number of Users.
	 */
	public void init() {
		LotsOfPlayers pl = new LotsOfPlayers(n, this);
		pl.setDaemon(true);
		pl.start();
	}
	
	/**
	 * Establish an RMI connection to the Server BoxMaze object. 
	 * @return BoxMazeInterface
	 */
	public BoxMazeInterface connect() {

		/*
		 ** Kobler opp mot RMIServer, under forutsetning av at disse
		 ** kj�rer p� samme maskin. Hvis ikke m� oppkoblingen
		 ** skrives om slik at dette passer med virkeligheten.
		 */
		if (server_hostname == null)
			server_hostname = RMIServer.getHostName();
		if (server_portnumber == 0)
			server_portnumber = RMIServer.getRMIPort();
		try {
			java.rmi.registry.Registry r = java.rmi.registry.LocateRegistry.
			getRegistry(server_hostname,
					server_portnumber);

			/*
			 ** Henter inn referansen til Labyrinten (ROR)
			 */
			BoxMazeInterface boxmaze = (BoxMazeInterface) r.lookup(RMIServer.MazeName);
			if (maze == null)
				maze = boxmaze.getMaze();
			if (bm == null)
				bm = boxmaze;

			return boxmaze;
		}
		catch (RemoteException e) {
			System.err.println("Remote Exception: " + e.getMessage());
			System.exit(0);
		}
		catch (NotBoundException f) {
			/*
			 ** En exception her er en indikasjon p� at man ved oppslag (lookup())
			 ** ikke finner det objektet som man s�ker.
			 ** �rsaken til at dette skjer kan v�re mange, men v�r oppmerksom p�
			 ** at hvis hostname ikke er OK (RMIServer gir da feilmelding under
			 ** oppstart) kan v�re en �rsak.
			 */
			System.err.println("Not Bound Exception: " + f.getMessage());
			System.exit(0);
		}
		return null;
	}
	
	/**
	 *  Thread to start a number of players
	 * @author runar
	 */
	class LotsOfPlayers extends Thread {
		private int n;
		private Maze mz;
		
		/**
		 * Constructor
		 * @param c number of players to create
		 * @param m client to connect to
		 */
		LotsOfPlayers(int c, Maze m) { n = c; mz = m; }
		
		/**
		 * run the thread to create new Workers
		 */
		public void run() {
			for ( int i = n; i != 0; i--) {
				Worker w = new Worker(mz);
				w.setDaemon(true);
				w.start();
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Start a Worker Thread for each VirtualUser.
	 * @author runar
	 */
	private class Worker extends Thread {
		private Maze mz;
		
		public Worker(Maze m) { mz = m; }
		
		public void run(){
			try {
				// Create a new user for this maze.
				VirtualUser vu = new VirtualUser(connect(), (self == null) ? mz : null);
				if (self == null)
					self = vu;
				
				// Move VirtualUser eternally
				while (true) {
					vu.move();
					sleep(150);
				}
			}
			catch (InterruptedException ex) {
				ex.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}

	//Get a parameter value
	public String getParameter(String key, String def) {
		return getParameter(key) != null ? getParameter(key) : def;
	}
	//Get Applet information
	public String getAppletInfo() {
		return "Applet Information";
	}

	//Get parameter info
	public String[][] getParameterInfo() {
		java.lang.String[][] pinfo = { {"Size", "int", ""},
		};
		return pinfo;
	}

	/**
	 * Viser labyrinten / tegner den i applet
	 * @param g Graphics
	 */
	public void paint (Graphics g) {
		int x, y;

		// Tegner baser p� box-definisjonene ....

		if (maze != null)
			for (x = 1; x < (dim - 1); ++x)
				for (y = 1; y < (dim - 1); ++y) {
					if (maze[x][y].getUp() == null)
						g.drawLine(x * 10, y * 10, x * 10 + 10, y * 10);
					if (maze[x][y].getDown() == null)
						g.drawLine(x * 10, y * 10 + 10, x * 10 + 10, y * 10 + 10);
					if (maze[x][y].getLeft() == null)
						g.drawLine(x * 10, y * 10, x * 10, y * 10 + 10);
					if (maze[x][y].getRight() == null)
						g.drawLine(x * 10 + 10, y * 10, x * 10 + 10, y * 10 + 10);
				}
		
		if (self != null)
			drawMap(g);
	}
	
	private void drawMap(Graphics g) {
		HashMap<String, Color> map = self.getMap();
		
		Set<String> keys = map.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()) {
			String key = it.next();
			String[] pos = key.split(",");
			int x = new Integer(pos[0]);
			int y = new Integer(pos[1]);
			g.setColor(map.get(key));
			g.fillOval((x * 10)+2, (y * 10)+2, 7, 7);
		}

		g.setColor(Color.black);
	}
	
	public boolean belongsToUser(Integer id) {
		return self.getId() == id;
	}
}

