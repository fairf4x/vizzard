/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CustomizedClasses;

import DataModel.dmDomain;
import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/**
 *
 * @author fairfax
 */
public class DefinitionTableModel implements TableModel {

    /*
     * Content of definition tables is always string with a prefix to determine
     * string semantics. Available prefixes are listed here:
     */
    public static final char DELIMITER = ':';        // prefix delimiter
    public static final char CONST_PREFIX = 'C';     // prefix for constant value
    public static final char WILDC_PREFIX = 'W';     // prefix for wildcard
    public static final char CONST_SET_PREFIX = 'S'; // prefix for set of constants
    public static final char GOAL_SET_DELIMITER = ','; // delimiter of constants in goal set
    
    public static final String DEFAULT_CELL_VALUE = ""+WILDC_PREFIX + DELIMITER + dmDomain.W_UNIVERSAL;
    /**
     * Every cell in this table should match this regex:
     * 
     * cellData.matches(CELL_DATA_REGEX) should return true
     */
    public static final String CELL_DATA_REGEX = "[CWS]:\\S+"; // prefix:nonzero string without whitespaces
    
    protected EventListenerList listenerList = new EventListenerList();
    
    protected int columnCount;
    protected List<String> labels;
    protected List<String[]> modelData = null;
    
    public DefinitionTableModel(List<String> columnLabels) {
        assert(columnLabels != null);
        columnCount = columnLabels.size();
        labels = columnLabels;
        modelData = new LinkedList<String[]>();
    }
    
    @Override
    public int getRowCount() {
        return modelData.size();
    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public String getColumnName(int i) {
        return labels.get(i);
    }

    @Override
    public Class<?> getColumnClass(int i) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int col) {
        assert((row >=0) &&  (row < modelData.size()));
        assert((col >=0) && (col < columnCount));
        return modelData.get(row)[col];
    }

    @Override
    public void setValueAt(Object o, int row, int col) {
        assert((row >=0) &&  (row < modelData.size()));
        assert((col >=0) && (col < columnCount));
        assert(o instanceof String);
        modelData.get(row)[col] = (String) o;
        fireTableCellUpdated(row,col);
    }

    @Override
    public void addTableModelListener(TableModelListener tl) {
        listenerList.add(TableModelListener.class, tl);
    }

    @Override
    public void removeTableModelListener(TableModelListener tl) {
        listenerList.remove(TableModelListener.class, tl);
    }
    
    public void addRow(String[] rowData) {
        if (checkRow(rowData)) {
            modelData.add(rowData);
            int lastIndex = modelData.size() - 1;
            fireTableRowsInserted(lastIndex, lastIndex);
        } else {
            System.err.println("DefinitionTableModel.addRow: Illegal row data!!");
        }
    }
    public void delRows(int[] rowIndexes) {
        Arrays.sort(rowIndexes);
        int first = rowIndexes[0];
        int last = rowIndexes[rowIndexes.length-1];
        
        for (int i=rowIndexes.length-1; i >= 0; --i) {
            int row = rowIndexes[i];
            assert((row >=0) &&  (row < modelData.size()));
            modelData.remove(row);
        }
        fireTableRowsDeleted(first,last);
    }
    
    public void delRow(int row) {
        assert((row >=0) &&  (row < modelData.size()));
        modelData.remove(row);
        fireTableRowsDeleted(row,row);
    }
    
    private void fireTableCellUpdated(int row, int col){
        fireTableChanged(new TableModelEvent(this,row,row,col));
    }
    
    private void fireTableStructureChanged(){
        fireTableChanged(new TableModelEvent(this,TableModelEvent.HEADER_ROW));
    }
    
    private void fireTableRowsInserted(int firstRow, int lastRow){
        fireTableChanged(new TableModelEvent(this,firstRow,lastRow,TableModelEvent.ALL_COLUMNS,TableModelEvent.INSERT));
    }
    
    private void fireTableRowsDeleted(int firstRow, int lastRow){  
        fireTableChanged(new TableModelEvent(this,firstRow,lastRow,TableModelEvent.ALL_COLUMNS,TableModelEvent.DELETE));
    }
    
