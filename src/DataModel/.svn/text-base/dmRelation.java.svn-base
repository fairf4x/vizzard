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
 * dmRelation is used in dmOperator.
 * Many instances of this class may exist - each of them representing particular expression in operator's list of expressions.
 * Main purpose is to organize slots.
 * 
 * @author fairfax
 */
public class dmRelation extends dmArgumentVector implements dmiExpression {
    
    private dmRelationDelegate relation;
    
    public dmRelation(dmRelationDelegate rel, Map<Integer, Object> dataMap) {
        super(dataMap);
        this.relation = rel;
        super.initArguments(relation.getArgumentCount());
    }
    
    public dmRelationDelegate getRelation() {
        return relation;
    }

    @Override
    public int getTokenCount() {
        return this.slotMapping.size();
    }

    @Override
    public Object[] toTableRow() {
        Object[] res = new Object[getTokenCount() + 1];
        
        int i=0;
        
        res[i] = relation.toString();        // relation name
        
        for (i=1; i<getTokenCount()+1; ++i) {    // relation arguments
            res[i] = this.getSlot(i-1);             // -1 because argument numberig starts at 0
        }
        return res;
    }

    @Override
    public dmClass[] toClassRow() {
        dmClass[] res = new dmClass[getTokenCount() + 1];
        
        int i=0;
        
        res[i] = null;        // relation name (no class assigned to this column)
        
        List<dmClass> args = relation.getArgumentValues();
        
        if (args == null) { // relation is not declared
            return res;
        }
        
        for (i=1; i<getTokenCount()+1; ++i) {    // relation arguments
            res[i] = args.get(i-1); // args indexes start at 0
        }
        
        return res;
    }
    
    @Override
    public Set<dmClass> getRelevantClasses() {
        HashSet<dmClass> result = new HashSet<dmClass>(relation.getArgumentValues());
        
        return result;
    }

    @Override
    public String printableDescription() {
        String result = this.relation.toString();
        
        List<dmClass> args = relation.getArgumentValues();
        for (int i=0; i<args.size(); ++i) {
            if (i == 0) {
                result += "( ";
            } else {
                if (i<= args.size()-1) {
                    result += ", ";
                }
            }
            result+=args.get(i).toString();
        }
        
        if (args.size() > 0)
            result += ")";
        
        return result;
    }

    @Override
    public Map<Integer, dmClass> getClassMap() {
        HashMap<Integer, dmClass> result = new HashMap<Integer, dmClass>();
        
        for (int i=0; i<this.getArgumentCount(); ++i) {
            result.put(i, relation.getArgumentValues().get(i));
        }
        
        return result;
    }

    @Override
    public byte getExpressionType() {
        return dmiExpression.RELATION;
    }

    @Override
    public String getDelegateName() {
        return relation.toString();
    }

    @Override
    public dmDelegate getDelegate() {
        return relation;
    }

    @Override
    public boolean checkDeclaration() {
        return relation.isDeclared();
    }
}
