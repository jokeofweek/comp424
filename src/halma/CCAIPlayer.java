package halma;

import java.awt.Point;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

/**
 *A random Halma player.
 */
public class CCAIPlayer extends Player {
    private final static int[] FRIEND_ID = {3,2,1,0};
    
    private Set<Point> movePoints = new HashSet<>();
    
    private final static Point[] END_POINTS = {
    	new Point(CCBoard.SIZE - 1, CCBoard.SIZE - 1),
    	new Point(0, CCBoard.SIZE - 1),
    	new Point(CCBoard.SIZE - 1, 0),
    	new Point(0, 0)
    };
        
    private final static int[][] OFFSETS = {
    	{1, 1},
    	{-1, 1},
    	{1, -1},
    	{-1, -1}
    };
    
    /** Provide a default public constructor */
    public CCAIPlayer() { super("AI Player"); }
    public CCAIPlayer(String s) { super(s); }
    
    
    public Board createBoard() { return new CCBoard(); }
        
    /** Implement a very stupid way of picking moves */
    public Move chooseMove(Board theboard) 
    {
    	System.out.println(playerID + " is choosing a move.");

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

    	System.out.println(playerID + " chose " + move.toPrettyString());
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
		
		int progressWeight = 5;
		int removedWeight = board.getTurnsPlayed() / 3;
		int stillInBaseWeight = board.getTurnsPlayed() / 3;
		int joinFriendBaseWeight = 0;
		int leaveFriendBaseWeight = 0;
		
		// If we are no longer in beginning game
		if (board.getTurnsPlayed() > 100) {
			progressWeight = 5;
			joinFriendBaseWeight = 20;
			removedWeight = 0;
			leaveFriendBaseWeight = -5;
			stillInBaseWeight = -1000;
		}
		
		return (progress * progressWeight) +
				(removedFromBase * removedWeight) +
				(stillInBase * stillInBaseWeight) +
				(joinFriendBase * joinFriendBaseWeight) +
				(leaveFriendBase * leaveFriendBaseWeight);
	}
    

	private int getProgress(CCMove m) {
		int ret = 0;
		int xOffset = m.to.x - m.from.x;
		int yOffset = m.to.y - m.from.y;
		
		if (OFFSETS[playerID][0] * xOffset > 0) ret += Math.abs(xOffset);
		if (OFFSETS[playerID][0] * xOffset < 0) ret -= 555;
		if (OFFSETS[playerID][1] * yOffset > 0) ret += Math.abs(yOffset);
		if (OFFSETS[playerID][1] * yOffset < 0) ret -= 555;

		return 2 * ret;
		
	}
} // End class
