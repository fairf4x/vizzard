/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import CustomizedClasses.DynamicTableModel;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.TableModelListener;

/**
 *
 * @author fairfax
 */
public class dmRelationModel extends DynamicTableModel implements dmiElementSet {
    
    // Map for fast lookup and summary of defined elements - is inherited by dmStateVariableModel
    // <elementName,relationDelegate>
    protected HashMap<String, dmDelegate> delegatesMap;
 
    public dmRelationModel(){
        // we will set some columns for start
        this.setColumnCount(5);
        
        // relation name is in the first column
        this.setColumnClass(ELEMENT_NAME, String.class);
        // user can edit relation name
        this.setColumnEditable(ELEMENT_NAME, true);
        
        // other columns will determine argument classes
        this.setDefaultColumnClass(dmClass.class);
        
        // delegatesMap store active delegates for each defined relation
        this.delegatesMap = new HashMap<String, dmDelegate>();
    }
    
    @Override
    public String testPrint() {
        String result = "";
        for (Object[] dataRow: data){
            
            result += dataRow[dmiElementSet.ELEMENT_NAME].toString() + "(";
            for (int i=dmiElementSet.ELEMENT_NAME+1; i<dataRow.length; ++i){
                if (dataRow[i] == null)
                    break;
                
                result += " " + dataRow[i].toString();
            }
            result += " )\n";
        }
        
        return result;
    }
    
    @Override
    public void addEntry(){
        // default unique name selection
        String name = "relation_";
        
        int i=1;
        while (!isNameUnique(name + i)) {   // find first unique name
            ++i;
        }
        
        name = name +i; // assign unique name
        
        int index = this.addNewRow();
        this.setValueAt(name, index, ELEMENT_NAME);
        delegatesMap.put(name, new dmRelationDelegate(this,name)); // adds self to table listeners
    }
    
    /**
     * Delete specified row from table.
     * Method is inherited by dmStateVariableModel.
     * @param tableRow deleted row index
     */
    @Override
    public void delEntry(int tableRow){
        // get name of removed element
        Object[] elementRow = data.get(tableRow);
        String relName = (String) elementRow[dmiElementSet.ELEMENT_NAME];
        
        // remove element from delegate map
        dmDelegate removed = delegatesMap.remove(relName);

        // delete row
        this.deleteRow(tableRow);  // send event to table listeners (to delegates)
        
        // remove from table listeners
        this.removeTableModelListener(removed);
    }
    
    /**
     * Control method for TransferHandler.
     * Returns true if col is lowest undefined (i.e. null) argument column in tableRow.
     * 
     * @param tableRow
     * @param col
     * @return 
     */
    public boolean insideValidArgRange(int tableRow, int col) {
        return ((getFirstFree(tableRow) >= col) && (col > 0));
    }
    
    public void setArgument(dmClass arg, int row, int col){
        // expand table if needed
        if (this.getRowCount() < row)
            this.setRowCount(row);
        
        if (this.getColumnCount() < col)
            this.setColumnCount(col);
        
        this.setValueAt(arg, row, col);
    }

    @Override
    public String getColumnName(int i) {
        if (i == ELEMENT_NAME)
            return "relation name";
        
        return "arg " + (i-1);
    }
    
    @Override
    public List<dmDelegate> getDelegateList() {
        return new LinkedList<dmDelegate>(delegatesMap.values());  
    }

    /**
     * Used for inserting relations readed from XML.
     * Inserts in the row specified by relation.getID().
     * 
     * @param relation data structure providing all necessary information
     */
    @Override
    public void insertItem(Object[] itemData) {
        assert(itemData.length == dmiElementSet.RELATION_ITEM_DATA_LEN);
        String name = (String)itemData[ITEM_NAME];
        List<dmClass> arguments = (List<dmClass>)itemData[ITEM_ARGS];
        
        int row = this.addNewRow();
        
        // we need to fill in relation arguments and its name into the table
        int columnsNeeded = arguments.size() + 1;
        
        if (this.getColumnCount() < columnsNeeded) {
            this.setColumnCount(columnsNeeded);
        }

        // fill in name
        this.setValueAt(name, row, ELEMENT_NAME);

        // fill in arguments
        int col = ELEMENT_NAME + 1;
        
        for (dmClass arg : arguments) {
            this.setValueAt(arg, row, col);
            ++col;
        }

        delegatesMap.put(name, new dmRelationDelegate(this,name));
    }

    /**
     * Method used by dmDelegate to get current data.
     * @param elementID
     * @return 
     */
    @Override
    public Object[] getElementRowByName(String elementID) {
        for (int row=0; row<getRowCount(); ++row) {
            Object[] actRow = data.get(row);
            if (elementID.equals(actRow[ELEMENT_NAME])) {
                return actRow;
            }
        }
        
        return null;
    }

