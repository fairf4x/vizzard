/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.Set;

/**
 * Method for support slot manipulation in operator.
 * 
 * @author fairfax
 */
public interface dmiSlotSetHandler {

    public boolean isSlotSelected(dmSlot slot);
    public int getSelectedSlotCount();
    public void clearSlotSelection();
    public void connectSlots(Object data);
    public void setSlotSelectionEnabled(boolean enable);
    public boolean getSlotSelectionEnabled();
    public dmClass getCommonClass();
}
