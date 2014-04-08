package s260526659;

import halma.CCBoard;
import halma.CCMove;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import s260526659.minimax.BoardPointPair;
import s260526659.minimax.MoveGenerator;
import s260526659.minimax.features.AdjacentToBaseFeature;
import s260526659.minimax.features.DontLeaveAloneFeature;
import s260526659.minimax.features.Feature;
import s260526659.minimax.features.HuddleFeature;
import s260526659.minimax.features.LeaveBaseFeature;
import s260526659.minimax.features.ManhattanDistanceFeature;
import s260526659.minimax.features.NotInOpposingBaseFeature;
import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

public class s260526659Player extends Player {
	

	// Agent configuration
	private static long TIMEOUT = 800;
	private static int ROLLOUT_MOVES = 10;
	// When <= this many pieces are in the goal zone, consider it as beginning game.
	private static int BEGINNING_GAME_PIECES = 0;
	 // When >= this many pieces are in the goal zone, consider it as end game.
	private static int END_GAME_PIECES = 10;
	private static double EXPLORATION_PARAMETER = Math.sqrt(2);
	private static double RANDOM_MOVE_PROBABILITY = 0.10;
	// Assign points depending on result of evaluation function.
	private static double[] SCORE_LEVELS = {5000, 100, 0, -0.5, -1, -100};
	// Number of points to reward for each score level (1 extra for last level)
	private static int[] SCORE_REWARDS = {1000, 10, 3, 1, -1, -4, -1000};

	// Agent variables
	private Random random;
	private Queue<CCMove> moveCache;
	
    // Enabled features
    protected List<Feature> features = Arrays.asList(
    		(Feature)new ManhattanDistanceFeature(0.9),
    		new LeaveBaseFeature(0.002),
    		new DontLeaveAloneFeature(0.005),
    		new NotInOpposingBaseFeature(0.015),
    		new AdjacentToBaseFeature(0.05),
    		new HuddleFeature(0.25)
    );
    
	// Static variables
	private static final int[] FRIEND = { 3, 2, 1, 0 };
	private final static int[][] DIRECTIONAL_OFFSETS = { { 0, 1 }, { 1, 1 },
			{ 1, 0 }, { 0, -1 }, { 1, -1 }, { -1, -1 }, { -1, 1 }, { -1, 0 } };

	private final static Point[] GOAL_POINTS = {
			new Point(CCBoard.SIZE - 1, CCBoard.SIZE - 1),
			new Point(0, CCBoard.SIZE - 1), new Point(CCBoard.SIZE - 1, 0),
			new Point(0, 0) };

	private static double[][][][][] MOVE_VALUE;
	{
		// Initialize the distance of each point to each goal
		MOVE_VALUE = new double[16][16][16][16][4];
		for (int x1 = 0; x1 < 16; x1++) {
			for (int y1 = 0; y1 < 16; y1++) {
				for (int x2 = 0; x2 < 16; x2++) {
					for (int y2 = 0; y2 < 16; y2++) {
						for (int p = 0; p < 4; p++) {
							double startDist = Math.sqrt(Math.pow(x1
									- GOAL_POINTS[p].x, 2)
									+ Math.pow(y1 - GOAL_POINTS[p].y, 2));
							double endDist = Math.sqrt(Math.pow(x2
									- GOAL_POINTS[p].x, 2)
									+ Math.pow(y2 - GOAL_POINTS[p].y, 2));
							MOVE_VALUE[x1][y1][x2][y2][p] = startDist - endDist;
						}
					}
				}
			}
		}
	}

	// Actual code

	public s260526659Player() {
		super("s260526659Player");
		this.random = new Random();
		this.moveCache = new LinkedList<CCMove>();
	}


	@Override
	public Move chooseMove(Board board) {
		// Start timings
		long endTime = System.currentTimeMillis() + TIMEOUT;
		
		CCBoard original = (CCBoard) board;
		
		// If there are any cached moves, return those
		if (!moveCache.isEmpty()) {
			return moveCache.remove();
		}
		
		BoardPointPair bestPair = null;
		// If we are in end or beginning game, simply brute force.
		if (!isMidGame(original)) {
			double best = -1000;
			MoveGenerator generator = new MoveGenerator(original, playerID);
			BoardPointPair current;
			double score;

			while (generator.hasNext()) {
				current = generator.next();
				// If it is a winner, go go go!
				if (current.getBoard().getWinner() != board.NOBODY) {
					bestPair = current;
					break;
				}
				score = evaluateBoard(current.getBoard(), original, playerID);
				if (score > best) {
					best = score;
					bestPair = current;
				}
			}
		} else {
			// Create the root move node
			BoardPointPair pair = new BoardPointPair((CCBoard) board, null, null);
			MoveNode node = new MoveNode(pair, null);
			
			// Initialize the root and then perform as many rounds as possible.
			node.initRoot();
			do {
				node.performRound();
			} while (System.currentTimeMillis() < endTime);
			System.out.println("Simulated: " + node.simulations);

			// Return the pair of the best child.			
			bestPair = node.getBestChild().pair;
		}

		// Generate the move cache if it was a hop
		if (bestPair == null || bestPair.getInitial() == null
				|| bestPair.getDestination() == null) {
			// Random non-hop move
			List<CCMove> moves = ((CCBoard) board).getLegalMoves();
			CCMove move = moves.get(0);
			// If it is a hop, we have to queue up the end of turn
			// so that we don't crash on next turn.
			if (!move.isHop()) {
				return move;
			} else {
				// Add end-of-turn
				moveCache.add(new CCMove(playerID, null, null));
				return move;
			}
		} else if (!bestPair.isHop()) {
			return new CCMove(playerID, bestPair.getInitial(),
					bestPair.getDestination());
		} else {
			generateMoveSequence((CCBoard) board, bestPair.getInitial(),
					bestPair.getDestination());
			return moveCache.remove();
		}
	}
	
