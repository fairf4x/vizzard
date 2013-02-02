/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import DataModelException.DomainException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author vodrazka
 */
public class dmOperator implements dmiOperator{
    private dmiExpressionListModel expressions;
    private String operatorName;
    
    public dmOperator(String name, dmiExpressionListModel list) {
        expressions = list;
        operatorName = name;
    }
    
    public dmOperator(dmiOperator original, String copyName) throws DomainException {
        operatorName = copyName;
        
        // copy list of expression
        expressions = new dmExpressionListModel((dmExpressionListModel)original.getExpressionList());
    }
    
    @Override
    public dmiExpressionListModel getExpressionList() {
        return expressions;
    }
    
    @Override
    public String getName(){
        return operatorName;
    }
    
    @Override
    public void setName(String newName) {
        operatorName = newName;
    }
    
    public String testPrint() {
        String result = "###### " + this.getName() + " ######\n\n";
        
        return result+expressions.testPrint() +"\n\n";
    }

    @Override
    public void updateExpressions() {
        List<Integer> deleteIndexSet = new LinkedList<Integer>(); // indexes of expressions to delete
        
        for (int i=0; i < expressions.getExpressionCount(); ++i) {
            dmiExpression expr = expressions.getExpressionAt(i);
            
            if (!expr.checkDeclaration()) { // if expression fail to find suitable declaration id returns false
                deleteIndexSet.add(i);
            }
        }
        
        // delete expressions that failed the test - start with the biggest index
        for (int i=deleteIndexSet.size()-1; i >= 0; --i) {
            Integer index = deleteIndexSet.get(i);
            expressions.removeExpression(index);
        }
        
        // clear values no longer refered by any slot
        expressions.clearUnusedValues();
    }
}
