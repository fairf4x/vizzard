/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import DataModelException.DomainException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.EventListenerList;

/**
 *
 * @author vodrazka
 */
public class dmExpressionListModel implements dmiExpressionListModel{
    
    protected javax.swing.event.EventListenerList listenerList;
    
    private LinkedList<dmiExpression> expressions;
    
    /**
     * This map contain centralized content of all slots from all expressions in the list.
     * In this way more than one dmSlot can refer to the same data such as Variable name or constant symbol used
     * in Operator definition.
     */
    private Map<Integer,Object> slotContentMap;

    public dmExpressionListModel() {
        slotContentMap = new HashMap<Integer,Object>();
        expressions = new LinkedList<dmiExpression>();
        listenerList = new javax.swing.event.EventListenerList();
    }
    
    /**
     * Copy constructor used when creating operator copy.
     * @param expressionList 
     */
    public dmExpressionListModel(dmExpressionListModel expressionList) throws DomainException {
        slotContentMap = new HashMap<Integer,Object>();
        expressions = new LinkedList<dmiExpression>();
        copyExpressions(expressionList.expressions);
        copySlotConnections(expressionList);
        listenerList = new javax.swing.event.EventListenerList();
    }
    
    /**
     * Remove records with no slot pointing at them from slotContentMap
     * 
     */
    @Override
    public void clearUnusedValues() {
        Set<Integer> allIndices = new HashSet<Integer>(slotContentMap.keySet());
        Set<Integer> usedIndices = getUsedIndices();
        
        allIndices.removeAll(usedIndices);
        for (Integer unused: allIndices) {
            slotContentMap.remove(unused);
        }
    }
    
    @Override
    public int getExpressionCount() {
        return expressions.size();
    }

    @Override
    public Object[] getExpressionAsArray(int i) {
        dmiExpression expr = expressions.get(i); 
        if (expr != null)
            return expr.toTableRow();
        else {
            System.out.println("dmExpressionModel.getExpressionAt: expression at " + i +" not found");
            return null;
        }
    }

    @Override
    public dmClass getClassAt(int row, int column) {
        dmiExpression expr = expressions.get(row); 
        if (expr != null) {
            dmClass[] array = expr.toClassRow();
            
            if (column >= array.length) {
                return null;
            }
            
            if (array[column] != null) {
                return array[column];
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
        
    /**
     * Insert new expression into the list. Content of expression's slot is
     * changed in order for new slots to have unique names if needed.
     * 
     * @param expr inserted expression 
     * @param initSlots when true all slots are assigned unique names
     */
    @Override
    public void insertExpression(dmiExpression expr,boolean initSlots) {       
        if (initSlots) {
            Set<String> usedNames = getUsedNames();     // get currently used variable and constant names
            String[] newNames = generateUniqueNames(usedNames,expr.getSlots().size()); // generate new variable names

            int i=0;                                                
            for (dmSlot slot: expr.getSlots()) {    // set generated names
                slot.changeContent(newNames[i]);
                ++i;
            }
        }
        
        expressions.add(expr);
        
        int index = expressions.size() - 1; // index of last inserted element
        fireExpressionInserted(index);
    }

    @Override
    public void removeExpression(int exprIndex) {       
        // remove expression
        expressions.remove(exprIndex);
        clearUnusedValues();
        fireExpressionRemoved(exprIndex);
    }

    
    @Override
    public void addExpressionListEventListener(dmiExpressionListListener listener) {
        listenerList.add(dmiExpressionListListener.class, listener);
    }
    
    @Override
    public void removeExpressionListEventListener(dmiExpressionListListener listener) {
        listenerList.remove(dmiExpressionListListener.class, listener);
    }
    
    private void fireExpressionInserted(int exprID) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==dmiExpressionListListener.class) {
                ((dmiExpressionListListener)listeners[i+1]).processExpressionListEvent(
                        new ExpressionListEvent(this,ExpressionListEvent.EXPR_INSERTED,exprID));
            }
        }
    }
    
