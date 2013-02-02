/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;


import CustomizedClasses.DynamicTableModelEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;

/**
 * Delegate class for StateVariable defined 
 * @author fairfax
 */
public class dmStateVariableDelegate extends dmDelegate {
    
    public dmStateVariableDelegate(dmiElementSet model, String stateVarName){
        super(model,stateVarName);
    }
    
    public final dmClassSet getValueRange(){
        Object[] elemData = dataModel.getElementRowByName(elementName);
        if (elemData[dmiElementSet.STATE_VAR_RANGE] instanceof dmClassSet) {
            return (dmClassSet)elemData[dmiElementSet.STATE_VAR_RANGE];
        } else {
            // if there are no data it should be null
            assert (elemData[dmiElementSet.STATE_VAR_RANGE] == null);
            // return empty class set
            return new dmClassSet();
        }    
    }
    
    public String printableDescription() {     
        String result = elementName;
        
        List<String> argClasses = this.getArgumentNames();
        
        if ( !argClasses.isEmpty() ) {
            result += "(";
            for (String arg: argClasses) {
                result += " " + arg;
            }
            result += ") in {";
        }
        
        dmClassSet range = this.getValueRange();
        
        for (String rangIt: range.toStringList()) {
            result += " " + rangIt;
        }
        result += "}";
        
        return result;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        assert(e.getFirstRow() == e.getLastRow());  // multiple row change is not possible
        assert (e.getSource() instanceof dmStateVariableModel);
        assert (e.getSource() == dataModel);
        
        if ( (e.getType() == TableModelEvent.INSERT) ||
             (e.getFirstRow() == TableModelEvent.HEADER_ROW) ||
             (e.getColumn() == TableModelEvent.ALL_COLUMNS) ) {    // do not process insert events
            return;
        }

        assert (e instanceof DynamicTableModelEvent);
        DynamicTableModelEvent dtme = (DynamicTableModelEvent) e;
        if (!isMyRow(dtme.getRowBeforeChange())) { // not my row - return
            return;
        }

        // my row was changed
        switch (e.getType()) {
            case TableModelEvent.UPDATE:
                System.out.println("StateVariableDelegate: tableChanged - UPDATE");
                // determine column of change
                if (e.getColumn() == dmiElementSet.ELEMENT_NAME) {
                    // name was changed
                    String newName = (String) dataModel.getValueAt(dtme.getFirstRow(), dmiElementSet.ELEMENT_NAME);
                    System.out.println(elementName + ": my name was changed to " + newName);
                    elementName = newName;
                    
                } else {
                    // other than name column was changed
                    System.out.println("StateVarDelegate: " + elementName + " - my declaration was changed.");
                }
                
                break;
            case TableModelEvent.DELETE:
                declared = false;
                System.out.println("StateVarDelegate: " + elementName + " - my declaration was deleted.");
                break;
            default:
                // this branch should never execute
                assert(true);
        }
    }
}
