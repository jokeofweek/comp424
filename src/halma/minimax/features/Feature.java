package halma.minimax.features;

import java.awt.Point;
import java.util.HashSet;

import halma.CCBoard;

public abstract class Feature {
	
	public final static Point[] BASE_POINTS={new Point(0,0), new Point(1,0), new Point(2,0), new Point(3,0),
		new Point(0,1), new Point(1,1), new Point(2,1), new Point(3,1),
		new Point(0,2), new Point(1,2), new Point(2,2),
		new Point(0,3), new Point(1,3)};

	public final static HashSet<Point>[] BASES = initializeBases();

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
	
	public abstract double getWeight(CCBoard board, int playerID);
	public abstract double getScore(CCBoard board, int playerID);
	

}
