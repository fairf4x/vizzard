/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ApplicationGUI;

import ApplicationGUIException.ExpressionListViewException;
import DataModel.dmiExpressionListModel;
import DataModel.ExpressionListEvent;
import DataModel.dmiExpressionListListener;
import CustomizedClasses.DynamicTableModel;
import DataModel.dmClass;
import DataModel.dmConstant;
import DataModel.dmSlot;
import DataModel.dmiSlotSetHandler;
import DataModel.dmiTransition;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;




/**
 *
 * @author fairfax
 */
public class ExpressionListView extends JTable implements dmiExpressionListListener, dmiSlotSetHandler{
    private static final int FONT_SIZE = 16;
    public static final Font LABEL_FONT = new Font("sansserif",Font.BOLD,FONT_SIZE);
    public static final Font SLOT_FONT = new Font("sansserif",Font.PLAIN,FONT_SIZE);
    private static final int EXPR_ROW_HEIGHT = 30;

    /**
     * Check if varName is name of some variable in the list of selected slots.
     * 
     * @param varName
     * @return first slot with the variable name varName or null if no such slot exists 
     */
    public dmSlot isVariableNameInSelection(String varName) {
        for (dmSlot slot: selectedSlots) {
            Object slotData = slot.getData();

            if (slotData instanceof String) { // object variable content
                String slotString = (String) slotData;
                if (varName.equals(slotString)) { // variable name is equal with variable name in other selected slot
                    return slot;
                }
                continue;
            }  
                        
            if (slotData instanceof dmConstant) { // constant content
                continue;
            }
        }
            
        return null;
    }

    void connectSelectedToOne(dmSlot slotWithName) {      
        dmSlot.connectSlotsToOne(slotWithName, selectedSlots);
        exprListModel.clearUnusedValues();
        clearSlotSelection();
    }

//  classes are displayed directly in cell - no need for tooltip
//    private class MouseMotionListener implements java.awt.event.MouseMotionListener {
//
//        public MouseMotionListener() {
//        }
//
//        @Override
//        public void mouseDragged(MouseEvent me) {
//            // we do not need this
//            return;
//        }
//
//        @Override
//        public void mouseMoved(MouseEvent me) {
//            setTableToolTip(me);
//        }
//
//        private void setTableToolTip(MouseEvent me) {
//            Object source = me.getSource();
//            if (source instanceof ExpressionListView) {
//                // get index of row under cursor
//                ExpressionListView elw = (ExpressionListView) source;
//                int row = elw.rowAtPoint(me.getPoint());
//                int col = elw.columnAtPoint(me.getPoint());
//                
//                dmiExpressionListModel exprListModel = elw.getListModel();
//                dmClass slotClass = exprListModel.getClassAt(row, col);
//                if (slotClass != null) {
//                    elw.setToolTipText(slotClass.getName());
//                }
//            }
//        }
//    }
    
    private class MouseListener implements java.awt.event.MouseListener {

        public MouseListener() {
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            int row = ((JTable) me.getSource()).getSelectedRow();
            int col = ((JTable) me.getSource()).getSelectedColumn();
            selectCell(row, col, slotSelectionEnabled);
        }

        @Override
        public void mousePressed(MouseEvent me) {
            // not used
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            // not used
        }

        @Override
        public void mouseEntered(MouseEvent me) {
            // not used
        }

        @Override
        public void mouseExited(MouseEvent me) {
            // not used 
        }

        private void selectCell(int row, int col, boolean selectionEnabled) {
            if (!selectionEnabled) {
                return;
            }

            Object cell = getModel().getValueAt(row, col);
            if (cell instanceof dmSlot) {
                if (selectedSlots.contains((dmSlot) cell)) {
                    selectedSlots.remove((dmSlot) cell);
                    slotPositions.remove((dmSlot) cell);
                } else {
                    selectedSlots.add((dmSlot) cell);
                    slotPositions.put((dmSlot) cell,new Pos(row,col));
                }
                // because of repaint 
                getModel().setValueAt(cell, row, col);
            }
        }
    }
    private class Pos {
        public int row;
        public int col;
        public Pos(int r, int c) {
            row = r;
            col = c;
        }
    }
    
    public final static int COLUMN_MARGIN = 4;
    
    private dmiExpressionListModel exprListModel;
    
    private Set<dmSlot> selectedSlots;
    private Map<dmSlot,Pos> slotPositions;
    
    private boolean slotSelectionEnabled = true;
    
