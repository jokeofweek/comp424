package halma.minimax.features;

import halma.CCBoard;

import java.awt.Point;

public class DontLeaveAloneFeature extends Feature {

	private double weight;
	
	public DontLeaveAloneFeature(double weight) {
		this.weight = weight;
	}
	
	@Override
	public double getWeight(CCBoard board, CCBoard original, int playerID) {
		return weight;
	}

	@Override
	public double getScore(CCBoard board, CCBoard original, int playerID) {
		// Figure out all stones that can hop at least one direction.
		int canHops = 0;
		for (Point p : board.getPieces(playerID)) {
			// If the piece is in a base, don't care
			if (BASES[playerID ^ 3].contains(p)) {
				canHops++;
				continue;
			}
			for (int[] offset : DIRECTIONAL_OFFSETS) {
				// Ensure the point we want to hop over has a stone and is in range
				Point hopBase = new Point(p.x + offset[0], p.y + offset[1]);
				if (hopBase.x >= 0 && hopBase.y >= 0 && hopBase.x < 16 && hopBase.y < 16 &&
						board.getPieceAt(hopBase) != null) {
					// Ensure the point we want to hop to has no stone and is in range
					Point hopResult = new Point(p.x + 2 * offset[0], p.y + 2 * offset[1]);
					if (hopResult.x >= 0 && hopResult.y >= 0 && hopResult.x < 16 && hopResult.y < 16 &&
							board.getPieceAt(hopResult) == null) {
						canHops++;
						break;
					}
				}
			}
		}
		
		return canHops / 10.0;
	}

}
