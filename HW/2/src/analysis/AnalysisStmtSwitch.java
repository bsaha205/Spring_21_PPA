package analysis;

import java.util.ArrayList;
import java.util.List;

import soot.Local;
import soot.PrimType;
import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FloatConstant;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.MulExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

public class AnalysisStmtSwitch extends AbstractStmtSwitch {
	
	/* Method whose statement body is being traversed
	 */
	private SootMethod enclMethod;
	/* Analysis to which this visitor issues callbacks to process statements
	 */
	private Analysis analysis;
	
	/* Call site counter/identifier 
	 */
	private int callCount = 0;
	/* Unique allocation site counter/identifier
	 */
	private int allocCount = 0;
	
	public AnalysisStmtSwitch(SootMethod m, Analysis a) {
		this.enclMethod = m;
		this.analysis = a;
	}
	

    @Override
    public void caseInvokeStmt(InvokeStmt stmt) {
    	   stmt.getInvokeExpr().apply(new ValueVisitor(null,null));
    }

    @Override
    public void caseAssignStmt(AssignStmt stmt)
    {
        Value leftOp = stmt.getLeftOp();
        Value rightOp = stmt.getRightOp();
        
        if (leftOp instanceof Local) {
        		Node lhs;
        		if (isPrimType(leftOp.getType())) {
        			lhs = null;
        		}
        		else {        			
        			String identifier = constructLocalIdentifier((Local) leftOp); 
        			lhs = Node.getNodeInstance(identifier, leftOp.getType(), Node.Kind.LOCAL);
        		}
            rightOp.apply(new ValueVisitor(lhs, null));
        } else if (rightOp instanceof Local) {
        		// If right-hand-side is a Primitive type, there is no need to descend into left
        		if (isPrimType(rightOp.getType())) return;
        		String identifier = constructLocalIdentifier((Local) rightOp);
            Node rhs = Node.getNodeInstance(identifier, rightOp.getType(), Node.Kind.LOCAL);
            leftOp.apply(new ValueVisitor(null,rhs));
        }
    }

    @Override
    public void caseReturnStmt(ReturnStmt stmt) {
        Value returnOp = stmt.getOp();
        if (isPrimType(returnOp.getType())) return;
        String identifier = enclMethod.getSignature() + "@" + "return";
        Node ret = Node.getNodeInstance(identifier, enclMethod.getReturnType(), Node.Kind.RETURN);
        returnOp.apply(new ValueVisitor(ret, null));
    }

    @Override
    public void caseIdentityStmt(IdentityStmt stmt) {
        Value left = stmt.getLeftOp();
        Node lhs;
        if (isPrimType(left.getType())) {
        		lhs = null;
        }
        else if (left instanceof Local) {
            String identifier = constructLocalIdentifier((Local) left);
            lhs = Node.getNodeInstance(identifier, left.getType(), Node.Kind.LOCAL);
        }
        else
            throw new RuntimeException("Unhandled: " + left + ": " + left.getClass());
        Value rightOp = stmt.getRightOp();
        rightOp.apply(new ValueVisitor(lhs, null));
    }

    @Override
    public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {}

    @Override
    public void caseExitMonitorStmt(ExitMonitorStmt stmt) {}

    @Override
    public void caseThrowStmt(ThrowStmt stmt) {}

    @Override
    public void caseGotoStmt(GotoStmt stmt) {}

    @Override
    public void caseIfStmt(IfStmt stmt) {}

    @Override
    public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {}

    @Override
    public void caseReturnVoidStmt(ReturnVoidStmt stmt) {}
    
    /* Class extracts the 8 kinds of statements from Jimple
     * 
     */
    class ValueVisitor extends AbstractJimpleValueSwitch {
    		/* Rep invariant: lhs = null && rhs = null => 
    	     *                assignment on primitive types, or caseInvokeStmt
    	     */                
    		Node lhs;
    		Node rhs;
    		ValueVisitor(Node lhs, Node rhs) {
    			this.lhs = lhs;
    			this.rhs = rhs;
    		}
    		
        @Override
        public void caseCastExpr(CastExpr v) {
        	    if (lhs == null) return; // Expr is of PrimType
            Value cv = v.getOp();
            if (!(cv instanceof Local)) return; // TODO: better check here!         
            String identifier = constructLocalIdentifier((Local) cv); 
            Node node = Node.getNodeInstance(identifier, cv.getType(), Node.Kind.LOCAL);
            analysis.assignStmt(enclMethod,lhs,node);
        }

        @Override
        public void caseParameterRef(ParameterRef v) {
        		String identifier = enclMethod.getSignature() + "@" + "parameter"+v.getIndex(); 
        		Node node = Node.getNodeInstance(identifier, v.getType(), Node.Kind.PARAMETER);
        		if (lhs == null && rhs == null) return; // Expr is of PrimType
        		else if (lhs == null) {
        			analysis.assignStmt(enclMethod,node,rhs);
        		}
        		else {
        			analysis.assignStmt(enclMethod,lhs,node);
        		}
        }

