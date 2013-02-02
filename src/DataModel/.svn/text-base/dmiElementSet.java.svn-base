/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.List;
import javax.swing.table.TableModel;

/**
 *
 * @author fairfax
 */
public interface dmiElementSet extends TableModel {
    
    /**
     * Clear all elements.
     * 
     */
    public void clearElementSet();
    
    // Table column indexes:
    // index to element (relation/state variable) data where relation name is stored.
    public final static int ELEMENT_NAME = 0;
    // indexes to state variable data
    public static final int STATE_VAR_RANGE = 1;
    
    // indexes for transformation from XML - used in insertItem
    public static final int ITEM_NAME = 0;
    public static final int ITEM_ARGS = 1;
    public static final int ITEM_RANG = 2;
    
    public static final int RELATION_ITEM_DATA_LEN = 2; // ITEM_NAME | ITEM_ARGS 
    public static final int STATEVAR_ITEM_DATA_LEN = 3; // ITEM_NAME | ITAM_ARGS | ITEM_RANGE
    
    /**
     * Get delegate by element (relation/state variable) name.
     * @param element name of element
     * @return 
     */
    public dmDelegate getElementDelegateByName(String element);
    
    public List<dmDelegate> getDelegateList();
    
    /**
     * Add new entry with unique name.
     */
    public void addEntry();
    
    /**
     * Delete entry at specified row.
     * @param tableRow row to be deleted
     */
    public void delEntry(int tableRow);
    
    /**
     * Add new empty argument column in the end of the table.
     */
    public void addArgumentColumn();
    
    /**
     * Delete last argument column of the table.
     */
    public void delArgumentColumn();
    
    public void insertItem(Object[] data);
    
    /**
     * dmiElementSet store elements as rows of table. First columns are used for
     * finite number of records describing element (such as name or value range for state variables).
     * Rest columns are used for argument description (each argument has its class).
     * 
     * @return index of table column of the first argument
     */
    public int argStartIndex();
    public int getElementCount();
    
    /**
     * Check for entry with specified name.
     * 
     * @param name element name to search
     * @return false if there is another element with this name true otherwise
     */
    public boolean isNameUnique(String name);
    
    /**
     * Return data defining relation with given id.
     * 
     * @param elementID element identification
     * @return cells of underlying table: |String elementID|...|...|...
     */
    public Object[] getElementRowByName(String elementID);
    /*
     * Testing purposes
     */
    public String testPrint();   
}