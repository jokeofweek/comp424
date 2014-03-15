package halma;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

/**
 *A random Halma player.
 */
public class CCAIPlayer extends Player {
    private boolean verbose = false;
    Random rand = new Random();
    
    private final Point[][] directionOffsets = new Point[][]{
    		{new Point(1,0), new Point(1,1), new Point(0,1)}, // top left
    		{new Point(-1,0), new Point(-1,1), new Point(0,1)}, // bottom left
    		{new Point(1,0), new Point(1,-1), new Point(0,-1)}, // top right
    		{new Point(-1,0), new Point(-1,-1), new Point(0,-1)} // bottom right
    };
    
    /** Provide a default public constructor */
    public CCAIPlayer() { super("AI Player"); }
    public CCAIPlayer(String s) { super(s); }
    
    public Board createBoard() { return new CCBoard(); }
    
    private CCMove lastMove;
    
    /** Implement a very stupid way of picking moves */
    public Move chooseMove(Board theboard) 
    {
    	System.out.println("Entering choose move.");
    	// For now, if our last move was a hop, end the turn
    	if (lastMove != null && lastMove.isHop()) {
    		CCMove endMove = new CCMove(playerID, lastMove.to, null);
    		lastMove = null;
    		System.out.println("Early end - " + endMove);
    		return endMove;
    	}
    	
        // Cast the arguments to the objects we want to work with
        CCBoard board = (CCBoard) theboard;
        
        // Get your pieces
        ArrayList<Point> pieces = board.getPieces(playerID);

        // Generate all possible moves, adding them to the queue
        PriorityQueue<CCMove> moves = new PriorityQueue<>(50, MoveComparator.INSTANCE);
        
        for (Point piece : pieces) {
        	Point[] offsets = directionOffsets[playerID];
        	for (Point offset : offsets) {
        		CCMove move = null;
        		// Check if it is a hop
        		Point firstMove = new Point(piece.x + offset.x, piece.y + offset.y);
        		if (board.getPieceAt(firstMove) != null) {
        			Point secondMove = new Point(firstMove.x + offset.x, firstMove.y + offset.y);
        			// Only register the move if there is no second move
        			if (board.getPieceAt(secondMove) == null) {
        				move = new CCMove(playerID, piece, secondMove);
        				System.out.println("HOP - " + MoveComparator.INSTANCE.getMoveScore(move) + " - " + move.toPrettyString());
        			}
        		} else {
        			// Add it in as a move
        			move = new CCMove(playerID, piece, firstMove);
        		}
        		
        		if (move != null && board.isLegal(move)) {
        			System.out.println("Adding move");
        			moves.add(move);
        		}
        	}
        }
                
        
        // Get the first moves
        lastMove = moves.remove();
        System.out.println("Picking " + lastMove);
        return lastMove;
    }
    
    private static class MoveComparator implements Comparator<CCMove> {
    	public static final MoveComparator INSTANCE = new MoveComparator();
    	private int getMoveScore(CCMove m) {
    		return (Math.abs(m.from.x - m.to.x) + Math.abs(m.from.y - m.to.y));
    	}
    	@Override
    	public int compare(CCMove o1, CCMove o2) {
    		System.out.println("Move: " + o1.toPrettyString() + "(" + getMoveScore(o1));
    		System.out.println("Move: " + o2.toPrettyString() + "(" + getMoveScore(o2));
    		return getMoveScore(o2) - getMoveScore(o1);
    	}
    }
    
} // End class
