/*
 * dmClass change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transition is characterized by its state variable and two classes.
 * Class of initial value and class of target value.
 * Example: botPos(Bot): RedNavpoint -> GreenNavpoint
 *          state var.   init. Class    target class
 * 
 * Its main purpose is to store one or more list of "expressions" which
 * can be understood as constraints which have to be true if this kind of transition
 * (from init to target class) is going to be executed.
 * 
 * @author fairfax
 */
public class dmTransition extends dmArgumentVector implements dmiTransition, dmiExpression {
    /*
     * TODO:
     * changes we need to know about (will be recieved as event from dmStateVariableDelegate):
     * stateVar value range changed (was reduced)
     * stateVar argument was added
     * stateVar argument was deleted
     * stateVar argument domain was changed
     * stateVar was deleted
     * 
     */
    private dmClass fClass;
    private dmClass tClass;
    
    protected dmStateVariableDelegate stateVar;
    protected boolean prevailing;

    /**
     * Constructor for non-prevailing transition.
     * Transition can be non-prevailing even if From and To class are equal - in this case
     * it means that value change from X to Y (X != Y) and both X and Y are members of same class.
     * 
     * @param stateVariable
     * @param from
     * @param to 
     */
    public dmTransition(dmStateVariableDelegate stateVariable, Map<Integer, Object> dataMap, dmClass from, dmClass to) {
        super(dataMap);
        this.fClass = from;
        this.tClass = to;
        this.stateVar = stateVariable;
        this.prevailing = false;
        super.initArguments(stateVar.getArgumentCount());
        
        this.slotMapping.put(FVAL_INDEX, new dmSlot("From",dataMap));
        this.slotMapping.put(TVAL_INDEX, new dmSlot("To",dataMap));
    }
    
    @Override
    public dmClass getFrom() {
        return this.fClass;
    }
    
    @Override
    public dmClass getTo() {
        return this.tClass;
    }
    
    @Override
    public dmStateVariableDelegate getStateVariable() {
        return this.stateVar;
    }
    
    @Override
    public void setFrom(dmClass from) {
        this.fClass = from;
    }
    
    @Override
    public void setTo(dmClass to) {
        this.tClass = to;
    }
    
    public void setStateVariable(dmStateVariableDelegate stateVariable) {
        this.stateVar = stateVariable;
    }
    
    @Override
    public boolean isPrevailing() {
        return this.prevailing;
    }
    
    @Override
    public String toString(){
            return fClass.toString() + SYM_ARR + tClass.toString();
    }
    
    @Override
    public dmSlot getSlot(int number) {
            return slotMapping.get(number);
    }
    
    @Override
    final public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof dmTransition) {
            dmiTransition otherTrans = (dmiTransition) other;
            // two transitions are equal if their from and to class is equal
            if (this.fClass.equals(otherTrans.getFrom()) && this.tClass.equals(otherTrans.getTo()))
                return true;
            else
                return false;
        } else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (this.fClass != null ? this.fClass.hashCode() : 0);
        hash = 13 * hash + (this.tClass != null ? this.tClass.hashCode() : 0);
        return hash;
    }

    @Override
    public int getTokenCount() {
        return slotMapping.size();
    }

    @Override
    public int getArgumentCount() {
        return slotMapping.size() - 2; // non prevailing has two non-argument mappings
    }
    
    @Override
    public Object[] toTableRow() {
        Object[] result = new Object[getTokenCount() + 3]; // +3 is for "name",":" and "->"
        
        int i = 0;
        // state variable name
        result[i] = stateVar.toString();
        
        // state variable arguments
        for (i=1; i<getArgumentCount()+1; ++i) {
            result[i] = this.getSlot(i-1);
        }
        // ":"
        result[i] = SYM_COL;
        ++i;
        // from value
        result[i] = slotMapping.get(FVAL_INDEX);
        ++i;
        
        // "->"
        result[i] = SYM_ARR;
        ++i;
        
        // to value
        result[i] = slotMapping.get(TVAL_INDEX);
        
        return result;
    }

        @Override
    public dmClass[] toClassRow() {
        dmClass[] result = new dmClass[getTokenCount() + 3]; // +3 is for "name",":" and "->"
        
        int i = 0;
        // state variable name
        result[i] = null;
        
        List<dmClass> args = stateVar.getArgumentValues();
        // state variable arguments
        for (i=1; i<getArgumentCount()+1; ++i) {
            result[i] = args.get(i-1);
        }
        // ":"
        result[i] = null;
        ++i;
        // from value
        result[i] = fClass;
        ++i;
        
        // "->"
        result[i] = null;
        ++i;
        
        // to value
        result[i] = tClass;
        
        return result;
    }
        
    @Override
    public Set<dmClass> getRelevantClasses() {
        HashSet<dmClass> result = new HashSet<dmClass>(stateVar.getArgumentValues());
        
        result.add(fClass);
        result.add(tClass);
        
        return result;
    }

    @Override
    public String printableDescription() {
        String result = this.stateVar.toString();
        
        List<dmClass> args = stateVar.getArgumentValues();
        for (int i=0; i<args.size(); ++i) {
            if (i == 0) {
                result += "( ";
            } else {
                if (i< args.size()-1) {
                    result += ", ";
                }
            }
            result+=args.get(i).toString();
        }
        
        if (args.size() > 0)
            result += ")" + SYM_COL;
        
        return result + this.toString();
    }

    @Override
    public Map<Integer, dmClass> getClassMap() {
        HashMap<Integer, dmClass> result = new HashMap<Integer, dmClass>();
        result.put(FVAL_INDEX, fClass);
        result.put(TVAL_INDEX,tClass);
        
        for (int i=0; i<this.getArgumentCount(); ++i) {
            result.put(i, stateVar.getArgumentValues().get(i));
        }
        
        return result;
    }

    @Override
    public byte getExpressionType() {
        return dmiExpression.TRANS;
    }

    @Override
    public String getDelegateName() {
        return stateVar.toString();
    }

    @Override
    public dmDelegate getDelegate() {
        return stateVar;
    }

    @Override
    public boolean checkDeclaration() {
        return stateVar.isDeclared();
    }
}