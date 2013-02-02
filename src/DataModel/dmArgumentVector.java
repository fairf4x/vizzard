/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Base class which can manage named vector of dmSlot arguments
 * 
 * @author vodrazka
 */
abstract public class dmArgumentVector {
    /**
     * Each argument in vector has its own slot.
     * Number of argument is key to slotMapping starting with 0.
     */
    protected HashMap<Integer,dmSlot> slotMapping = new HashMap<Integer,dmSlot>();
    
    /**
     * See slotContentMap in dmExpressionListModel.
     */
    protected Map<Integer,Object> slotContentMap = null; 
    
    public dmArgumentVector(Map<Integer,Object> slotDataMap) {
        slotContentMap = slotDataMap;
    }
    
    protected void initArguments(int cnt){
        for (int i=0; i < cnt; ++i) {
            slotMapping.put(i, new dmSlot("A" + i,slotContentMap));
        }
    }
    
    public int getArgumentCount() {
        return this.slotMapping.size();
    }
    
    /**
     * Return slot for argument at given position.
     * 
     * @param number position of argument in vector (starting at 0)
     * @return 
     */
    public dmSlot getSlot(int number) {
        return slotMapping.get(number);
    }
    
    public List<dmSlot> getSlots() {
        return new LinkedList<dmSlot>(slotMapping.values());
    }
    
    public Map<Integer,dmSlot> getSlotMap(){
        return slotMapping;
    }
}
