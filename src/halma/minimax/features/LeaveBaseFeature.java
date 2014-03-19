package halma.minimax.features;

import java.awt.Point;
import java.util.HashSet;

import halma.CCBoard;

public class LeaveBaseFeature extends Feature {
	private double weightPerTurn;

	public LeaveBaseFeature(double weightPerTurn) {
		this.weightPerTurn = weightPerTurn;
	}	
	
	@Override
	public double getWeight(CCBoard board, int playerID) {
		return board.getTurnsPlayed() <= 100 ? board.getTurnsPlayed() * weightPerTurn : 0;
	}

	@Override
	public double getScore(CCBoard board, int playerID) {
		int piecesLeft = 0;
		for (Point p : board.getPieces(playerID)) {
			if (BASES[playerID].contains(p)) {
				piecesLeft++;
			}
		}
		// Want to maximize the number of pieces out of the base, normalized to the total number of pieces.
		return (-piecesLeft) / 10.0;
	}

}
