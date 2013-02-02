/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CustomizedClasses;

import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * 
 * @author fairfax
 */
public class DynamicTableModel implements TableModel, ListModel {

    private int maxColumnCount = 0;
    
    private HashMap<Integer,Class<?>> columnClassMap = new HashMap<Integer,Class<?>>();
    
    private Class<?> defaultColumnClass = String.class;
    
    private HashSet<Integer> editable = new HashSet();
    
    protected LinkedList<Object[]> data = new LinkedList<Object[]>();
    
    protected EventListenerList listenerList = new EventListenerList();
    
    protected void clearModel() {
        maxColumnCount = 0;
        columnClassMap = new HashMap<Integer,Class<?>>();
        defaultColumnClass = String.class;
        editable = new HashSet();
        
        int rowCnt = data.size();
        data = new LinkedList<Object[]>();
        
        // fire events to all listeners
        fireTableDataChanged();
        
        if (rowCnt > 0) {   // if there was something to clear
            fireContentsRemoved(this,0,rowCnt-1);
        }
    }
    
    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return maxColumnCount;
    }

    @Override
    public String getColumnName(int i) {
        return "" + i;
    }

    @Override
    public Class<?> getColumnClass(int col) {
       if (columnClassMap.containsKey(col)){
           return columnClassMap.get(col);
       }
       else
           return defaultColumnClass;
    }
    
    public void setDefaultColumnClass(Class<?> defaultClass){
        defaultColumnClass = defaultClass;
    }

    public void setColumnClass(int col, Class<?> val) {
        if (col < 0 || col > maxColumnCount)
            throw new IndexOutOfBoundsException("Can not set column class - column out of range.");
        
        columnClassMap.put(col, val);
    }
    
    @Override
    public boolean isCellEditable(int row, int col) {
        return editable.contains(col);
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (col >= maxColumnCount)
            throw new IndexOutOfBoundsException("Can not get Value - column index too big"); 
        
        assert(col >= 0);
        // 0 <= col < maxColumnCount
        
        Object[] line = data.get(row);
        
        if (col < line.length)
            return data.get(row)[col]; // there are really some data stored
        else
            return null; // when there are no data we return null
    }

    @Override
    public void setValueAt(Object o, int row, int col) {
        if (row < 0 || row >= data.size())
            throw new IndexOutOfBoundsException("Can not set value - row doesn't exist.");
        
        Object[] line = data.get(row);
        
        /* Create copy of original table row - it will be attached to event for further processing
         * in listeners - dmDelegate and its successors
         */
        Object[] copy = new Object[line.length];
        for (int i=0; i<line.length; ++i) {
            copy[i] = line[i];
        }
        
        if (col >= maxColumnCount)
            throw new IndexOutOfBoundsException("Can not set value - column index too big."); 
        
        assert(col >= 0);
        // 0 <= col < maxColumnCount
        
        if (col < line.length)
            line[col] = o;  // rewrite stored data
        else {
            Object[] newLine = new Object[col+1]; // allocate new line
            
            for (int i=0; i<line.length; ++i) {  // transfer data
                newLine[i] = line[i];
            }
            
            newLine[col] = o;                   // insert new value
            
            data.set(row, newLine);             // replace line with newLine
        }
        // TableModel event
        fireTableCellUpdated(row,col,copy);
        
        /*
         * TODO: following code is assuming that column 0 contains list-important value
         * like relation name or state variable name. Viz. comment at getElementAt.
         */
        if (col == 0) {
            // ListModel event
            fireContentsChanged(this,row,row);
        }
    }

    @Override
    public void addTableModelListener(TableModelListener tl) {
        listenerList.add(TableModelListener.class, tl);
    }

    @Override
    public void removeTableModelListener(TableModelListener tl) {
        listenerList.remove(TableModelListener.class, tl);
    }
    
    public void setRowCount(int count){
        int diff = count - data.size();
        
        if (diff == 0)
            return;
        
        if (diff < 0)
            throw new UnsupportedOperationException("Can not decrease row count - use delete row instead.");
        
        for (int i = 0; i<diff; ++i){
            data.add(new Object[Math.round(maxColumnCount / 2) + 1]); // we don't need rows to have maxColumnCount length
        }
        
        fireTableStructureChanged();
    }
    
    public void setColumnCount(int count){
        int diff = count - maxColumnCount;
        
        if (diff == 0)
            return;
        
        if (diff < 0)
            throw new UnsupportedOperationException("Can not decrease column count - use delete column instead.");
        
        maxColumnCount = count; // maxColumnCount is just bound which is respected in getValueAt and setValueAt
        
        fireTableStructureChanged();
    }
    
    private void fireTableDataChanged(){
        fireTableChanged(new DynamicTableModelEvent(this));
    }
    
    private void fireTableCellUpdated(int row, int col, Object[] originalRow){
        fireTableChanged(new DynamicTableModelEvent(this,row,row,col,originalRow));
    }
    
    private void fireTableStructureChanged(){
        fireTableChanged(new DynamicTableModelEvent(this,TableModelEvent.HEADER_ROW));
    }
    
    private void fireTableRowsInserted(int firstRow, int lastRow, Object[] originalRow){
        fireTableChanged(new DynamicTableModelEvent(this,firstRow,lastRow,TableModelEvent.ALL_COLUMNS,TableModelEvent.INSERT,originalRow));
    }
    
    private void fireTableRowsDeleted(int firstRow, int lastRow, Object[] originalRow){  
        fireTableChanged(new DynamicTableModelEvent(this,firstRow,lastRow,TableModelEvent.ALL_COLUMNS,TableModelEvent.DELETE,originalRow));
    }
    
    private void fireTableChanged(TableModelEvent e){
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableModelListener.class) {
                ((TableModelListener) listeners[i + 1]).tableChanged(e);
            }
        }
    }
    
    public void setColumnEditable(int col, boolean allowEdit){
        if (allowEdit)
            editable.add(col);
        else
            editable.remove(col);
    }
    
    /**
     * Append empty row at the end of the table.
     * New row has <code>maxColumnCount</code> columns which are all set to null value.
     * 
     * @return index of inserted row 
     */
    protected int addNewRow(){
        data.add(new Object[maxColumnCount]);
        int insertedIndex = data.size() - 1; // index of last item in list
        
        fireTableRowsInserted(insertedIndex,insertedIndex,null); // table event
        fireContentsAdded(this,insertedIndex,insertedIndex); // list event
        return insertedIndex; 
    }
    
    public void cleanRow(int row) {
        assert row >=0 && row < data.size();
        data.set(row, new Object[maxColumnCount]);
    }
    
    public void deleteRow(int row){
        Object[] deleted = data.get(row);
        Object[] copy = new Object[deleted.length];
        for (int i=0; i<deleted.length; ++i) {
            copy[i] = deleted[i];
        }
        
        data.remove(row);    
        fireTableRowsDeleted(row,row,copy); // table event
        fireContentsRemoved(this,row,row); // list event
    }
    
    public void deleteColumn(int column){
        assert((column < maxColumnCount) && (column >= 0));
        
        // remove target column data
        for (int row=0; row<data.size(); ++row) {
            Object[] rowData = data.get(row);
            if (rowData.length == maxColumnCount) {
                rowData = cutLastColumn(rowData);
                data.set(row, rowData);
            }
        }
        
        maxColumnCount -= 1;
        
        fireTableStructureChanged();
    }
    
    private void fireContentsChanged(Object source, int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).contentsChanged(e);
            }
        }
    }
    
    private void fireContentsAdded(Object source, int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).intervalAdded(e);
            }
        }    
    }
    
    private void fireContentsRemoved(Object source, int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).intervalRemoved(e);
            }
        }    
    }
    
    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public Object getElementAt(int i) {
        /*
         * TODO: this method returns value from (i,0) which mean i-th row and its "first" column
         * which is String name of relation in dmRelationModel and String name of State variable in dmStateVariableModel
         * This behavior should suffice for now however for sake of good coding practice it should be done in a more
         * elegant way -> move implementation of list model to rmRelationTableModel ?
         */
        return this.getValueAt(i,0);
    }

    @Override
    public void addListDataListener(ListDataListener ll) {
        listenerList.add(ListDataListener.class, ll);
    }

    @Override
    public void removeListDataListener(ListDataListener ll) {
        listenerList.remove(ListDataListener.class, ll);
    }
    
    public String printTable() {
        String result = "====== table ======\n";
        
        for (int row=0; row < this.getRowCount(); ++row) {
            for (int col=0; col < this.getColumnCount(); ++col) {
                Object cell = this.getValueAt(row, col);
                if (cell != null) {
                    result += cell.toString() + ", ";
                } else {
                    result += "EMPTY, ";
                }
            }
            result += "\n";
        }
        
        return result;
    }

    private Object[] cutLastColumn(Object[] rowData) {
        assert(rowData.length > 1);
        Object[] result = new Object[rowData.length - 1];
        
        for (int i=0; i < result.length; ++i) {
            result[i] = rowData[i];
        }
        
        return result;
    }
}