    public int getArgColumnCount() {
        return (getColumnCount() - argStartIndex());
    }
    
    @Override
    public int argStartIndex() {
        return ELEMENT_NAME + 1;
    }

    @Override
    public int getElementCount() {
        return delegatesMap.size();
    }
    
    protected void addCellToRow(Object o, int row){
        if (row < 0 || row >= data.size())
            throw new IndexOutOfBoundsException("Can not append cell - row does not exist.");
        
        int freeIndex = getFirstFree(row); 
        if (freeIndex >= 0) {               // we can add cell - space is already available
            setValueAt(o,row,freeIndex);
        }
        else {                              // we need to expand the table
            int index = getColumnCount();
            setColumnCount(index + 1); // maxColumnCount is changed
            setValueAt(o,row,index);
        }   
    }
 
    protected int getFirstFree(int row) {
        int freeIndex = this.getColumnCount();
        for (int i = ELEMENT_NAME + 1; i < this.getColumnCount(); ++i) {
            Object val = getValueAt(row, i);
            if ( val == null) {
                freeIndex = i;
                break;
            }
        }

        return freeIndex;
    }
    
    /**
     * Find delegate for element with given name
     * @param element - element name
     * @return delegate for element or null if not found
     */
    @Override
    public dmDelegate getElementDelegateByName(String element) {
        return delegatesMap.get(element);
    }
    
    @Override
    public boolean isNameUnique(String name) {
        
        for (int i=0; i<this.getElementCount(); ++i) {  // check all present elements
            String currentName = (String)this.getValueAt(i, ELEMENT_NAME);
            System.out.println();
            if (currentName.equals(name))   // if the name is already used return false
                return false;
        }
        
        return true;
    }
    
    @Override
    public void setValueAt(Object o, int row, int col) {
        // catch element rename to update delegatesMap
        if (col == dmiElementSet.ELEMENT_NAME) {
            String newName = (String) o;
            String oldName = (String) this.getValueAt(row, col);
            
            // remove old record
            dmDelegate deleg = delegatesMap.get(oldName);
            delegatesMap.remove(oldName);
            
            // insert again with new name
            delegatesMap.put(newName, deleg);
        }
        
        super.setValueAt(o, row, col);
    }

    /**
     * Return list of relation templates.
     * Relation templates are used in SelectRelationsDialog.
     * 
     * @return 
     */
    public List<dmRelationTemplate> getAvailableRelations() {
        List<dmRelationTemplate> result = new LinkedList<dmRelationTemplate>();
        
        for (int row=0; row<this.getRowCount(); ++row) {
            String relName = (String)data.get(row)[ELEMENT_NAME];
            String[] args = listToStringArray(getArgumentNames(row));
            
            dmRelationTemplate relation = new dmRelationTemplate(relName,args);
            result.add(relation);
        }
        
        return result;
    }

    protected List<String> getArgumentNames(int row) {
        LinkedList<String> list = new LinkedList<String>();
        Object[] elemData = data.get(row);

        int argStart = argStartIndex();
        for (int i = argStart; i < elemData.length; ++i) {
            if (elemData[i] != null) {
                list.add(elemData[i].toString());
            } else {
                break;
            }
        }
        return list;
    }

    protected static String[] listToStringArray(List<String> list) {
        String[] result = new String[list.size()];
        
        for (int i=0; i < result.length; ++i) {
            result[i] = list.get(i);
        }
        
        return result;
    }
    
    @Override
    protected void clearModel() {
        super.clearModel();
    }
    
    @Override
    public void clearElementSet() {
                
        clearListenerList(delegatesMap.values());
        
        clearModel();
        
        // we will set some columns for start
        this.setColumnCount(5);
        
        // relation name is in the first column
        this.setColumnClass(ELEMENT_NAME, String.class);
        // user can edit relation name
        this.setColumnEditable(ELEMENT_NAME, true);
        
        // other columns will determine argument classes
        this.setDefaultColumnClass(dmClass.class);
        
        // delegatesMap store active delegates for each defined relation
        this.delegatesMap = new HashMap<String, dmDelegate>();
    }

    /**
     * Remove all delegates from listener list.
     * @param values 
     */
    protected void clearListenerList(Collection<dmDelegate> values) {
        for (dmDelegate deleg: values) {
            listenerList.remove(TableModelListener.class, deleg);
        }
    }

    @Override
    public void addArgumentColumn() {
        int col = this.getColumnCount();
        this.setColumnCount(col+1);
    }

    @Override
    public void delArgumentColumn() {
        int colCnt = this.getColumnCount();
        int removedIndex = colCnt -1;
        if (removedIndex == ELEMENT_NAME) {
            System.err.println("Can not remove name column.");
            return;
        }
        deleteColumn(removedIndex);
    }
}
