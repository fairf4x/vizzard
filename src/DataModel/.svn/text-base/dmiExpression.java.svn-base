/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for elements which can be included in expression list.
 * 
 * @author fairfax
 */
public interface dmiExpression {
    
    /*
     * Mapping for From and To slot. Argument slots are mapped starting from 0.
     */
    
    /**
     * Index for From-value of non-prevailing transition.
     */
    public static final int FVAL_INDEX = -1;
    
    /**
     * Index for To-value of non-prevailing transition.
     */
    public static final int TVAL_INDEX = -2;
    /**
     * Index for value of prevailing transition.
     */
    public static final int VAL_INDEX = -3;
    
    /*
     * Return values for getExpressionType
     */
    public static final byte RELATION = 0; //"Relation";
    public static final byte P_TRANS = 1;  //"PrevTransition";
    public static final byte TRANS = 2;    //"Transition";
    
    
    /**
     * Token is either argument or from/to value of state variable.
     * 
     * @return number of tokens in this expression 
     */
    public int getTokenCount();
    
    /**
     * Every expression can have arguments.
     * @return argument count for this expression
     */
    public int getArgumentCount();
    
    /**
     * Transform expression into vector of data which can be displayed
     * in ExpressionListView.
     * 
     * @return array of Objects 
     */
    public Object[] toTableRow();
    
    /**
     * Make array of dmClass. This array has at index i class which is compatible
     * with i-th slot at index i in ExpressionListView.
     * @return array of dmClass (filled with null at non-argument cell positions)
     */
    public dmClass[] toClassRow();
    
    public dmSlot getSlot(int index);
    
    /**
     * Expression is interface implemented by dmTransition, dmPrevailingTransition and dmRelation.
     * 
     * @return PrevTransition/Transition/Relation 
     */
    public byte getExpressionType();
    
    /**
     * Each expression is constructed with reference to its delegate - viz. dmDelegate class.
     * 
     * @return name of delegate 
     */
    public String getDelegateName();
    
    public dmDelegate getDelegate();
    
    /**
     * Class is relevant to expression iff it appears either in its argument list or 
     * in place of From/To value in case of expression derived from state variable.
     * Child classes are not included.
     * 
     * @return set of classes relevant to the expression
     */
    public Set<dmClass> getRelevantClasses();
    
    public String printableDescription();
    
    public List<dmSlot> getSlots();
    
    /**
     * Method intended for giving transition data when exporting to XML.
     * Arguments are mapped to values 0,1,...,n and state variable values according to
     * mapping: FVAL_INDEX, TVAL_INDEX, VAL_INDEX
     *
     * @return 
     */
    public Map<Integer,dmSlot> getSlotMap();
    
    /**
     * Map of classes for arguments and state variable values. Mapping is corresponding with
     * slot mapping.
     * 
     * @return 
     */
    public Map<Integer,dmClass> getClassMap();

    /**
     * Check the delegate. If the delegate has changed only its name, change it accordingly.
     * If the delegate changed argument count, value range or argument classes return false.
     * @return true if there is a valid declaration for the expression false otherwise
     */
    public boolean checkDeclaration();
}
