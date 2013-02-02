package DataModel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import DataModelException.TreeModelException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * dmTreeModel
 * 
 * @author fairfax
 */
public class dmTreeModel implements TreeModel, dmiClassHierarchy {
    /**
     * root
     */
    private dmiTreeNode root;
    
    // for faster lookup by node name
    private HashMap<String,dmiTreeNode> nodeMap = new HashMap<String,dmiTreeNode>();
    
    /**
     *  listenerList
     */
    private EventListenerList listenerList = new EventListenerList();
    
    /**
     * Constructor of dmiTreeNode. Setting root as root node.
     * 
     * @param root 
     */
    public dmTreeModel(dmiTreeNode root){
        this.root = root;
        this.nodeMap.put(root.getName(), root);
    }
    
    /**
     * Invoked this to insert newChild at location index in parents children.
     * This will then message nodesWereInserted to create the appropriate
     * event. This is the preferred way to add children as it will create
     * the appropriate event.
     */
    public void insertNodeInto(dmiTreeNode parent, dmiTreeNode child) {
        // we don't allow constants to have children
        if (parent instanceof dmConstant) {
            return;
        }

        assert(parent instanceof dmClass);

        child.setParent(parent);
        parent.insert(child);

        int[] newIndexes = new int[1];
        newIndexes[0] = parent.children().indexOf(child);
        
        // update node map
        nodeMap.put(child.getName(), child);
        nodesWereInserted(parent, newIndexes);
    }

    /**
     * Message this to remove node from its parent. This will message
     * nodesWereRemoved to create the appropriate event. This is the
     * preferred way to remove a node as it handles the event creation
     * for you.
     */
    public void removeNodeFromParent(dmiTreeNode node) {
        assert(node != null);
        dmiTreeNode parent = node.getParent();

        if (parent == null) {
            throw new IllegalArgumentException("node does not have parent.");
        }

        // update of nodeMap (remove names of nodes from subtree - node included)
        List<dmiTreeNode> subtreeNodes = node.subtree();
        for (dmiTreeNode subNode : subtreeNodes) {
            nodeMap.remove(subNode.getName());
        }

        // removal of node subtree (node excluded)
        removeNodeSubtree(node);

        // removal of node itself
        int[] childIndex = new int[1];
        Object[] removedArray = new Object[1];

        childIndex[0] = parent.getIndex(node);
        removedArray[0] = node;
        parent.remove(node);
        nodesWereRemoved(parent, childIndex, removedArray);
    }

    /**
     * Remove all nodes from subtree begining in root (root excluded). 
     * @param root - starting node
     */
    private void removeNodeSubtree(dmiTreeNode root) {
        List<dmiTreeNode> children = root.children();
        if (children.isEmpty()) {
            return;
        }
        
        dmiTreeNode childNode;
        
        // fill in data structures for event
        int[] childIndex = new int[children.size()];
        Object[] removedArray = new Object[children.size()];
        for (int i=0; i < children.size(); ++i) {
            childNode = children.get(i);
            childIndex[i] = i;
            removedArray[i] = childNode;
        }
        
        // remove all child nodes
        while (root.children().size() > 0) {
            childNode = root.children().get(0);
            removeNodeSubtree(childNode);   // recursive call
            root.remove(childNode);
        }
        
        nodesWereRemoved(root,childIndex,removedArray);
    }
    
    /*
     * interface TreeModel
     */
    
    @Override
    public Object getRoot() {
        return root;
    }

    public dmiTreeNode[] getPathToRoot(dmiTreeNode node){
        if (node == null)
            throw new IllegalArgumentException();
        
        return node.getPath();
    }
    
    @Override
    public Object getChild(Object o, int i) {
        dmiTreeNode node = (dmiTreeNode)o;
        return node.getChildAt(i);
    }

    @Override
    public int getChildCount(Object o) {
        dmiTreeNode node = (dmiTreeNode)o;
        return node.getChildCount();
    }

    @Override
    public boolean isLeaf(Object o) {
        dmiTreeNode node = (dmiTreeNode)o;
        return node.isLeaf();
    }

    /**
     * Called when some node is edited.
     * Nodes are not editable - rename is done through renameNode method.
     * 
     * @param tp
     * @param o
     */
    @Override
    public void valueForPathChanged(TreePath tp, Object o) {
       return;
    }

