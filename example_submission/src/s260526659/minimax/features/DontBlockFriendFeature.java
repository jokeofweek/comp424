package s260526659.minimax.features;

import halma.CCBoard;

import java.awt.Point;

public class DontBlockFriendFeature extends Feature {

	private static Point[] DEFAULT_FRIEND_PIECE = {new Point(0, 0)};
	private static Point[] DEFAULT_TRAP_PIECES = {
		new Point(1, 1),
		new Point(0, 1),
		new Point(1, 0),
		new Point(2, 0),
		new Point(0, 2),
		new Point(2, 2)
	};
	
	private static Point[][] FRIEND_PIECE = new Point[4][DEFAULT_FRIEND_PIECE.length];
	private static Point[][] TRAP_PIECES = new Point[4][DEFAULT_TRAP_PIECES.length];
	
	{
		for (int i = 0; i < DEFAULT_FRIEND_PIECE.length; i++) {
			Point piece = DEFAULT_FRIEND_PIECE[i];
			FRIEND_PIECE[0][i] = new Point(15 - piece.x, 15 - piece.y);
			FRIEND_PIECE[1][i] = new Point(piece.x, 15 - piece.y);
			FRIEND_PIECE[2][i] = new Point(15 - piece.x, piece.y);
			FRIEND_PIECE[3][i] = new Point(piece.x, piece.y);
		}
		for (int i = 0; i < DEFAULT_TRAP_PIECES.length; i++) {
			Point piece = DEFAULT_TRAP_PIECES[i];
			TRAP_PIECES[0][i] = new Point(15 - piece.x, 15 - piece.y);
			TRAP_PIECES[1][i] = new Point(piece.x, 15 - piece.y);
			TRAP_PIECES[2][i] = new Point(15 - piece.x, piece.y);
			TRAP_PIECES[3][i] = new Point(piece.x, piece.y);
		}
	}
	
	@Override
	public double getScore(CCBoard board, CCBoard original, int playerID) {
		// If we trapped our friend, return -500 since we're basically going to lose.
		for (Point p : FRIEND_PIECE[playerID]) {
			if (board.getPieceAt(p) == null || board.getPieceAt(p) != (playerID ^ 3)) return 0;
		}
		for (Point p : TRAP_PIECES[playerID]) {
			if (board.getPieceAt(p) == null || board.getPieceAt(p) != playerID) return 0;
		}
		return -500;
	}
	
	@Override
	public double getWeight(CCBoard board, CCBoard original, int playerID) {
		return 1;
	}
	
}
