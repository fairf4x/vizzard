/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fairfax
 */
public class dmClass implements dmiTreeNode {
    private String className;
    private String oldName = null;
    private dmiTreeNode parent;
    private LinkedList<dmiTreeNode> children;
    
    
    public dmClass() {
        this.className = null;
        this.parent = null;
        this.children = new LinkedList<dmiTreeNode>();
    }
    
    public dmClass(String name){
        this.className = name;
        this.parent = null;
        this.children = new LinkedList<dmiTreeNode>();
    }
    
    public dmClass(String name, dmiTreeNode parent){
        this.className = name;
        this.parent = parent;
        this.children = new LinkedList<dmiTreeNode>();
    }
    
    /**
     * dmClass copy constructor
     * @param orig 
     */
    public dmClass(dmClass orig) {
        this.className = orig.className;
        
        this.parent = null; // if this node is root null value is OK
                            // else parent value will be set in following loop
        
        this.children = new LinkedList<dmiTreeNode>();
        
        for (dmiTreeNode child: orig.children) {
            if (child instanceof dmClass) {
                dmClass origChild = (dmClass) child;    // typecast
                dmClass newChild = new dmClass(origChild);
                newChild.setParent(this);     // set parent
                this.children.add(newChild); // insert copy of child
                continue;
            }
            
            if (child instanceof dmConstant) {
                dmConstant origChild = (dmConstant) child; // typecast
                dmConstant newChild = new dmConstant(origChild);
                newChild.setParent(this);   // set parent of copied constant
                this.children.add(newChild);
            }
        }
    }
    
    @Override
    public String toString(){
        return className;
    }


    @Override
    public dmiTreeNode getChildAt(int i) {
        return children.get(i);
    }


    @Override
    public int getChildCount() {
        return children.size();
    }


    @Override
    public dmiTreeNode getParent() {
        return parent;
    }


    @Override
    public int getIndex(dmiTreeNode tn) {
        return children.indexOf(tn);
    }


    @Override
    public boolean getAllowsChildren() {
        return true;
    }
   
    @Override
    public boolean isLeaf() {
        return (children.isEmpty());
    }

    @Override
    public List<dmiTreeNode> children() {    
        return children;
    }
    
    @Override
    public List<dmiTreeNode> subtree() {
        List<dmiTreeNode> result = new LinkedList<dmiTreeNode>();
        
        // add self into resulting list
        result.add(this);
        
        if (!children.isEmpty()) {
            for (dmiTreeNode child: children) {
                result.addAll(child.subtree());
            }
        }
        
        // in case of leaf node return list of length 1 
        return result;
    }
    
    @Override
    public dmiTreeNode[] getPath(){
        return getPathToRoot(this,0);
    }
    
    protected dmiTreeNode[] getPathToRoot(dmiTreeNode node, int depth){
        dmiTreeNode[] result;
        
        if (node.isRoot()){
            result = new dmiTreeNode[depth+1];
            result[0] = node;
        }else{
            result = getPathToRoot(node.getParent(),depth+1);
            result[result.length - (depth+1)] = node;
        }
        return result;
    }

    @Override
    public void insert(dmiTreeNode node) {
        node.setParent(this);
        children.add(node);
    }

    @Override
    public void remove(dmiTreeNode node) {
        children.remove(node);
    }

    @Override
    public void setParent(dmiTreeNode parent) {
        this.parent = parent;
    }

    @Override
    public boolean isRoot() {
        return (this.parent == null);
    }
            
    @Override
    public String printSubtree(int indent) {
        String result = "";
        for (int i = 0; i < indent + 1; ++i) {
            result += " ";
        }

        result += className;
    
        if (!children.isEmpty()) {
            result += " = { ";
            for (dmiTreeNode cons : children) {
                result += cons.printSubtree(indent) + " ";
            }
            result += " }";
        } else {
            result += "\n";
            for (dmiTreeNode node : children) {
                result += node.printSubtree(indent + 2);
            }
        }

        return result + "\n";
    }

    /**
     * Class name is domain unique string.
     * @return class name
     */
    @Override
    public String getName() {
        return className;
    }

    @Override
    public void setName(String newName) {
        oldName = className;
        className = newName;
    }