	private boolean isMidGame(CCBoard board) {
		int inOwnGoalZone = 0;
		int inOtherGoalZone = 0;
		for (Point piece : board.getPieces(playerID)) {
			if (board.bases[FRIEND[playerID]].contains(piece)) {
				inOtherGoalZone++;
			} else if (board.bases[playerID].contains(piece)) {
				inOwnGoalZone++;
			}
		}
		return inOwnGoalZone <= BEGINNING_GAME_PIECES && inOtherGoalZone < END_GAME_PIECES;
	}

	private class MoveNode {
		private BoardPointPair pair;
		public MoveNode parent;

		private List<MoveNode> children;
		public int simulations;
		public int points;
		public double heuristic;

		/**
		 * @param pair
		 *            The board point pair representing this move.
		 * @param parent
		 *            The parent node (optional for root).
		 */
		public MoveNode(BoardPointPair pair, MoveNode parent) {
			this.heuristic = 0;
			this.pair = pair;
			this.parent = parent;
			this.children = new LinkedList<>();
		}

		/**
		 * Initializes the root, setting the heuristic on each child.
		 */
		private void initRoot() {
			expansion();
			for (MoveNode child : children) {
				// Give a bias to the root's children.
				child.heuristic = evaluateBoard(child.pair.getBoard(), pair.getBoard(), playerID);
			}
		}

		/**
		 * @return the best child of the noe, aka the move to play.
		 */
		private MoveNode getBestChild() {
			// Return the child with the most simulations, which by default
			// will be the most promising child.
			MoveNode bestNode = null;
			int best = Integer.MIN_VALUE;
			String content = "";
			for (MoveNode child : children) {
				content += child.points + "/" + child.simulations + ", ";
				if (child.simulations > best) {
					best = child.simulations;
					bestNode = child;
				}
			}
			System.out.println(content);
			return bestNode;
		}

		/**
		 * Performs one round of the 4 steps.
		 */
		private void performRound() {
			MoveNode current = this;
			// Select nodes until we reach a leaf.
			while (!current.isLeaf()) {
				current = current.selection();
			}
			// Expand the leaf.
			current.expansion();
			// Pick a child if any were created
			if (!current.isLeaf()) {
				current = current.selection();
			}
			int win = current.simulation();
			// Simulate a game and then backpropagate
			current.propagation(win);
		}

		/**
		 * @return the score for a given node.
		 */
		private double evaluate() {
			double bias = heuristic / (simulations + 1);
			// To avoid dividing by 0
			if (simulations == 0)
				return bias;
			return ((double) points / (simulations))
					+ bias
					+ (EXPLORATION_PARAMETER * Math.sqrt(Math
							.log(parent.simulations) / (simulations)));
		}

		/**
		 * @return true if the node is a leaf.
		 */
		private boolean isLeaf() {
			return children.size() == 0;
		}

		/**
		 * @return the best child node.
		 */
		private MoveNode selection() {
			// Go through the children, finding the one with the best value
			MoveNode bestNode = null;
			double best = -1000.0;
			double current;
			for (MoveNode child : children) {
				current = child.evaluate();
				if (current > best) {
					best = current;
					bestNode = child;
				}
			}
			return bestNode;
		}

		/**
		 * This expands all children node by converting all legal moves to a new
		 * node in the tree.
		 */
		private void expansion() {
			// If the game is over, don't expand.
			if (pair.getBoard().getWinner() != Board.NOBODY) {
				return;
			}
			MoveGenerator generator = new MoveGenerator(pair.getBoard(), pair
					.getBoard().getTurn());
			while (generator.hasNext()) {
				BoardPointPair pair = generator.next();
				children.add(new MoveNode(pair, this));
			}
		}

