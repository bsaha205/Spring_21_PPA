package analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;

public abstract class Analysis extends BodyTransformer {

	/* sootConstraints is a map from SootMethod m to the set of constraints in m.
	 * Soot's pass creates the map, however, Soot's pass is an over-approximation.
	 * Particularly, sootConstraints.keySet is an over-approximation of the actually 
	 * reachable methods. Our analyses refine this set. 
	 */
	protected Map<SootMethod,Set<Constraint>> sootConstraints = new HashMap<SootMethod,Set<Constraint>>();
	
	/* Stores entry methods: main + static initializers <clinit> 
	 * Rep invariant: each element of entryMethods is either main or <clinit>
	 */
	protected Set<SootMethod> entryMethods = new HashSet<>();

	
	/* Reachable methods according to analysis (e.g., RTA, XTA, 0-CFA, PTA)
	 * Rep invariant: reachableMethods is a subset of sootConstraints.keySet
	 */
	protected Set<SootMethod> reachableMethods = new HashSet<SootMethod>();

	
	
	
	
	/* 
	 * Returns a visitor-like object that "visits" statements in m.
	 * I have written a visitor, AnalysisStmtSwitch that extracts 8 kinds of statements 
	 * relevant to our analysis 
	 */
	abstract protected AbstractStmtSwitch getStmtVisitor(SootMethod m);
	
	/*
	 * Called back from the Soot framework. Traverses method Body calling .apply 
	 * (i.e., .accept) on each statement. 
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	@Override
	protected void internalTransform(Body body, String arg1, Map<String, String> arg2) {
		
		SootMethod m = body.getMethod();
		if (m.isMain()) entryMethods.add(m);
		if (m.isStatic() && m.getName().equals("<clinit>")) {
			entryMethods.add(m);
		}
		AbstractStmtSwitch visitor = getStmtVisitor(m);

        final PatchingChain<Unit> units = body.getUnits();
        for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
            final Unit u = iter.next();
            u.apply(visitor);
        }
	}

	/* 
	 * A generic worklist-like algorithm for flow-insensitive analysis. 
	 * A Constraint encapsulates a transfer function. Each Constraint/transfer function 
	 * acts on data structures that represent dataflow information. A specific analysis
	 * defines its own constraints and structures.
	 * 
	 * requires: must be called after Soot has finished its pass 
	 */	
	public void worklistSolve() {
		Queue<Constraint> workList = new LinkedList<>();
		
		// Initialize worklist with constraints from all entry methods
		for (SootMethod entryMethod : entryMethods) {
			reachableMethods.add(entryMethod);
			workList.addAll(getFromMap(sootConstraints,entryMethod));
		}
		while (!workList.isEmpty()) {
			
			Constraint c = workList.poll();
			Set<Constraint> successors = c.solve();
			for (Constraint sc : successors) {
				if (!workList.contains(sc)) {
					workList.add(sc);
				}
			}
		}
	}
	
	// Displays analysis result in format required for autograding on Submitty
	public abstract void showResult();

	
	/* Callback (hook) methods. */
	
	/* modifies: sootConstraints map
	 * effects: creates Constraint c for assignment lhs = rhs in enclMethod, and adds
	 * c to map: sootConstraints.get(enclMethod) becomes sootConstraints.get(enclMethod) U { c }
	 * 
	 * Note: The Constraint encapsulates the transfer function for a statement. It would
	 * act upon analysis structures that represent dataflow information.  
	 */ 
	public abstract void assignStmt(SootMethod enclMethod, Node lhs, Node rhs);
	
	/* 
	 * modifies: sootConstraints
	 * effects: creates Constraint c for lhs.f = rhs, and adds c to sootConstraints
	 */
	public abstract void fieldWriteStmt(SootMethod enclMethod, Node lhs, SootField f, Node rhs);
	
	/* 
	 * modifies: sootConstraints
	 * effects: creates Constraint c for lhs = rhs.f, and adds c to sootConstraints
	 */
	public abstract void fieldReadStmt(SootMethod enclMethod, Node lhs, Node rhs, SootField f);
	
	/* 
	 * modifies: sootConstraints
	 * effects: creates Constraint c for lhs[] = rhs, and adds c to sootConstraints
	 */
	public abstract void arrayWriteStmt(SootMethod enclMethod, Node lhs, Node rhs);
	
	/* 
	 * modifies: sootConstraints
	 * effects: creates Constraint c for lhs = rhs[], and adds c to sootConstraints
	 */
	public abstract void arrayReadStmt(SootMethod enclMethod, Node lhs, Node rhs);
		
	/* 
	 * modifies: sootConstraints
	 * effects: creates Constraint c for lhs = target(args), and adds c to sootConstraints
	 * 
	 * params: handles direct calls which are either StaticInvokes or SpecialInvokes.
	 * In case of SpecialInvoke arg[0] stores the receiver and args[1:] stores the rest 
	 * of the arguments. If the call is not part of a call assignment, e.g., it's just 
	 * m(1) instead of x = m(1), then lhs is null.
	 * callSiteId is the unique call site identifier 
	 */
	public abstract void directCallStmt(SootMethod enclMethod, int callSiteId, Node lhs, SootMethod target, List<Node> args);

	
	/* 
	 * modifies: sootConstraints
	 * effects: creates Constraint c for virtual call lhs = r.target(args), and adds c to 
	 * sootConstraints
	 * 
	 * params: args[0] stores the receiver r and args[1:] stores the rest of the args. 
	 * If the expression is not part of a call assignment, then lhs is null.
	 * callSiteId is the unique call site identifier
	 */
	public abstract void virtualCallStmt(SootMethod enclMethod, int callSiteId, Node lhs, SootMethod target, List<Node> args);
	

	/* 
	 * modifies: sootConstraints
	 * effects: creates Constraint c for virtual call lhs = new A, and adds c to sootConstraints
	 * 
	 * params: alloc represents the allocated object, allocSiteId is the unique site identifier
	 */
	public abstract void allocStmt(SootMethod enclMethod, int allocSiteId, Node lhs, Node alloc);

	
	/* ========= Utility methods ========= 
	 * Useful if storing analysis result as maps from keys to sets of values.
	 * Ignore if you prefer different representation, e.g., your Principles of Software graph
	 */
	
	/* 
	 * params: map from keys K to sets of E, a key and an element 
	 * modifies: map
	 * effects: adds element to set of key in map. 
	 */
	protected static <K,E> void addToMap(Map<K,Set<E>> map, K key, E element) {
		Set<E> elems = map.get(key);
		if (elems == null) {
			elems = new HashSet<E>();
			map.put(key,elems);
		}
		elems.add(element);
	}
	
	/* 
	 * modifies: map
	 * returns: corresponding set, if key in map, returns the empty set otherwise
	 * effects: associates and the empty set with key if key not in map
	 */
	protected static <K,E> Set<E> getFromMap(Map<K,Set<E>> map, K key) {
		Set<E> result = map.get(key);
		if (result == null) { 
			result = new HashSet<>();
			map.put(key,result);
		}
		return result;
	}


}
