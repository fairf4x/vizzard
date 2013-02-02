/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fairfax
 */
public class dmPrevailingTransition extends dmTransition implements dmiExpression {
    
    
    private dmClass valClass;
    
    public dmPrevailingTransition(dmStateVariableDelegate stateVariable, Map<Integer, Object> dataMap, dmClass valueClass) {
        super(stateVariable,dataMap,valueClass,valueClass);
        this.valClass = valueClass;
        this.stateVar = stateVariable;
        
        // set transition prevailing
        this.prevailing = true;
        
        slotContentMap = dataMap;
        super.initArguments(stateVar.getArgumentCount());
        
        // init slots - there is no diference between from and to class.
        slotMapping.put(VAL_INDEX, new dmSlot("Value",dataMap));
        
        // remove redundant mapping created in parent constructor
        slotMapping.remove(dmiExpression.FVAL_INDEX);
        slotMapping.remove(dmiExpression.TVAL_INDEX);
    }

    @Override
    public dmClass getFrom() {
        return this.valClass;
    }
    
    @Override
    public dmClass getTo() {
        return this.valClass;
    }
    
    @Override
    public void setFrom(dmClass from) {
        this.valClass = from;
    }
    
    @Override
    public void setTo(dmClass to) {
        this.valClass = to;
    }
    
    @Override
    public String toString(){
            return this.valClass.toString();
    }
    
    @Override
    public int getArgumentCount() {
        return slotMapping.size() - 1; // prevailing has one non argument mapping
    }
    
    @Override
    public Object[] toTableRow() {
        Object[] result = new Object[getTokenCount() + 2]; // +2 is for "name" and "=="
        
        int i = 0;
        // state variable name
        result[i] = stateVar.toString();
        
        // state variable arguments
        for (i=1; i<getArgumentCount()+1; ++i) {
            result[i] = this.getSlot(i-1);
        }
        // ":"
        result[i] = SYM_EQ;
        ++i;
        // from value
        result[i] = slotMapping.get(VAL_INDEX);
        
        return result;
    }
    
    @Override
    public dmClass[] toClassRow() {
        dmClass[] result = new dmClass[getTokenCount() + 2]; // +2 is for "name" and "=="
        
        int i = 0;
        // state variable name
        result[i] = null;
        
        List<dmClass> args = stateVar.getArgumentValues();
        // state variable arguments
        for (i=1; i<getArgumentCount()+1; ++i) {
            result[i] = args.get(i-1);
        }
        // "=="
        result[i] = null;
        ++i;
        // from value
        result[i] = valClass;

        return result;
    }
      
    @Override
    public Set<dmClass> getRelevantClasses() {
        HashSet<dmClass> result = new HashSet<dmClass>(stateVar.getArgumentValues());
        
        result.add(valClass);
        
        return result;
    }

    @Override
    public byte getExpressionType() {
        return dmiExpression.P_TRANS;
    }
    
    @Override
    public Map<Integer, dmClass> getClassMap() {
        HashMap<Integer, dmClass> result = new HashMap<Integer, dmClass>();
        result.put(VAL_INDEX, valClass);
        
        for (int i=0; i<this.getArgumentCount(); ++i) {
            result.put(i, stateVar.getArgumentValues().get(i));
        }
        
        return result;
    }
}
