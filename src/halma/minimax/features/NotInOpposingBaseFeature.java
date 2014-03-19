package halma.minimax.features;

import java.awt.Point;
import java.util.HashSet;

import halma.CCBoard;

public class NotInOpposingBaseFeature extends Feature {
	private double weight;

	public NotInOpposingBaseFeature(double weight) {
		this.weight = weight;
	}	
	
	@Override
	public double getWeight(CCBoard board, CCBoard original, int playerID) {
		return weight;
	}

	@Override
	public double getScore(CCBoard board, CCBoard original, int playerID) {
		int piecesLeft = 0;
		for (Point p : board.getPieces(playerID)) {
			if (!BASES[playerID^3].contains(p)) {
				piecesLeft++;
			}
		}
		// Want to maximize the number of pieces out of the base, normalized to the total number of pieces.
		return (-piecesLeft) / 10.0;
	}

}
