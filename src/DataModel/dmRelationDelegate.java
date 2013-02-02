/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import CustomizedClasses.DynamicTableModelEvent;
import javax.swing.event.TableModelEvent;

/**
 * This class is used as delegate for particular relation in dmiElementSet model.
 * It has read only access to model data. When model content is updated dmRelationDelegate can
 * notify ( TODO ) all instances which depends on particular relation that a change was made.
 * There should be only one instance of this class for each defined relation.
 * 
 * @author fairfax
 */
public class dmRelationDelegate extends dmDelegate {
   
    public dmRelationDelegate(dmiElementSet model, String relName){
        super(model,relName);
    }
    
    @Override
    public void tableChanged(TableModelEvent e) {
        assert(e.getSource() instanceof dmRelationModel);
        assert(e.getSource() == dataModel);

        if ( (e.getType() == TableModelEvent.INSERT) ||
             (e.getFirstRow() == TableModelEvent.HEADER_ROW) ||
             (e.getColumn() == TableModelEvent.ALL_COLUMNS) ) {    // do not process insert events
            return;
        }
        
        assert (e instanceof DynamicTableModelEvent);
        DynamicTableModelEvent dtme = (DynamicTableModelEvent) e;
        if (!isMyRow(dtme.getRowBeforeChange())) { // not my row or insert - do not process
            return;
        }
        
        // my row was changed
        switch (e.getType()) {
            case TableModelEvent.UPDATE:
                System.out.println("RelationDelegate: tableChanged - UPDATE");
                // determine column of change
                if (e.getColumn() == dmiElementSet.ELEMENT_NAME) { // watch for name change
                    // name was changed
                    String newName = (String) dataModel.getValueAt(dtme.getFirstRow(), dmiElementSet.ELEMENT_NAME);
                    System.out.println(elementName + ": my name was changed to " + newName);
                    elementName = newName;
                } else {
                    // something other than name was changed
                    System.out.println("RelationDelegate: " + elementName + " - my declaration was changed.");
                }
                break;
            case TableModelEvent.DELETE:
                declared = false;
                System.out.println("RelationDelegate: " + elementName + " - my declaration was deleted.");
                break;
            default:
                // this branch should never execute
                assert(true);
        }
    }
}
