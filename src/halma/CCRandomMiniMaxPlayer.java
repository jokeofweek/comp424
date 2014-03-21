package halma;

import halma.minimax.features.AdjacentToBaseFeature;
import halma.minimax.features.DontLeaveAloneFeature;
import halma.minimax.features.Feature;
import halma.minimax.features.LeaveBaseFeature;
import halma.minimax.features.ManhattanDistanceFeature;
import halma.minimax.features.NotInOpposingBaseFeature;

import java.util.Arrays;
import java.util.Random;

public class CCRandomMiniMaxPlayer extends CCMiniMaxPlayer {
	
    public CCRandomMiniMaxPlayer() { 
    	super("");
    	
    	// Generate the weights
    	double[] manhattanWeights = {0.7, 0.75, 0.8, 0.85, 0.9, 0.95};
    	double[] leaveBaseWeights = {0.001, 0.002, 0.003};
    	double[] dontLeaveAloneWeights = {0.01, 0.005, 0.015, 0.02};
    	double[] notInOpposingWeights = {0.01, 0.015, 0.02, 0.025};
    	double[] adjacentToBaseWeights = {0.04, 0.05, 0.06};
    	
    	double manhattanWeight = getRandomElement(manhattanWeights);
    	double leaveBaseWeight = getRandomElement(leaveBaseWeights);
    	double dontLeaveAloneWeight = getRandomElement(dontLeaveAloneWeights);
    	double notInOpposingWeight = getRandomElement(notInOpposingWeights);
    	double adjacentToBaseWeight = getRandomElement(adjacentToBaseWeights);
    	
    	this.features = Arrays.asList(
	    	(Feature)new ManhattanDistanceFeature(manhattanWeight),
			new LeaveBaseFeature(leaveBaseWeight),
			new DontLeaveAloneFeature(dontLeaveAloneWeight),
			new NotInOpposingBaseFeature(notInOpposingWeight),
			new AdjacentToBaseFeature(adjacentToBaseWeight)
    	);
    	
    	setName("Minimax_" + manhattanWeight + "_" + leaveBaseWeight + "_" + dontLeaveAloneWeight + "_" + notInOpposingWeight + "_" + adjacentToBaseWeight);
    }   
    
    private static double  getRandomElement(double[] elements) {
    	Random r = new Random();
    	return elements[r.nextInt(elements.length)];
    }
    
}
