/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;

/**
 * Class containing definitions of operators.
 * 
 * @author vodrazka
 */
public class dmOperatorModel extends DefaultListModel implements dmiOperatorSet {
   
    
    
    @Override
    public dmiOperator getOperatorAt(int index) {
        Object oper = super.get(index);
        if (!(oper instanceof dmiOperator)) {
            throw new UnsupportedOperationException("Stored value is not operator !!");
        }

        return (dmiOperator) oper;
    }

    @Override
    public List<dmOperator> getOperatorList() {
        LinkedList<dmOperator> operators = new LinkedList<dmOperator>();
        for (int i = 0; i < this.getSize(); ++i) {
            operators.add((dmOperator) this.getElementAt(i));
        }

        return operators;
    }

    @Override
    public String testPrint() {
        String result = "";
        
        for (dmOperator oper: getOperatorList()) {
            result += oper.testPrint();
        }
        
        return result;
    }

    @Override
    public void clearOperatorSet() {
        this.clear();
    }

    public boolean isUniqueName(String operatorName) {
        
        for (int i = 0; i < this.getSize(); ++i) {
            String existingName = this.getOperatorAt(i).getName();
            
            if (operatorName.equals(existingName)) {    // the name was found -> is not unique
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateOperators() {
        for (int i = 0; i < this.getSize(); ++i) {
            dmiOperator oper = this.getOperatorAt(i);
            oper.updateExpressions();
        }
    }
}
