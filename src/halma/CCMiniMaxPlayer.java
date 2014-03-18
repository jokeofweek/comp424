package halma;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

    public CCMiniMaxPlayer() { super("Minimaxin"); }
    public CCMiniMaxPlayer(String s) { super(s); }
    
    public Queue<CCMove> moveList = new LinkedList<>();
    
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
		BoardPointPair pair = minimax(board, 2, playerID, null, null).getSecond();

		// If the move isn't a hop, then simply apply it
		if (!pair.getMove().isHop()) {
			return pair.getMove();
		}
		
		// If the move is a hop, then need to figure out sequence from initial to hop.
		generateMoveSequence(board, pair.getInitial(), pair.getMove().to);
		for (CCMove m : moveList) {
			System.out.print(m.from + " ");
		}
		System.out.println();
		return moveList.remove();
	}

	@Override
	public void movePlayed(Board board, Move move) {
	}
	
	public Pair<Integer, BoardPointPair> minimax(CCBoard startBoard, int depth, int playerID, Point from, Point to) {
		if (depth == 0 || startBoard.getWinner() != Board.NOBODY) {
			return new Pair<Integer, BoardPointPair>(evaluateBoard(startBoard, playerID, from, to), null);
		}
		
		boolean isMaximizing = (playerID == this.playerID || playerID == FRIEND[this.playerID]);
		Pair<Integer, BoardPointPair> bestValue;
		Pair<Integer, BoardPointPair> val;
		
		if (isMaximizing) {
			bestValue = new Pair<Integer, BoardPointPair>(Integer.MIN_VALUE, null);
			for (BoardPointPair pair : generateBoards(startBoard, playerID)) {
				val = minimax(pair.getBoard(), depth - 1, (playerID + 1) % 4, pair.getInitial(), pair.getMove().getTo());
				if (val.getFirst() > bestValue.getFirst()) {
					bestValue = new Pair<Integer, BoardPointPair>(val.getFirst(), pair);
				}				
			}
		} else {
			bestValue = new Pair<Integer, BoardPointPair>(Integer.MAX_VALUE, null);
			for (BoardPointPair pair : generateBoards(startBoard, playerID)) {
				val = minimax(pair.getBoard(), depth - 1, (playerID + 1) % 4, pair.getInitial(), pair.getMove().getTo());
				if (val.getFirst() < bestValue.getFirst()) {
					bestValue = new Pair<Integer, BoardPointPair>(val.getFirst(), pair);
				}
			}			
		}
		
		return bestValue;
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
	private class BoardPointPair {
		private CCBoard board;
		private Point initial;
		private CCMove move;
		public BoardPointPair(CCBoard board, Point initial, CCMove move) {
			this.board = board;
			this.initial = initial;
			this.move = move;
		}
		public CCMove getMove() { return move; }
		public CCBoard getBoard() { return board; }
		public Point getInitial() { return initial; }
	}
	
	public List<BoardPointPair> generateBoards(CCBoard startBoard, int player) {
		List<BoardPointPair> boards = new LinkedList<>();
		Queue<BoardPointPair> boardQueue = new LinkedList<>();
		
		// Cache where each initial point has been to avoid rehopping to a point
		HashMap<Point, Set<Point>> hoppedPoints = new HashMap<>(10);
		for (Point p : startBoard.getPieces(player)) {
			hoppedPoints.put(p, (Set)new HashSet<>());
		}
		
		// Initialize the queue
		for (CCMove move: startBoard.getLegalMoves()) {
			// Create the board with the hop and end
			CCBoard b = (CCBoard)startBoard.clone();
			b.move(move);
			BoardPointPair p = new BoardPointPair(b, move.from, move);
			
			if (move.isHop()) {
				boardQueue.add(p);
				// Register the initial in the set of moves we've went to
				// to avoid hopping back
				hoppedPoints.get(move.from).add(move.from);
			} else {
				boards.add(p);
			}	
		}
		
		// Iterate through all possible hops
		while (!boardQueue.isEmpty()) {
			BoardPointPair pair = boardQueue.remove();
			Point initial = pair.getInitial();
			
			for (CCMove m : pair.getBoard().getLegalMoves()) {
				// Don't go to a move if it's been done before
				if (m.to != null && m.from != null && !hoppedPoints.get(initial).contains(m.to)) {
					//Clone the board with the move applied
					CCBoard b = (CCBoard)pair.getBoard().clone();
					b.move(m);
					
					// Add the board with the sequence over.
					CCBoard b2 = (CCBoard)b.clone();
					b2.move(new CCMove(player, null, null));
					boards.add(new BoardPointPair(b2, initial, m));
					
					// Sequence of hop is still going, add it to the visited points and continue
					hoppedPoints.get(initial).add(m.to);
					boardQueue.add(new BoardPointPair(b, initial, m));
				}
			}
		}
		
		return boards;		
	}
	
	public int manhattanDistance(Point from, Point to) {
		return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
	}
	
	public int evaluateBoard(CCBoard board, int player, Point from, Point to) {
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
		
		return /*manhattanDistance(from, to) **/ (ownDistance - enemyDistance);
	}
		
}