    private void fireExpressionRemoved(int exprID) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==dmiExpressionListListener.class) {
                ((dmiExpressionListListener)listeners[i+1]).processExpressionListEvent(
                        new ExpressionListEvent(this,ExpressionListEvent.EXPR_REMOVED,exprID));
            }
        }
    }

    private Set<String> getUsedNames() {
        Set<String> result = new HashSet<String>();
        
        for (Object slotData: slotContentMap.values()) {
            result.add(slotData.toString());
        }
        
        return result;
    }

    private String[] generateUniqueNames(Set<String> usedNames, int size) {
        String[] result = new String[size];
        
        int index = 0;
        String newName = "X" + index; 
        
        for (int i=0; i<result.length; ++i) {
            while(usedNames.contains(newName)) {
                index++;
                newName = "X" + index;
                
                if (index > 100) {
                    throw new UnsupportedOperationException("Suspiciously high value reached during unique variable name generation.");
                }
            }
            // newName should be unique
            
            usedNames.add(newName); // insert newName into set - prevent it to be generated again
            
            result[i] = newName; 
        }
        
        return result;
    }

    @Override
    public List<dmiExpression> getExpressions() {
        return this.expressions;
    }

    @Override
    public String testPrint() {
        String result = "";
        
        for (dmiExpression expr: expressions) {
            result += expr.printableDescription() + "\n";
        }
        
        return result;
    }
    
    /**
     * Passed to constructor of dmiExpression implementations.
     * 
     * @return 
     */
    @Override
    public Map<Integer,Object> getSlotContentMap() {
        return slotContentMap;
    }

    private Set<Integer> getUsedIndices() {
        Set<Integer> result = new HashSet<Integer>();
        for (dmiExpression expr: expressions) {
            List<dmSlot> exprSlots = expr.getSlots();
            for (dmSlot slot: exprSlots) {
                result.add(slot.getIndex());
            }
        }
        
        return result;
    }

    @Override
    public List<Object> getSlotContentList() {
        return new LinkedList<Object>(slotContentMap.values());
    }

    private void copyExpressions(LinkedList<dmiExpression> exprList) throws DomainException {
        for (dmiExpression expr: exprList) {
            byte type = expr.getExpressionType();

            switch (type) {
                case dmiExpression.RELATION: {
                    expressions.add(new dmRelation((dmRelationDelegate) expr.getDelegate(), slotContentMap));
                    break;
                }
            
                case dmiExpression.P_TRANS: {
                    dmPrevailingTransition prevTrans = (dmPrevailingTransition) expr;
                    expressions.add(new dmPrevailingTransition(prevTrans.getStateVariable(), slotContentMap, prevTrans.getFrom()));
                    break;
                }

                case dmiExpression.TRANS: {
                    dmTransition trans = (dmTransition) expr;
                    expressions.add(new dmTransition(trans.getStateVariable(), slotContentMap, trans.getFrom(), trans.getTo()));
                    break;
                }
                default:
                    throw new DomainException("copyExpressions: unknown expression type");
            }
        }
    }

    private void copySlotConnections(dmExpressionListModel expressionList) {
        assert(expressionList.expressions.size() == expressions.size()); // two equal lists
        
        LinkedList<dmiExpression> original = expressionList.expressions;
        LinkedList<dmiExpression> copy = expressions;
        
        for (int i = 0; i < copy.size(); ++i) {
            dmiExpression origExpr = original.get(i);
            dmiExpression copyExpr = copy.get(i);

            copySlots(origExpr,copyExpr);
        }
    }

    private void copySlots(dmiExpression origExpr, dmiExpression copyExpr) {
        byte origT = origExpr.getExpressionType();
        byte copyT = copyExpr.getExpressionType();

        assert (origT == copyT); // kontrola typu
        assert (origExpr.getDelegate() == copyExpr.getDelegate()); // stejny delegat
        
        dmSlot origSlot;
        dmSlot copySlot;
        
        // copy argument slot connection
        for (int arg = 0; arg < origExpr.getArgumentCount(); ++arg) {
            origSlot = origExpr.getSlot(arg);
            copySlot = copyExpr.getSlot(arg);
            
            int origSlotIndex = origSlot.getIndex();
            Object origSlotData = origSlot.getData();
            
            if (!this.slotContentMap.containsKey(origSlotIndex)) { // data not present
                slotContentMap.put(origSlotIndex, origSlotData);
            }
            
            copySlot.dataIndex = origSlotIndex;
        }
        
        // copy value slot connection if necessary
        if (copyT == dmiExpression.P_TRANS) { // prevailing transition
            origSlot = origExpr.getSlot(dmiExpression.VAL_INDEX);
            copySlot = copyExpr.getSlot(dmiExpression.VAL_INDEX);
            
            int origSlotIndex = origSlot.getIndex();
            Object origSlotData = origSlot.getData();
            
            if (!this.slotContentMap.containsKey(origSlotIndex)) { // data not present
                slotContentMap.put(origSlotIndex, origSlotData);
            }
            
            copySlot.dataIndex = origSlotIndex;
        }
        
        if (copyT == dmiExpression.TRANS) { // transition
            origSlot = origExpr.getSlot(dmiExpression.FVAL_INDEX);
            copySlot = copyExpr.getSlot(dmiExpression.FVAL_INDEX);
            
            int origSlotIndex = origSlot.getIndex();
            Object origSlotData = origSlot.getData();
            
            if (!this.slotContentMap.containsKey(origSlotIndex)) { // data not present
                slotContentMap.put(origSlotIndex, origSlotData);
            }
            
            copySlot.dataIndex = origSlotIndex;
            
            origSlot = origExpr.getSlot(dmiExpression.TVAL_INDEX);
            copySlot = copyExpr.getSlot(dmiExpression.TVAL_INDEX);
            
            origSlotIndex = origSlot.getIndex();
            origSlotData = origSlot.getData();
            
            if (!this.slotContentMap.containsKey(origSlotIndex)) { // data not present
                slotContentMap.put(origSlotIndex, origSlotData);
            }
            
            copySlot.dataIndex = origSlotIndex;
        }
    }

    @Override
    public dmiExpression getExpressionAt(int i) {
        return expressions.get(i);
    }

}
