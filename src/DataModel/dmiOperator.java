/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

/**
 *
 * @author fairfax
 */
public interface dmiOperator {
    /**
     * Get list of operator expressions.
     * @return list of expressions as dmiExpressionListModel
     */
    public dmiExpressionListModel getExpressionList();
    
    /**
     * Get operator name.
     * @return operator name
     */
    public String getName();
    
    /**
     * Set operator name.
     * @param newName 
     */
    public void setName(String newName);
    
    /**
     * Delete expressions without declarations and rename those with unchanged structure.
     */
    public void updateExpressions();
}
