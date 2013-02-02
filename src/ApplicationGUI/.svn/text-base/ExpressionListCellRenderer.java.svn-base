/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ApplicationGUI;

import DataModel.dmiExpression;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * This class is used in SelectExpressionDialog and SelectTransition dialog
 * @author fairfax
 */
public class ExpressionListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof dmiExpression) {
            String text = ((dmiExpression)value).printableDescription();
            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        } else {
            throw new UnsupportedOperationException("ExpressionCellRenderer render only objects of class dmiExpression.");
        }
        
    }
}