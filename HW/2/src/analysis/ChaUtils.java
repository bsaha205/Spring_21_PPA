package analysis;

import java.util.LinkedList;
import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.util.NumberedString;

public class ChaUtils {
		
	// Implement utility methods isJavaSubtype and resolve
	// You may add as many as you need
	
	/*
	 *  returns: true if sub is Java subtype of sup, false otherwise
	 */
	public static boolean isJavaSubtype(SootClass sub, SootClass sup) {
		//TODO: YOUR CODE HERE
//		if(sup.equals(sub.getSuperclass())){
//			return true;
//		}
	
        while(sub != null)
        {
            if(sub == sup) return true;
            
            if(sub.hasSuperclass()) {
            	sub = sub.getSuperclass();
			}
            else return false;        
        }
        
		return false;
	}

	/*
	 * returns: dynamic target of receiver and subSignature
	 */
	public static SootMethod resolve(SootClass rec, String subSignature) {
		// TODO: YOUR CODE HERE
		
		//System.out.println("ChaUtils: SootMethod: rec: " + rec + "  subSignature: " + subSignature);
		while(true) {
			
			if(rec.declaresMethod(subSignature)) {
				return rec.getMethod(subSignature);
			}
			
			if(rec.hasSuperclass()) {
				rec = rec.getSuperclass();
			} else return null;
		}
	}
}