    public ExpressionListView() {
        super();
        this.selectedSlots = new HashSet<dmSlot>();
        this.slotPositions = new HashMap<dmSlot,Pos>();
        DynamicTableModel tableModel = new DynamicTableModel();
        this.setModel(tableModel);
        this.showVerticalLines = false;
        this.setRowHeight(EXPR_ROW_HEIGHT);
        // selection mode
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setCellSelectionEnabled(true);
        
        // set renderer
        CustomTableCellRenderer renderer = new CustomTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        this.setDefaultRenderer(Object.class, renderer);
        
        this.addMouseListener(new MouseListener());
//        this.addMouseMotionListener(new MouseMotionListener());
    }
    
    public void setListModel(dmiExpressionListModel m) {
        if (this.exprListModel != null) {                               
            this.exprListModel.removeExpressionListEventListener(this);     // unregister old model listener
        }
        
        this.exprListModel = m;
        this.exprListModel.addExpressionListEventListener(this);    // register new model listener
        
        DynamicTableModel tableModel = new DynamicTableModel(); // create new tableModel
        
        // fill in the model with new data
        
        tableModel.setRowCount(exprListModel.getExpressionCount()); // create rows in table
        
        for (int i=0; i<exprListModel.getExpressionCount(); ++i) {
            Object[] expression = exprListModel.getExpressionAsArray(i);
            
            if (expression.length > tableModel.getColumnCount())    // set max column count in table if necessary
                tableModel.setColumnCount(expression.length);
            
            for (int j=0; j<expression.length; ++j) {
                tableModel.setValueAt(expression[j], i, j);
            }
        }
        
        // set new table model
        this.setModel(tableModel);
        
        ColumnsAutoSizer.sizeColumnsToFit(this, COLUMN_MARGIN);
        
        // reset selectedSlots
        clearSlotSelection();
    }
    
    public dmiExpressionListModel getListModel() {
        return this.exprListModel;
    }
    
    @Override
    public boolean isSlotSelected(dmSlot slot) {
        return this.selectedSlots.contains(slot);
    }
    
