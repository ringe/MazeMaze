package simulator;

import java.awt.Color;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import mazeoblig.Box;
import mazeoblig.BoxMaze;
import mazeoblig.BoxMazeInterface;
import mazeoblig.Maze;

/**
 * <p>
 * The {@link VirtualUser} instance is the server callback receiver in this Maze
 * RMI setup. The constructor takes a {@link BoxMazeInterface} to establish a
 * connection to the server and a {@link Maze} applet instance, to establish a
 * connection to the Graphical User Interface.
 * </p>
 * <p>
 * The {@link #init} method establishes the connection to the server by
 * {@link BoxMazeInterface#join joining} the maze there and also puts the
 * {@link VirtualUser} at a random starting {@link PositionInMaze position}.
 * </p>
 * <p>
 * a. getFirstIterationLoop() returns an array of {@link PositionInMaze
 * positions} that leads the way out of the the {@link #maze}, around and to the
 * beginning of the {@link #maze}, from the starting {@link PositionInMaze
 * position}.<br>
 * b. getIterationLoop() returns an array of {@link PositionInMaze positions}
 * that leads the way out of the {@link #maze} from the entry and around the
 * {@link #maze} to the beginning.
 * </p>
 * <p>
 * The movement through the {@link #maze} is performed by calling {@link #move},
 * this happens at the {@link #client}.
 * 
 * @author asd, runar
 * 
 */
public class VirtualUser extends UnicastRemoteObject implements User {

	private static final long serialVersionUID = -8926308983633431228L;

	/** The actual maze of this game, in a local copy. */
	private Box[][] maze;
	private int dim;

	int xp;
	int yp;
	boolean found = false;

	private Stack<PositionInMaze> myWay = new Stack<PositionInMaze>();
	private PositionInMaze[] FirstIteration;
	private PositionInMaze[] NextIteration;

	/** The Color of this {@link VirtualUser} */
	private Color color;
	/** The {@link Maze} applet client connection */
	private Maze client;
	/** The map of all {@link PositionInMaze positions} */
	private HashMap<String, Color> usersMap = new HashMap<String, Color>();
	/** This {@link VirtualUser}'s id at the {@link BoxMaze} server. */
	private Integer id;

	/** The {@link BoxMaze} server connection. */
	private BoxMazeInterface server;

	/** The current array of moves to perform */
	private PositionInMaze[] moves;
	/** The current position in the {@link #moves} array. */
	private int position;

	/** Go back to random start ? */
	private boolean turn = true;

	/**
	 * Constructor for the local {@link User} implementation. Takes an enabled
	 * {@link BoxMazeInterface} connection and an instance of a {@link Maze} applet
	 * for the client connection, plus a Color, of course.
	 * 
	 * @param bm	The server connection
	 * @param mz	The client to connect to
	 * @param c		The color to use
	 * @throws RemoteException
	 */
	public VirtualUser(BoxMazeInterface bm, Maze mz, Color c) throws RemoteException {
		setColor(c);
		server = bm;
		client = mz;
		maze = server.getMaze();
		dim = maze[0].length;
		init();
	}

