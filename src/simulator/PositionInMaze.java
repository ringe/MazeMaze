package simulator;

import java.awt.Color;
import java.io.Serializable;

public class PositionInMaze implements Serializable {

	private static final long serialVersionUID = -3275691301441215733L;
	private int xpos, ypos;
	Color color = Color.black;
	
	public PositionInMaze(int xp, int yp, Color c) {
		xpos = xp;
		ypos = yp;
		color = c;
	}

	public int getXpos() {
		return xpos;
	}

	public int getYpos() {
		return ypos;
	}
	
	public String toString() {
		return "xpos: " + xpos + "\typos: " + ypos;
	}
	
	public Color getColor() {
		return color;
	}
}