    @Override
    final public boolean equals(Object other) {
        if (other == this)
            return true;
        
        if (other instanceof dmClass) {
            dmClass otherCl = (dmClass) other;
            if (this.className.equals(otherCl.className))
                return true;
            else
                return false;
        } else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.className != null ? this.className.hashCode() : 0);
        return hash;
    }

    /**
     * Walk through subtree and put all constants found on the constList. 
     * @param constList 
     */
    public void makeSubtreeConstantSymbolList(List<String> constList) {
        assert(constList != null);
               
        if (this.getChildCount() == 0) {
            return;
        }
        
        for (dmiTreeNode child: this.children) {
            if (child instanceof dmConstant) {
                constList.add(child.getName());
            }
            
            if (child instanceof dmClass) {
                dmClass classNode = (dmClass) child;
                classNode.makeSubtreeConstantSymbolList(constList);
            }
        }
    }

    public void makeSubtreeConstantList(List<dmConstant> constList) {
       assert(constList != null);
               
        if (this.getChildCount() == 0) {
            return;
        }
        
        for (dmiTreeNode child: this.children) {
            if (child instanceof dmConstant) {
                constList.add((dmConstant)child);
            }
            
            if (child instanceof dmClass) {
                dmClass classNode = (dmClass) child;
                classNode.makeSubtreeConstantList(constList);
            }
        }
    }
    
    public void makeSubtreeClassSymbolList(List<String> classList) {
        assert(classList != null);
        
        if (this.getChildCount() == 0) {
            return;
        }
            
        for (dmiTreeNode child: this.children) {
            if (child instanceof dmClass) {
                dmClass classNode = (dmClass) child;
                classList.add(child.getName());
                classNode.makeSubtreeClassSymbolList(classList);
            }
        }
    }
    
    public boolean isChildClass(String className) {
        LinkedList<String> childrenNames = new LinkedList<String>();
        makeSubtreeClassSymbolList(childrenNames);
        
        if (childrenNames.contains(className)) {
            return true;
        } else {
            return false;
        }
            
    }

    @Override
    public String getOldName() {
        return oldName;
    }

    // transferable interface
    private static final DataFlavor flavors[] = { dmiTreeNode.classFlavor };
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor df) {
        return flavors[0].equals(df);
    }

    @Override
    public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(df)) {
            return this;
        }
        return null;
    }
    
    /* closestCommonAncestor algorithm
     * Input: S - set of tree nodes
     * Output: node which is closest common ancestor for all nodes from S

     // initialization
     forall x in S: hit(x) = 0;
     cnt = |S| // number of hits needed for a node to be recognized as closest common ancestor

     while |S| > 0
	T = \emptyset
	forall x in S:
		if x.parent != null // non-root node
			hit(x) ++;
			if hit(x) == cnt
				return x; // all paths to root node went through this one and it is first such node 
			add(x.parent,T);
	
	S = T;	// if x was root node it is left out from S and |S| decreases
     */
    
    /**
     * Return the most specific class which is ancestor for all classes in cList.
     * 
     * @param cList class list
     * @return 
     */
    public static dmClass closestCommonAncestor(List<dmClass> cList) {
        List<dmClass> nodes = new LinkedList<dmClass>(cList);
        int cnt = nodes.size(); // number of nodes
       
        if (nodes.isEmpty()) { // empty set of nodes has no common ancestor
            return null;
        }
        
        Map<dmClass,Integer> hitCnt = new HashMap<dmClass,Integer>(); // counters of paths going through each node to root
        for(dmClass cl: cList) {
            hitCnt.put(cl,0);
        }
        
        while(!nodes.isEmpty()) {
            List<dmClass> temp = new LinkedList<dmClass>();
            for (dmClass node: nodes) {
                if (!node.isRoot()) {
                    
                    if (hitCnt.containsKey(node)) {
                        hitCnt.put(node,hitCnt.get(node)+1);
                    } else {
                        hitCnt.put(node, 1);
                    }
                    if (hitCnt.get(node) == cnt)    // closest common ancestor was found
                        return node;
                    
                    temp.add((dmClass)node.getParent());
                }
            }
            
            nodes.clear();       // temp set become nodes set
            nodes.addAll(temp);
        }
        
        return (dmClass) cList.get(0).getRootNode();
    }

    @Override
    public dmiTreeNode getRootNode() {
        dmiTreeNode anyNode = this;
        while (!anyNode.isRoot())
            anyNode = anyNode.getParent();
        
        return anyNode;
    }
}