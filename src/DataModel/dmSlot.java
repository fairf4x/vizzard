package DataModel;

import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Slot can refer to another slot or carry a value.
 * The class is used as placeholder for anything we can use as
 * argument of relation or state variable and state variable "from" and "to" value as well.
 * 
 * @author fairfax
 */
public class dmSlot {

    int dataIndex;
    Map<Integer,Object> dataMap = null;
    
    public dmSlot(String varName, Map<Integer,Object> dataMapReference) {
        assert(dataMapReference != null);
        dataMap = dataMapReference;
        
        dataIndex = generateUniqueKey();
        
        // insert data in centralized map
        dataMap.put(dataIndex, varName);
    }

    /**
     * Connect to other slot.
     * Original value of this slot is removed from map.
     * 
     * @param target slot we are connecting to.
     */
    private void connectTo(dmSlot target) {
        assert(target != null);
        
        if (this.dataIndex == target.dataIndex) { // no need to set anything
            return;
        }
        
        // point to target's data
        dataIndex = target.dataIndex;
    }
    
    /**
     * Inserts new record in centralized dataMap with given data and
     * sets this slot's data index to point at it.
     * Can not be used if the "data" are already present in the dataMap.
     *
     * @param data new content of the slot
     */
    public void setContent(Object data) {
        assert(data != null); // this should not happen

        dataIndex = generateUniqueKey();

        // insert data in centralized map
        dataMap.put(dataIndex, data);
    }
    
    /**
     * Replace original slot content with new data.
     * 
     * @param data 
     */
    public void changeContent(Object data) {
        assert(data != null);
        dataMap.put(dataIndex, data);
    }
    
    /**
     * Get value stored either in this slot or in the slot it is connected to.
     * Returned value is value from terminating slot (first slot in chain which has its own value).
     * 
     * @return value from terminating slot 
     */
    public Object getData() {
        if (dataMap.containsKey(dataIndex)) {
            return dataMap.get(dataIndex);
        }
        
        return null;
    }

    /**
     * Content of the slot should be printable.
     * 
     * @return thisSlotData.toString()  
     */
    @Override
    public String toString() {
        return this.getData().toString();
    }

    /**
     * Sets content of first slot in slotSet and connects all remaining slots to
     * it.
     * 
     * @param content after this method is executed all slots in slotSet has this content
     * @param slotSet list of slots to unify
     */
    public static void connectSlots(Object content, Set<dmSlot> slotSet) {
        if (slotSet.isEmpty()) {
            System.out.println("dmSlot.connectSlots: method called with empty set as argument (?!?)");
            return;
        }
        
        boolean first = true;
        dmSlot firstSlot = null;
        for (dmSlot slot: slotSet) {
            if (first) { // we need to generate unique key
                first = false;
                slot.setContent(content); // by setting content a new index is generated
                firstSlot = slot;
                continue;  // this slot is already pointed to right index
            }
            
            assert(firstSlot != null);
            // and point all slots to it
            slot.connectTo(firstSlot);
        }
    }

    public static void connectSlotsToOne(dmSlot master, Set<dmSlot> slotSet) {
        if (slotSet.isEmpty()) {
            System.out.println("dmSlot::connectSlots: method called with empty set as argument (?!?)");
            return;
        }
        
        if (master == null) {
            System.out.println("dmSlot::connectSlots: method called with null master slot (?!?)");
            return;
        }

        for (dmSlot slot: slotSet) {
            // point all slots to it
            slot.connectTo(master);
        }
    }
    
    private int generateUniqueKey() {
        // determine new unused index for slot data
        Random randomGenerator = new Random();
        int result = randomGenerator.nextInt();
        while (dataMap.containsKey(result)) {
            result = randomGenerator.nextInt();
        }
        
        return result;
    }
    
    /**
     * Only used in dmExpressionListModel.getUsedIndices to clear unused indices from dataMap !!
     * 
     * @return 
     */
    public int getIndex() {
        return dataIndex;
    }
}
