package halma;

import halma.minimax.features.AdjacentToBaseFeature;
import halma.minimax.features.DontLeaveAloneFeature;
import halma.minimax.features.Feature;
import halma.minimax.features.LeaveBaseFeature;
import halma.minimax.features.ManhattanDistanceFeature;
import halma.minimax.features.NotInOpposingBaseFeature;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

public class CCSimulatedAnnealingPlayer extends Player {


    /**
     * The enabled features.
     */
    protected List<Feature> features = Arrays.asList(
    		(Feature)new ManhattanDistanceFeature(0.8),
    		new LeaveBaseFeature(0.002),
    		new DontLeaveAloneFeature(0.01),
    		new NotInOpposingBaseFeature(0.02),
    		new AdjacentToBaseFeature(0.05) 
    );

    private static final int TOTAL_TIME_ALLOWED = 200;
	private static final int[][] OPPONENT = {{1,2},{0,3},{0,3},{1,2}};
	private Random random = new Random();
	private CCBoard originalBoard;
	private LinkedList<CCMove> moveQueue = new LinkedList<>();
    
	public CCSimulatedAnnealingPlayer() {
		super("Simulated Annealing");
	}

	@Override
	public Move chooseMove(Board board) {
		long maximumTime = System.currentTimeMillis() + TOTAL_TIME_ALLOWED;
		// If we have moves left in the queue, play those
		if (!moveQueue.isEmpty()) {
			return moveQueue.removeFirst();
		}
		
		originalBoard = (CCBoard) board;
		
		// Generate a bunch of random moves and anneal.
		List<CCMove> legalMoves = originalBoard.getLegalMoves();
		Pair<Double, LinkedList<CCMove>> best = new Pair(Integer.MIN_VALUE + 0.0, null);
		Pair<Double, LinkedList<CCMove>> current;
		
		int totalMoves = legalMoves.size();
		while (System.currentTimeMillis() < maximumTime) {
			// Pick a random move
			CCMove firstMove = legalMoves.get(random.nextInt(totalMoves));
			// Apply the move
			CCBoard firstBoard = (CCBoard)originalBoard.clone();
			firstBoard.move(firstMove);
			// If the move wasn't a hop, simply eval, else anneal.
			if (!firstMove.isHop()) {
				LinkedList<CCMove> moves = new LinkedList<>();
				moves.add(firstMove);
				current = new Pair<Double, LinkedList<CCMove>>(evaluateBoard(firstBoard), moves);
			} else {
				current = anneal(firstMove, firstBoard);
			}
			// If we did better then best, then keep that
			if (current.first > best.first) {
				best = current;
			}
			
		}
		
		moveQueue = best.second;
		return moveQueue.removeFirst();
	}
	
	public Pair<Double, LinkedList<CCMove>> anneal(CCMove move, CCBoard board) {
		LinkedList<CCMove> moves = new LinkedList<>();
		LinkedList<CCMove> bestMoves = new LinkedList<>();
		moves.add(move);
		bestMoves.add(move);

        // Set initial temp
        double temp = 10000;
        // Cooling rate
        double coolingRate = 0.003;

        double currentScore = evaluateBoard(board);
        double bestScore = evaluateBoard(board);
        
        while (temp > 1) {
        	// Pick a random move
        	List<CCMove> legalMoves = board.getLegalMoves();
        	
        	// If no legal moves, break out
        	if (legalMoves.isEmpty()) {
        		break;
        	}
        	
        	CCMove potentialMove = legalMoves.get(random.nextInt(legalMoves.size()));
        	CCBoard potentialBoard = (CCBoard)board.clone();
        	potentialBoard.move(potentialMove);
        	
        	// Get new score
        	double newScore = evaluateBoard(potentialBoard);
        	
        	// First we test if we should move to it.
        	if (newScore > currentScore || Math.exp((newScore - currentScore) / temp) > random.nextDouble()) {
        		currentScore = newScore;
        		moves.add(potentialMove);
        		board = potentialBoard;
        	}
        	
        	// Now we test if we've beat our best.
        	if (newScore > bestScore) {
        		bestMoves = new LinkedList<>();
        		bestMoves.addAll(moves);
        		bestScore = newScore;
        	}

        	// If this move was an end of turn, we break out
        	if (potentialMove.from == null || potentialMove.to == null) {
        		break;
        	}
            // Cool system
            temp *= 1-coolingRate;
        }
		
		return new Pair<Double, LinkedList<CCMove>>(bestScore, bestMoves);
	}
	
	
	public double evaluateBoard(CCBoard board) {
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
	
	
	
}
