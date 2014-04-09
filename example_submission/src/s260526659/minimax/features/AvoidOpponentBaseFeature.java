package s260526659.minimax.features;

import halma.CCBoard;

import java.awt.Point;

public class AvoidOpponentBaseFeature extends Feature {
	
	private static final Point[] PLAYER_GOAL_ZONES = {
		new Point(0, 0),
		new Point(15, 0),
		new Point(0, 15),
		new Point(15, 15)
	};
	
	private static final int[][] OPPONENTS = {
		{1,2},
		{0,3},
		{0,3},
		{1,2}
	};
	
	private static double[][][] DISTANCE_TO_GOAL = new double[16][16][4];
	
	static {
		// Initialize the distance from each point to the player's base.
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				for (int p = 0; p < 4; p++) {
					DISTANCE_TO_GOAL[x][y][p] = Math.sqrt(
							Math.pow(x - PLAYER_GOAL_ZONES[p].x, 2) +
							Math.pow(y - PLAYER_GOAL_ZONES[p].y, 2));
							
				}
			}
		}
	}
	
	private double maxDistance = 5;
	private double weight;
	
	public AvoidOpponentBaseFeature(double weight) {
		this.weight = weight;
	}
	
	@Override
	public double getScore(CCBoard board, CCBoard original, int playerID) {
		// For every piece within the max distance threshold to an opponent base
		// negatively score them based on how close they are
		double score = 0;
		for (Point piece : board.getPieces(playerID)) {
			for (int o = 0; o < OPPONENTS[playerID].length; o++) {
				double distance = DISTANCE_TO_GOAL[piece.x][piece.y][OPPONENTS[playerID][o]];
				if (distance <= maxDistance) {
					score -= Math.pow((maxDistance - distance), 2);
				}
			}
		}
		return score;
	}

	@Override
	public double getWeight(CCBoard board, CCBoard original, int playerID) {
		return this.weight;
	}
}