		/**
		 * This rolls out an entire game.
		 * 
		 * @return Points for the game
		 */
		private int simulation() {
			CCBoard gameBoard = (CCBoard) pair.getBoard().clone();

			// Do a heavy playout by generating all moves and picking the one
			// that maximizes the change in distance
			double lastProbability = 0;
			CCBoard lastBoard = null;

			// Simulate up to a fixed depth.
			for (int i = 0; i < ROLLOUT_MOVES && gameBoard.getWinner() == Board.NOBODY; i++) {
				lastProbability = random.nextDouble();
				lastBoard = (CCBoard)gameBoard.clone();
				if (lastProbability <= RANDOM_MOVE_PROBABILITY) {
					gameBoard = randomMove(gameBoard, gameBoard.getTurn());
				} else {
					gameBoard = rolloutMove(gameBoard, gameBoard.getTurn());
				}
			}

			// Return the board heuristic value.
			double score = evaluateBoard(gameBoard, pair.getBoard(), playerID);
			for (int i = 0; i < SCORE_LEVELS.length; i++) {
				if (score >= SCORE_LEVELS[i]) {
					return SCORE_REWARDS[i];
				}
			}
			// Didn't return a score, so must be on the last level.
			return SCORE_REWARDS[SCORE_REWARDS.length - 1];
			//return (gameBoard.getWinner() == gameBoard.getTeamIndex(playerID)) ? 1
			//		: ((gameBoard.getWinner() == Board.DRAW) ? 0 : -1);
		}

		/**
		 * Performs a random move.
		 * 
		 * @param startBoard
		 * @param player
		 * @return
		 */
		private CCBoard randomMove(CCBoard startBoard, int player) {
			while (startBoard.getTurn() == player) {
				List<CCMove> moves = startBoard.getLegalMoves();
				startBoard.move(moves.get(random.nextInt(moves.size())));
			}
			return startBoard;
		}

		/**
		 * Performs a non-random move based on a simple heuristic.
		 * 
		 * @param startBoard
		 * @param player
		 * @return
		 */
		private CCBoard rolloutMove(CCBoard startBoard, int player) {
			double best = -1000;
			double value;
			CCBoard bestBoard = null;
			MoveGenerator generator = new MoveGenerator(startBoard, player);
			while (generator.hasNext()) {
				BoardPointPair pair = generator.next();
				// If the move is an actual move.
				if (pair.getDestination() != null && pair.getInitial() != null) {

					// Get the move value
					value = MOVE_VALUE[pair.getInitial().x][pair.getInitial().y][pair.getDestination().x][pair.getDestination().y][player];
					if (value > best) {
						best = value;
						bestBoard = pair.getBoard();
					}
				}
			}
			// If the move hasn't changed, apply a random legal move
			if (bestBoard == null) {
				return randomMove(startBoard, player);
			} else {
				return bestBoard;
			}
		}

		/**
		 * This propagates a simulation result back up the tree based on the
		 * node's parent.
		 * 
		 * @param points
		 *            Points associated with a game
		 */
		private void propagation(int points) {
			// Iterate through parents until we get to null, backpropagating the
			// value
			MoveNode node = this;
			while (node != null) {
				node.simulations++;
				node.points += points;
				node = node.parent;
			}
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
	 * This fills the move list with a sequence of moves from one point to
	 * another.
	 * 
	 * @param board
	 *            The board.
	 * @param from
	 *            Initial starting point.
	 * @param to
	 *            End point.
	 */
	private boolean generateMoveSequence(CCBoard board, Point from, Point to,
			Set<Point> visitedPoints, List<Point> points) {
		// If we've reached the end, then generate all the moves
		if (from.equals(to)) {
			Point lastPoint = null;
			for (Point p : points) {
				if (lastPoint != null) {
					moveCache.add(new CCMove(playerID, lastPoint, p));
				}
				lastPoint = p;
			}

			// Add last move
			moveCache.add(new CCMove(playerID, null, null));
			return true;
		}

		for (int[] offset : DIRECTIONAL_OFFSETS) {
			Point hopPoint = new Point(from.x + offset[0], from.y + offset[1]);
			if (board.getPieceAt(hopPoint) == null) {
				continue;
			}

			Point newPoint = new Point(from.x + offset[0] * 2, from.y
					+ offset[1] * 2);
			// If in range, not visited, and empty
			if (newPoint.x >= 0 && newPoint.y >= 0 && newPoint.x < 16
					&& newPoint.y < 16) {
				if (board.getPieceAt(newPoint) == null
						&& !visitedPoints.contains(newPoint)) {
					// Test that if you are going out of the friend's base,
					// which is illegal
					if (!CCBoard.bases[FRIEND[playerID]].contains(from)
							|| CCBoard.bases[FRIEND[playerID]]
									.contains(newPoint)) {
						visitedPoints.add(newPoint);
						// Visit the point
						points.add(newPoint);
						if (generateMoveSequence(board, newPoint, to,
								visitedPoints, points)) {
							return true;
						}
						points.remove(points.size() - 1);
					}
				}
			}
		}
		return false;
	}
	

	public double evaluateBoard(CCBoard board, CCBoard originalBoard, int context) {
		// If someone is winning, return extra large score.
		if (board.getWinner() == CCBoard.getTeamIndex(context)) {
			return 10000;
		} else if (board.getWinner() != CCBoard.NOBODY && board.getWinner() != CCBoard.DRAW) {
			return -10000;
		}

		// Sum up the result for all features
		double result = 0;
		for (Feature feature : features) {
			result += (feature.getWeight(board, originalBoard, context) * feature.getScore(board, originalBoard, context));
		}
		
		return result;
	}


}
