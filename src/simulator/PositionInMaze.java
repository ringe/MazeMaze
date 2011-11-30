package simulator;

/**
 * <p>
 * Title: PositionInMaze
 * </p>
 * 
 * <p>
 * Description: A Position In Maze
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
import java.awt.Color;
import java.io.Serializable;

/**
 * The {@link PositionInMaze} is responsible for holding information about a
 * current position for the {@link VirtualUser}s and the
 * {@link mazeoblig.BoxMaze BoxMaze}. Very useful.
 * 
 * @author runar
 * 
 */
public class PositionInMaze implements Serializable {

	private static final long serialVersionUID = -3275691301441215733L;
	private int xpos, ypos;
	/** The Color valid for this {@link PositionInMaze} */
	Color color;

	/**
	 * The constructor creates a new {@link PositionInMaze} at the given x, y
	 * coordinates and with the given Color c of the inherent {@link User} who
	 * was here.
	 * 
	 * @param x
	 *            X position
	 * @param y
	 *            Y position
	 * @param c
	 *            Color
	 */
	public PositionInMaze(int x, int y, Color c) {
		xpos = x;
		ypos = y;
		color = (c == null ? Color.white : c);
	}

	/**
	 * Get the X position.
	 * 
	 * @return X
	 */
	public int getXpos() {
		return xpos;
	}

	/**
	 * Get the Y position.
	 * 
	 * @return Y
	 */
	public int getYpos() {
		return ypos;
	}

	/**
	 * Get the Color valid for this {@link PositionInMaze} instance.
	 * 
	 * @return The valid Color
	 */
	public Color getColor() {
		return color;
	}

	@Override
	public String toString() {
		return "xpos: " + xpos + "\typos: " + ypos;
	}

}