    private void fireTableChanged(TableModelEvent e){
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TableModelListener.class) {
                ((TableModelListener) listeners[i + 1]).tableChanged(e);
            }
        }
    }
    
    /**
     * Method for comparing given row of two different models.
     * @param row row to compare
     * @param columnIndexes columns to be compared
     * @param first model to compare
     * @param second model to compare
     * @return true if matching columns with indexes from columnIndexes has all equal values (using equal() method)
     */
    public static boolean matchingRow(int[] columnIndexes, int firstRow, int secondRow, DefinitionTableModel first, DefinitionTableModel second) {
        assert ((first != null) && (second != null));
        if (first.equals(second)) { // two equal models has equal rows
            return true;
        }
        
        // compare specified columns - if there is a mismatch return false
        for (int i=0; i<columnIndexes.length; ++i) {
            int index = columnIndexes[i];
            Object firstVal = first.getValueAt(firstRow, index);
            Object secondVal = second.getValueAt(secondRow, index);
            if ( !firstVal.equals(secondVal) ) {
                return false;
            }
        }
        
        return true;
    }

    private boolean checkRow(String[] rowData) {
        if (rowData.length != this.columnCount) {
            return false;
        }
        
        for (int i=0; i<rowData.length; ++i) {
            if (rowData[i].isEmpty()) {     // emty cells are not allowed
                return false;
            }
            
            if (!rowData[i].matches(CELL_DATA_REGEX)) {
                return false;
            }
        }
        
        // nothing went wrong - guess the row is OK
        return true;
    }
    
    public boolean isInitColumn(int col) {
        return (col == (this.columnCount - 2)); // one before last column is init column
    }
    
    public boolean isGoalColumn(int col) {
        return (col == (this.columnCount - 1)); // last column is goal column
    }
    
    // content of DefinitionTableModel cell
    public static boolean prefixMatch(String cellStr, char prefix) {
        String[] splitCell = cellStr.split(""+DefinitionTableModel.DELIMITER);  // prefix will be at index 0 and value at 1
        assert (splitCell.length == 2);
        
        return (splitCell[0].charAt(0) == prefix);
    }
    
    public static String getCellValue(String cellStr) {        
        String[] splitCell = cellStr.split(""+DefinitionTableModel.DELIMITER);
        assert (splitCell.length == 2);
        
        return splitCell[1];
    }
    
        /**
     * Search for constants named "oldName" and replaces those with "newName".
     * @param tableModel - table model in which is renaming done
     * @param relevantColumns - indexes of columns which can contain replaced symbol
     * @param oldName - old name (simple string without prefix)
     * @param newName - new name (simple string without prefix)
     */
    public void renameConstantsInTable(Set<Integer> relevantColumns, String oldName, String newName) {
        // rename oldName to newName in relevant columns
        for (Integer col : relevantColumns) {
            for (int row = 0; row < getRowCount(); ++row) {
                String cell = (String) getValueAt(row, col);  // should contain only Strings in format("regex"): "[CWS]:xxxxx"
                                
                if (prefixMatch(cell,CONST_PREFIX)) {
                    String oldConst = getCellValue(cell);
                    if (oldConst.equals(oldName)) {
                        String newVal = ""+CONST_PREFIX + DELIMITER + newName;
                        setValueAt(newVal, row, col);   // replace old constant name with new name
                    }
                    continue;
                }
                
                if (prefixMatch(cell,CONST_SET_PREFIX)) {
                    // rename all items in the goal set
                    String goalSetStr = getCellValue(cell);
                    String renamedSet = goalSetStr.replace(oldName, newName);
                    
                    // insert renamed set back
                    String newVal = ""+CONST_SET_PREFIX + DELIMITER + renamedSet;
                    setValueAt(newVal, row, col);
                    continue;
                }
                
                assert(prefixMatch(cell,DefinitionTableModel.WILDC_PREFIX));
            }
        }
    }

    /**
     * Rename table labels - if found.
     * @param oldName - label to be renamed
     * @param newName - new name of the renamed label
     */
    public void renameClassInHeader(String oldName, String newName) {
        for (int i=0; i<labels.size(); ++i) {
            String currentLabel = labels.get(i);
            if (currentLabel.equals(oldName)) {
                labels.set(i, newName);
            }
        }
        
        fireTableStructureChanged();
    }
}
