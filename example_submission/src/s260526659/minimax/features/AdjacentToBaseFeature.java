package s260526659.minimax.features;

import java.awt.Point;

import halma.CCBoard;

/**
 * This feature tests for the number of pieces that can enter the opposing base on the next turn.
 */
public class AdjacentToBaseFeature extends Feature {

	private double weight;
	
	public AdjacentToBaseFeature(double weight) {
		this.weight = weight;
	}	
	
	@Override
	public double getWeight(CCBoard board, CCBoard original, int playerID) {
		return weight;
	}
	
	private int getAdjacentPieces(CCBoard board, int playerID) {
		int adjacentPieces = 0;
		int[] multiplier = PLAYER_MULTIPLIERS[playerID];
		for (Point p : board.getPieces(playerID)) {
			if (!BASES[playerID^3].contains(p)) {
				for (int[] offset : PROGRESS_OFFSETS) {
					// If we can move directly into the base
					Point hopBase = new Point(p.x + multiplier[0] * offset[0], p.y + multiplier[1] * offset[1]);
					if (BASES[playerID^3].contains(hopBase)) {
						if (board.getPieceAt(hopBase) == null) {
							adjacentPieces++;
							break;
						}
					} 

					// If the piece can hop into the base
					if (board.getPieceAt(hopBase) != null) {
						Point hopResult = new Point(hopBase.x + multiplier[0] * offset[0], hopBase.y + multiplier[1] * offset[1]);
						if (BASES[playerID^3].contains(hopResult) && board.getPieceAt(hopResult) == null) {
							adjacentPieces++;
							break;
						}
					}
				}
				adjacentPieces++;
			}
		}
		return adjacentPieces;
	}
	
	@Override
	public double getScore(CCBoard board, CCBoard original, int playerID) {
		// Want to increase this number, but can't be weighted too much. We normalize it to the number of available pieces.
		return Math.max((getAdjacentPieces(board, playerID) - getAdjacentPieces(original, playerID)), 0) / 10.0;
	}

}
