package halma;
import boardgame.*;

/** Human Halma client with GUI.
 *
 *  To run, set a large timeout on the server so the 
 *  human player has time to enter a move.
 */
public class CCHumanPlayer extends HumanPlayer {

    public CCHumanPlayer() { super( new CCBoard() ); } 
    public Board createBoard() { return new CCBoard(); }
}

