/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.LinkedList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * Base class implementing common functionality for dmRelationDelegete and dmStateVariableDelegate
 * Delegate can generate instances of dmiExpression which are based either on relations or state variables.
 * 
 * @author vodrazka
 */
public abstract class dmDelegate implements TableModelListener {


    
    /**
     * Every delegate has its own name unique in dataModel.
     * Delegate gets all information from dataModel from row starting with elementName.
     */
    protected String elementName;
    protected dmiElementSet dataModel;
    
    /**
     *  Delegate is declared with some row in DynamicTableModel.
     *  If this row is deleted, delegate may become "undeclared"
     *  and set this property to false.
     */
    protected boolean declared;
    
    public dmDelegate(dmiElementSet model, String elementID) {
        assert(model != null);
        this.elementName = elementID;
        this.dataModel = model;
        this.declared = true;
        // delegate which is registered as TableModelListener will receive informations about its "client" changes
        model.addTableModelListener(this);
    }
    
    /**
     * Mainly for printing purposes.
     * 
     * @return list of argument class names  
     */
    public List<String> getArgumentNames() {
        LinkedList<String> list = new LinkedList<String>();
        Object[] elemData = dataModel.getElementRowByName(elementName);

        int argStart = dataModel.argStartIndex();
        for (int i = argStart; i < elemData.length; ++i) {
            if (elemData[i] != null) {
                list.add(elemData[i].toString());
            } else {
                break;
            }
        }

        return list;
    }

    public List<dmClass> getArgumentValues() {
        if (!declared) {
            return null;
        }
        
        LinkedList<dmClass> list = new LinkedList<dmClass>();
        Object[] elemData = dataModel.getElementRowByName(elementName);

        int argStart = dataModel.argStartIndex();
        for (int i = argStart; i < elemData.length; ++i) {
            if (elemData[i] != null) {
                assert elemData[i] instanceof dmClass;
                list.add((dmClass) elemData[i]);
            } else {
                break;
            }
        }

        return list;
    }

    public int getArgumentCount() {
        Object[] elemData = dataModel.getElementRowByName(elementName);
        int result = 0;
        
        int argStart = dataModel.argStartIndex();
        for (int i = argStart; i < elemData.length; ++i) {
            if (elemData[i] != null) {
                result++;
            } else {
                break;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return elementName;
    }

    /**
     * Check if given table row begins with element name.
     * @param elementRow row returned from DynamicTableModel
     * @return 
     */
    protected boolean isMyRow(Object[] elementRow) {
        return elementName.equals(elementRow[dmiElementSet.ELEMENT_NAME]);
    }
    
    public boolean isDeclared() {
        return declared;
    }
    
    @Override
    public abstract void tableChanged(TableModelEvent tme);
}
