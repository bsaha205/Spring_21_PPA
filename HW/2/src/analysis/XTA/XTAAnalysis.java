package analysis.XTA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

public class XTAAnalysis extends Analysis {

	// TODO: YOUR CODE HERE: Add representation for the RTA analysis results
	
	Map<SootMethod,Set<SootClass>> instantiatedClassesPerMethod = new HashMap<SootMethod,Set<SootClass>>();
	Map<SootField,Set<SootClass>> instantiatedClassesPerField = new HashMap<SootField,Set<SootClass>>();
	Map<String,Set<SootClass>> instantiatedClassesPerNode = new HashMap<String,Set<SootClass>>();
	List<String> primitiveTypes = Arrays.asList("void", "int", "double", "char", "boolean", "long", "float");
	
	
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
            Set<SootClass> instantiatedClasses = instantiatedClassesPerMethod.get(sm);
            
            if(instantiatedClasses == null) {
            	instantiatedClasses = new HashSet<SootClass>();
            }
            
            if(!instantiatedClasses.contains(sc)) {

            	instantiatedClasses.add(sc);
            	instantiatedClassesPerMethod.put(sm, instantiatedClasses);
                
                for(SootMethod m: reachableMethods) {
                	successorList.addAll(getFromMap(sootConstraints, m));
                }              
            }           
            return successorList;
        }
    }
	
	class DirectCallTransferFunction implements Constraint { 
		SootMethod enMethod;
    	SootMethod target;
    	List<Node> args;
        
		DirectCallTransferFunction(SootMethod ecMethod, SootMethod target, List<Node> args) {
			this.target = target;
            this.enMethod = ecMethod;
            this.args = args;
        }
		
        @Override
        public Set<Constraint> solve() {
        	Set<Constraint> successorList = new HashSet<Constraint>();            
          
            if(!reachableMethods.contains(target)) {
            	reachableMethods.add(target);
                           
                successorList.addAll(getFromMap(sootConstraints, target));               
            }
            
            Set<SootClass> sootClassesFromMethod = instantiatedClassesPerMethod.get(enMethod);   
            Set<SootClass> iClasses = instantiatedClassesPerMethod.get(target);
            if(iClasses == null) {
     			iClasses = new HashSet<SootClass>();
     		}
            
            if(sootClassesFromMethod == null) {
            	sootClassesFromMethod = new HashSet<SootClass>();
     		}
            
            int flag = 0;
            
            if(!args.isEmpty()) {
            	SootClass sClass =  Scene.v().getSootClass(args.get(0).getType().toString());
            	
            	//System.out.println("XTAAnalysis: DirectCallTransferFunction: enMethod: " + enMethod + "  target: " + target + "  sClass: " + sClass);
        		if(sootClassesFromMethod != null) {
         			for(SootClass sc: sootClassesFromMethod) {
                 		if(ChaUtils.isJavaSubtype(sc, sClass)) {
                 			if(iClasses.add(sc)) flag =1;
                 		}
                 	}
         		}
        		
        		for(int i = 0; i < target.getParameterCount(); i++) {
            		String paramClassAsString = target.getParameterType(i).toString();
            		if(!primitiveTypes.contains(paramClassAsString)) {
            			SootClass paramClass = Scene.v().getSootClass(paramClassAsString);
    	            	for(SootClass sc1: sootClassesFromMethod) {
    	            		if(ChaUtils.isJavaSubtype(sc1, paramClass)) {
    	            			if(iClasses.add(sc1)) flag = 1;
    	            		}
    	            	}
            		}
            		
            	}
            	
            	String retClassAsString = target.getReturnType().toString();
            	if(!primitiveTypes.contains(retClassAsString)) {
            		SootClass retClass = Scene.v().getSootClass(retClassAsString);
                	
                	for(SootClass sc1: iClasses) {
                		if(ChaUtils.isJavaSubtype(sc1, retClass)) {
                			if(sootClassesFromMethod.add(sc1)) flag = 1;
                		}
                	}
            	}
            	
         		instantiatedClassesPerMethod.put(target, iClasses);
         		instantiatedClassesPerMethod.put(enMethod, sootClassesFromMethod);
         		
         		if(flag == 1) {                	
                	for(SootMethod m: reachableMethods) {
                    	successorList.addAll(getFromMap(sootConstraints, m));
                    }
                }
            }
   		
            return successorList;
        }
    }
    
    class VirtualCallTransferFunction implements Constraint {       
    	SootMethod enMethod;
    	SootMethod target;
    	SootClass sClass;
        
    	VirtualCallTransferFunction(SootMethod ecMethod, SootMethod target, SootClass sClass) {
            this.target = target;
            this.enMethod = ecMethod;
            this.sClass = sClass;
        }
		
        @Override
        public Set<Constraint> solve() {
        	Set<Constraint> successorList = new HashSet<Constraint>();
        	
        	//System.out.println("XTAAnalysis: VirtualCallTransferFunction: enMethod: " + enMethod + "  target: " + target + "  sClass: " + sClass);	
        	Set<SootClass> sootClassesFromMethod = instantiatedClassesPerMethod.get(enMethod);     	
        	
        	Set<SootClass> iClassesToAdded = new HashSet<SootClass>();
        	int flag = 0;
        	if(sootClassesFromMethod != null) {
        		for(SootClass sc: sootClassesFromMethod) {
            		if(ChaUtils.isJavaSubtype(sc, sClass)) {
            			SootMethod obtainedSm = ChaUtils.resolve(sc, target.getSubSignature());

                		if(obtainedSm != null && !reachableMethods.contains(obtainedSm)) {
        	            	reachableMethods.add(obtainedSm);            
        	            }
                		
                		Set<SootClass> iClasses = instantiatedClassesPerMethod.get(obtainedSm);
                		if(iClasses == null) {
                			iClasses = new HashSet<SootClass>();
                		}
    	            	if(iClasses.add(sc)) flag = 1;
    	            	
    	            	for(int i = 0; i < obtainedSm.getParameterCount(); i++) {
    	            		String paramClassAsString = obtainedSm.getParameterType(i).toString();
    	            		if(!primitiveTypes.contains(paramClassAsString)) {
    	            			SootClass paramClass = Scene.v().getSootClass(paramClassAsString);
            	            	for(SootClass sc1: sootClassesFromMethod) {
            	            		if(ChaUtils.isJavaSubtype(sc1, paramClass)) {
            	            			if(iClasses.add(sc1)) flag = 1;
            	            		}
            	            	}
    	            		}
    	            		
    	            	}
    	            	
    	            	String retClassAsString = obtainedSm.getReturnType().toString();
    	            	//System.out.println("XTAAnalysis: VirtualCallTransferFunction: retClassAsString: " +  retClassAsString);
    	            	//System.out.println("XTAAnalysis: VirtualCallTransferFunction: retClassAsString: " +  obtainedSm.getReturnType().getClass().getName());
    	            	if(!primitiveTypes.contains(retClassAsString)) {
    	            		SootClass retClass = Scene.v().getSootClass(retClassAsString);
        	            	
        	            	for(SootClass sc1: iClasses) {
        	            		if(ChaUtils.isJavaSubtype(sc1, retClass)) {
        	            			iClassesToAdded.add(sc1);
        	            		}
        	            	}
    	            	}
    	            	instantiatedClassesPerMethod.put(obtainedSm, iClasses);
            		}
            	}
        	}
        	
        	for(SootClass sc1: iClassesToAdded) {
        		if(sootClassesFromMethod.add(sc1)) flag = 1;
        	}
        	instantiatedClassesPerMethod.put(enMethod, sootClassesFromMethod);
        	
        	if(flag == 1) {                	
            	for(SootMethod m: reachableMethods) {
                	successorList.addAll(getFromMap(sootConstraints, m));
                }
            }
             	           
            return successorList;
        }
    }
    
    class FieldWriteTransferFunction implements Constraint { 
		SootMethod sm;
		SootField sf;
        
		FieldWriteTransferFunction(SootMethod sm, SootField sf) {
            this.sm = sm;
            this.sf = sf;
        }
		
        @Override
        public Set<Constraint> solve() {
        	Set<Constraint> successorList = new HashSet<Constraint>();
            
        	String type = sf.getType().toString();
        	if(type.contains("[]")) {
        		type = type.substring(0,type.indexOf("[]"));
        	}
        	
        	Set<SootClass> sootClassesFromMethod = instantiatedClassesPerMethod.get(sm);
        	SootClass sootClass = Scene.v().getSootClass(type);
        	
        	Set<SootClass> classList = instantiatedClassesPerField.get(sf);
        	
        	if(classList == null) classList = new HashSet<SootClass>();
        	
        	int flag = 0;
        	if(sootClassesFromMethod != null) {
        		for(SootClass sc: sootClassesFromMethod) {
            		if(ChaUtils.isJavaSubtype(sc, sootClass)) {
            			if(classList.add(sc)) flag = 1;
            		}
            	}
        	}       	
        	
        	instantiatedClassesPerField.put(sf, classList);
        	
        	if(flag == 1) {           	
            	for(SootMethod m: reachableMethods) {
                	successorList.addAll(getFromMap(sootConstraints, m));
                }
            }
         
            return successorList;
        }
    }
    
    class FieldReadTransferFunction implements Constraint { 
		SootMethod sm;
		SootField sf;
        
		FieldReadTransferFunction(SootMethod sm, SootField sf) {
            this.sm = sm;
            this.sf = sf;
        }
		
        @Override
        public Set<Constraint> solve() {
        	Set<Constraint> successorList = new HashSet<Constraint>();
            
        	Set<SootClass> sootClassesFromMethod = instantiatedClassesPerMethod.get(sm);
        	Set<SootClass> sootClassesFromField = instantiatedClassesPerField.get(sf);

        	if(sootClassesFromMethod == null) {
        		sootClassesFromMethod = new HashSet<SootClass>();
     		}
        	int flag = 0;
        	if(sootClassesFromField != null) {
        		for(SootClass sc: sootClassesFromField) {
            		if(sootClassesFromMethod.add(sc)) flag = 1;
            	}
            	
            	instantiatedClassesPerMethod.put(sm, sootClassesFromMethod);
        	}

        	if(flag == 1) {
            	
            	for(SootMethod m: reachableMethods) {
                	successorList.addAll(getFromMap(sootConstraints, m));
                }
            }
        	          
            return successorList;
        }
    }
    
    class ArrayWriteTransferFunction implements Constraint { 
		SootMethod sm;
		String node;
        
		ArrayWriteTransferFunction(SootMethod sm, String node) {
            this.sm = sm;
            this.node = node;
        }
		
        @Override
        public Set<Constraint> solve() {
        	Set<Constraint> successorList = new HashSet<Constraint>();
        	
        	Set<SootClass> sootClassesFromMethod = instantiatedClassesPerMethod.get(sm);
        	
        	Set<SootClass> classList = instantiatedClassesPerNode.get(node);
        	
        	if(classList == null) classList = new HashSet<SootClass>();
        	
        	int flag = 0;
        	if(sootClassesFromMethod != null) {
        		for(SootClass sc: sootClassesFromMethod) {
            		if(classList.add(sc)) flag = 1;           		
            	}
        	}
        	
        	instantiatedClassesPerNode.put(node, classList);
        	
        	if(flag == 1) {           	
            	for(SootMethod m: reachableMethods) {
                	successorList.addAll(getFromMap(sootConstraints, m));
                }
            }
         
            return successorList;
        }
    }
    
    class ArrayReadTransferFunction implements Constraint { 
		SootMethod sm;
		String node;
        
		ArrayReadTransferFunction(SootMethod sm, String node) {
            this.sm = sm;
            this.node = node;
        }
		
        @Override
        public Set<Constraint> solve() {
        	Set<Constraint> successorList = new HashSet<Constraint>();
        	
        	Set<SootClass> sootClassesFromMethod = instantiatedClassesPerMethod.get(sm);
        	Set<SootClass> sootClassesFromNode = instantiatedClassesPerNode.get(node);
        	
        	if(sootClassesFromMethod == null) {
        		sootClassesFromMethod = new HashSet<SootClass>();
     		}

        	int flag = 0;
        	if(sootClassesFromNode != null) {
        		for(SootClass sc: sootClassesFromNode) {
            		if(sootClassesFromMethod.add(sc)) flag = 1;
            	}
            	
            	instantiatedClassesPerMethod.put(sm, sootClassesFromMethod);
        	}
        	
        	if(flag == 1) {           	
            	for(SootMethod m: reachableMethods) {
                	successorList.addAll(getFromMap(sootConstraints, m));
                }
            }
         
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
		
		
		for(SootMethod sm: reachableMethods) {
			reachableMethodList.add(sm.toString());
		}
		
		
        
		Collections.sort(reachableMethodList); 
       
        
        System.out.println("Reachable methods:");
        for(String rm: reachableMethodList) {
        	System.out.println(rm);
        	
        	Set<SootClass> instantiatedClasses = instantiatedClassesPerMethod.get(Scene.v().getMethod(rm));
        	//System.out.println("XTAAnalysis: showResult: instantiatedClasses: " + instantiatedClasses);
        	List<String> instantiatedClassesList = new ArrayList<String>(); 
        	
        	if(instantiatedClasses == null) {
            	instantiatedClasses = new HashSet<SootClass>();
            }
        	for(SootClass sc: instantiatedClasses) {
    			instantiatedClassesList.add(sc.toString());
    		}
        	
        	Collections.sort(instantiatedClassesList);
        	for(String ic: instantiatedClassesList) {
            	System.out.println("=== " + ic);
    		}
		}
               

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
//		System.out.println("XTAAnalysis: assignStmt: enclMethod: " + enclMethod);
//		System.out.println("XTAAnalysis: assignStmt: lhs: " + lhs + "  type: " + lhs.getKind().toString());
//		System.out.println("XTAAnalysis: assignStmt: rhs: " + rhs + "  type: " + rhs.getKind().toString());
		//System.out.println("XTAAnalysis: assignStmt: lhsField: " + Scene.v().getField(lhs.toString()));
		
		
		if(lhs.getKind().toString().equals("STATIC_FIELD")) {
			addToMap(sootConstraints,enclMethod,new FieldWriteTransferFunction(enclMethod, Scene.v().getField(lhs.toString())));
		}
		else if(rhs.getKind().toString().equals("STATIC_FIELD")) {
			addToMap(sootConstraints,enclMethod,new FieldReadTransferFunction(enclMethod, Scene.v().getField(rhs.toString())));
		}
			
	}

	@Override
	public void fieldWriteStmt(SootMethod enclMethod, Node lhs, SootField f, Node rhs) {
		// TODO Auto-generated method stub
//		System.out.println("XTAAnalysis: fieldWriteStmt: enclMethod: " + enclMethod);
//		System.out.println("XTAAnalysis: fieldWriteStmt: lhs: " + lhs);
//		System.out.println("XTAAnalysis: fieldWriteStmt: f: " + f);
//		System.out.println("XTAAnalysis: fieldWriteStmt: rhs: " + rhs);
		
		addToMap(sootConstraints,enclMethod,new FieldWriteTransferFunction(enclMethod, f));

	}

	@Override
	public void fieldReadStmt(SootMethod enclMethod, Node lhs, Node rhs, SootField f) {
		// TODO Auto-generated method stub
//		System.out.println("XTAAnalysis: fieldReadStmt: enclMethod: " + enclMethod);
//		System.out.println("XTAAnalysis: fieldReadStmt: lhs: " + lhs);
//		System.out.println("XTAAnalysis: fieldReadStmt: rhs: " + rhs);
//		System.out.println("XTAAnalysis: fieldReadStmt: f: " + f);		

		addToMap(sootConstraints,enclMethod,new FieldReadTransferFunction(enclMethod, f));
	}

	@Override
	public void arrayWriteStmt(SootMethod enclMethod, Node lhs, Node rhs) {
		// TODO Auto-generated method stub
//		System.out.println("XTAAnalysis: arrayWriteStmt: enclMethod: " + enclMethod);
//		System.out.println("XTAAnalysis: arrayWriteStmt: lhs: " + lhs + "  type: " + lhs.getType());
//		System.out.println("XTAAnalysis: arrayWriteStmt: rhs: " + rhs + "  type: " + rhs.getType().toString());
		
		addToMap(sootConstraints,enclMethod,new ArrayWriteTransferFunction(enclMethod, lhs.getType().toString()));
		
//		if(rhs.getType() instanceof RefType) {
////			System.out.println("XTAAnalysis: arrayWriteStmt: lhsField: " + rhs.getType());
////			System.out.println("XTAAnalysis: arrayWriteStmt: lhsField: " + Scene.v().getSootClass(rhs.getType().toString()));
//			//addToMap(sootConstraints,enclMethod,new FieldWriteTransferFunction(enclMethod, Scene.v().getField(lhs.toString())));
//		}

	}

	@Override
	public void arrayReadStmt(SootMethod enclMethod, Node lhs, Node rhs) {
		// TODO Auto-generated method stub
//		System.out.println("XTAAnalysis: arrayReadStmt: enclMethod: " + enclMethod);
//		System.out.println("XTAAnalysis: arrayReadStmt: lhs: " + lhs + "  type: " + lhs.getType());
//		System.out.println("XTAAnalysis: arrayReadStmt: rhs: " + rhs + "  type: " + rhs.getType());
		
		addToMap(sootConstraints,enclMethod,new ArrayReadTransferFunction(enclMethod, rhs.getType().toString()));
		
//		if(lhs.getType() instanceof RefType) {
//			//System.out.println("XTAAnalysis: arrayReadStmt: rhsField: " + lhs.getType());
//			//System.out.println("XTAAnalysis: arrayReadStmt: rhsField: " + Scene.v().getField(lhs.toString()));
//			//addToMap(sootConstraints,enclMethod,new FieldReadTransferFunction(enclMethod, Scene.v().getField(rhs.toString())));
//        }
		
//		if(!lhs.toString().contains("void")) {
//			
//		}
		

	}

	@Override
	public void directCallStmt(SootMethod enclMethod, int callSiteId, Node lhs, SootMethod target, List<Node> args) {
		// TODO Auto-generated method stub
//		System.out.println("XTAAnalysis: directCallStmt: enclMethod: " + enclMethod);
//		System.out.println("XTAAnalysis: directCallStmt: callSiteId: " + callSiteId);
//		System.out.println("XTAAnalysis: directCallStmt: lhs: " + lhs);
//		System.out.println("XTAAnalysis: directCallStmt: target: " + target);
//		System.out.println("XTAAnalysis: directCallStmt: args: " + args);
		//System.out.println("XTAAnalysis: directCallStmt: args2: " + Scene.v().getSootClass(args.get(0).getType().toString()));
		
		addToMap(sootConstraints,enclMethod,new DirectCallTransferFunction(enclMethod, target, args));

	}

	@Override
	public void virtualCallStmt(SootMethod enclMethod, int callSiteId, Node lhs, SootMethod target, List<Node> args) {
		// TODO Auto-generated method stub
		
//		System.out.println("XTAAnalysis: virtualCallStmt: enclMethod: " + enclMethod);
//		System.out.println("XTAAnalysis: virtualCallStmt: callSiteId: " + callSiteId);
//		System.out.println("XTAAnalysis: virtualCallStmt: lhs: " + lhs);
//		System.out.println("XTAAnalysis: virtualCallStmt: target: " + target);
//		System.out.println("XTAAnalysis: virtualCallStmt: args: " + args);	
		
		addToMap(sootConstraints,enclMethod,new VirtualCallTransferFunction(enclMethod, target, Scene.v().getSootClass(args.get(0).getType().toString())));
	}

	@Override
	public void allocStmt(SootMethod enclMethod, int allocSiteId, Node lhs, Node alloc) {
		// TODO Auto-generated method stub
		
		//System.out.println("XTAAnalysis: allocStmt: alloc: " + alloc);
		if(alloc.getType() instanceof RefType) {
            addToMap(sootConstraints,enclMethod,new AllocTransferFunction(enclMethod, Scene.v().getSootClass(alloc.getType().toString())));
        }
	}

}
