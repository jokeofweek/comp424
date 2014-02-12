package halma;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import boardgame.BoardPanel;

/**
 * A board panel for dispaly and input for the Halma game.
 */
public class CCBoardPanel extends BoardPanel 
implements MouseListener, MouseMotionListener, ComponentListener {

	private static final long serialVersionUID = 2648134549469132906L;

	/** CVS version information */
	private static final String CVSID = "$Id: CCBoardPanel.java,v 1.0 2014/02/08 16:21:21 rvince3 Exp $";

	// Some constants affecting display
	static final Color BGCOLOR = new Color(130,130,0);
	static final Color COLOR1 = new Color(230,230,160);
	static final Color COLOR2 = new Color(190,190,130);
	static final Color TCOLOR = new Color(210,210,145);

	static final Color[] TEAMCOLOR= {new Color(50,50,50), new Color(217, 93, 81), new Color(56, 93, 103), new Color(250, 250,250)};
	static final Color HIGHLIGHTCOLOR= new Color(100, 100, 100);

	static final int BORDER = 16;
	static final int BSIZE = CCBoard.SIZE;

	// Board location of piece playing 
	Point current = null; // Bad naming: x == i, y==j for board coords...
	//	Point dragStart = null, dragEnd = null;
	BoardPanelListener list = null; // Who needs a move input ?
	private int w, h, wp, hp, xoff, yoff;
	
	boolean hopping=false;

	public CCBoardPanel() {
		this.addMouseListener( this );
		this.addMouseMotionListener( this );
		this.addComponentListener( this );
	}

	protected void requestMove( BoardPanelListener l ) {
		list = l;
	}

	protected void cancelMoveRequest() {
		list = null; 
	}

	public void mousePressed(MouseEvent arg0) {
		CCBoard board = (CCBoard) getCurrentBoard();
		Point p = getSquare( arg0.getX(), arg0.getY() );
		if(arg0.getButton() == MouseEvent.BUTTON1 && !hopping){
			if(list != null && p != null 
					&& board.getPieceAt(p) != null 
					&& board.getPieceAt(p).equals(board.getTurn()) ){
				current = p;
			}else{
				current = null;
			}
		}

		repaint();
	}

	public void mouseDragged(MouseEvent arg0) {};

	public void mouseReleased(MouseEvent arg0) {
		CCBoard board = (CCBoard) getCurrentBoard();
		Point p = getSquare( arg0.getX(), arg0.getY() );
	
		if(arg0.getButton() == MouseEvent.BUTTON3){
			if( list != null && current != null){
				CCMove move;
				if( p.equals(current)){
					move= new CCMove(board.getTurn(), null, null);
				}else{
					move= new CCMove(board.getTurn(), current, p);
				}
				if(board.isLegal(move)){
					list.moveEntered(move);
					cancelMoveRequest();
					if(move.isHop()){
						current= move.to;
						hopping=true;
					}else{
						hopping=false;
						current=null;	
					}
					
				}
			}
		}

		repaint();
	}

	/** Paint the board to the offscreen buffer. This does the painting
	 * of the actual board, but not the pieces being moved by the user.*/
	public void drawBoard( Graphics g ) {
		CCBoard bd = (CCBoard) getCurrentBoard();
		Rectangle clip = g.getClipBounds();
		int w = (clip.width - BORDER*2)/ BSIZE;
		int h = (clip.height - BORDER*2)/ BSIZE;
		if( w < h ) h = w; else w = h; // Make square aspect ratio
		int wp = w*8/10, hp = h*8/10; 
		int xoff = (clip.width - BSIZE * w) / 2;
		int yoff = (clip.height - BSIZE * h) / 2;
		g.setColor(BGCOLOR);
		g.fillRect(clip.x,clip.y,clip.width,clip.height);
		FontMetrics fm = g.getFontMetrics();
		int x1 = xoff - BORDER, x2 = xoff + w * BSIZE;
		int y1 = yoff - BORDER, y2 = yoff + w * BSIZE;
		g.setColor(TCOLOR);
		for( int i = 0; i < BSIZE; i++ ) {
			String s = Integer.toString(i+1);
			String t = Character.toString((char) ('A' + i));
			Rectangle r = fm.getStringBounds(s, g).getBounds();
			Rectangle q = fm.getStringBounds(t, g).getBounds();
			int o = (BORDER - r.width) / 2;
			int p = (h-r.height) / 2 + yoff;
			g.drawString(s,x1+o,h*i+p+r.height-1);
			g.drawString(s,x2+o,h*i+p+r.height-1);
			int m = (BORDER - q.height) / 2;
			int n = (w-q.width) / 2 + xoff;
			g.drawString(t,w*i+n,y1+m+q.height-1);
			g.drawString(t,w*i+n,y2+m+q.height-1);
		}
		for( int i = 0; i < BSIZE; i++ ) {
			int y = yoff + i * h, yp = y + h/10;
			for( int j = 0; j < BSIZE; j++ ) {
				int x = xoff + j * w, xp = x + w/10;
				g.setColor( (i + j) % 2 == 0 ? COLOR1 : COLOR2 );
				g.fillRect( x,y,w,h );
				Point renderPoint= new Point(i,j);
				Integer id = bd.getPieceAt(renderPoint);
				if( id != null){
					if(renderPoint.equals(current)){
						g.setColor(HIGHLIGHTCOLOR);
					}else{
						g.setColor( TEAMCOLOR[id.intValue()]);
					}
					g.fillOval(xp,yp,wp,hp);
				}
			}
		}
	}

	/** We use the double-buffering provided by the superclass, but draw
	 *  the "transient" elements in the paint() method. */
	public void paint( Graphics g ) {
		// Paint the board as usual, this will used the offscreen buffer
		super.paint(g);
		if( current != null ) {
			Rectangle clip = g.getClipBounds();
			int w = (clip.width - BORDER*2)/ BSIZE;
			int h = (clip.height - BORDER*2)/ BSIZE;
			if( w < h ) h = w; else w = h; // Make square aspect ratio
			int wp = w*8/10, hp = h*8/10; 
			int xoff = (clip.width - BSIZE * w) / 2;
			int yoff = (clip.height - BSIZE * h) / 2;
			int x = xoff + current.y * w, xp = x + w/10;
			int y = yoff + current.x * h, yp = y + h/10;
			// Paint the current piece's origin
			g.setColor(HIGHLIGHTCOLOR);
			g.fillOval(xp,yp,wp,hp);
		}
	}

	public void componentResized(ComponentEvent arg0) {
		w = (this.getWidth() - BORDER*2) / BSIZE;
		h = (this.getHeight() - BORDER*2) /BSIZE;
		if( w < h ) h = w; else w = h; // Make square aspect ratio
		wp = w*8/10; hp = h*8/10; 
		xoff = (this.getWidth() - BSIZE * w) / 2;
		yoff = (this.getHeight() - BSIZE * h) / 2;
	}

	private Point getSquare( int x, int y) {
		// This is a bit ugly, since we swap coord order
		return new Point( (y-yoff) / w, (x-xoff) / w);
	}

	/* Don't use these interface methods */
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseMoved(MouseEvent arg0) {}
	public void componentMoved(ComponentEvent arg0) {}
	public void componentShown(ComponentEvent arg0) {}
	public void componentHidden(ComponentEvent arg0) {}
}


