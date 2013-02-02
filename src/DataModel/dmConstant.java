/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author fairfax
 */
public class dmConstant implements dmiTreeNode {
    private String constantName;
    private String oldName = null;
    private dmiTreeNode parent;
    private boolean taskDependent;  // is constant specific for particular task ?
    
    public dmConstant(boolean taskDep){
        this.parent = null;
        this.constantName = null;
        this.taskDependent = taskDep;
    }
    
    public dmConstant(String name, boolean taskDep){
        this.parent = null;
        this.constantName = name;
        this.taskDependent = taskDep;
    }
    
    public dmConstant(String name, dmClass parent, boolean taskDep){
        this.parent = parent;
        this.constantName = name;
        this.taskDependent = taskDep;
    }
    
    public dmConstant(dmConstant orig) {
        this.taskDependent = orig.taskDependent;
        this.constantName = orig.constantName;
        this.parent = null; // this value will be always adjusted
    }
    
    @Override
    public void insert(dmiTreeNode node) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void remove(dmiTreeNode node) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setParent(dmiTreeNode parent) {
        this.parent = parent;
    }

    @Override
    public List<dmiTreeNode> children() {
        return new LinkedList<dmiTreeNode>();   // return empty list
    }
    
    @Override
    public List<dmiTreeNode> subtree() {
        LinkedList<dmiTreeNode> result = new LinkedList<dmiTreeNode>();
        result.add(this);
        
        return result;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public dmiTreeNode getChildAt(int index) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public int getIndex(dmiTreeNode node) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public dmiTreeNode getParent() {
        return parent;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    public boolean isTaskDependent() {
        return this.taskDependent;
    }
    
    @Override
    public dmiTreeNode[] getPath() {
        dmiTreeNode[] parentPath = parent.getPath();
        dmiTreeNode[] result = new dmiTreeNode[parentPath.length + 1];
        for (int i = 0; i < parentPath.length; ++i){
            result[i] = parentPath[i];
        }
        result[parentPath.length] = this;
        return result;
    }
    
    @Override
    public String toString(){
        return constantName;
    }

    @Override
    public String printSubtree(int indent) {
        return constantName;
    }

    @Override
    public String getName() {
        return constantName;
    }

    @Override
    public void setName(String newName) {
        oldName = constantName;
        constantName = newName;
    }

    @Override
    public String getOldName() {
        return oldName;
    }
    
    // transferable interface
    private static final DataFlavor flavors[] = { dmiTreeNode.constantFlavor };
    
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

    @Override
    public dmiTreeNode getRootNode() {
        dmiTreeNode anyNode = this;
        while (!anyNode.isRoot())
            anyNode = anyNode.getParent();
        
        return anyNode;
    }
}