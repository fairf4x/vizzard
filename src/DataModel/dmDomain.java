/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import DataModelException.DomainException;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import xmladapter.DomainXMLAdapterException;
import xmladapter.domainXMLAdapter;

/**
 *
 * @author fairfax
 */
public class dmDomain {

    /**
     * Wildcards used in operator definition and task definition.
     * List of basic wildcards:
     */
    public static final String W_UNIVERSAL = "*";        // univerzal wildcard
    
    private static dmDomain instance;
    
    // complete list of requirements from PDDL 3.1 specification (not everything is used)
    public static final int STRIPS = 0;
    public static final int TYPING = 1;
    public static final int NEGATIVE_PRECONDITIONS = 2;
    public static final int DISJUNCTIVE_PRECONDITIONS = 3;
    public static final int EQUALITY = 4;
    public static final int EXISTENTIAL_PRECONDITIONS = 5;       
    public static final int UNIVERSAL_PRECONDITIONS = 6;
    public static final int CONDITIONAL_EFFECTS = 7;
    public static final int NUMERIC_FLUENTS = 8;
    public static final int OBJECT_FLUENTS = 9;
    public static final int DURATIVE_ACTIONS = 10;
    public static final int DURATION_INEQUALITIES = 11;
    public static final int CONTINUOUS_EFFECTS = 12;
    public static final int DERIVED_PREDICATES = 13;
    public static final int TIMED_INITIAL_LITERALS = 14; // implies 10
    public static final int PREFERENCES = 15;
    public static final int ACTION_COSTS = 16;
    // --- shortcuts --- //
    public static final int QUANTIFIED_PRECONDITIONS = 17; // == 5, 6
    public static final int ADL = 18; // == 0, 1, 2, 3, 4, 17, 7
    public static final int FLUENTS = 19; // == 9, 10
      
    private String domainName;
    private Set<Integer> pddlRequirements;

    private static void XMLwriteDomainProperties() {
        instance.adapter.exportDomainProperties(instance);
    }
    
    private static void XMLreadDomainProperties() {
        instance.adapter.importDomainProperties(instance);
    }
     
    private static void XMLwriteClassHierarchy() {
        instance.adapter.exportClassRoot(instance.classHierarchy.getRootClass());
    }

    private static void XMLwriteRelationSet() {
        for (dmDelegate deleg: instance.relationSet.getDelegateList()){ 
            instance.adapter.exportRelation(deleg);
        }
    }

    private static void XMLwriteStateVariableSet() {
        for (dmDelegate deleg : instance.stateVariableSet.getDelegateList()) {
            instance.adapter.exportStateVariable(deleg);
        }
    }
    
    private static void XMLwriteOperatorSet() throws DomainException {
        for (dmOperator oper : instance.operatorSet.getOperatorList()) {
            instance.adapter.exportOperator(oper);
        }
    }
    
    private static void XMLreadClassHierarchy() {
        dmiTreeNode root = instance.adapter.importRootNode();
        
        // root node have to be of type class
        assert root instanceof dmClass;
        instance.classHierarchy.setRootClass((dmClass) root);
    }

    private static void XMLreadRelationSet() {
        instance.adapter.importRelationsToModel(instance.relationSet,instance.classHierarchy);  
    }

    private static void XMLreadStateVariableSet() {
        instance.adapter.importStateVariablesToModel(instance.stateVariableSet,instance.classHierarchy);
    }

    private static void XMLreadOperatorSet() {
        instance.adapter.importOperatorsToModel(instance.operatorSet,instance.classHierarchy,instance.relationSet,instance.stateVariableSet);
    }

    private dmiClassHierarchy classHierarchy;
    private dmiElementSet relationSet;
    private dmiElementSet stateVariableSet;
    private dmiOperatorSet operatorSet;
    
    public dmiClassHierarchy getClassHierarchy() {
        return instance.classHierarchy;
    }
    
    public dmiElementSet getRelationSet() {
        return instance.relationSet;
    }
    
    public dmiElementSet getStateVariableSet() {
        return instance.stateVariableSet;
    }
    
    /**
     * adapter is used for reading and writing domain as XML document
     */
    private domainXMLAdapter adapter = null;
    
    private dmDomain() {
    }
    
    public void initDomain(dmiClassHierarchy classHier, dmiElementSet relSet, dmiElementSet stateVarSet, dmiOperatorSet operators){
        if (instance == null) {
            instance = dmDomain.getInstance();
        }
            
        
        instance.pddlRequirements = new HashSet<Integer>();
        instance.addDefaultPDDLRequirements();
        instance.domainName = "DEFAULT";
        
        instance.classHierarchy = classHier;
        instance.relationSet = relSet;
        instance.stateVariableSet = stateVarSet;
        instance.operatorSet = operators;
    }
    
    @Override
    public String toString() {
        return instance.domainName;
    }
    
    public void setDomainName(String newName) {
        instance.domainName = newName;
    }
    
    public Set<Integer> getRequirements() {
        return instance.pddlRequirements;
    }
    
    public void clearDomain() throws DomainException {
        if (instance == null) {
            throw new DomainException("Domain is not initialized.");
        }
        
        instance.pddlRequirements = new HashSet<Integer>();
        addDefaultPDDLRequirements();
        instance.domainName = "DEFAULT";
        
        instance.classHierarchy.clearClassHierarchy();
        instance.relationSet.clearElementSet();
        instance.stateVariableSet.clearElementSet();
        instance.operatorSet.clearOperatorSet();   
    }
    
    public static dmDomain getInstance() {
        if (instance == null) {
            instance = new dmDomain();
        }

        return instance;
    }
    
    
    
    public static void loadDomain(File file) throws DomainXMLAdapterException{
        instance.adapter = new domainXMLAdapter(file);
        
        // read xml file
        instance.adapter.unmarshall();
        
        XMLreadDomainProperties();
        XMLreadClassHierarchy();
        XMLreadRelationSet();
        XMLreadStateVariableSet();
        XMLreadOperatorSet();
    }

    public static void writeDomain(File file) throws DomainException{
        instance.adapter = new domainXMLAdapter(file);
        
        XMLwriteDomainProperties();
        XMLwriteClassHierarchy();
        XMLwriteRelationSet();
        XMLwriteStateVariableSet();
        XMLwriteOperatorSet();
        
        // write xml file
        instance.adapter.marshall();
    }
    
    public static void testPrint(){
        String classes = instance.classHierarchy.testPrint();
        String relations = instance.relationSet.testPrint();
        String stateVariables = instance.stateVariableSet.testPrint();
        String operators = instance.operatorSet.testPrint();
        
        System.out.println(" --- Class hierarchy ---");
        System.out.print(classes);
        
        System.out.println("--- Relations ---");
        System.out.print(relations);
        
        System.out.println("--- State variables ---");
        System.out.print(stateVariables);
        
        System.out.println("--- Operators ---");
        System.out.print(operators);
    }

    private void addDefaultPDDLRequirements() {
        pddlRequirements.add(STRIPS);
        pddlRequirements.add(TYPING);
        pddlRequirements.add(EQUALITY);
        pddlRequirements.add(OBJECT_FLUENTS);
    }
}