    /**
     * 
     * @param parent parent node
     * @param child child node whose index we want
     * @return index of child node in list of parent node children
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        dmiTreeNode node = (dmiTreeNode)parent;
        return node.getIndex((dmiTreeNode)child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        listenerList.add(TreeModelListener.class, listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
       listenerList.remove(TreeModelListener.class, listener);
    }
    
    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param source the source of the {@code TreeModelEvent};
     *               typically {@code this}
     * @param path the path to the parent of the nodes that changed; use
     *             {@code null} to identify the root has changed
     * @param childIndices the indices of the changed elements
     * @param children the changed elements
     */
    protected void fireTreeNodesChanged(Object source, Object[] path,
            int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices,
                            children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param source the source of the {@code TreeModelEvent};
     *               typically {@code this}
     * @param path the path to the parent the nodes were added to
     * @param childIndices the indices of the new elements
     * @param children the new elements
     */
    protected void fireTreeNodesInserted(Object source, Object[] path,
            int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices,
                            children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
            }
        }
    }
    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param source the source of the {@code TreeModelEvent};
     *               typically {@code this}
     * @param path the path to the parent the nodes were removed from
     * @param childIndices the indices of the removed elements
     * @param children the removed elements
     */
    protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices,
                            children);
                }
                ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param source the source of the {@code TreeModelEvent};
     *               typically {@code this}
     * @param path the path to the parent of the structure that has changed;
     *             use {@code null} to identify the root has changed
     * @param childIndices the indices of the affected elements
     * @param children the affected elements
     */
    protected void fireTreeStructureChanged(Object source,
            Object[] path, int[] childIndices, Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path, childIndices,
                            children);
                }
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance 
     * is lazily created using the parameters passed into 
     * the fire method.
     *
     * @param source the source of the {@code TreeModelEvent};
     *               typically {@code this}
     * @param path the path to the parent of the structure that has changed;
     *             use {@code null} to identify the root has changed
     */
    protected void fireTreeStructureChanged(Object source, TreePath path) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new TreeModelEvent(source, path);
                }
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }

    /**
     * Invoke this method after you've inserted some TreeNodes into
     * node.  childIndices should be the index of the new elements and
     * must be sorted in ascending order.
     */
    protected void nodesWereInserted(dmiTreeNode node, int[] childIndices) {
        if (listenerList != null && node != null
                && childIndices != null && childIndices.length > 0) {
            int cCount = childIndices.length;
            Object[] newChildren = new Object[cCount];

            for (int counter = 0; counter < cCount; counter++) {
                newChildren[counter] = node.getChildAt(childIndices[counter]);
            }
            fireTreeNodesInserted(this, getPathToRoot(node),
                    childIndices, newChildren);
        }
    }

    /**
     * Invoke this method after you've removed some TreeNodes from
     * node.  childIndices should be the index of the removed elements and
     * must be sorted in ascending order. And removedChildren should be
     * the array of the children objects that were removed.
     */
    protected void nodesWereRemoved(dmiTreeNode node, int[] childIndices,
            Object[] removedChildren) {
        if (node != null && childIndices != null) {
            fireTreeNodesRemoved(this, getPathToRoot(node),
                    childIndices, removedChildren);
        }
    }

    /**
     * Invoke this method after you've changed how the children identified by
     * childIndicies are to be represented in the tree.
     */
    protected void nodesChanged(dmiTreeNode node, int[] childIndices) {
        if (node != null) {
            if (childIndices != null) {
                int cCount = childIndices.length;

                if (cCount > 0) {
                    Object[] cChildren = new Object[cCount];

                    for (int counter = 0; counter < cCount; counter++) {
                        cChildren[counter] = node.getChildAt(childIndices[counter]);
                    }
                    fireTreeNodesChanged(this, getPathToRoot(node),
                            childIndices, cChildren);
                }
            } else if (node == getRoot()) {
                fireTreeNodesChanged(this, getPathToRoot(node), null,
                        null);
            }
        }
    }

    /**
     * Invoke this method if you've totally changed the children of
     * node and its childrens children...  This will post a
     * treeStructureChanged event.
     */
    protected void nodeStructureChanged(dmiTreeNode node) {
        if (node != null) {
            fireTreeStructureChanged(this, getPathToRoot(node), null,
                    null);
        }
    }
    
    /**
     * Invoke this method after you've changed how node is to be
     * represented in the tree.
     */
    protected void nodeChanged(dmiTreeNode node) {
        if (listenerList != null && node != null) {
            dmiTreeNode parent = node.getParent();

            if (parent != null) {
                int anIndex = parent.getIndex(node);
                if (anIndex != -1) {
                    int[] cIndexs = new int[1];

                    cIndexs[0] = anIndex;
                    nodesChanged(parent, cIndexs);
                }
            } else if (node == getRoot()) {
                nodesChanged(node, null);
            }
        }
    }
    
    /*
     * interface dmiClassHierarchy
     */
    @Override
    public void insertNewClass(String name, dmiTreeNode parent) throws TreeModelException {
        if (name.isEmpty()) {
            throw new TreeModelException("Class name must not be empty.");
        }
        
        if (!(parent instanceof dmClass)) {
            throw new TreeModelException("Class can be added only into other class.");
        }
        
        if (isClassNameUnique(name))
            this.insertNodeInto(parent, new dmClass(name));
        else {
            throw new TreeModelException("Class name must be unique.");
        }   
    }
    
    @Override
    public void insertNewClass(dmiTreeNode parent) throws TreeModelException {
        if (parent instanceof dmClass) {
            this.insertNodeInto(parent, new dmClass(this.generateUniqueClassName()));
        } else {
            throw new TreeModelException("Class can be added only into other class.");
        }
    }
    
    @Override
    public void insertNewConstant(String name, dmiTreeNode parent, boolean taskDep) throws TreeModelException {
        if (name.isEmpty()) {
            throw new TreeModelException("Constant name must not be empty.");
        }
        
        if (parent instanceof dmClass) {
            if (this.isConstantNameUnique(name)) {
                this.insertNodeInto(parent, new dmConstant(name,taskDep));
            } else {
                throw new TreeModelException("Constant name must be unique.");
            }
        } else {
            throw new TreeModelException("Constant node can be added to a class only.");
        }
    }
    
    @Override
    public void insertNewConstant(dmiTreeNode parent, boolean taskDep) throws TreeModelException{
        if (parent instanceof dmClass) {
            this.insertNodeInto(parent, new dmConstant(this.generateUniqueConstName(),taskDep));
        } else {
            throw new TreeModelException("Constant can be added to a class only.");
        }
    }
    
    @Override
    public List<dmClass> getClassList() {
        LinkedList<dmClass> list = new LinkedList<dmClass>();
        
        for (dmiTreeNode item: nodeMap.values()) {
            if (item instanceof dmClass)
                list.add((dmClass) item);
        }
        
        return list;
    }

    @Override
    public List<dmConstant> getConstantList(String constClass, boolean taskDep, boolean taskIndep) {
        dmiTreeNode treeNode = nodeMap.get(constClass);
        assert(treeNode instanceof dmClass);
        
        dmClass classNode = (dmClass) treeNode; // typecast of corresponding class node
        
        LinkedList<dmConstant> result = new LinkedList<dmConstant>();
        for (dmiTreeNode child : classNode.children() ) { // walk through list of children
            if ( !(child instanceof dmConstant)) {
                continue;
            }
            // we filter only constants which are direct sibligs of classNode
            dmConstant constNode = (dmConstant) child;
            
            if (taskDep && constNode.isTaskDependent()) { // we want task dependent constants
                result.add(constNode);
                continue;
            }
            
            if (taskIndep && !(constNode.isTaskDependent())) { // we want task independent constants
                result.add(constNode);
            }
        }
        
        return result;
    }

    @Override
    public String testPrint() {
        return root.printSubtree(0);
    }

    @Override
    public dmClass getRootClass() {
        assert root instanceof dmClass;
        return (dmClass) root;
    }

    @Override
    public void setRootClass(dmClass newRoot) {
        this.root = newRoot;
        this.nodeMap.put(newRoot.getName(), newRoot);
        // update node map
        reinitNodeMap();
        fireTreeStructureChanged(this, new TreePath(getPathToRoot(root)));
    }
    
    @Override
    public dmiTreeNode getNodeByName(String name){
        if (nodeMap.containsKey(name))
            return nodeMap.get(name);
        else
            return null;
    }

    private void reinitNodeMap() {
        nodeMap = new HashMap<String,dmiTreeNode>();
        
        treeWalk(root);
    }

    /**
     * Walks the tree structure and add every encountered node into nodeMap.
     * @param node 
     */
    private void treeWalk(dmiTreeNode node) {
        if (node.getChildCount() == 0) {
            nodeMap.put(node.getName(), node);
            return;
        }
        
        for (dmiTreeNode child: node.children()) {
            treeWalk(child);
        }
        
        nodeMap.put(node.getName(), node);
    }

    /**
     * Search class tree for class with given name.
     * 
     * @param className name of class
     * @return dmClass with specified name if found. Otherwise null.
     */
    @Override
    public dmClass getClassByName(String className) {
        dmiTreeNode result = getNodeByName(className);
        if (result instanceof dmClass)
            return (dmClass)result;
        else
            return null;
    }

    /**
     * Search tree for constant with given name.
     * 
     * @param constName name of constant
     * @return  dmConstant with specified name. Otherwise null.
     */
    @Override
    public dmConstant getConstantByName(String constName) {
        dmiTreeNode result = getNodeByName(constName);
        if (result instanceof dmConstant)
            return (dmConstant)result;
        else
            return null;
    }

    private String generateUniqueClassName() {
        int i = 1;
        String result = "class" + i;
        while (!isClassNameUnique(result)) {
            ++i;
            result = "class" + i;
        }
        
        return result;
    }

    public boolean isClassNameUnique(String name) {
        dmiTreeNode node = this.getNodeByName(name);
        return (node == null);  // there is no such node => name is unique
    }

    public boolean isConstantNameUnique(String string) {
        dmiTreeNode node = this.getConstantByName(string);
        return (node == null);
    }

    private String generateUniqueConstName() {
        int i = 1;
        String result = "const" + i;
        while (!isClassNameUnique(result)) {
            ++i;
            result = "const" + i;
        }
        
        return result;
    }

    void clearTaskSpecificConstants() {
        // get list of constants which are to be removed
        // (removing inside the inner loop causes java.util.ConcurrentModificationException)
        HashSet<dmConstant> toRemove = new HashSet<dmConstant>();
        for (String node: nodeMap.keySet()) {
            dmiTreeNode treeNode = nodeMap.get(node);
            if (!(treeNode instanceof dmClass) ) {
                continue;
            }
            
            // get list of task dependent constants for this class
            List<dmConstant> constList = this.getConstantList(node, true, false);
            
            // remove all constants from the list
            for (dmConstant constNode: constList) {
                toRemove.add(constNode);
            }
        }
        
        // remove constants
        for (dmConstant removed: toRemove) {
            this.removeNodeFromParent(removed);
        }
    }

    public void renameNode(dmiTreeNode renamed, String newName) throws TreeModelException {
        if ((newName == null) || newName.isEmpty()) {
            throw new TreeModelException("Node name must not be empty.");
        }
        
        /*
         * Rename class node.
         */
        if (renamed instanceof dmClass) {
            dmClass crenamed = (dmClass) renamed;
            if (this.isClassNameUnique((String) newName)) {
                
                this.nodeMap.remove(crenamed.getName());   // update map
                this.nodeMap.put((String)newName,renamed);
                
                crenamed.setName((String) newName);
                nodeChanged(crenamed);
                return;
            } else {
                throw new TreeModelException("Class name must be unique.");
            }
        }
        /*
         * Rename constant node.
         */
        if (renamed instanceof dmConstant) {
            dmConstant constrenamed = (dmConstant) renamed;
            if (this.isConstantNameUnique((String) newName)) {
                
                this.nodeMap.remove(constrenamed.getName());   // update map
                this.nodeMap.put((String)newName,renamed);
                
                constrenamed.setName((String) newName);
                nodeChanged(constrenamed);
            } else {
                throw new TreeModelException("Constant name must be unique.");
            }
        }
    }

    @Override
    public void clearClassHierarchy() {
        // remove all children from root
        removeNodeSubtree(root);
    }

    @Override
    public dmClass getCommonAncestor(Set<String> classList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}