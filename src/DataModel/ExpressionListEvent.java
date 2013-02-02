/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import java.util.EventObject;

/**
 *
 * @author vodrazka
 */
public class ExpressionListEvent extends EventObject {
    
    public final static int EXPR_INSERTED = 1;
    public final static int EXPR_REMOVED = 2;
    
    private int code;
    private int exprID;
    
    public ExpressionListEvent(Object source, int eventCode, int exprIdentification){
        super(source);
        this.code = eventCode;
        this.exprID = exprIdentification;
    }
    
    public int getCode(){
        return this.code;
    }
    
    public int getExprID(){
        return this.exprID;
    }
}
