package halma.minimax;

import halma.CCBoard;
import halma.CCMove;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/**
 * This lazily generates moves for one player.
 */
public class MoveGenerator implements Iterator<BoardPointPair>
{

	private CCBoard startBoard;
	private int player;
	private Stack<BoardPointPair> boards = new Stack<BoardPointPair>();
	private Queue<BoardPointPair> boardQueue = new LinkedList<>();
	private HashMap<Point, Set<Point>> hoppedPoints = new HashMap<>(10);
	
	public MoveGenerator(CCBoard startBoard, int player) {
		this.startBoard = startBoard;
		this.player = player;

		// Cache where each initial point has been to avoid rehopping to a point
		for (Point p : startBoard.getPieces(player)) {
			hoppedPoints.put(p, (Set)new HashSet<>());
		}
		
		// Initialize the queue
		ArrayList<CCMove> moves = startBoard.getLegalMoves();
		Collections.shuffle(moves);
		for (CCMove move : moves) {
			// Create the board with the hop and end
			CCBoard b = (CCBoard)startBoard.clone();
			b.move(move);
			BoardPointPair p = new BoardPointPair(b, move.getFrom(), move.getTo());
			
			if (move.isHop()) {
				boardQueue.add(p);
				// Register the initial in the set of moves we've went to
				// to avoid hopping back
				hoppedPoints.get(move.getFrom()).add(move.getTo());
				hoppedPoints.get(move.getFrom()).add(move.getFrom());
			} else {
				boards.add(p);
			}	
		}
	}
	
	private void generateBoardsFromPair(BoardPointPair pair) {
		Point initial = pair.getInitial();
		
		for (CCMove m : pair.getBoard().getLegalMoves()) {
			// Don't go to a move if it's been done before
			if (m.getTo() != null && m.getFrom() != null && !hoppedPoints.get(initial).contains(m.getTo())) {
				//Clone the board with the move applied
				CCBoard b = (CCBoard)pair.getBoard().clone();
				b.move(m);
				
				// Add the board with the sequence over.
				CCBoard b2 = (CCBoard)b.clone();
				b2.move(new CCMove(player, null, null));
				boards.add(new BoardPointPair(b2, initial, m.getTo()));
				
				// Sequence of hop is still going, add it to the visited points and continue
				hoppedPoints.get(initial).add(m.getTo());
				boardQueue.add(new BoardPointPair(b, initial, m.getTo()));
			}
		}
	}
	
	@Override
	public boolean hasNext() {
		// If no boards are currently loaded, iterate until we found some.
		while (boards.isEmpty() && !boardQueue.isEmpty()) {
			generateBoardsFromPair(boardQueue.remove());
		}
		return !boards.isEmpty();
	}

	@Override
	public BoardPointPair next() {
		// Return the first board
		return boards.pop();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
