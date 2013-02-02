/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

/**
 *
 * @author fairfax
 */
public interface dmiTransition extends dmiExpression {
    
    public static final String SYM_EQ = "==";
    public static final String SYM_COL = ":";
    public static final String SYM_ARR = "->";
    /**
     * Starting value of each transition belongs to some class. 
     * 
     * @return class of starting value
     */
    public dmClass getFrom();
    
    /**
     * Target value of each transition belongs to some class.
     * 
     * @return class of target value
     */
    public dmClass getTo();
    /**
     * Set class for starting value.
     * 
     * @param fVal class
     */
    public void setFrom(dmClass fVal);
    
    /**
     * Set class for target value.
     * 
     * @param tVal class
     */
    public void setTo(dmClass tVal);
    
    /**
     * Transition is used to describe change of state variable.
     * If there is no change we call this transition to be prevailing.
     * 
     * @return true if this transition is prevailing
     */
    public boolean isPrevailing();
    
    /**
     * Transition is describing value change of some state variable.
     * 
     * @return delegate for state variable whose change this transition describes
     */
    public dmStateVariableDelegate getStateVariable();    

    @Override
    public boolean equals(Object other);
    @Override
    public int hashCode();
}
