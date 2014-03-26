package halma;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import boardgame.Board;
import boardgame.BoardPanel;
import boardgame.Move;

public class CCBoard extends Board{

	public final static int NUMBER_OF_PLAYERS=4;
	public final static int SIZE = 16;

	private static final int MAX_TURN=5000;
	private static final int MAX_BASE_TURN=100;

	private static final int[][] moves= {{1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}, {0,1}};

	final static Point[] basePoints={new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0),
		new Point(0,1), new Point(1,1), new Point(2,1), new Point(3,1),
		new Point(0,2), new Point(1,2), new Point(2,2),
		new Point(0,3), new Point(1,3)};

	public final static HashSet<Point>[] bases= initializeBases();

	private int turnNumber;
	private int winner= NOBODY;
	private int turn_player;
	private Point lastMovedInTurn;
	public HashMap<Point, Integer> board= new HashMap<Point, Integer>(40);


	private CCBoard(HashMap<Point, Integer> board, int turnNumber, int winner,
			int turn_player, Point lastMovedInTurn) {
		super();
		this.board = board;
		this.turnNumber = turnNumber;
		this.winner = winner;
		this.turn_player = turn_player;
		this.lastMovedInTurn = lastMovedInTurn;
	}


	public CCBoard() {
		turnNumber=0;
		turn_player=0;
		lastMovedInTurn=null;
		initializeBoard();
	}


	private void initializeBoard() {
		for(int i=0; i<4; i++){
			for(int j=0; j<basePoints.length; j++){
				Point p= new Point( (i%2==0)?basePoints[j].x:SIZE-basePoints[j].x-1, 
						((i>>1)%2==0)?basePoints[j].y:SIZE-basePoints[j].y-1);
				board.put(p, i);
			}
		}
	}


	@Override
	public int getWinner() {
		if(turnNumber >= MAX_TURN)
			return DRAW;
		return winner;
	}

	/**
	 * Generate a set of all points in the bases
	 * @return a set of all points in the bases
	 */
	private static HashSet<Point>[] initializeBases() {
		HashSet<Point>[] b= new HashSet[4];
		for(int i=0; i<4; i++){
			b[i] = new HashSet<Point>();
			for(int j=0; j<basePoints.length; j++){
				Point p= new Point( (i%2==0)?basePoints[j].x:SIZE-basePoints[j].x-1, 
						((i>>1)%2==0)?basePoints[j].y:SIZE-basePoints[j].y-1);
				b[i].add(p);
			}
		}


		return b;
	}

	/**
	 * Check if a player has a piece in a certain base
	 * @param player_id the id of the player to check
	 * @param base_id the id of the base to check
	 * @return true if any piece belonging to player_id is in base_id
	 */
	private boolean checkIfInBase(int player_id, int base_id){
		boolean in=false;
		Integer IDInteger= new Integer(player_id);
		for(Point p: bases[player_id]){
			in |= IDInteger.equals(board.get(p));
		}
		return in;
	}

	private void updateWinner(int player_id){
		if(turnNumber >= MAX_BASE_TURN){
			// if either player of a team is still in the wrong base, then opposing team wins
			boolean inWrongBase = false;
			for(int i=0; i<4; i++){
				if(i!= (player_id^3))
					inWrongBase |= checkIfInBase(player_id, i);
			}
			if(inWrongBase){
				winner= (getTeamIndex(player_id)+1)%2;
			}

		}
		// check if this team is winning
		if(checkIfWin(player_id) && checkIfWin(player_id^3))
			winner= getTeamIndex(player_id);

	}

	/**
	 * Get the team index belonging to a player
	 * @param player_id player id
	 * @return the team index of player_id (either 0 or 1)
	 */
	public static int getTeamIndex(int player_id){
		return (player_id == 0 || (player_id^3)==0)?0:1;
	}


	/**
	 * Check if all player ID has all his pieces in his target corner
	 * @param ID player ID
	 * @return true if all pieces of player ID is in his target corner
	 */
	private boolean checkIfWin(int ID){
		assert(ID<4);
		boolean win=true;
		int base_id= ID^3;
		Integer IDInteger= new Integer(ID);

		for(Point p: bases[base_id]){
			win &= IDInteger.equals(board.get(p));
		}

		return win;
	}


	@Override
	public void forceWinner(int win) {
		if(win > 3 ) throw new IllegalArgumentException("Invalid player given to forceWinner()");
		winner=win;
	}
	@Override
	public int getTurn() {
		return turn_player;
	}

	@Override
	public int getTurnsPlayed() {
		return turnNumber;
	}

	@Override
	public void move(Move m) throws IllegalArgumentException {
		CCMove ccm= (CCMove) m;

		// check if the move is legal, then execute it
		if(isLegal(ccm) ){
			if(ccm.to != null && ccm.from != null ){
				board.remove(ccm.from);
				board.put(ccm.to, ccm.player_id);
				lastMovedInTurn= ccm.to;
			}
		}else{
			throw new IllegalArgumentException("Invalid move sent: "+ ccm.toPrettyString());
		}
		
		// update winner tracking
		updateWinner(ccm.player_id);

		// if the turn is ending, update turn counter and make it the next players turn to play
		if(ccm.to== null || ccm.from == null || !ccm.isHop()){
			if(turn_player==3)
				turnNumber++;
			turn_player=(turn_player+1)%4;
			lastMovedInTurn=null;
		}
	}

	@Override
	public String getNameForID(int p) {
		return String.format("Player-%d", p);
	}

	@Override
	public int getIDForName(String s) {
		return Integer.valueOf(s.split("-")[1]);
	}

	@Override
	public int getNumberOfPlayers() {
		return NUMBER_OF_PLAYERS;
	}

	@Override
	public Move parseMove(String str) throws NumberFormatException,
	IllegalArgumentException {
		return new CCMove(str);
	}

	@Override
	public Object clone() {
		return new CCBoard((HashMap<Point, Integer>) board.clone(), turnNumber, winner, turn_player, lastMovedInTurn);
	}

	/** get the player_id of the piece at a given position. 
	 * Returns null if no piece if found.
	 * @param p the point on the board to check
	 * @return the player_id of the piece found. Null if no piece is found.
	 */
	public Integer getPieceAt(Point p){
		return board.get(p);
	}
	

	/**
	 * Get all pieces belonging to a given player
	 * @param player_id the player ID
	 * @return a list of all pieces belonging to player_id
	 */
	public ArrayList<Point> getPieces(int player_id){
		ArrayList<Point> points = new ArrayList<Point>();
		for(Entry<Point, Integer> e: board.entrySet()){
			if(e.getValue() == player_id)
				points.add(e.getKey());
		}
		return points;
	}

	/**
	 * Check if a given move is legal
	 * @param m The move to check
	 * @return true of the move is allowed, false otherwise
	 */
	public boolean isLegal(CCMove m){
		boolean legal=true;

		// check if the turn is allowed to end
		if(m.to == null || m.from == null)
			return lastMovedInTurn!=null || checkIfWin(m.player_id);

		// check if the move is within the board
		legal &= CCMove.inRange(m, 0, SIZE-1);

		// check if move comes from the correct player
		legal &= m.player_id == getTurn();

		// check if piece to move is from the correct player
		legal &= board.containsKey(m.from);
		if(legal)
			legal &= board.get(m.from).intValue() == m.player_id;

		// check if the position to move to is empty
		legal &= !board.containsKey(m.to);

		// check if the move is a hop and legal
		boolean h= m.isHop();
		legal &= !h || (h && board.containsKey(m.getMid()) && ((lastMovedInTurn == null) || lastMovedInTurn.equals(m.from)));

		// check if the move is a simple move and if it is allowed
		boolean sm= m.maxDist() == 1;
		legal &= (h && !sm) || (sm && lastMovedInTurn == null);

		// check if the move would take a piece out of a opposing base
		boolean toIn= bases[m.player_id ^ 3].contains(m.to);
		boolean fromIn= bases[m.player_id ^ 3].contains(m.from);
		legal &= !fromIn || (toIn && fromIn);

		return legal;
	}


	/**
	 * Get all legal move for the current state of the board. 
	 * If the player is allowed to end his turn, then a move with from=null and to=null is included.
	 * NOTE: this will give moves for any player regardless of who calls this method.
	 * @return A list of all allowed moves for the current state of the board
	 */
	public ArrayList<CCMove> getLegalMoves(){
		ArrayList<CCMove> legalMoves= new ArrayList<CCMove>(10);

		if(lastMovedInTurn != null){
			// if last move was a hop allow termination
			legalMoves.add(new CCMove(getTurn(), null, null));

			// allow all further hops with the same piece
			Point from= lastMovedInTurn;
			for(int i=0;i<8; i++){
				int dx = moves[i][0];
				int dy = moves[i][1];
				Point to=new Point(from.x+2*dx, from.y+2*dy);
				CCMove move=new CCMove(getTurn(), from, to);
				if(isLegal(move))
					legalMoves.add(move);
			}
		}else{
			for( Entry<Point, Integer> entry: board.entrySet()){
				addLegalMoveForPiece(entry.getKey(), entry.getValue().intValue(), legalMoves);
			}
			if(checkIfWin(getTurn()))
				legalMoves.add(new CCMove(getTurn(), null, null));
		}
		return legalMoves;
	}

	/**
	 * Get all the allowed moves for a given pi
	 * @param p
	 * @param player_id
	 * @return
	 */
	public ArrayList<CCMove> getLegalMoveForPiece(Point p, int player_id){
		ArrayList<CCMove> moveList = new ArrayList<CCMove>();
		addLegalMoveForPiece(p, player_id, moveList);
		return moveList;
	}

	public void addLegalMoveForPiece(Point p, int player_id, ArrayList<CCMove> moveList){
		if(player_id == getTurn() && board.containsKey(p)
				&& (board.get(p).intValue()==getTurn())){
			Point from= p;
			for(int i=0;i<8; i++){
				// check all moves surrounding that piece
				int dx = moves[i][0];
				int dy = moves[i][1];
				Point to=new Point(from.x+dx, from.y+dy);
				CCMove move;

				// if there is another piece adjacent check for a hop move
				if(board.containsKey(to)){
					move = new CCMove(getTurn(), from, new Point(to.x+dx, to.y+dy));
				}else{
					// otherwise add the move to that square
					move = new CCMove(getTurn(), from, to);
				}
				// filter out any moves that might not be legal (i.e., moving pieces out of the objective)
				if(isLegal(move))
					moveList.add(move);
			}
		}
	}
	
	/**
	 * Get the last piece moved in the turn.
	 * @return A point representing the last pieced moved. Null if called at the begining of a turn.
	 */
	public Point getLastMoved(){
		return lastMovedInTurn;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(int y=SIZE-1; y>=0; y--){
			for(int x=0; x<SIZE; x++){
				Integer i= board.get(new Point(x,y));
				if(i != null)
					sb.append(i.toString());
				else
					sb.append('-');
				if(x<SIZE-1)
					sb.append(' ');
				else
					sb.append('\n');
			}
		}
		return sb.toString();
	}

	@Override
	public BoardPanel createBoardPanel() { return new CCBoardPanel(); }

}
