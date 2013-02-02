/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CustomizedClasses;

import java.util.Map;

/**
 *
 * @author fairfax
 */
public class Entry<Key,Value> implements Map.Entry{
    
    private Key entryKey;
    private Value entryVal;
    
    public Entry(Key key,Value value) {
        this.entryKey = key;
        this.entryVal = value;
    }

    @Override
    public Object getKey() {
        return this.entryKey;
    }

    @Override
    public Object getValue() {
        return this.entryVal;
    }

    @Override
    public Object setValue(Object v) {
        Value old = this.entryVal;
        this.entryVal = (Value)v;
        return this.entryVal;
    }
    
    @Override
    public String toString() {
        return entryKey.toString() + " : " + entryVal.toString();
    }
}
