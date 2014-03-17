package halma;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

/**
 *A random Halma player.
 */
public class CCSimpleAIPlayer extends Player {
    private final static int[] FRIEND_ID = {3,2,1,0};
    
    private Set<Point> movePoints = new HashSet<>();
    
    private final static Point[] END_POINTS = {
    	new Point(CCBoard.SIZE - 1, CCBoard.SIZE - 1),
    	new Point(0, CCBoard.SIZE - 1),
    	new Point(CCBoard.SIZE - 1, 0),
    	new Point(0, 0)
    };
    
    private final static int[][] DIRECTIONAL_PROGRESS_OFFSETS = {
    	{0,1},{1,1},{1,0}
    };
    
    private final static int[][] DIRECTIONAL_OFFSETS = {
    	{0,1},{1,1},{1,0},
    	{0,-1},{1,-1},{-1, -1},{-1,1},{-1,0}
    };
            
    private final static int[][] OFFSETS = {
    	{1, 1},
    	{-1, 1},
    	{1, -1},
    	{-1, -1}
    };
    
    private final static Point[] GOAL_POINTS = {
    	new Point(CCBoard.SIZE - 1, CCBoard.SIZE - 1),
    	new Point(0, CCBoard.SIZE - 1),
    	new Point(CCBoard.SIZE - 1, 0),
    	new Point(0, 0)
    };
    
    /** Provide a default public constructor */
    public CCSimpleAIPlayer() { super("Simple AI Player"); }
    public CCSimpleAIPlayer(String s) { super(s); }
    
    
    public Board createBoard() { return new CCBoard(); }
        
    /** Implement a very stupid way of picking moves */
    public Move chooseMove(Board theboard) 
    {
    	long startTime = System.currentTimeMillis();

        // Cast the arguments to the objects we want to work with
        CCBoard board = (CCBoard) theboard;

        // Generate all possible moves, adding them to the queue
                
        CCMove move = null;
        int bestScore = Integer.MIN_VALUE;
        int score;
        for (CCMove testMove : board.getLegalMoves()) {
        	if (!movePoints.contains(testMove)) {
        		score = getMoveScore(testMove, board);
        		if (score > bestScore || (score == bestScore && Math.random() > 0.5)) {
        			bestScore = score;
        			move = testMove;
        		}
        	}
        }
 
    	if (move.isHop()) {
    		movePoints.add(move.to);
    	} else {
    		movePoints = new HashSet<>();
    	}

    	System.out.println("Move length: " + (System.currentTimeMillis() - startTime));
        return move;
    }
    
    private int getMoveScore(CCMove m, CCBoard board) {
		// Empty move
		if (m.from == null && m.to == null) {
			return 0;
		}

		int progress = getProgress(m);
		int removedFromBase = (CCBoard.bases[playerID].contains(m.from) && !CCBoard.bases[playerID].contains(m.to)) ? 1 : 0;
		int stillInBase = CCBoard.bases[playerID].contains(m.to) ? 1 : 0;
		int joinFriendBase = (!CCBoard.bases[FRIEND_ID[playerID]].contains(m.from) && CCBoard.bases[FRIEND_ID[playerID]].contains(m.to)) ? 1 : 0;
		int leaveFriendBase = (CCBoard.bases[FRIEND_ID[playerID]].contains(m.from) && !CCBoard.bases[FRIEND_ID[playerID]].contains(m.to)) ? 1 : 0;
		int canHopAfter = canHopAfterAndProgress(m.to, board) ? 1 : 0;
		int bestHopSequence = (m.isHop() ? getClosestHopSequence(m.to, board) : 0);


		int progressWeight = 5;
		int removedWeight = board.getTurnsPlayed() / 3;
		int stillInBaseWeight = board.getTurnsPlayed() / 3;
		int joinFriendBaseWeight = 0;
		int leaveFriendBaseWeight = 0;
		int bestHopSequenceWeight = board.getTurnsPlayed() / 20;

		// If we are no longer in beginning game
		if (board.getTurnsPlayed() > 100) {
			progressWeight = 2;
			joinFriendBaseWeight = 20;
			removedWeight = 0;
			leaveFriendBaseWeight = -5;
			stillInBaseWeight = -1000;
		}

		return (progress * progressWeight) +
				(removedFromBase * removedWeight) +
				(stillInBase * stillInBaseWeight) +
				(joinFriendBase * joinFriendBaseWeight) +
				(leaveFriendBase * leaveFriendBaseWeight) + 
				(bestHopSequence * bestHopSequenceWeight);
	}
    
    private boolean canHopAfterAndProgress(Point p, CCBoard board) {
    	for (int[] offset : DIRECTIONAL_PROGRESS_OFFSETS) {
    		if (board.getPieceAt(new Point(p.x + (OFFSETS[playerID][0] * offset[0]), 
    				p.y + (OFFSETS[playerID][1] * offset[1]))) != null) {
    			Point hopDest = (new Point(p.x + 2 * (OFFSETS[playerID][0] * offset[0]),
    					p.y + 2 * (OFFSETS[playerID][1] * offset[1])));
    			// Only count it if the hop would reduce the distance
    			if (board.getPieceAt(hopDest) == null && hopDest.x >= 0 && hopDest.y >= 0
    					&& hopDest.x < CCBoard.SIZE && hopDest.y < CCBoard.SIZE
    					&& manhattanDistance(p, GOAL_POINTS[playerID]) - manhattanDistance(hopDest, GOAL_POINTS[playerID]) > 0) {
    				
    				
    				return true;
    			}
    		}
    	}
    	return false;
    }

    private int manhattanDistance(Point from, Point to) {
    	return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
    }
    
    private int getClosestHopSequence(Point start, CCBoard board) {
    	Set<Point> visited = new HashSet<>();
    	
    	int initial = manhattanDistance(start, GOAL_POINTS[playerID]);
    	int bestDiff = 0;
    	
    	Queue<Point> points = new LinkedList<>();
    	points.add(start);
    	while (!points.isEmpty()) {
    		Point p = points.remove();
    		if (!visited.contains(p)) {
    			visited.add(p);
    			// Test all directions for possible hops
    			for (int[] offset : DIRECTIONAL_OFFSETS) {
	    			if (board.getPieceAt(new Point(p.x + offset[0], 
	        				p.y + offset[1])) != null) {
	        			Point hopDest = (new Point(p.x + 2 * offset[0],
	        					p.y + 2 *  offset[1]));
	        			// Only count it if the hop would reduce the distance
	        			if (board.getPieceAt(hopDest) == null && hopDest.x >= 0 && hopDest.y >= 0
	        					&& hopDest.x < CCBoard.SIZE && hopDest.y < CCBoard.SIZE) {
	        				points.add(hopDest);
	        			}
	        		}
    			}
    			
    			// Update best diff if we found a better point
    			int diff = initial - manhattanDistance(p, GOAL_POINTS[playerID]);
    			if (diff > bestDiff) {
    				diff = bestDiff;
    			}
    		}
    	}
    	
    	return bestDiff;
    }
    
	private int getProgress(CCMove m) {
		int ret = 0;
		int xOffset = m.to.x - m.from.x;
		int yOffset = m.to.y - m.from.y;

		if (OFFSETS[playerID][0] * xOffset > 0) ret += Math.abs(xOffset);
		if (OFFSETS[playerID][0] * xOffset < 0) ret -= Math.abs(xOffset);
		if (OFFSETS[playerID][1] * yOffset > 0) ret += Math.abs(yOffset);
		if (OFFSETS[playerID][1] * yOffset < 0) ret -= Math.abs(yOffset);

		return 2 * ret;

	}
} // End class