        @Override 
        public void caseThisRef(ThisRef v) {
        		assert lhs != null;
        		String identifier = enclMethod.getSignature() + "@" + "this"; 
        		Node node = Node.getNodeInstance(identifier, v.getType(), Node.Kind.THIS);
        		analysis.assignStmt(enclMethod,lhs,node);
        }

        @Override
        public void caseLocal(Local v) {
        		String identifier = constructLocalIdentifier(v); 
        		Node node = Node.getNodeInstance(identifier, v.getType(), Node.Kind.LOCAL);
        		if (lhs == null && rhs == null) return; // Expr is of PrimType
        		else if (lhs == null) {
        			analysis.assignStmt(enclMethod,node,rhs);
        		}
        		else {
        			analysis.assignStmt(enclMethod,lhs,node);
        		}
        }

        @Override
        public void caseStaticFieldRef(StaticFieldRef v) {
        		try {
        			SootField field = v.getField();
        			String identifier = field.getSignature(); 
        			Node node = Node.getNodeInstance(identifier, field.getType(), Node.Kind.STATIC_FIELD);
        			if (lhs == null && rhs == null) return;
        			else if (lhs == null) {
        				analysis.assignStmt(enclMethod,node,rhs);
        			}
        			else {
        				analysis.assignStmt(enclMethod,lhs,node);
        			}
        		}
        		catch (RuntimeException e) {
        			// TODO: Make exception type more precise
        			System.out.println("Cannot resolve field for StaticFieldRef "+v);
        		}
        }

        @Override
        public void caseInstanceFieldRef(InstanceFieldRef v) {
        		try {
        			Value base = v.getBase();
        			assert base instanceof Local;
        			String identifier = constructLocalIdentifier((Local) base); 
        			Node node = Node.getNodeInstance(identifier, base.getType(), Node.Kind.LOCAL);
        			SootField field = v.getField();
        			if (field == null) {
        				System.out.println("WARN: " + base.getType() + " doesn't have field, in"
        						+ "\n\t" + enclMethod);
        				return;
        			}
        			if (lhs == null && rhs == null) return;
        			else if (lhs == null) {
        				analysis.fieldWriteStmt(enclMethod,node,field,rhs);
        			}
        			else {
        				analysis.fieldReadStmt(enclMethod,lhs,node,field);        				
        			}
        		}
        		catch (RuntimeException e) {
        			// TODO: Make exception type more precise!
        			System.out.println("Cannot resolve field referenced in InstanceFieldRef "+v);
        			// System.exit(1);
        		}
        }

        @Override
        public void caseArrayRef(ArrayRef v) {
            Value base = v.getBase();
            assert base instanceof Local;
            String identifier = constructLocalIdentifier((Local) base); 
            Node node = Node.getNodeInstance(identifier, base.getType(), Node.Kind.LOCAL);
            if (lhs == null && rhs == null) return;
            else if (lhs == null) {
            		analysis.arrayWriteStmt(enclMethod,node,rhs);
            }
            else {
            		analysis.arrayReadStmt(enclMethod,lhs,node);
            }
        }

        @Override
        public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
            // Ignore calls on ArrayType receivers (e.g., equals(), hashCode())
        		if (v.getBase().getType() instanceof RefType)
            		handleVirtualCall(v);
        }

