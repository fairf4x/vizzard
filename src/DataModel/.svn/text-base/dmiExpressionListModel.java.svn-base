/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import DataModel.dmiExpression;
import java.util.List;
import java.util.Map;

/**
 * This interface is provided to ExpressionListView to display
 * list of expressions.
 * 
 * @author vodrazka
 */
public interface dmiExpressionListModel {
    /**
     * We need to know how long the list is.
     * 
     * @return expression count 
     */
    public int getExpressionCount();
    
    /**
     * Method used to retrieve information to display by ExpressionListView.
     * 
     * @param i index of expression in the list
     * @return list of dmSlots and Strings
     */
    public Object[] getExpressionAsArray(int i);
    
    /**
     * Method used to get particular expression from the list.
     * @param i expression index in the list
     * @return 
     */
    public dmiExpression getExpressionAt(int i);
    
    /**
     * Find out class of slot at specified position.
     * 
     * @param row
     * @param column
     * @return instance of dmClass if possible, null otherwise
     */
     public dmClass getClassAt(int row, int column);
    
    /**
     * Insert expression at the end of the list.
     * 
     * @param expr inserted expression
     * @param initSlots when true all slots in new expression are initialized
     *          with names unique in this expression list
     */
    public void insertExpression(dmiExpression expr, boolean initSlots);
    
    /**
     * Remove expression at given position.
     * Expression must be editable - isExprEditable must return true
     * 
     * @param exprIndex index of removed expression
     */
    public void removeExpression(int exprIndex);
    
    /**
     * Method used when marshalling operator into XML.
     * @return list of expressions
     */
    public List<dmiExpression> getExpressions();
    
    public void addExpressionListEventListener(dmiExpressionListListener listener);
    public void removeExpressionListEventListener(dmiExpressionListListener listener);

    public String testPrint();
    public Map<Integer,Object> getSlotContentMap();
    public List<Object> getSlotContentList();
    public void clearUnusedValues();
}
