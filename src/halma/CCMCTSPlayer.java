package halma;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

public class CCMCTSPlayer extends Player {
	private static double EXPLORATION_PARAMETER = Math.sqrt(2);
	private static long TIMEOUT = 800;
	
	private Random random;
	
	public CCMCTSPlayer() {
		super("MCTSPlayer");
		long seed = System.currentTimeMillis();
		random = new Random(seed);
		System.out.println("Starting MCTS player with seed: " + seed);
	}
	
	@Override
	public Move chooseMove(Board board) {
		// Create the root move node
		MoveNode node = new MoveNode((CCBoard) board, null, null);
		// Start timing
		long endTime = System.currentTimeMillis() + TIMEOUT;
		do {
			node.round();
		} while (System.currentTimeMillis() < endTime);

		// Return the move of the best child.
		return node.bestAction().move;
	}
	
	private class MoveNode {
		private CCBoard board;
		private CCMove move;
		public MoveNode parent;
		
		private List<MoveNode> children;
		public int simulations;
		public int wins;
		
		/**
		 * @param board The board representing the node.
		 * @param move The last move (optional for root).
		 * @param parent The parent node (optional for root).
		 */
		public MoveNode(CCBoard board, CCMove move, MoveNode parent) {
			this.board = board;
			this.move = move;
			this.parent = parent;
			this.children = new LinkedList<>();
		}
		
		/**
		 * @return the best child of the root, aka the move to play.
		 */
		private MoveNode bestAction() {
			// Return the child with the most simulations, which by default
			// will be the most promising child.
			MoveNode bestNode = null;
			int best = Integer.MIN_VALUE;
			for (MoveNode child : children) {
				if (child.simulations > best) {
					best = child.simulations;
					bestNode = child;
				}
			}
			return bestNode;
		}
		
		/**
		 * Performs one round of the 4 steps.
		 */
		private void round() {
			MoveNode current = this;
			// Select nodes until we reach a leaf.
			while (!current.isLeaf()) {
				current = current.selection();
			}
			// Expand the leaf.
			current.expansion();
			// Pick a child
			current = current.selection();
			// Simulate a game and then backpropagate
			current.propagation(current.simulation());
		}
		
		/**
		 * @return the score for a given node.
		 */
		private double evaluate() {
			return ((double)wins / simulations) + (EXPLORATION_PARAMETER * Math.sqrt(Math.log(parent.simulations)/simulations));
		}
				
		/**
		 * @return true if the node is a leaf.
		 */
		private boolean isLeaf() {
			return children.size() == 0;
		}
		
		/**
		 * @return the best child node.
		 */
		private MoveNode selection() {
			// Go through the children, finding the one with the best value
			MoveNode bestNode = null;
			double best = Double.MIN_VALUE;
			double current;
			for (MoveNode child : children) {
				current = child.evaluate();
				if (current > best) {
					best = current;
					bestNode = child;
				}
			}
			return bestNode;
		}
		
		/**
		 * This expands all children node by converting all legal moves to a new
		 * node in the tree.
		 */
		private void expansion() {
			List<CCMove> moves = board.getLegalMoves();
			for (CCMove move : moves) {
				// Create a new node for this move
				CCBoard newBoard = (CCBoard) board.clone();
				newBoard.move(move);
				// Add the child
				children.add(new MoveNode(newBoard, move, this));
			}
		}
		
		/**
		 * This rolls out an entire game using a random policy.
		 * @return 1 if the simulation resulted in a win, else 0.
		 */
		private int simulation() {
			CCBoard gameBoard = (CCBoard) board.clone();
			// Randomly simulate a game
			while (gameBoard.getWinner() != Board.NOBODY) {
				List<CCMove> legalMoves = gameBoard.getLegalMoves();
				gameBoard.move(legalMoves.get(random.nextInt(legalMoves.size())));
			}
			// If we won't, return 1, else return 0
			return (gameBoard.getWinner() == gameBoard.getTeamIndex(playerID)) ? 1 : 0;
		}
		
		/**
		 * This propagates a simulation result back up the tree based on the node's parent.
		 * @param win Whether the game was won or not.
		 */
		private void propagation(int win) {
			// Iterate through parents until we get to null, backpropagating the value
			MoveNode node = this;
			while (node != null) {
				node.simulations++;
				node.wins += win;
				node = node.parent;
			}
		}
	}
}
