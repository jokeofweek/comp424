package boardgame;

import halma.CCBoard;


/**
 * Base class for a board game player. Do not modify this class, 
 * extend it instead. Override the chooseMove() method to pick
 * moves to play.
 * 
 * The client software needs to know what board to use, and
 * uses the createBoard() method for this. The default implementation
 * creates a BreakThrough board, and needs only be changed to 
 * play a different game. This should be the same board class
 * as used by the server.
 *
 * The client software maintains a copy of the board and updates
 * it as moves are received from the server.
 * 
 * Implementations must provide a default _public_ constructor taking no
 * arguments. 
 *
 * Initialization should be done by overriding the 
 * gameStarted() method, since the player's color is not known
 * during construction. Override the gameOver() method for any
 * cleanup tasks. You may also override the movePlayed() method to
 * be notified whenever a move is received from the server.
 * 
 * See the RandomBTPlayer class for an example implementation.
 */
public abstract class Player {
    private String name;
    protected int playerID;
    
    /** Constructor: the name is used to identify the player to
     *  the server.*/
    public Player( String name ) { setName(name); }
    
    /** Return the assigned color of the player. */
    final public    int getColor() { return this.playerID; }
    
    /** Return the name assigned to this player. */
    final public String getName() { return this.name; }
    
    /** Called when a START message is retruned from the server. The 
     * player is now initialized (it's color has been set).
     * @param msg The START message received from the server.
     */
    public void gameStarted( String msg ) { 
	System.out.println( "Game started: " + msg ); }
    
    /** Called when a move is received from the server. This includes
     *	the moves made in chooseMove() which are echoed back (possibly
     *	modified!!!, depending on the game rules) from the server.
     * @param board The current board state. This is a copy of the 
     * board maintained by the server, so it may be modified.
     * @param move The move received from the server.
     */
    public void movePlayed( Board board, Move move ) {
	System.out.println( "Move: " + move.toPrettyString() ); }

    /** Implement this method to provide the moves to be played. This
     * method is called whenever a move must be chosen, i.e. on this
     * player's turn.
     * 
     * @param board The current board state. This is a copy of the 
     * board maintained by the server, so it may be modified.
     * @return the move to be sent to the server.
     */
    abstract public Move chooseMove(Board board);
    
    /** Called when a GAMEOVER message is received from the server.
     * @param msg The "GAMEOVER" message or an error message if there was
     * an unexpected disconnection. 
     */
    public void gameOver( String msg ) { 
	System.out.println( "Game ended: " +  msg ); }
    
    /** Return an initialized board for the game to be played.
     * Default implementation returns a BreakThrough board. Override
     * this method to play a different game.
     * @return an initialized game board.
     */
    public Board createBoard() { return new CCBoard(); }
    
    /* Package-level accessors for client software */
    public void setColor( int c ) { this.playerID = c; }
    public void setName( String s ) { 
	this.name = s.length()<=0 ? "<none>": s; }
} // End class
