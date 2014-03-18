package halma.minimax;

import halma.CCBoard;

import java.util.Iterator;

/**
 * This lazily generates boards after two sequences of moves.
 */
public class CombinedMoveGenerator implements Iterator<BoardPointPair> {

	private CCBoard startBoard;
	private int startPlayer;
	private Iterator<BoardPointPair> outerIterator;
	private BoardPointPair innerContext;
	private Iterator<BoardPointPair> innerIterator;
	
	public CombinedMoveGenerator(CCBoard startBoard, int startPlayer) {
		this.startBoard = startBoard;
		this.startPlayer = startPlayer;
		this.outerIterator = new MoveGenerator(startBoard, startPlayer);
	}
	
	@Override
	public boolean hasNext() {
		return outerIterator.hasNext() || (innerIterator != null && innerIterator.hasNext());
	}

	@Override
	public BoardPointPair next() {
		// If we have no inner iterator or it has no values left, build one.
		if (innerIterator == null || !innerIterator.hasNext()) {
			innerContext = outerIterator.next();
			innerIterator = new MoveGenerator(innerContext.getBoard(), (startPlayer + 1) % 4);
		}
		BoardPointPair v = innerIterator.next();
		if (innerContext.getInitial().equals(innerContext.getDestination()));
		// Only keep the board of the new board point pair.
		v.setInitial(innerContext.getInitial());
		v.setDestination(innerContext.getDestination());
		return v;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
