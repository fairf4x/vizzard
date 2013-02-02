/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CustomizedClasses;

import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;

/**
 *
 * @author fairfax
 */
public class DynamicTableModelEvent extends TableModelEvent {
    
    /**
     * oldRow provides information in case of change in one single row
     */
    private Object[] oldRow;
            
    public DynamicTableModelEvent(TableModel source) {
        super(source);
        oldRow = null;
    }
    
    public DynamicTableModelEvent(TableModel source, int row) {
        super(source,row);
        oldRow = null;
    }
    
    public DynamicTableModelEvent(TableModel source, int firstRow, int lastRow) {
        super(source,firstRow,lastRow);
        oldRow = null;
    }
    
    public DynamicTableModelEvent(TableModel source, int firstRow, int lastRow, int column, Object[] originalRow) {
        super(source,firstRow, lastRow, column);
        if (firstRow == lastRow) {
            oldRow = originalRow;
        } else {
            oldRow = null;
        }
    }
    
    public DynamicTableModelEvent(TableModel source, int firstRow, int lastRow, int column, int type, Object[] originalRow) {
        super(source,firstRow,lastRow,column,type);
        if (firstRow == lastRow) {
            oldRow = originalRow;
        } else {
            oldRow = null;
        }
    }
    
    public Object[] getRowBeforeChange() {
        return oldRow;
    }
}
