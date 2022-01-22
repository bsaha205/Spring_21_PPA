package analysis.XTA;


import analysis.Analysis;

import soot.PackManager;
import soot.SourceLocator;
import soot.Transform;


public class XTA {
	
	/* A main driver for RTA analysis. */
	public static void main(String[] args) {
		
        long startTime = System.currentTimeMillis();
		
        // Code hooks the RTAAnalysis then launches Soot, which traverses 
        // all classes and creates and stores the appropriate constraints. 
        Analysis rtaAnalysis = new XTAAnalysis(); 
        PackManager.v().getPack("jtp").add(new Transform("jtp.rta", rtaAnalysis));
        //System.out.println("RTA: Starting the program! 2");
		soot.Main.main(args);
		//System.out.println("RTA: Starting the program! 3");
		
        String outputDir = SourceLocator.v().getOutputDir();
        
        
        // Solves the constraints using _reachability_ analysis
        rtaAnalysis.worklistSolve();
        
        
        // Displays final result in format required for autograding
        rtaAnalysis.showResult();
        
		
        
        long endTime   = System.currentTimeMillis();
        System.out.println("INFO: Total running time: " + ((float)(endTime - startTime) / 1000) + " sec");
        
	}
	
	
}
