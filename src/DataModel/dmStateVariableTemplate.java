/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author fairfax
 */
public class dmStateVariableTemplate extends dmRelationTemplate {
    private List<dmTransitionTemplate> transitions;
    public dmStateVariableTemplate(String name, String[] args, List<dmTransitionTemplate> trans) {
        super(name,args);
        transitions = trans;
    }
    
    public List<dmTransitionTemplate> getTransitionList(boolean prevailing) {
        List<dmTransitionTemplate> result = new LinkedList<dmTransitionTemplate>();
        
        for (dmTransitionTemplate trans: transitions) {
            if (trans.isPrevailing() == prevailing) {
                result.add(trans);
            }
        }
        
        return result;
    }
}
