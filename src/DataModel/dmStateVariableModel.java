/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.TableModelListener;

/**
 * Main data structure for State Variables. Each state variable has its own delegate
 * who listen for changes in the data model. <= TODO
 * 
 * @author fairfax
 */
public class dmStateVariableModel extends dmRelationModel implements dmiElementSet {
    
    /**
     * for each defined state variable contains list of transitions
     * - updates when range changed
     */
    private Map<String,Map<Integer,dmTransitionTemplate>> transitionMap = new HashMap<String,Map<Integer,dmTransitionTemplate>>();
    
    public dmStateVariableModel(){
        this.setColumnCount(5);
        
        // first column will contain state variable name
        this.setColumnClass(ELEMENT_NAME, String.class);
        this.setColumnEditable(ELEMENT_NAME, true);
        
        // second column is reserved for set of classes which
        // determine state variable possible set of values
        this.setColumnClass(STATE_VAR_RANGE, dmClassSet.class);
        
        // rest of columns is for agruments
        this.setDefaultColumnClass(dmClass.class);
        delegatesMap = new HashMap<String, dmDelegate>();
    }

    @Override
    public void addEntry(){
        // default unique name selection
        String name = "state_variable_";
        
        int i=1;
        while (!isNameUnique(name + i)) {   // find first unique name
            ++i;
        }
        
        name = name +i; // assign unique name
        
        int index = this.addNewRow();
        this.setValueAt(name, index, ELEMENT_NAME);

        delegatesMap.put(name, new dmStateVariableDelegate(this,name)); // in constructor add self to table listeners
        
        // initialize record in transition map
        transitionMap.put(name, new HashMap<Integer,dmTransitionTemplate>());
    }
    
    @Override
    public void delEntry(int tableRow) {
        // update transitionMap
        String name = (String) data.get(tableRow)[ELEMENT_NAME];
        transitionMap.remove(name);
        super.delEntry(tableRow);
    }
    
    @Override
    public String getColumnName(int i) {
        if (i == ELEMENT_NAME)
            return "state variable name";
        
        if (i == STATE_VAR_RANGE)
            return "value range";
            
        return "arg " + (i-2);
    }
    
    /**
     * Value newClass will be added into set of classes for
     * state variable on tableRow.
     * 
     * @param newClass
     * @param tableRow 
     */
    public void extendDomain(dmClass newClass, int tableRow){
        dmClassSet varDom = (dmClassSet)data.get(tableRow)[STATE_VAR_RANGE];
        if (varDom == null) {
            varDom = new dmClassSet();
        }
        varDom.addClass(newClass);
        
        // update record in transitionMap
        String name = (String) data.get(tableRow)[ELEMENT_NAME];
        Map<Integer,dmTransitionTemplate> oldMap = transitionMap.get(name);
        updateTransitions(oldMap,varDom);
        
        this.setValueAt(varDom, tableRow, STATE_VAR_RANGE);
    }
    
    public void clearDomain(int tableRow) {
        this.setValueAt(null, tableRow, STATE_VAR_RANGE);
        
        // clear transition map
        String name = (String) data.get(tableRow)[ELEMENT_NAME];
        transitionMap.get(name).clear();
    }
    
    @Override
    public void setValueAt(Object o, int row, int col) {
        // catch element rename to update transitionMap
        if (col == dmiElementSet.ELEMENT_NAME) {
            String newName = (String) o;
            String oldName = (String) this.getValueAt(row, col);
            
            // remove old record
            Map<Integer,dmTransitionTemplate> transMap = transitionMap.get(oldName);
            transitionMap.remove(oldName);
            
            // insert again with new name
            transitionMap.put(newName, transMap);
        }
        
        super.setValueAt(o, row, col);
    }
    
    @Override
    protected int getFirstFree(int row) {
        int freeIndex = this.getColumnCount();
        for (int i = STATE_VAR_RANGE + 1; i < this.getColumnCount(); ++i) {
            Object val = getValueAt(row, i);
            if ( val == null) {
                freeIndex = i;
                break;
            }
        }

        return freeIndex;
    }
    
    @Override
    public String testPrint(){
        String result = "";
        
        for (Object[] dataRow: data){
            
            result += dataRow[ELEMENT_NAME] + "(";
            for (int i=STATE_VAR_RANGE+1; i<dataRow.length; ++i){
                if (dataRow[i] == null)
                    break;
                
                result += " " + dataRow[i].toString();
            }
            result += " ) in { " + dataRow[STATE_VAR_RANGE].toString() + " }\n";
        }
        
        return result;
    }

