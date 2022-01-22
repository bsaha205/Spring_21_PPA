package analysis.RTA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import analysis.Analysis;
import analysis.AnalysisStmtSwitch;
import analysis.ChaUtils;
import analysis.Constraint;
import analysis.Node;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.jimple.AbstractStmtSwitch;

public class RTAAnalysis extends Analysis {

	// TODO: YOUR CODE HERE: Add representation for the RTA analysis results
	
	Set<SootClass> instantiatedClasses = new HashSet<>();
	
	
	class AllocTransferFunction implements Constraint {
        SootClass sc;
        SootMethod sm;
        
        AllocTransferFunction(SootMethod sm, SootClass sc) {
            this.sc = sc;
            this.sm = sm;
        }
        
        @Override
        public Set<Constraint> solve() {
            Set<Constraint> successorList = new HashSet<Constraint>();
            
            //update the set of instantiated classes
            if(!instantiatedClasses.contains(sc)) {
            	instantiatedClasses.add(sc);
                
                for(SootMethod m: reachableMethods) {
                	successorList.addAll(getFromMap(sootConstraints, m));
                }              
            }           
            return successorList;
        }
    }
	
	class DirectCallTransferFunction implements Constraint { 
		SootMethod sm;
        
		DirectCallTransferFunction(SootMethod sm) {
            this.sm = sm;
        }
		
        @Override
        public Set<Constraint> solve() {
        	Set<Constraint> successorList = new HashSet<Constraint>();
            
          
            if(!reachableMethods.contains(sm)) {
            	reachableMethods.add(sm);
                           
                successorList.addAll(getFromMap(sootConstraints, sm));               
            }           
            return successorList;
        }
        
        //Will handle direct calls
    }
    
    class VirtualCallTransferFunction implements Constraint {       
    	SootMethod sm;
        
    	VirtualCallTransferFunction(SootMethod sm) {
            this.sm = sm;
        }
		
        @Override
        public Set<Constraint> solve() {
        	Set<Constraint> successorList = new HashSet<Constraint>();
            
          
        	for(SootClass sc: instantiatedClasses) {
        		SootMethod obtainedSm = ChaUtils.resolve(sc, sm.getSubSignature());
	            
        		if(obtainedSm != null && !reachableMethods.contains(obtainedSm)) {
	            	reachableMethods.add(obtainedSm);
	                           
	                successorList.addAll(getFromMap(sootConstraints, obtainedSm));               
	            }
        	}
        	
//        	if(!reachableMethods.contains(sm)) {
//            	reachableMethods.add(sm);
//                           
//                successorList.addAll(getFromMap(sootConstraints, sm));               
//            }
        	           
            return successorList;
        }
    }
	    
	    
	
	@Override
	protected AbstractStmtSwitch getStmtVisitor(SootMethod m) {
		// If you are certified wizardTM, go ahead and rewrite the Jimple visitor,
		// otherwise you can use mine
		return new AnalysisStmtSwitch(m,this);
	}

	@Override
	public void showResult() {
		// TODO: Auto-generated method stub
		// TODO: YOUR CODE HERE: Display results according to specification
		// for Submitty autograding
		List<String> reachableMethodList = new ArrayList<String>(); 
		List<String> instantiatedClassesList = new ArrayList<String>(); 
		
		for(SootMethod sm: reachableMethods) {
			reachableMethodList.add(sm.toString());
		}
		
		for(SootClass sc: instantiatedClasses) {
			instantiatedClassesList.add(sc.toString());
		}
        
		Collections.sort(reachableMethodList); 
        Collections.sort(instantiatedClassesList);
        
        System.out.println("Reachable methods:");
        for(String rm: reachableMethodList) {
        	System.out.println("=== " + rm);
		}
        
        System.out.println();
        System.out.println();
        System.out.println("Instantiated classes:");
        for(String ic: instantiatedClassesList) {
        	System.out.println("=== " + ic);
		}
		
//		System.out.println("RTAAnalysis: showResult: reachableMethods: " + reachableMethods);
//		System.out.println("RTAAnalysis: showResult: instantiatedClasses: " + instantiatedClasses);
		
	}

		
	// TODO: YOUR CODE HERE: fill in the auto-generated stubs by creating RTA
	// constraints/transfer functions. Note that you DO NOT NEED constraints for
	// each kind of statement. If you do create a constraint, don't forget to
	// add it to sootConstraints: 
	// addToMap(sootConstraints,enclMethod,newConstraint);
	
	// TODO: YOUR CODE HERE: If you are new to program analysis, it may be 
	// useful to add prints in these hooks, just to see what gets analyzed
	
	@Override
	public void assignStmt(SootMethod enclMethod, Node lhs, Node rhs) {
		// TODO Auto-generated method stub
		//System.out.println("RTAAnalysis: assignStmt");
	}

	@Override
	public void fieldWriteStmt(SootMethod enclMethod, Node lhs, SootField f, Node rhs) {
		// TODO Auto-generated method stub
		//System.out.println("RTAAnalysis: fieldWriteStmt");

	}

	@Override
	public void fieldReadStmt(SootMethod enclMethod, Node lhs, Node rhs, SootField f) {
		// TODO Auto-generated method stub
		//System.out.println("RTAAnalysis: fieldReadStmt");

	}

	@Override
	public void arrayWriteStmt(SootMethod enclMethod, Node lhs, Node rhs) {
		// TODO Auto-generated method stub
		//System.out.println("RTAAnalysis: arrayWriteStmt");

	}

	@Override
	public void arrayReadStmt(SootMethod enclMethod, Node lhs, Node rhs) {
		// TODO Auto-generated method stub
		//System.out.println("RTAAnalysis: arrayReadStmt");

	}

	@Override
	public void directCallStmt(SootMethod enclMethod, int callSiteId, Node lhs, SootMethod target, List<Node> args) {
		// TODO Auto-generated method stub
		
		addToMap(sootConstraints,enclMethod,new DirectCallTransferFunction(target));

	}

	@Override
	public void virtualCallStmt(SootMethod enclMethod, int callSiteId, Node lhs, SootMethod target, List<Node> args) {
		// TODO Auto-generated method stub
		
		addToMap(sootConstraints,enclMethod,new VirtualCallTransferFunction(target));
	}

	@Override
	public void allocStmt(SootMethod enclMethod, int allocSiteId, Node lhs, Node alloc) {
		// TODO Auto-generated method stub
		
		if(alloc.getType() instanceof RefType) {
            addToMap(sootConstraints,enclMethod,new AllocTransferFunction(enclMethod, Scene.v().getSootClass(alloc.getType().toString())));
        }
	}

}
