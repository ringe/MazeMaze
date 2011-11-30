package mazeoblig;

/**
 * <p>Title: </p>
 * RMIServer - En server som kobler seg opp � kj�rer server-objekter p�
 * rmiregistry som startes automagisk.
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.ExportException;

/**
 * RMIServer starts execution at the standard entry point
 * "public static void main"; It creates an instance of itself and continues
 * processing in the constructor.
 */

public class RMIServer {
	private final static int DEFAULT_PORT = 9000;
	private final static String DEFAULT_HOST = "undefined";
	public static int PORT = DEFAULT_PORT;
	private static String HOST_NAME;
	private static InetAddress myAdress = null;
	private static BoxMaze maze;
	public static String MazeName = "Maze";

	/**
	 * Constructor for the RMIServer.
	 * 
	 * @throws RemoteException
	 * @throws MalformedURLException
	 * @throws NotBoundException
	 * @throws AlreadyBoundException
	 */
	public RMIServer() throws RemoteException, MalformedURLException,
			NotBoundException, AlreadyBoundException {
		getStaticInfo();
		LocateRegistry.createRegistry(PORT);
		System.out.println("RMIRegistry created on host computer " + HOST_NAME
				+ " on port " + Integer.toString(PORT));

		/*
		 * * Legger inn labyrinten
		 */
		maze = new BoxMaze(Maze.DIM);
		System.out.println("Remote implementation object created");
		String urlString = "//" + HOST_NAME + ":" + PORT + "/" + MazeName;

		// Binding the Maze to the given urlString.
		Naming.rebind(urlString, maze);
		System.out.println("Bindings Finished, waiting for client requests.");
	}

	/**
	 * Get static host name
	 */
	private static void getStaticInfo() {
		/**
		 * Henter hostname på min datamaskin
		 */
		if (HOST_NAME == null)
			HOST_NAME = DEFAULT_HOST;
		if (PORT == 0)
			PORT = DEFAULT_PORT;
		if (HOST_NAME.equals("undefined")) {
			try {
				myAdress = InetAddress.getLocalHost();
				/*
				 * * Merk at kallet under vil kunne gi meldingen :**
				 * "Internal errorjava.net.MalformedURLException: invalid authority"
				 * ** i tilfeller hvor navnen på maskinen ikke tilfredstiller*
				 * spesielle krav.* I så tilfelle, bruk "localhost" i stedet.**
				 * Meldingen som gis har ingen betydning
				 */
				HOST_NAME = myAdress.getHostName();
				// HOST_NAME = "localhost";
			} catch (java.net.UnknownHostException e) {
				System.err.println("Klarer ikke å finne egen nettadresse");
				e.printStackTrace(System.err);
			}
		} else
			System.out.println("En MazeServer kjører allerede, bruk den");

		System.out.println("Maze server navn: " + HOST_NAME);
		System.out.println("Maze server ip:   " + myAdress.getHostAddress());
	}

	public static int getRMIPort() {
		return PORT;
	}

	public static String getHostName() {
		return HOST_NAME;
	}

	public static String getHostIP() {
		return myAdress.getHostAddress();
	}

	/**
	 * Run the server program and catch any startup exceptions to end
	 * gracefully.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			new RMIServer();
			java.rmi.registry.Registry r = java.rmi.registry.LocateRegistry
					.getRegistry(HOST_NAME, PORT);
			r.list();
			System.out.println("RMIRegistry on " + HOST_NAME + ":" + PORT
					+ "\n----------------------------");
		} catch (java.rmi.UnknownHostException uhe) {
			System.out.println("Maskinnavnet, " + HOST_NAME
					+ " er ikke korrekt.");
		} catch (ExportException be) {
			System.out.println("ABORTED: A Maze server is already started.");
		} catch (RemoteException re) {
			System.out.println("Error starting service");
			System.out.println("" + re);
			re.printStackTrace(System.err);
		} catch (MalformedURLException mURLe) {
			System.out.println("Internal error" + mURLe);
		} catch (NotBoundException nbe) {
			System.out.println("Not Bound");
			System.out.println("" + nbe);
		} catch (AlreadyBoundException abe) {
			System.out.println("Already Bound");
			System.out.println("" + abe);
		}
	} // main
} // class RMIServer
