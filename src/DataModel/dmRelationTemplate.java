package DataModel;

/**
 * Simple read only structure which hold all information needed to print
 * and identify a relation in order to create dmRelation expression in operator.
 * 
 * @author fairfax
 */
public class dmRelationTemplate {
    protected String[] argClasses;
    protected String templateName;
    
    dmRelationTemplate(String name, String[] args) {
        argClasses = args;
        templateName = name; 
    }
    
    public String getName() {
        return templateName;
    }
    
    /**
     * Assemble string in format: templateName(arg1,...,argn)
     * 
     * @return 
     */
    @Override
    public String toString() {
        String result = templateName + "(";
        
        for (int i=0; i < argClasses.length; ++i) {
            result += " " + argClasses[i];
        }
        
        return result + " )";
    }
}