	/**
	 * Initiate the VirtualUser: Create a random start position, calculate moves,
	 * and join the server.
	 */
	private void init() {
		// Setter en tifeldig posisjon i maze (xp og yp)
		Random rand = new Random();
		xp = rand.nextInt(dim - 1) + 1;
		yp = rand.nextInt(dim - 1) + 1;

		// Løser veien ut av labyrinten basert på tilfeldig inngang ...
		makeFirstIteration();
		// og deretter løses labyrinten basert på inngang fra starten
		makeNextIteration();

		// Prepare moves.
		setMoves();
		
		// Join the Maze, get User Id
		try {
			id = server.join(this);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * L�ser maze ut fra en tilfeldig posisjon i maze
	 */
	private void solveMaze() {
		found = false;
		// Siden posisjonen er tilfeldig valgt risikerer man at man kjører i en
		// brønn
		// Av denne grunn .... det er noe galt med kallet under
		myWay.push(new PositionInMaze(xp, yp, color));
		backtrack(maze[xp][yp], maze[1][0]);
	}

	/**
	 * Selve backtracking-algoritmen som brukes for å finne løsningen
	 * 
	 * @param b
	 *            Box
	 * @param from
	 *            Box
	 */
	private void backtrack(Box b, Box from) {
		// Aller først - basistilfellet, slik at vi kan returnere
		// Under returen skrives det med Rødt
		if ((xp == dim - 2) && (yp == dim - 2)) {
			found = true;
			// Siden vi tegner den "riktige" veien under returen opp gjennom
			// Java's runtime-stack, så legger vi utgang inn sist ...
			return;
		}
		// Henter boksene som det finnes veier til fra den boksen jeg står i
		Box[] adj = b.getAdjecent();
		// Og sjekker om jeg kan gå de veiene
		for (int i = 0; i < adj.length; i++) {
			// Hvis boksen har en utganger som ikke er lik den jeg kom fra ...
			if (!(adj[i].equals(from))) {
				adjustXYBeforeBacktrack(b, adj[i]);
				myWay.push(new PositionInMaze(xp, yp, color));
				backtrack(adj[i], b);
				// Hvis algoritmen har funnet veien ut av labyrinten, så
				// inneholder stacken (myWay)
				// veien fra det tilfeldige startpunktet og ut av labyrinten
				if (!found)
					myWay.pop();
				adjustXYAfterBacktrack(b, adj[i]);
			}
			// Hvis veien er funnet, er det ingen grunn til å fortsette
			if (found) {
				break;
			}
		}
	}

	/**
	 * Oppdatere x og y i labyrinten før backtracking kalles
	 * 
	 * @param from
	 *            Box
	 * @param to
	 *            Box
	 */
	private void adjustXYBeforeBacktrack(Box from, Box to) {
		if ((from.getUp() != null) && (to.equals(from.getUp())))
			yp--;
		if ((from.getDown() != null) && (to.equals(from.getDown())))
			yp++;
		if ((from.getLeft() != null) && (to.equals(from.getLeft())))
			xp--;
		if ((from.getRight() != null) && (to.equals(from.getRight())))
			xp++;
	}

	/**
	 * Oppdatere x og y i labyrinten etter at backtracking er kalt
	 * 
	 * @param from
	 *            Box
	 * @param to
	 *            Box
	 */
	private void adjustXYAfterBacktrack(Box from, Box to) {
		if ((from.getUp() != null) && (to.equals(from.getUp())))
			yp++;
		if ((from.getDown() != null) && (to.equals(from.getDown())))
			yp--;
		if ((from.getLeft() != null) && (to.equals(from.getLeft())))
			xp++;
		if ((from.getRight() != null) && (to.equals(from.getRight())))
			xp--;
	}

	/**
	 * Returnerer hele veien, fra tilfeldig startpunkt og ut av Maze som en
	 * array
	 * 
	 * @return [] PositionInMaze
	 */
	private PositionInMaze[] solve() {
		solveMaze();
		PositionInMaze[] pos = new PositionInMaze[myWay.size()];
		for (int i = 0; i < myWay.size(); i++)
			pos[i] = myWay.get(i);
		return pos;
	}

	/**
	 * Returnerer posisjonene som gir en vei rundt maze, tilfeldig valgt - mot
	 * høyre eller mot venstre
	 * 
	 * @return [] PositionInMaze;
	 */
	private PositionInMaze[] roundAbout() {
		PositionInMaze[] pos = new PositionInMaze[dim * 2];
		int j = 0;
		pos[j++] = new PositionInMaze(dim - 2, dim - 1, color);
		// Vi skal enten gå veien rundt mot høyre ( % 2 == 0)
		// eller mot venstre
		if (System.currentTimeMillis() % 2 == 0) {
			for (int i = dim - 1; i >= 0; i--)
				pos[j++] = new PositionInMaze(dim - 1, i, color);
			for (int i = dim - 1; i >= 1; i--)
				pos[j++] = new PositionInMaze(i, 0, color);
		} else {
			for (int i = dim - 1; i >= 1; i--)
				pos[j++] = new PositionInMaze(i, dim - 1, color);
			for (int i = dim - 1; i >= 0; i--)
				pos[j++] = new PositionInMaze(0, i, color);
		}
		// Uansett, så returneres resultatet
		return pos;
	}

	/**
	 * Låser hele maze, fra startposisjonen
	 * 
	 * @return PositionInMaze[]
	 */
	@SuppressWarnings("unused")
	private PositionInMaze[] solveFull() {
		solveMaze();
		PositionInMaze[] pos = new PositionInMaze[myWay.size()];
		for (int i = 0; i < myWay.size(); i++)
			pos[i] = myWay.get(i);
		return pos;
	}

	/**
	 * Genererer opp veien ut av labyrinten fra en tilfeldig posisjon, samt
	 * veien rundt og frem til inngangen av labyrinten
	 */
	private void makeFirstIteration() {
		PositionInMaze[] outOfMaze = solve();
		PositionInMaze[] backToStart = roundAbout();
		FirstIteration = VirtualUser.concat(outOfMaze, backToStart);
	}

	/**
	 * Genererer opp veien ut av labyrinten fra inngangsposisjonen i labyrinten,
	 * samt veien rundt og frem til inngangen av labyrinten igjen
	 */
	private void makeNextIteration() {
		// Tvinger posisjonen til å være ved inngang av Maze
		xp = 1;
		yp = 1;
		myWay = new Stack<PositionInMaze>();
		PositionInMaze[] outOfMaze = solve();
		PositionInMaze[] backToStart = roundAbout();
		NextIteration = VirtualUser.concat(outOfMaze, backToStart);
	}

	/**
	 * Generisk metode som slår sammen to arrayer av samme type
	 * 
	 * @param <T>
	 * @param first
	 * @param second
	 * @return <T>
	 */
	private static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	/**
	 * Returnerer en PositionInMaze [] som inneholder x- og y-posisjonene som en
	 * virituell spiller benytter for å finne veien ut av labyrinten ut fra
	 * inngangen i labyrinten.
	 * 
	 * @return PositionInMaze[]
	 */
	public PositionInMaze[] getIterationLoop() {
		return NextIteration;
	}

	/**
	 * Returnerer en PositionInMaze [] som inneholder x- og y-posisjonene som en
	 * virituell spiller benytter for å finne veien ut av labyrinten ut fra en
	 * tilfedlig generert startposisjon i labyrinten.
	 * 
	 * @return PositionInMaze[]
	 */
	public PositionInMaze[] getFirstIterationLoop() {
		return FirstIteration;
	}

	/**
	 * Receive a map of all {@link User}'s positions and update the
	 * {@link #client}.
	 */
	@Override
	public void updateMap(HashMap<String, Color> map) throws RemoteException {
		usersMap = map;
		if (client != null)
			if (client.belongsToUser(id))
				client.repaint();
	}

	/**
	 * Return a map of all known user's positions
	 * 
	 * @return HashMap of strings and colors
	 */
	public HashMap<String, Color> getMap() {
		return usersMap;
	}

	/**
	 * Perform move and update {@link #server} about position
	 */
	public void move() {
		// Perform move until out of moves
		if (position < (moves.length - 1))
			position++;
		else setMoves();

		// Update server about position
		try {
			server.update(id, moves[position]);
		} catch (RemoteException e) {
			System.out.println("Couldn't connect to server. Exiting.");
			System.exit(0);
		}
	}

	/**
	 * Set the next {@link #moves}.
	 */
	private void setMoves() {
		position = 0; // Start over.
		turn = !turn; // Change which loop to move over every other time.
		moves = turn ? getIterationLoop() : getFirstIterationLoop();
	}

	/**
	 * Return User Id.
	 */
	@Override
	public Integer getId() {
		return id;
	}
	
	/**
	 * Set Color for this VirtualUser
	 * @param given
	 * 
	 */
	public void setColor (Color given) {
		if (given != null) {
			color = given;
			return;
		}
		Random generator = new Random();
		Color[] array = {Color.black, Color.blue, Color.red, Color.pink, Color.white, Color.green, Color.gray, Color.magenta}; 
        int rnd = generator.nextInt(array.length);
        color = array[rnd];
    }
}