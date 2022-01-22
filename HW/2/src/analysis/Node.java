package analysis;

import java.util.HashMap;
import java.util.Map;

import soot.Type;

/* 
 * A Node is an immutable object that abstracts a Java reference, either 
 * (1) a local (local, this, parameter, or return), (2) a static field, 
 * or (3) heap object.
 */
public class Node {
	
	private static Map<String,Node> nodes = new HashMap<>();

	// Rep invariant: counter = nodes.size
	// TODO: implement counter
	// TODO: consider whether other Node info should be included
	// TODO: ALLOC nodes are conceptually different. Fix!
    private static int counter = 0;
	
	public enum Kind {
        LOCAL, 
        STATIC_FIELD, 
        PARAMETER,
        THIS, 
        RETURN,
        ALLOC
    }	

    private String identifier;

    private Type type;

    private Kind kind;
    
    private Node(String identifier, Type type, Kind kind) {
    		this.identifier = identifier;
    		this.type = type;
    		this.kind = kind;
    }
    
    public int hashCode() { return identifier.hashCode(); }
    
    public boolean equals(Object o) {
    		if (o instanceof Node) {
    			Node n = (Node) o;
    			return n.identifier.equals(identifier);
    		}
    		else {
    			return false;
    		}
    }

    /*
     * modifies: nodes (interning of nodes)
     * effects: adds new node if not found in nodes
     * returns: factory method returns interned node if it exists, or the new one
     */
    public static Node getNodeInstance(String identifier, Type type, Kind kind) {
    		Node node = nodes.get(identifier);
    		if (node == null) {
    			counter++;
    			node = new Node(identifier, type, kind);
    			nodes.put(identifier,node);
    		}
    		return node;
    }
    
    public String toString() {
    		return identifier;
    }
    
    public Type getType() {
    		return type;
    }
    
    public Kind getKind() {
    		return kind;
    }
    
}
