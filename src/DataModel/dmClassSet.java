/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;


/**
 *
 * @author fairfax
 */
public class dmClassSet {
    private HashSet<dmClass> set = new HashSet<dmClass>();
    
    public LinkedList<String> toStringList(){
        LinkedList<String> list = new LinkedList<String>();
        
        for (dmClass item: set) {
            list.add(item.toString());
        }
        
        return list;
    }
    /**
     * Convert to set of classes. Returned value is reference to new instance.
     * 
     * @return 
     */
    public Set<dmClass> toSet() {
        HashSet<dmClass> result = new HashSet<dmClass>(set);
        return result;
    }
    
    public void addClass(dmClass newClass){
        set.add(newClass);
    }
    
    public void removeClass(dmClass toRemove){
        set.remove(toRemove);
    }
    
    @Override
    public String toString(){
        String result = "";
        
        if (set.isEmpty())
            return result;
        
        for (dmClass item: set){
            result += item.toString() + ", ";
        }
        
        return result.substring(0, result.lastIndexOf(","));
    }
}
