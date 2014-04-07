package halma.minimax.features;

import halma.CCBoard;

import java.awt.Point;
import java.util.List;

public class HuddleFeature extends Feature {

	private static double[][][][] DISTANCE = new double[16][16][16][16];
	// This is the furthest we can be from another piece.
	private static double DISTANCE_THRESHOLD = 3;
	
	{
		// Initialize distances between every 2 points
		for (int x1 = 0; x1 < 16; x1++) {
			for (int y1 = 0; y1 < 16; y1++) {
				for (int x2 = 0; x2 < 16; x2++) {
					for (int y2 = 0; y2 < 16; y2++) {
						DISTANCE[x1][y1][x2][y2] = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
					}
				}
			}
		}
	}
	
	

	private double weight;
	
	public HuddleFeature(double weight) {
		this.weight = weight;
	}
	
	@Override
	public double getWeight(CCBoard board, CCBoard original, int playerID) {
		return weight;
	}
	
	@Override
	public double getScore(CCBoard board, CCBoard original, int playerID) {
		List<Point> pieces = board.getPieces(playerID);
		double tally = 0;
		double distance;
		double closest;
		for (Point p : pieces) {
			// If the piece is in the goal zone, ignore it.
			if (board.bases[playerID^3].contains(p)) continue;
			closest = 1000;
			for (Point p2 : pieces) {
				if (p == p2) continue;
				distance = DISTANCE[p.x][p.y][p2.x][p2.y];
				// If we are close enough to at least one piece, then we're good.
				if (distance < closest) {
					closest = distance;
				}
			}
			// Normalize based on threshold.
			closest = Math.max(0, closest - DISTANCE_THRESHOLD);
			tally += closest;
		}
		
		// Normalize based on the number of pieces which are 5 away.
		return tally / (-2 * 13);
	}
	
}
