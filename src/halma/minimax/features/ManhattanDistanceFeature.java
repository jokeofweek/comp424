package halma.minimax.features;

import java.awt.Point;

import halma.CCBoard;

/**
 * This features compares the distance between all your pieces and your friends pieces to your respective
 * goal zones against the distance for the opponent pieces.
 */
public class ManhattanDistanceFeature extends Feature {

	private final static Point[] GOAL_POINTS = {
    	new Point(CCBoard.SIZE - 1, CCBoard.SIZE - 1),
    	new Point(0, CCBoard.SIZE - 1),
    	new Point(CCBoard.SIZE - 1, 0),
    	new Point(0, 0)
    };
	
	private double weight;

	public ManhattanDistanceFeature(double weight) {
		this.weight = weight;
	}

	public int manhattanDistance(Point from, Point to) {
		return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
	}
	
	@Override
	public double getWeight(CCBoard board, int playerID) {
		return weight;
	}
	
	@Override
	public double getScore(CCBoard board, int playerID) {
		// For now just sum of your manhattan distance
		int ownDistance = 0;
		int enemyDistance = 0;

		for (int i = 0; i < 4; i++) {
			for (Point p : board.getPieces(i)) {
				int distance = 32 - manhattanDistance(p, GOAL_POINTS[i]);
				if (i == playerID || i == (playerID ^ 3)) {
					ownDistance += distance;
				} else {
					enemyDistance += distance;
				}
			}
		}
		
		// Normalize own and enemy distance. To normalize, we assume every piece is as far as possible
		// from the goal.
		double normalizedOwnDistance = ownDistance / (13 * 30.0);
		double normalizedEnemyDistance = enemyDistance / (13 * 30.0);
		
		return (normalizedOwnDistance - normalizedEnemyDistance);
	}
}
