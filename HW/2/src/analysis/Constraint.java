package analysis;

import java.util.Set;

public interface Constraint {
	
	/*
	 *  effects: solves this Constraint. Has effects on specific Analysis data
	 *  returns: constraints that are affected by change caused by this constraint
	 */
	public Set<Constraint> solve();

}
