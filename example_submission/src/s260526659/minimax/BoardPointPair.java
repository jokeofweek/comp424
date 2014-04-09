package s260526659.minimax;

import halma.CCBoard;

import java.awt.Point;

/**
 * This represents a board as well as a starting piece and the point where
 * the piece ended at.
 */
public class BoardPointPair {
	
	private CCBoard board;
	private Point initial;
	private Point destination;
	
	public BoardPointPair(CCBoard board, Point initial, Point destination) {
		super();
		this.board = board;
		this.initial = initial;
		this.destination = destination;
	}
	public CCBoard getBoard() {
		return board;
	}
	public Point getDestination() {
		return destination;
	}
	public Point getInitial() {
		return initial;
	}

	public void setBoard(CCBoard board) {
		this.board = board;
	}
	public void setDestination(Point destination) {
		this.destination = destination;
	}
	public void setInitial(Point initial) {
		this.initial = initial;
	}
	public boolean isHop() {
		return Math.abs(initial.x - destination.x) > 1 || Math.abs(initial.y - destination.y) > 1;
	}
}
