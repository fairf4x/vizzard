/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import DataModelException.TreeModelException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author fairfax
 */
public interface dmiClassHierarchy {
    
    /**
     * Clear all classes and constants.
     */
    public void clearClassHierarchy();
    
    /*
     * Class related
     */
    
    /**
     * Insert new class as a child of given parent.
     * 
     * @param name new class name - if not unique class is not inserted
     * @param parent parent node of new class
     */
    public void insertNewClass(String name, dmiTreeNode parent) throws TreeModelException;
    
    /**
     * Insert new class as child of given parent.
     * Unique name for new class is generated.
     * 
     * @param parent parent node of new class
     */
    public void insertNewClass(dmiTreeNode parent) throws TreeModelException;
    
    public void insertNewConstant(String name, dmiTreeNode parent, boolean taskDep) throws TreeModelException;
    public void insertNewConstant(dmiTreeNode parent, boolean taskDep) throws TreeModelException;
    
    public dmClass getRootClass();
    public void setRootClass(dmClass newRoot);
    public dmiTreeNode getNodeByName(String nodeName);
    public List<dmClass> getClassList();
    /*
     * Constant related
     */
    
    /**
     * Using this method we can obtain list of constants defined as direct siblings of constClass.
     * We can decide if we need taskDependent constants - taskDep, domain specific constants - taskIndep
     * or both groups of constants.
     * 
     * @param constClass
     * @param taskDep
     * @param taskIndep
     * @return 
     */
    public List<dmConstant> getConstantList(String constClass, boolean taskDep, boolean taskIndep);
    
    /**
     * Return closest common ancestor class for given class set.
     * If there is no closest ancestor in hierarchy class "object" will be returned. 
     * 
     * @param classList
     * @return 
     */
    public dmClass getCommonAncestor(Set<String> classList);
    
    /*
     * Testing purposes
     */
    
    public String testPrint();

    public dmClass getClassByName(String contentClass);

    public dmConstant getConstantByName(String content);
}
