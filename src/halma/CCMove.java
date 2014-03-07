package halma;

import java.awt.Point;
import java.util.Scanner;

import boardgame.Move;

public class CCMove extends Move{
	
	protected static final String alphabet= "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	int player_id;
	Point from, to;

	/**
	 * Constructor from a string. 
	 * The string will be parsed and, if it is correct, will construct the appropriate move.
	 * @param str The string to parse.
	 */
	public CCMove(String str) {
		String[] components = str.split(" ");
		player_id= Integer.valueOf(components[0]);

		from = parsePoint(components[1]);
		to = parsePoint(components[2]);
	}

	/**
	 * Generate a move by explicitly giving the arguments.
	 * @param player_id player to move belongs to
	 * @param from which piece to move
	 * @param to where to move it to
	 */
	public CCMove(int player_id, Point from, Point to){
		this.player_id= player_id;
		this.from= from;
		this.to= to;
	}
	
	private Point parsePoint(String strp){
		Point p = null;
		if(strp != null && !strp.equals("null")){
			int par= strp.indexOf('(');
			int comma= strp.indexOf(',');
			String strx= strp.substring(par+1, comma);
			String stry= strp.substring(comma+1, strp.length()-1);
			p = new Point(Integer.valueOf(strx), Integer.valueOf(stry));
		}
		return p;
	}


	public int getPlayer_id() {
		return player_id;
	}

	public Point getFrom() {
		return from;
	}

	public Point getTo() {
		return to;
	}

	@Override
	public String toPrettyString() {
		String s="";

		if(from == null || to == null)
			s=  String.format("Player %d end turn", player_id);
		else
			s= String.format("Player %d move from (%c,%d) to (%c,%d)", player_id, alphabet.charAt(from.y), from.x+1, alphabet.charAt(to.y), to.x+1);

		return s;
	}

	@Override
	public String toTransportable() {
		String s="";
		if(from == null || to == null)
			s=  String.format("%d null null", player_id);
		else
			s= String.format("%d (%d,%d) (%d,%d)", player_id, from.x, from.y, to.x, to.y);

		return s;
	}

	@Override
	public int getPlayerID() {
		return player_id;
	}

	/**
	 * Get the max distance traveled between x and  y axis
	 * @return the max distance traveled
	 */
	public int maxDist(){
		int distx = Math.abs(from.x- to.x);
		int disty = Math.abs(from.y- to.y);
		return Math.max(distx, disty);
	}


	/**
	 * Check if this move would be a hop move.
	 * This assumes the move is legal.
	 * @return true if the move is a hop move.
	 */
	public boolean isHop(){
		if( from == null || to == null)
			return false;
		int distx = Math.abs(from.x- to.x);
		int disty = Math.abs(from.y- to.y);
		return (distx==2 && (disty==2 || disty==0)) || (distx==0 && disty==2);
	}

	/**
	 * Get the point which is in between to and from
	 * @return Point which is the mid point of the move.
	 */
	public Point getMid(){
		return new Point((from.x+to.x)/2, (from.y+to.y)/2);
	}

	/**
	 * Check if move is with the square board defined by min and max (inclusive)
	 * @param m
	 * @param min
	 * @param max
	 * @return
	 */
	public static boolean inRange(CCMove m, int min, int max) {
		boolean test=true;
		test &= m.to.x <= max && m.to.x >= min;
		test &= m.to.y <= max && m.to.y >= min;
		test &= m.from.x <= max && m.from.x >= min;
		test &= m.from.y <= max && m.from.y >= min;
		return test;
	}


}
