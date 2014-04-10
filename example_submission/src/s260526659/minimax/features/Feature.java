package s260526659.minimax.features;

import halma.CCBoard;

import java.awt.Point;
import java.util.HashSet;

/**
 * A generic feature which can be included in a linear feature evaluation function.
 */
public abstract class Feature {
	
	public final static Point[] BASE_POINTS={new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0),
		new Point(0,1), new Point(1,1), new Point(2,1), new Point(3,1),
		new Point(0,2), new Point(1,2), new Point(2,2),
		new Point(0,3), new Point(1,3)};

	public final static HashSet<Point>[] BASES = initializeBases();

    public final static int[][] DIRECTIONAL_OFFSETS = {
    	{0,1},{1,1},{1,0},
    	{0,-1},{1,-1},{-1, -1},{-1,1},{-1,0}
    };
    
    public static final int[][] PROGRESS_OFFSETS = {
    	{0, 1}, {1, 1}, {1, 0}
    };
    
    /**
     * Given a progress offset, multiply x and y by corresponding values
     * to get the progress directions for a given player.
     */
    public static final int[][] PLAYER_MULTIPLIERS = {
    	{1, 1}, {-1,1}, {1, -1}, {-1, -1}
    };
            
	/**
	 * Generate a set of all points in the bases
	 * @return a set of all points in the bases
	 */
	private static HashSet<Point>[] initializeBases() {
		HashSet<Point>[] b= new HashSet[4];
		for(int i=0; i<4; i++){
			b[i] = new HashSet<Point>();
			for(int j=0; j<BASE_POINTS.length; j++){
				Point p= new Point( (i%2==0)?BASE_POINTS[j].x:CCBoard.SIZE-BASE_POINTS[j].x-1, 
						((i>>1)%2==0)?BASE_POINTS[j].y:CCBoard.SIZE-BASE_POINTS[j].y-1);
				b[i].add(p);
			}
		}

		return b;
	}
	
	public abstract double getWeight(CCBoard board, CCBoard original, int playerID);
	public abstract double getScore(CCBoard board, CCBoard original, int playerID);
	

}