    /**
     * Used to insert state variable readed from XML.
     * Inserts in the row specified by stateVariable.getID().
     * 
     * @param stateVariable data structure holding necessary information 
     */
    @Override
    public void insertItem(Object[] itemData) {
        assert (itemData.length == dmiElementSet.STATEVAR_ITEM_DATA_LEN);
        String name = (String)itemData[ITEM_NAME];
        List<dmClass> arguments = (List<dmClass>)itemData[ITEM_ARGS];
        dmClassSet valueRange = (dmClassSet)itemData[ITEM_RANG];
                
        int row = this.addNewRow();
        
        // we need to fill in the arguments, value range and name
        int columnsNeeded = arguments.size() + 2;
        if (this.getColumnCount() < columnsNeeded) {
            this.setColumnCount(columnsNeeded);
        }

        // fill in name
        this.setValueAt(name, row, ELEMENT_NAME);

        // fill in value range
        this.setValueAt(valueRange, row, STATE_VAR_RANGE);

        // fill in arguments
        int col = STATE_VAR_RANGE + 1;

        for (dmClass arg : arguments) {
            this.setValueAt(arg, row, col);
            ++col;
        }
        
        delegatesMap.put(name, new dmStateVariableDelegate(this,name));
        
        transitionMap.put(name, initTransitions(valueRange));
    }

    @Override
    public int argStartIndex(){
        return STATE_VAR_RANGE + 1;
    }

    /**
     * Generate all possible transitions based on given valueRange.
     * 
     * @param valueRange
     * @return 
     */
    private Map<Integer, dmTransitionTemplate> initTransitions(dmClassSet valueRange) {
        Map<Integer, dmTransitionTemplate> transitions = new HashMap<Integer, dmTransitionTemplate>();
        Set<dmClass> classSet = valueRange.toSet();

        for (dmClass fromClass : classSet) {               // add all possible transitions and enable each of them
            for (dmClass toClass : classSet) {
                dmTransitionTemplate newTrans;
                if (fromClass.equals(toClass)) {
                    newTrans = new dmTransitionTemplate(fromClass.getName()); // prevailing version
                    transitions.put(newTrans.hashCode(), newTrans);
                    newTrans = new dmTransitionTemplate(fromClass.getName(), toClass.getName()); // non-prevailing version
                    transitions.put(newTrans.hashCode(), newTrans);
                } else {
                    newTrans = new dmTransitionTemplate(fromClass.getName(), toClass.getName()); // non-prevailing
                    transitions.put(newTrans.hashCode(), newTrans);
                }
            }
        }

        return transitions;
    }

    private void updateTransitions(Map<Integer, dmTransitionTemplate> transMap, dmClassSet varDom) {
        // TODO - ? what to do if transitions are removed by value range change
        Set<dmClass> classSet = varDom.toSet();
        // add new transitions if any
        for (dmClass fromVal: classSet) {
            for (dmClass toVal: classSet) {
                if (fromVal.equals(toVal)) {
                    // need to add two transitions - prevailing and nonprevailing 
                    // prevailing
                    addNewTransition(transMap,new dmTransitionTemplate(fromVal.getName()));
                    // non-prevailing
                    addNewTransition(transMap,new dmTransitionTemplate(fromVal.getName(),toVal.getName()));
                } else {
                    // only non-prevailing transition will be added
                    addNewTransition(transMap,new dmTransitionTemplate(fromVal.getName(),toVal.getName()));
                }
            }
        }
    }

    /**
     * Insert newTrans into transMap.
     * @param transMap
     * @param newTrans
     */
    private void addNewTransition(Map<Integer, dmTransitionTemplate> transMap, dmTransitionTemplate newTrans) {
        // do not add existing transition
        assert(transMap != null);
        assert(newTrans != null);
        if (!transMap.containsKey(newTrans.hashCode())) {
            transMap.put(newTrans.hashCode(), newTrans);
        }
    }

    public List<dmStateVariableTemplate> getAvailableStateVariables() {
        List<dmStateVariableTemplate> result = new LinkedList<dmStateVariableTemplate>();
        
        for (int row=0; row<this.getRowCount(); ++row) {
            String stVarName = (String)data.get(row)[ELEMENT_NAME];
            String[] args = listToStringArray(getArgumentNames(row));
            
            assert(transitionMap.containsKey(stVarName));
            List<dmTransitionTemplate> trans = new LinkedList<dmTransitionTemplate>(transitionMap.get(stVarName).values());
            
            dmStateVariableTemplate stateVar = new dmStateVariableTemplate(stVarName,args,trans);
            result.add(stateVar);
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
        
        this.setColumnCount(5);
        
        // first column will contain state variable name
        this.setColumnClass(ELEMENT_NAME, String.class);
        this.setColumnEditable(ELEMENT_NAME, true);
        
        // second column is reserved for set of classes which
        // determine state variable possible set of values
        this.setColumnClass(STATE_VAR_RANGE, dmClassSet.class);
        
        // rest of columns is for agruments
        this.setDefaultColumnClass(dmClass.class);
        delegatesMap = new HashMap<String, dmDelegate>();
    }
    
    @Override
    public void delArgumentColumn() {
        int colCnt = this.getColumnCount();
        int removedIndex = colCnt -1;
        if (removedIndex == STATE_VAR_RANGE) {
            System.err.println("Can not remove range column.");
            return;
        }
        deleteColumn(removedIndex);
    }
}
