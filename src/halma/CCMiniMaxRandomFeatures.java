package halma;

import halma.minimax.features.AdjacentToBaseFeature;
import halma.minimax.features.DontLeaveAloneFeature;
import halma.minimax.features.Feature;
import halma.minimax.features.HuddleFeature;
import halma.minimax.features.LeaveBaseFeature;
import halma.minimax.features.ManhattanDistanceFeature;
import halma.minimax.features.NotInOpposingBaseFeature;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

public class CCMiniMaxRandomFeatures extends CCMiniMaxPlayer {
	

    protected List<List<Feature>> features = Arrays.asList(
    		Arrays.asList((Feature)new ManhattanDistanceFeature(0.9),
    				new LeaveBaseFeature(0.002),
    				new DontLeaveAloneFeature(0.005),
    				new NotInOpposingBaseFeature(0.015),
    				new AdjacentToBaseFeature(0.05),
    				new HuddleFeature(0.25)),
    		Arrays.asList((Feature)new ManhattanDistanceFeature(0.9),
    				new LeaveBaseFeature(0.002),
    				new DontLeaveAloneFeature(0.005),
    				new NotInOpposingBaseFeature(0.015),
    				new AdjacentToBaseFeature(0.05))
    );
    
    protected String[] names = {"All-dressed", "Huddle-free"};
    
    public CCMiniMaxRandomFeatures() {
    	int value = Integer.parseInt(JOptionPane.showInputDialog(null, "Feature set?")); 
    			//new Random().nextInt(names.length);
    	setFeatures(features.get(value));
    	setName(names[value]);
	}
}
