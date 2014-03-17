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
public class CCManhattanOptimizerPlayer extends Player {
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
    public CCManhattanOptimizerPlayer() { super("Simple AI Player"); }
    public CCManhattanOptimizerPlayer(String s) { super(s); }
    
    private boolean isHopping = false;
    
 
    public Board createBoard() { return new CCBoard(); }
        
    /** Implement a very stupid way of picking moves */
    public Move chooseMove(Board theboard) 
    {
    	long startTime = System.currentTimeMillis();

        // Cast the arguments to the objects we want to work with
        CCBoard board = (CCBoard) theboard;

        // Gerate moves, trying to find best distance change
        CCMove bestMove = null;

        if (isHopping) {
        	int longestDist = 0;
        	bestMove = new CCMove(playerID, null, null);
            for (CCMove testMove : board.getLegalMoves()) {
            	int dist = getClosestHopSequence(testMove.from, board);
            	if (dist > longestDist || (dist == longestDist && Math.random() > 0.5)) {
            		longestDist = dist;
            		bestMove = testMove;
            	}
            }
        } else {
        	int longestDist = 0;
        	        	
            for (CCMove testMove : board.getLegalMoves()) {
            	int dist = 0;
            	if (testMove.isHop()) {
            		dist = getClosestHopSequence(testMove.to, board);
            		
            	} else {
            		dist = manhattanDistance(testMove.from, GOAL_POINTS[playerID]) - manhattanDistance(testMove.to, GOAL_POINTS[playerID]);
            	}
            	if (dist > longestDist || (dist == longestDist && Math.random() > 0.5)) {
            		longestDist = dist;
            		bestMove = testMove;
            	}
            }
        }
        
        if (bestMove.from != null && bestMove.isHop()) {
        	isHopping = true;
        } else {
        	isHopping = false;
        }
 
        return bestMove;
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
	        					p.y + 2 * offset[1]));
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
} // End class