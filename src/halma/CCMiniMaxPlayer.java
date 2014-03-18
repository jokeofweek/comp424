package halma;

import halma.minimax.BoardPointPair;
import halma.minimax.CombinedMoveGenerator;
import halma.minimax.MoveGenerator;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

public class CCMiniMaxPlayer extends Player {
	
	private static final int[] FRIEND = {3,2,1,0};
	private static final int[][] OPPONENT = {{1,2},{0,3},{0,3},{1,2}};
	private final static Point[] GOAL_POINTS = {
    	new Point(CCBoard.SIZE - 1, CCBoard.SIZE - 1),
    	new Point(0, CCBoard.SIZE - 1),
    	new Point(CCBoard.SIZE - 1, 0),
    	new Point(0, 0)
    };
    private final static int[][] DIRECTIONAL_OFFSETS = {
    	{0,1},{1,1},{1,0},
    	{0,-1},{1,-1},{-1, -1},{-1,1},{-1,0}
    };
    private final static int[] NEXT_OPPONENT = {1, 3, 3, 1};

    public CCMiniMaxPlayer() { super("Minimaxin"); }
    public CCMiniMaxPlayer(String s) { super(s); }
    
    public Queue<CCMove> moveList = new LinkedList<>();
    private int count = 0;
    
	@Override
	public Move chooseMove(Board theBoard) {
		// Convert the board.
		CCBoard board = (CCBoard) theBoard;

		// If we still have queued moves remaining, run them
		if (!moveList.isEmpty()) {
			return moveList.remove();
		}
		
		// Minimax
		long initialTime = System.currentTimeMillis();
		BoardPointPair pair = minimax(board, 3, true,
				new Pair<Integer,BoardPointPair>(Integer.MIN_VALUE, null),
				new Pair<Integer,BoardPointPair>(Integer.MAX_VALUE, null)
			).getSecond();
		System.out.println("Evaluated " + count + " in " + (System.currentTimeMillis() - initialTime));
		
		// If the move isn't a hop, then simply apply it
		if (!pair.isHop()) {
			return new CCMove(playerID, pair.getInitial(), pair.getDestination());
		}
		// If the move is a hop, then need to figure out sequence from initial to hop.
		generateMoveSequence(board, pair.getInitial(), pair.getDestination());
		return moveList.remove();
	}

	@Override
	public void movePlayed(Board board, Move move) {
	}
	
	public Pair<Integer, BoardPointPair> minimax(CCBoard startBoard, int depth, boolean isMaximizing,
			Pair<Integer, BoardPointPair> a, Pair<Integer, BoardPointPair> b) {
		if (depth == 0 || startBoard.getWinner() != Board.NOBODY) {
			return new Pair<Integer, BoardPointPair>(evaluateBoard(startBoard), null);
		}
		
		Pair<Integer, BoardPointPair> val;
		// If the board isn't player 1 or 3, don't need to use combined move generator
		Iterator<BoardPointPair> iterator = null;
		int depthDiff = 0;
		if (startBoard.getTurn() == 0 || startBoard.getTurn() == 2 || depth == 1) {
			iterator = new MoveGenerator(startBoard, startBoard.getTurn());
			depthDiff = 1;
		} else {
			iterator = new CombinedMoveGenerator(startBoard, startBoard.getTurn());
			depthDiff = 2;
		}

		if (isMaximizing) {
			while (iterator.hasNext()) {
				BoardPointPair pair = iterator.next();
				val = minimax(pair.getBoard(), depth - depthDiff, false, a, b);
				if (val.getFirst() > a.getFirst()) {
					a = new Pair<Integer, BoardPointPair>(val.getFirst(), pair);
				}			
				if (b.getFirst() <= a.getFirst()) {
					break;
				}
			}
			return a;
		} else {
			while (iterator.hasNext()) {
				BoardPointPair pair = iterator.next();
				val = minimax(pair.getBoard(), depth - depthDiff, true, a, b);
				if (val.getFirst() < b.getFirst()) {
					b = new Pair<Integer, BoardPointPair>(val.getFirst(), pair);
				}
				if (b.getFirst() <= a.getFirst()) {
					break;
				}
			}			
			return b;
		}
	}
	
	private void generateMoveSequence(CCBoard board, Point from, Point to) {
		HashSet<Point> visited = new HashSet<Point>(100);
		visited.add(from);
		ArrayList<Point> points = new ArrayList<Point>(100);
		points.add(from);
		generateMoveSequence(board, from, to, visited, points);
	}
	
	/**
	 * This fills the move list with a sequence of moves from one point to another.
	 * @param board The board.
	 * @param from Initial starting point.
	 * @param to End point.
	 */
	private boolean generateMoveSequence(CCBoard board, Point from, Point to, Set<Point> visitedPoints, List<Point> points) {
		// If we've reached the end, then generate all the moves
		if (from.equals(to)) {
			Point lastPoint = null;
			for (Point p : points) {
				if (lastPoint != null) {
					moveList.add(new CCMove(playerID, lastPoint, p));
				}
				lastPoint = p;
			}
			
			// Add last move
			moveList.add(new CCMove(playerID, null, null));
			return true;
		}
		
		for (int[] offset : DIRECTIONAL_OFFSETS) {
			Point hopPoint = new Point(from.x + offset[0], from.y + offset[1]);
			if (board.getPieceAt(hopPoint) == null) {
				continue;
			}
			Point newPoint = new Point(from.x + offset[0] * 2, from.y + offset[1] * 2);
			// If in range, not visited, and empty
			if (newPoint.x >= 0 && newPoint.y >= 0 && newPoint.x < 16 && newPoint.y < 16) {
				if (board.getPieceAt(newPoint) == null && !visitedPoints.contains(newPoint)) {
					visitedPoints.add(newPoint);
					// Visit the point
					points.add(newPoint);
					if (generateMoveSequence(board, newPoint, to, visitedPoints, points)) {
						return true;
					}
					points.remove(points.size() - 1);
				}
			}
		}
		return false;
	}
	
	private class Pair<A, B> {
		private A first;
		private B second;
		public Pair(A a, B b) {
			this.first = a;
			this.second = b;
		}
		public A getFirst() {
			return first;
		}
		public B getSecond() {
			return second;
		}
	}
	
	public int manhattanDistance(Point from, Point to) {
		return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
	}
	
	public int evaluateBoard(CCBoard board) {
		count++;
		if (board.getWinner() == board.getTeamIndex(playerID)) {
			return Integer.MAX_VALUE;
		} else if (board.getWinner() == board.getTeamIndex(OPPONENT[playerID][0])) {
			return Integer.MIN_VALUE;
		}

		// For now just sum of your manhattan distance
		int ownDistance = 0;
		int enemyDistance = 0;

		for (int i = 0; i < 4; i++) {
			for (Point p : board.getPieces(i)) {
				int distance = 32 - manhattanDistance(p, GOAL_POINTS[i]);
				if (i == playerID || i == FRIEND[playerID]) {
					ownDistance += distance;
				} else {
					enemyDistance += distance;
				}
			}
		}
		
		return (ownDistance - enemyDistance);
	}
		
}
