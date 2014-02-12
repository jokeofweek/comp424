package boardgame;
import boardgame.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** Abstract class to run human client with GUI. Subclasses 
 * should add a default
 * constructor to pass the correct board type to this class.
 *
 * The server must be run with a large timeout to leave humans time to play!
 */
abstract public class HumanPlayer extends Player 
    implements BoardPanel.BoardPanelListener {
    final BoardPanel thePanel;
    final JFrame theFrame;
    final JLabel theLabel;
    private boolean windowClosed = false;
    private Move myMove = null; 
    private boolean moveNeeded = false;
    private Thread clientThread = null;

    public HumanPlayer( Board b ) { 
	super( "Human" ); 
	theFrame = new JFrame( "Boardgame Client" );
	theFrame.getContentPane().
	    setLayout( new BoxLayout(theFrame.getContentPane(), 
				     BoxLayout.Y_AXIS) );
	thePanel = b.createBoardPanel();
	thePanel.setPreferredSize( new Dimension(400, 400) );
	theLabel = new JLabel("Connecting...");
	theFrame.getContentPane().add( thePanel );
      	theFrame.getContentPane().add( theLabel );
	thePanel.setCurrentBoard( b );
	theFrame.addWindowListener( new WindowAdapter() {
		public void WindowClosed( WindowEvent e ) {
		    windowClosed = true;
		    cancelMoveRequestThread();
		} } );				  
	theFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	theFrame.pack();
	theFrame.setVisible(true);
	setName(JOptionPane.showInputDialog( theFrame, 
					     "Enter your name:", 
					     "Human" ));
	theFrame.setTitle( "Boardgame Client: " + this.getName() );
    }

    /* Called by client threads when user input is needed */
    synchronized public Move chooseMove(Board b) {
	if( windowClosed ) return null; // This will terminate the client
	if( moveNeeded ) 
	    throw new IllegalStateException( "Concurrent chooseMove() calls");
	clientThread = Thread.currentThread();
	moveNeeded = true;
	// Have the dispatch thread tell the GUI we're waiting
	// for a move
	final HumanPlayer myself = this;
	EventQueue.invokeLater( new Runnable() {
		public void run() { 
		    theLabel.setText( "Please enter a move..." );
		    thePanel.requestMove(myself); 
		} } );
	// Sleep until we have the move
	while( moveNeeded && myMove  == null )
	    try { this.wait(); } 
	    catch (InterruptedException e ) {}
	moveNeeded = false;
	Move theMove = myMove; myMove = null;
	clientThread = null;
	return theMove;
    } 

    /* Called by client thread when a move is received from the server */
    public void movePlayed(final Board board, final Move m) {
	final HumanPlayer thisPointer = this;
	EventQueue.invokeLater( new Runnable() {
		public void run() {
		    if( m.getPlayerID() == thisPointer.getColor() ) 
			theLabel.setText( "Played " + m.toPrettyString() );
		    else
			theLabel.setText( "Received " + m.toPrettyString() );
		    thePanel.setCurrentBoard( board );
		} } );
	System.out.println( m.toPrettyString() );
    }
    
    /* Callback by the AWT dispatch thread */
    synchronized public void moveEntered(Move m) {
	if( !moveNeeded ) {
	    System.err.println( "BoardPanel sent unrequested move!" );
	    return;
	}
	theLabel.setText( "Played " + m.toPrettyString() );
	myMove = m; // Set the move
	notify(); 
    }

    /* Interrupt the thread waiting for a move if the game is 
     * ended */
    synchronized void cancelMoveRequestThread() {
	if( clientThread != null ) {
	    moveNeeded = false;
	    clientThread.interrupt();
	}
    }

    public void gameStarted( final String msg ) { 
	super.gameStarted(msg); 
	final HumanPlayer thisPointer = this;
	EventQueue.invokeLater( new Runnable() {
		public void run() {
		    theLabel.setText("Starting: " + msg); 
		    theFrame.setTitle("Boardgame Client: " +
				      thisPointer.thePanel.
				      getCurrentBoard().getNameForID
				      ( thisPointer.getColor() ) +
				      " / " + thisPointer.getName());
		} } ); }

    public void gameOver( final String msg ) { 
	super.gameOver(msg); 
	EventQueue.invokeLater( new Runnable() {
		public void run() {
		    theLabel.setText("Game over: " + msg); } } ); }
} // End class HumanPlayer