    public boolean isVariableNameUnique(String varName) {
        List<Object> slots = exprListModel.getSlotContentList();
        
        for (Object slotVal: slots) {   // if var name is already used return false
            if (slotVal instanceof String) {
                if (varName.equals(slotVal)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override
    public int getSelectedSlotCount() {
        return this.selectedSlots.size();
    }
    
    @Override
    public dmClass getCommonClass() {
        int cnt = this.getSelectedSlotCount();
        if ( cnt == 0) {
            return null;
        }
        
        List<dmClass> selectedCl = this.getSelectedSlotClasses();   /* non-empty list of classes of currently selected slots */
        if ( selectedCl.size() == 1) {
            return selectedCl.get(0); // return first item in the list - class of the only selected slot
        } else {
            return dmClass.closestCommonAncestor(this.getSelectedSlotClasses());
        }
    }
    
    /**
     * Walks through all cells of the table to get list of classes of currently selected slots.
     * If no slots are selected, returned list is empty.
     * 
     * @return list of classes of selected slots
     */
    private  List<dmClass> getSelectedSlotClasses() {
        List<dmClass> res = new LinkedList<dmClass>();
        int rows = this.getRowCount();
        int cols = this.getColumnCount();
        dmiExpressionListModel model = this.getListModel();
        
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                Object val = this.getValueAt(i, j);
                if (val instanceof dmSlot) {
                    if (this.isSlotSelected((dmSlot)val)) {
                        res.add(model.getClassAt(i, j));
                    }
                }
            }
        }
        
        return res;
    }
    
    /**
     * Selected slots are highlighted.
     * When view model is changed or expression with highlighted slot is deleted
     * this method should be called.
     * 
     */
    @Override
    public void clearSlotSelection(){
        selectedSlots.clear();
        slotPositions.clear();
        repaint();
    }

    /**
     * Connect selected slots to specified data.
     * 
     * @param data 
     */
    @Override
    public void connectSlots(Object data) {
        dmSlot.connectSlots(data, selectedSlots);
        exprListModel.clearUnusedValues();
        clearSlotSelection();
    }

// method never used
//    public void setSlot(Object content) {
//        int selRow = this.getSelectedRow();
//        int selCol = this.getSelectedColumn();
//        
//        Object cell = getModel().getValueAt(selRow, selCol);
//        if (cell instanceof dmSlot) {
//            ((dmSlot)cell).setContent(content);
//            // update table
//            repaint();
//        }
//    }

    @Override
    public void processExpressionListEvent(ExpressionListEvent ev) {
        DynamicTableModel tableModel = (DynamicTableModel) this.getModel();

        int row = ev.getExprID();
        switch (ev.getCode()) {
            case ExpressionListEvent.EXPR_INSERTED:
                Object[] expression = exprListModel.getExpressionAsArray(row);
                assert (expression != null);
                tableModel.setRowCount(tableModel.getRowCount() + 1);
                if (tableModel.getColumnCount() < expression.length) {
                    tableModel.setColumnCount(expression.length);
                }
                for (int j = 0; j < expression.length; ++j) {
                    tableModel.setValueAt(expression[j], row, j);
                }
                break;
            case ExpressionListEvent.EXPR_REMOVED:
                tableModel.deleteRow(row);
                break;
        }
    }

    @Override
    public void setSlotSelectionEnabled(boolean enable) {
        slotSelectionEnabled = enable;
    }
    
    @Override
    public boolean getSlotSelectionEnabled() {
        return slotSelectionEnabled;
    }
    
    
    boolean wildcardAllowed() {
        if (this.getSelectedSlotCount() != 1) {
            return false;
        }
        
        // exactly one slot is selected
        dmSlot selected = (dmSlot)selectedSlots.toArray()[0];
        // get its position
        Pos slotPos = slotPositions.get(selected);
        // if it is a from value slot
        if (representsTransitionFromValue(slotPos)) {
            return true;
        }
        
        return false;
    }
    
    
    private boolean representsTransitionFromValue(Pos slotPos) {
        Object[] expression = this.exprListModel.getExpressionAsArray(slotPos.row);
        if (1 >= slotPos.col || slotPos.col >= (expression.length - 1)) {
            return false;
        }
        
        Object cellData = expression[slotPos.col + 1];  // right neighbour of selected slot
        if (cellData instanceof String) {
            if (((String)cellData).equals(dmiTransition.SYM_ARR)) {
                return true;
            }
        }
        
        return false;
    }
}
class CustomTableCellRenderer extends DefaultTableCellRenderer {
    private static final Color NON_SLOT_ACTIVE = new Color(175,175,175);
    private static final Color NON_SLOT_INACTIVE = new Color(160,160,160);
    
    private static final Color SLOT_ACTIVE = new Color(101,212,219);
    private static final Color SLOT_ACTIVE_SEL = new Color(16,220,0);
    
    private static final Color SLOT_INACTIVE = new Color(103,191,197);
    private static final Color SLOT_INACTIVE_SEL = new Color(38,179,27); 
    
    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object value, boolean selected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(jtable, value, selected, hasFocus, row, column);
        
        this.setForeground(Color.BLACK); // default text color
        ExpressionListView view = (ExpressionListView)jtable;
        dmiExpressionListModel model = view.getListModel();
        int selRow = view.getSelectedRow();

        if (row == selRow) { // active row
            if ((value == null) || (value instanceof String)) { // non slot cell
                this.setFont(ExpressionListView.LABEL_FONT);
                this.setBackground(NON_SLOT_ACTIVE);
            }
            
            if (value instanceof dmSlot) { // slot cell
                this.setFont(ExpressionListView.SLOT_FONT);
                Object slotData = ((dmSlot)value).getData();
                if (slotData instanceof dmConstant) {
                    this.setForeground(Color.BLUE);
                }
                this.setText(this.getText() + " [" + model.getClassAt(row, column) +"]");
                
                if (view.isSlotSelected((dmSlot)value)) {   // slot is selected
                   this.setBackground(SLOT_ACTIVE_SEL);
                } else {    // slot is not selected
                   this.setBackground(SLOT_ACTIVE);
                }
            }
        } else { // inactive row
            if ((value == null) || (value instanceof String)) { // non slot cell
                this.setFont(ExpressionListView.LABEL_FONT);
                this.setBackground(NON_SLOT_INACTIVE);
            }
            
            if (value instanceof dmSlot) { // slot cell
                this.setFont(ExpressionListView.SLOT_FONT);
                Object slotData = ((dmSlot)value).getData();
                if (slotData instanceof dmConstant) {
                    this.setForeground(Color.BLUE);
                }
                this.setText(this.getText() + " [" + model.getClassAt(row, column) +"]");
                if (view.isSlotSelected((dmSlot)value)) {   // slot is selected
                    this.setBackground(SLOT_INACTIVE_SEL);
                } else {    // slot is not selected
                    this.setBackground(SLOT_INACTIVE);
                }
            }
        }

        return this;
    }
}
