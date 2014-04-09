package halma;

import halma.minimax.BoardPointPair;
import halma.minimax.CombinedMoveGenerator;
import halma.minimax.MoveGenerator;
import halma.minimax.features.AdjacentToBaseFeature;
import halma.minimax.features.DontLeaveAloneFeature;
import halma.minimax.features.Feature;
import halma.minimax.features.LeaveBaseFeature;
import halma.minimax.features.ManhattanDistanceFeature;
import halma.minimax.features.NotInGoalZoneFeature;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

public class CCHeuristicPlayer extends Player {
	
	private static final int[] FRIEND = {3,2,1,0};
	private static final int[][] OPPONENT = {{1,2},{0,3},{0,3},{1,2}};
    private final static int[][] DIRECTIONAL_OFFSETS = {
    	{0,1},{1,1},{1,0},
    	{0,-1},{1,-1},{-1, -1},{-1,1},{-1,0}
    };

	private final static Point[] BASE_POINTS = {new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0),
		new Point(0,1), new Point(1,1), new Point(2,1), new Point(3,1),
		new Point(0,2), new Point(1,2), new Point(2,2),
		new Point(0,3), new Point(1,3)};
	private final static HashSet<Point>[] BASES= initializeBases();

	// Minimax_0.95_0.001_0.005_0.015_0.06
	//         0.9_0.002_0.005_0.015_0.05

    /**
     * The enabled features.
     */
    protected List<Feature> features = Arrays.asList(
    		(Feature)new ManhattanDistanceFeature(0.9),
    		new LeaveBaseFeature(0.002),
    		new DontLeaveAloneFeature(0.005),
    		new NotInGoalZoneFeature(0.015),
    		new AdjacentToBaseFeature(0.05) 
    );
    
    /**
     * The cached list of moves.
     */
    public Queue<CCMove> moveList = new LinkedList<>();
    private int count = 0;
	private CCBoard originalBoard;
    
    public CCHeuristicPlayer() { super("Heuristic"); }
    public CCHeuristicPlayer(String s) { super(s); }
    
    
	@Override
	public Move chooseMove(Board theBoard) {
		count = 0;
		// Convert the board.
		CCBoard board = (CCBoard) theBoard;
		originalBoard = board;
		
		
		// If we still have queued moves remaining, run them
		if (!moveList.isEmpty()) {
			return moveList.remove();
		}
		
		// Time a single rollout
		long endTime = System.currentTimeMillis() + 800;
		Random r = new Random();
		int rollouts = 0;
		while (endTime > System.currentTimeMillis()) {
			CCBoard rolloutBoard = (CCBoard) board.clone();
			while (rolloutBoard.getWinner() == Board.NOBODY) {
				List<CCMove> moves = rolloutBoard.getLegalMoves();
				rolloutBoard.move(moves.get(r.nextInt(moves.size())));
			}
			rollouts++;
		}
		System.out.println("Performed " + rollouts + " rollouts.");
		
		// Get the best move
		Pair<Double, BoardPointPair> best = new Pair<Double,BoardPointPair>(0.0 + Integer.MIN_VALUE, null);
		Iterator<BoardPointPair> iterator = new MoveGenerator(originalBoard, playerID);
		int count = 0;
		while (iterator.hasNext()) {
			BoardPointPair current = iterator.next();
			count++;
			double score = evaluateBoard(current.getBoard());
			if (score > best.first) {
				best = new Pair<Double, BoardPointPair>(score, current);
			}
		}
		System.out.println("Total moves: " + count);
		System.out.println("Rollouts/move: " + rollouts /  (count + 0.0) );
		
		BoardPointPair pair = best.second;
		
		// If we have no move, then simply get the first non-hop legal move and apply it.
		if (pair == null || pair.getInitial() == null || pair.getDestination() == null) {
			for (CCMove m : board.getLegalMoves()) {
				if (!m.isHop()) {
					return m;
				}
			}
		}
		
		// If the move isn't a hop, then simply apply it
		if (!pair.isHop()) {
			return new CCMove(playerID, pair.getInitial(), pair.getDestination());
		}
		
		
		// If the move is a hop, then need to figure out sequence from initial to hop.
		generateMoveSequence(board, pair.getInitial(), pair.getDestination());
		return moveList.remove();
	}


	/**
	 * Generate a set of all points in the bases
	 * @return a set of all points in the bases
	 */
	private static HashSet<Point>[] initializeBases() {
		HashSet<Point>[] b= new HashSet[4];
		for(int i=0; i<4; i++){
			b[i] = new HashSet<Point>();
			for(int j=0; j<BASE_POINTS.length; j++){
				Point p= new Point( (i%2==0)?BASE_POINTS[j].x:CCBoard.SIZE-BASE_POINTS[j].x-1, 
						((i>>1)%2==0)?BASE_POINTS[j].y:CCBoard.SIZE-BASE_POINTS[j].y-1);
				b[i].add(p);
			}
		}

		return b;
	}
	
	@Override
	public void movePlayed(Board board, Move move) {
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
					// Test that if you are going out of the friend's base, which is illegal
					if (!BASES[FRIEND[playerID]].contains(from) || BASES[FRIEND[playerID]].contains(newPoint)) {
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
	
	public double evaluateBoard(CCBoard board) {
		count++;
		// If someone is winning, return -Inf or +Inf
		if (board.getWinner() == CCBoard.getTeamIndex(playerID)) {
			return Integer.MAX_VALUE;
		} else if (board.getWinner() == CCBoard.getTeamIndex(OPPONENT[playerID][0])) {
			return Integer.MIN_VALUE;
		}

		// Sum up the result for all features
		double result = 0;
		for (Feature feature : features) {
			result += (feature.getWeight(board, originalBoard, playerID) * feature.getScore(board, originalBoard, playerID));
		}
		
		return result;
	}
		
}
