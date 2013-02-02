/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ApplicationGUI;

import DataModel.dmStateVariableDelegate;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author fairfax
 */
public class StateVariableCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof dmStateVariableDelegate) {
            String text = ((dmStateVariableDelegate)value).printableDescription();
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        } else {
            throw new UnsupportedOperationException("StateVariableCellRenderer render only objects of class dmStateVariableDelegate.");
        }
    }
}
