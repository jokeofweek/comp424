package halma;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

/**
 *A random Halma player.
 */
public class CCAIPlayer extends Player {
    private boolean verbose = false;
    Random rand = new Random();
    
    /** Provide a default public constructor */
    public CCAIPlayer() { super("AI Player"); }
    public CCAIPlayer(String s) { super(s); }
    
    public Board createBoard() { return new CCBoard(); }

    /** Implement a very stupid way of picking moves */
    public Move chooseMove(Board theboard) 
    {
    	System.out.println("\nPlaying move.\n");
        // Cast the arguments to the objects we want to work with

        CCBoard board = (CCBoard) theboard;

        // Get the list of legal moves.
        
        ArrayList<CCMove> moves = board.getLegalMoves();

        // Otherwise, return a randomly selected move.

        return (CCMove) moves.get(rand.nextInt(moves.size()));
    }
    
} // End class
