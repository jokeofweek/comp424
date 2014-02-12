package boardgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * Boardgame player client code. Do not modify this class, implement
 * Player subclass instead.
 */
public class Client implements Runnable {
    protected static final String DEFAULT_SERVER = "localhost";
    protected static final int DEFAULT_PORT = Server.DEFAULT_PORT;
    protected static final String DEFAULT_PLAYER = "halma.CCHumanPlayer";
    protected static final boolean DBGNET = false;
    
    private Socket socket;
    private PrintWriter sockOut;
    private BufferedReader sockIn;
    private String serverName;
    private int serverPort;
    
    Player player;
    int playerID;
    Board board;
    boolean gameOver = false;

    private static void printUsage() {
        System.err.println( 
            "Usage: java boardgame.Client [PlayerClass [serverName [serverPort]]]\n" +
            "  Where PlayerClass is the player to be run (default=" + DEFAULT_PLAYER + "\n" + 
            "        serverName is the server address (default=" + DEFAULT_SERVER + ") and\n" +
            "        serverPort is the port number (default="+DEFAULT_PORT+").\n" +
            "  e.g.\n" +
            "  java boardgame.Client halma.CCRandomPlayer localhost " + DEFAULT_PORT );
    }
    
    public static void main(String[] args) {
        if( args.length > 3 ) {
            printUsage(); return;
        } else {
            Player p;
            try {
                Class cl = Class.forName(args.length > 0 ? args[0] : DEFAULT_PLAYER);
                java.lang.reflect.Constructor co = cl.getConstructor(new Class[0]);
                p = (Player) co.newInstance(new Object[0]);
            } catch (Exception e) {
                System.err.println( "Failed to create Player object: " + e);
                printUsage();
                return;
            }
            Client client;
            try {
                client = new Client(p, 
                    args.length > 1 ? args[1] : DEFAULT_SERVER, 
                    args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_PORT);
            } catch(Exception e) { printUsage(); return; }
            client.run();
        }
    }
    
    public Client( Player p, String svr, int pt ) {
        this.board = p.createBoard();
        this.player = p;
        this.serverName = svr;
        this.serverPort = pt;
    }
    
    public void run() {
        if( connect() ) clientLoop();
    }
    
    /** Process message received from server. */
    protected void processMessage( String msg ) {
        if( msg.startsWith( "GAMEOVER" ) ) {
            player.gameOver( msg );
            this.gameOver = true;
        } else if( msg.startsWith( "PLAY") ){ // My turn
            if( board.getTurn() != playerID ||
                    board.getWinner() != Board.NOBODY )
                System.err.println( "Ignoring request for move since not my turn.");
            else
                playMove();
        } else {
            // Expect a move 
            Move m;
            try{ m = board.parseMove(msg); }
            catch( Exception e ) {
                System.err.println( "Ingnoring unparseable move from server" +
                        ": " + msg );
                return; }
            try { board.move(m); }
            catch( Exception e ) {
                System.err.println( "Failed executing move from server" +
                        ": " + msg );
                e.printStackTrace();
                return; }
            player.movePlayed( (Board)board.clone(), m );
        }
    }
    
    protected void playMove() {
        Move myMove = null;
        try { 
	    myMove = player.chooseMove( (Board)board.clone() );
	    if( myMove == null) {
		System.err.println( "ABORTING: Player didn't return a move. Nothing to send to server!" );
		return;
	    }
	} catch( Exception e ) {
            System.err.println( "ABORTING: Exception in " + 
				player.getClass().getName() + 
				".choseMove()" );
            e.printStackTrace();
            gameOver = true;
            return;
        }
        try { String msg = myMove.toTransportable(); 
              sockOut.println(msg); 
              if( DBGNET )
                System.out.println( player.getColor() + "< " + msg );}
        catch( Exception e ) {
            System.err.println( "Error sending move to server: " );
            e.printStackTrace();
            gameOver = true;
        }
    }
    
    /** Connect to a server. This blocks until the game starts. */
    protected boolean connect() {
	System.out.println( "Connecting to " + serverName + ":" + serverPort + "... " );
        try {
            socket = new Socket(serverName, serverPort);
            sockOut = new PrintWriter(
                        socket.getOutputStream(), true);
            sockIn = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
            
            // Send the start message to the server and wait for reply
            sockOut.println("START " + player.getName());
            if( DBGNET )
                System.out.println( player.getColor() + "< START " + player.getName() );
	    System.out.println( "Connected. Waiting for game to start..." );
            String msg = null;
            while (msg == null || !msg.startsWith("START")) {
                msg = sockIn.readLine(); //Waits for server response.
                if( DBGNET )
                System.out.println( player.getColor() + "> " + msg );
            }
            // Set the colour
            String str = msg.substring(6);
            String clr = str.substring(0,str.indexOf(' '));
            player.setColor(playerID = board.getIDForName(clr));
            player.gameStarted( msg );
            return true;
        } catch(Exception e) {
            System.err.println( "Failed to connect: " );
            e.printStackTrace();
            return false;
        }
    }
    
    /** Pump messages from the server */
    protected void clientLoop() {
        String inputLine;
        try {
            while (!gameOver) {
                // Blocking read
                inputLine = sockIn.readLine();
                if( inputLine == null ) continue;
                if( DBGNET )
                System.out.println( player.getColor() + "> " + inputLine );
                processMessage(inputLine);
            }
        } catch(IOException e) { 
            System.err.println( "Connection error: " + e );
            e.printStackTrace();
            player.gameOver( "CONNECTION ERROR " + e );
        } finally { try{socket.close();} catch(Exception e) {} }
    }

} // End class Client