        @Override
        public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
        		// System.out.println(v);
            handleDirectCall(v);
        }

        @Override
        public void caseStaticInvokeExpr(StaticInvokeExpr v) {
        	    // System.out.println(v);
            handleDirectCall(v);
        }

        @Override
        public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
        		// Ignore calls on ArrayType receivers (e.g., clone())
    			if (v.getBase().getType() instanceof RefType)
    				handleVirtualCall(v);
        }
        
        // Can ignore. We consider Java bytecode. 
        @Override 
        public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
            handleDirectCall(v);
        }

        @Override
        public void caseNewExpr(NewExpr v) {
        	    handleAllocExpr(v);        	
        }

        @Override
        public void caseNewArrayExpr(NewArrayExpr v) {
        		handleAllocExpr(v);
        }

        @Override
        public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
        		handleAllocExpr(v);
        }
        
        
        // No need to handle the following expressions as we are interested in 
        // propagation of reference values
        @Override
        public void caseLengthExpr(LengthExpr v) { }

        @Override
        public void caseAddExpr(AddExpr v) { }

        @Override
        public void caseAndExpr(AndExpr v) { }

        @Override
        public void caseDivExpr(DivExpr v) { }

        @Override
        public void caseMulExpr(MulExpr v) { }

        @Override
        public void caseOrExpr(OrExpr v) { }

        @Override
        public void caseSubExpr(SubExpr v) { }

        @Override
        public void caseXorExpr(XorExpr v) { }

        @Override
        public void caseShlExpr(ShlExpr v) { }

        @Override
        public void caseShrExpr(ShrExpr v) { }

        @Override
        public void caseUshrExpr(UshrExpr v) { }

        @Override
        public void caseDoubleConstant(DoubleConstant v) { }

        @Override
        public void caseFloatConstant(FloatConstant v) { }

        @Override
        public void caseIntConstant(IntConstant v) { }

        @Override
        public void caseLongConstant(LongConstant v) { }

        @Override
        public void caseNullConstant(NullConstant v) { }

        @Override
        public void caseStringConstant(StringConstant v) { }

        @Override
        public void caseClassConstant(ClassConstant v) { }

        @Override
        public void caseCmpExpr(CmpExpr v) { }

        @Override
        public void caseCmpgExpr(CmpgExpr v) { }

        @Override
        public void caseCmplExpr(CmplExpr v) { }

        @Override
        public void caseCaughtExceptionRef(CaughtExceptionRef v) { }

        @Override
        public void caseInstanceOfExpr(InstanceOfExpr v) { }

        @Override
        public void defaultCase(Object v) {
            System.out.println("Unhandled value: " + v + " of type " + v.getClass()
                    + "\n\t" + lhs + "  =  " + v + "  =  " + rhs);
        }
        
        private void handleVirtualCall(InstanceInvokeExpr v) {
        		try {
        			if (v.getMethod() == null) {
        				System.out.println("WARN: Cannot find method at "
        						+ "\n\t" + v);
        				return;
        			}
        			SootMethod sm = v.getMethod();
        			analysis.virtualCallStmt(enclMethod,callCount++,lhs,sm,getArgs(v));
        		} catch (RuntimeException e) {
        			System.out.println("Cannot find method in invokeExpr "+v);
        		}
        }
        
        private void handleDirectCall(InvokeExpr v) {
        		try {
        			if (v.getMethod() == null) {
        				System.out.println("WARN: Cannot find method at "
    						+ "\n\t" + v);
        				return;
        			}
        			SootMethod sm = v.getMethod();
        			if (sm.getName().equals("<init>") 
    					&& sm.getDeclaringClass().getName().equals("java.lang.Object"))
        				return;
        			analysis.directCallStmt(enclMethod,callCount++,lhs,sm,getArgs(v));
        		} catch (RuntimeException e) {
        			System.out.println("Cannot find method in InvokeExpr "+v);
        		}
        	}
        
        /* 
         * modifies: none 
         * returns: the list of actual arguments at InvokeExpr v.
         * If v is an InstanceInvoke, then receiver is stored as the 0-th element of the list.
         * E.g., r.m(a1,a2,a3), returns [r,a1,a2,a3].
         * 
         */
        private List<Node> getArgs(InvokeExpr v) {
        		List<Node> result = new ArrayList<>();
        		if (v instanceof InstanceInvokeExpr) {
        			InstanceInvokeExpr iv = (InstanceInvokeExpr) v;
    				Value base = iv.getBase();
    			    assert base instanceof Local;
    				String identifier = constructLocalIdentifier((Local) base); 
    				Node aBase = Node.getNodeInstance(identifier, base.getType(), Node.Kind.LOCAL);
    				result.add(aBase);
        		}
        		for (Value arg : v.getArgs()) {
    				// Can be local, or constant
        			String identifier;
        			// We need to track all actual arguments (including primitives and constants) 
        			// in order to map actuals to formals in analyses that need it (e.g., points-to).
        			// In order to do so, we create a dummy identifier/Node when arg is a primitive 
        			// type or reference type constant; we create a proper identifier/Node otherwise.
        			if (isPrimType(arg.getType()) || !(arg instanceof Local))
        				identifier = "fake-null"; 
        			else 
        				identifier = constructLocalIdentifier((Local) arg); 
    				Node aArg = Node.getNodeInstance(identifier,arg.getType(), Node.Kind.LOCAL);
    				result.add(aArg);
        		}
        		return result;
         }
        
        	 private void handleAllocExpr(AnyNewExpr v) {
        		 String identifier = allocCount +":"+enclMethod.getSignature()+"@"+v.toString();
         	 Node node = Node.getNodeInstance(identifier, v.getType(), Node.Kind.ALLOC);
         	 analysis.allocStmt(enclMethod, allocCount, lhs, node);
         	 allocCount++;
        	 }
            
    }
    
    private boolean isPrimType(Type t) {
    		return t instanceof PrimType;
    }
    
    private String constructLocalIdentifier(Local local) {
    		String result = enclMethod.getSignature() + "@" + local.toString();
    		if (local.toString().equals("this")) result += "0";
    		return result;
    }
    
    public void defaultCase(Object obj)
    {
        System.out.println("Default case (" + obj.getClass() + "): " + obj);
    }
    
}
