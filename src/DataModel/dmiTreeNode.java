package DataModel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;

/**
 * Node interface for dmTreeModel.
 * 
 * @author fairfax
 */
public interface dmiTreeNode extends Transferable {
    public static final DataFlavor classFlavor = new DataFlavor(dmClass.class,"Class node");
    public static final DataFlavor constantFlavor = new DataFlavor(dmConstant.class,"Constant node");
    /**
     * Insert <code>node</code> as new child at the end of the list.
     * 
     * @param node 
     */
    public void insert(dmiTreeNode node);
    
    /**
     * Remove <code>node</code> from children list of this node. 
     * 
     * @param node 
     */
    public void remove(dmiTreeNode node);
    
    /**
     * Set new parent for this node.
     * 
     * @param parent 
     */
    public void setParent(dmiTreeNode parent);
    
    public List<dmiTreeNode> children();
    
    /**
     * Preorder traversal of subtree puting every node into list.
     * @return list of all nodes in subtree
     */
    public List<dmiTreeNode> subtree();
    
    public boolean getAllowsChildren();
    
    public dmiTreeNode getChildAt(int index);
    
    public int getChildCount();
    
    public int getIndex(dmiTreeNode node);
    
    public dmiTreeNode getParent();
    
    public boolean isLeaf();
    
    public boolean isRoot();
    
    /**
     * Return root node of the tree containing this node.
     *
     * @return root node of this tree 
     */
    public dmiTreeNode getRootNode();
    
    public dmiTreeNode[] getPath();
    
    public String printSubtree(int indent);
    
    public String getName();
    
    public void setName(String newName);
    
    /**
     * Return last node name before it was changed by setName method.
     * 
     * @return node name before change or null if setName was never called yet. 
     */
    public String getOldName();
}
