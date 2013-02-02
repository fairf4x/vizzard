/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package convertor;

import CustomizedClasses.DefinitionTableModel;
import DataModel.dmDomain;
import DataModel.dmiExpression;
import domainXMLModel.ArgumentType;
import domainXMLModel.DomainProperties;
import domainXMLModel.DomainType;
import domainXMLModel.ExpressionType;
import domainXMLModel.NodeType;
import domainXMLModel.OperatorType;
import domainXMLModel.RelationType;
import domainXMLModel.SlotType;
import domainXMLModel.StateVariableType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import taskXMLModel.ConstantListType;
import taskXMLModel.ConstantType;
import taskXMLModel.RelationDefType;
import taskXMLModel.RelationSectionType;
import taskXMLModel.RowType;
import taskXMLModel.StateVariableDefType;
import taskXMLModel.StateVariablesSectionType;
import taskXMLModel.TableType;
import taskXMLModel.TaskProperties;
import taskXMLModel.TaskType;
import xmladapter.DomainXMLAdapterException;
import xmladapter.TaskXMLAdapterException;
import xmladapter.domainXMLAdapter;
import xmladapter.taskXMLAdapter;

/**
 *
 * @author fairfax
 */
public class xml2pddlConvertor {
    private static final boolean INIT_VALUE = true;
    private static final boolean GOAL_VALUE = false;
    
    /* transition use cases for method buildPropositionFromTransition */
    private static final int TRANS_UC_EFFECT = 0;   // converted transition will be used in :effect section
    private static final int TRANS_UC_PRECONDITION = 1; // converted transition will be used in :precndition section
    
    private static Set<Integer> pddlRequirements = new HashSet<Integer>();
    
    public static void domainXML2PDDL(File domainXML, File domainPDDL) throws ConvertorException, DomainXMLAdapterException {
        domainXMLAdapter adapter = new domainXMLAdapter(domainXML);
        
        adapter.unmarshall();
        
        PrintStream ps = null;
        try {
            ps = new PrintStream(domainPDDL);
        } catch (FileNotFoundException ex) {
            System.err.println("domainXML2PDDL: file not found");
        }

        assert(ps!=null);
        writePDDLDomain(adapter.getXMLRoot(),ps);
        
        ps.close();
    }

    public static void problemXML2PDDL(File problemXML, File problemPDDL) throws ConvertorException, TaskXMLAdapterException {
        taskXMLAdapter adapter = new taskXMLAdapter(problemXML);
        
        adapter.unmarshall();
        
        PrintStream ps = null;
        try {
            ps = new PrintStream(problemPDDL);
        } catch (FileNotFoundException ex) {
            System.err.println("domainXML2PDDL: file not found");
        }

        assert(ps!=null);
        writePDDLProblem(adapter.getXMLRoot(),ps);
        
        ps.close();
    }
    
    private static void writePDDLDomain(DomainType domainRoot, PrintStream ps) throws ConvertorException {
        readRequirements(domainRoot);   // set static variable pddlRequirements
        
        ps.println("(define ");
        
        writeDomainName(domainRoot,ps);
        ps.println(buildRequirements());    // write domain requiremetns (write empty string if no requirements specified)
        if (pddlRequirements.contains(dmDomain.TYPING)) {
            writeTypes(domainRoot,ps);
        }
        writeConstants(domainRoot,ps);
        writePredicates(domainRoot,ps);
        if (    pddlRequirements.contains(dmDomain.OBJECT_FLUENTS) ||
                pddlRequirements.contains(dmDomain.NUMERIC_FLUENTS) ||
                pddlRequirements.contains(dmDomain.FLUENTS)) {
                writeFluents(domainRoot,ps);
        }
        writeActions(domainRoot,ps);
        
        ps.println(")");
    }

    private static void writeDomainName(DomainType domainRoot, PrintStream ps) {
        String domainName = domainRoot.getProperties().getName();
        ps.println("(domain " + domainName +")");
    }

    private static String buildRequirements() {
        if (pddlRequirements.isEmpty()) {
            return "";    // no requirements specified
        }
        
        String result = "(:requirements";
        for (Integer reqCode: pddlRequirements) {
            result += " " + domainXMLAdapter.reqCode2string(reqCode);
        }
        
        return result + ")";
    }
    
    private static void writeTypes(DomainType domainRoot, PrintStream ps) {
        ps.println("(:types");
        writeNode(domainRoot.getClasses().getNode(),ps);
        ps.println(")");
    }

    private static void writeNode(NodeType node, PrintStream ps) {
        assert(node.getType().equals(domainXMLAdapter.CLASS_TYPE));
        // write children classes of node to line
        if ( (node.getChildren() != null) && hasNonConstantChild(node)) {
            for (NodeType child : node.getChildren().getNode()) {
                if (child.getType().equals(domainXMLAdapter.CLASS_TYPE)) {
                    ps.print(child.getName() + " ");
                }
            }

            // close line with parent node name
            ps.println("- " + node.getName());

            // recursive call to process all children
            for (NodeType child : node.getChildren().getNode()) {
                if (child.getType().equals(domainXMLAdapter.CLASS_TYPE)) {
                    writeNode(child, ps);
                }
            }
        }
    }

    private static void writePredicates(DomainType domainRoot, PrintStream ps) {
        ps.println("(:predicates");
        
        // convert predicates based on relations
        for (RelationType rel: domainRoot.getRelations().getRelation()) {
            ps.println(buildAtomicFormulaSkeleton(rel));
        }
        
        if (!pddlRequirements.contains(dmDomain.OBJECT_FLUENTS)) {
            // convert state variables as predicates
            // (if :object-fluents are possible, state wariables should be processed in writeFluents)
            for (StateVariableType stVar : domainRoot.getStateVariables().getStateVariable()) {
                ps.println(buildAtomicFormulaSkeleton(stVar));
            }
        }
        ps.println(")");
    }

    private static void writeActions(DomainType domainRoot, PrintStream ps) throws ConvertorException {
        for (OperatorType oper: domainRoot.getOperators().getOperator()) {
            writeAct(oper,ps);
        }
    }

    private static void writeAct(OperatorType oper, PrintStream ps) throws ConvertorException {
        HashMap<String, String> parameterMap = new HashMap<String, String>();
        Queue<String> preconditionQueue = new LinkedList<String>();
        Queue<String> effectQueue = new LinkedList<String>();
            
        if (pddlRequirements.contains(dmDomain.CONDITIONAL_EFFECTS)) {
            ps.println("(:action " + oper.getName());

            processExpressions_conditional_effects(oper, parameterMap, preconditionQueue, effectQueue);

            writeActionBody(parameterMap, preconditionQueue, effectQueue, ps);

            ps.println(")");
            return;
        }
        
        if (    pddlRequirements.contains(dmDomain.OBJECT_FLUENTS) ||
                pddlRequirements.contains(dmDomain.FLUENTS)) {
            ps.println("(:action " + oper.getName());
            
            processExpressions_object_fluents(oper, parameterMap, preconditionQueue, effectQueue);
            
            writeActionBody(parameterMap, preconditionQueue, effectQueue, ps);
            
            ps.println(")");
            return;
        }
        
        System.err.println("xml2pddlConvertor.writeAct: action export with current pddl requirements not available");        
    }

    

    /*
     * How are expressions transformed:
     * 
     * relation(a1,...,aN) ===> :precondition (and relation(a1,...,aN) ...
     * stateVar(a1,...,aN) == val ===> :precondition (and stateVar(a1,...,aN,val) ...
     * stateVar(a1,...,aN): val1 -> val2 ===>  :precondition (and stateVar(a1,...,aN,val1) ...
     *                                         :effect (and (not stateVar(a1,...,aN,val1)) stateVar(a1,...,aN,val2) ...
     * 
     * On the left side of  "===>" is expression found in domain and on the right side is how it could be transformed into PDDL file.
     * relations and prevailing transitions yields one entry in :precondition and non-prevailing transition yields one entry in :precondition
     * and two more in :effect.
     */

    private static boolean hasNonConstantChild(NodeType node) {
        for (NodeType child: node.getChildren().getNode()) {
            // if there is some child node which is of type "class" we return true
            if (child.getType().equals(domainXMLAdapter.CLASS_TYPE)) {
                return true;
            }
        }
        
        // all child nodes are of type "constant"
        return false;
    }

    private static void processExpressions_conditional_effects(OperatorType oper, HashMap<String, String> parameterMap, Queue<String> preconditionQueue, Queue<String> effectQueue) throws ConvertorException {
        // transitionMap - <key - group signature ,value - set of expressions>
        // expressions are in one group on following conditions:
        // 1. they are non-prevailing transitions
        // 2. they have common name
        // 3. they have common arguments
        // therefore group signature is string made of expression's name concatenated with argument symbols
        Map<String,Set<ExpressionType>> transitionMap = new HashMap<String,Set<ExpressionType>>();
        
        for (ExpressionType expr: oper.getExpression()) {
            // read all slots and update parameterMap with new variables - these variables make up operator's parameters
            for (SlotType slot: expr.getSlot()) {
                if (slot.getContentType().equals(domainXMLAdapter.VAR_TYPE)) {
                    parameterMap.put(slot.getContent(), slot.getContentClass());
                }
            }
            
            // relation expressions
            if (expr.getType().equals(dmiExpression.RELATION)) {
                preconditionQueue.add(buildPreconditionEntryFromRelation(expr));
                continue;
            }
            
            // prevailing transition expressions
            if (expr.getType().equals(dmiExpression.P_TRANS)) {
                preconditionQueue.add(buildPreconditionEntryFromPrevailingTransition(expr));
                continue;
            }
            
            // non-prevailing transition expressions
            if (expr.getType().equals(dmiExpression.TRANS)) {
                // we need to sort non-prevailing transitions into groups with common signature
                String signature = makeSignatureString(expr);
                if (!transitionMap.containsKey(signature)) {
                    Set<ExpressionType> newSet = new HashSet<ExpressionType>();
                    newSet.add(expr);
                    transitionMap.put(signature, newSet);
                } else {
                    transitionMap.get(signature).add(expr);
                }
            }
        }
        
        // process non-prevailing expressions
        // usage of conditional effect can be decided only if we know how many expressions with common signature we have
        // to process (transitions are sorted in trasitionMap - we can do it now)
        for (String sign: transitionMap.keySet()) {
            Set<ExpressionType> exprSet = transitionMap.get(sign);
            
            // there is more than one possible transition for one state variable
            if (exprSet.size() > 1) {
                for (ExpressionType expr : exprSet) {
                    String oldValueTerm = buildPropositionFromTransition(expr, TRANS_UC_PRECONDITION);
                    String newValueTerm = buildPropositionFromTransition(expr, TRANS_UC_EFFECT);
                    if ((oldValueTerm == null) || (newValueTerm == null)) {
                        throw new ConvertorException("Unable to convert expression containing wildcards.");
                    }
                    effectQueue.add("(when " + oldValueTerm + " (and (not " + oldValueTerm + ") " + newValueTerm + "))");
                }
            } else {
                assert(exprSet.size() == 1);
                // there is only one possible transition for this state variable
                Iterator<ExpressionType> iter = exprSet.iterator();
                assert(iter.hasNext());
                ExpressionType expr = iter.next();
                String oldValueTerm = buildPropositionFromTransition(expr, TRANS_UC_PRECONDITION);
                String newValueTerm = buildPropositionFromTransition(expr, TRANS_UC_EFFECT);
                if ( (oldValueTerm == null) || (newValueTerm == null) ) {
                    throw new ConvertorException("Unable to convert expression containing wildcards.");
                }
               
                preconditionQueue.add(oldValueTerm);
                effectQueue.add("(not " + oldValueTerm + ")"); // delete old value in effect
 
                effectQueue.add(newValueTerm); // set new value in effect
            }
        }
    }

    private static void processExpressions_object_fluents(  OperatorType oper,
                                                            HashMap<String,String> parameterMap,
                                                            Queue<String> preconditionQueue,
                                                            Queue<String> effectQueue) throws ConvertorException {
        for (ExpressionType expr: oper.getExpression()) {
            // read all slots and update parameterMap with new variables - these variables make up operator's parameters
            for (SlotType slot: expr.getSlot()) {
                if (slot.getContentType().equals(domainXMLAdapter.VAR_TYPE)) {
                    parameterMap.put(slot.getContent(), slot.getContentClass());
                }
            }
            
            // relation expressions
            if (expr.getType().equals(dmiExpression.RELATION)) {
                preconditionQueue.add(buildPreconditionEntryFromRelation(expr));
                
            } else {
                // either prevailing or non-prevailing transition
                assert(expr.getType().equals(dmiExpression.TRANS) || expr.getType().equals(dmiExpression.P_TRANS));
                String fluentCond = buildFluentCondition(expr);
                if (fluentCond != null) {   // if a wildcard is encountered null will be returned (nothing to be queued)
                    preconditionQueue.add(fluentCond);
                }
                
                if (expr.getType().equals(dmiExpression.TRANS)) {
                    // only non-prevailing transitions will be translated as (assign <function> <term>) in effect
                    effectQueue.add(buildFluentChangeFromTransition(expr));
                }
            }
        }
    }
    
    private static String buildPreconditionEntryFromRelation(ExpressionType expr) {
        String result = "(" + expr.getDelegate();
  
        if (expr.getSlot() != null) {
            result += " ";
            for (SlotType slot : expr.getSlot()) {
                // there is no "?" before constant symbols in PDDL actions
                if (slot.getContentType().equals(domainXMLAdapter.VAR_TYPE)) {
                    result +=" ?" + slot.getContent();
                    continue;
                }
                
                if (slot.getContentType().equals(domainXMLAdapter.CONST_TYPE)) {
                    result += " " + slot.getContent();
                }
            }
            
            result += " )";
        }
        return result;
    }

    private static String buildPreconditionEntryFromPrevailingTransition(ExpressionType expr) {
        String result = "(" + expr.getDelegate();
        HashMap<Integer, SlotType> exprSlotMap = domainXMLAdapter.mapExpressionSlots(expr.getSlot());
        
        int argCnt = exprSlotMap.size() - 1; // one slot containing prevailing transition value
        
        if (argCnt > 0) {
            
            SlotType slot;
            // process expression arguments
            result += " ";
            for (int i = 0; i < argCnt; ++i) {
                slot = exprSlotMap.get(i);
                // there is no "?" before constant symbols in PDDL actions
                if (slot.getContentType().equals(domainXMLAdapter.VAR_TYPE)) {
                    result += " ?" + slot.getContent();
                    continue;
                }

                if (slot.getContentType().equals(domainXMLAdapter.CONST_TYPE)) {
                    result += " " + slot.getContent();
                }
            }
            // write prevailing trasition value
            slot = exprSlotMap.get(dmiExpression.VAL_INDEX);
            if (slot.getContentType().equals(domainXMLAdapter.VAR_TYPE)) {
                result += " ?" + slot.getContent();
            } else {
                if (slot.getContentType().equals(domainXMLAdapter.CONST_TYPE)) {
                    result += " " + slot.getContent();
                }
            }

            result += " )";
        }
        return result;
    }

    /**
     * String proposition is to be constructed from transition
     * @param expr expression from which a proposition will be constructed
     * @param useCase determine context of usage for the proposition
     * @return proposition string or null when a wildcard value was encountered
     */
    private static String buildPropositionFromTransition(ExpressionType expr, int useCase) {
        String result = "( " + expr.getDelegate();
        HashMap<Integer, SlotType> exprSlotMap = domainXMLAdapter.mapExpressionSlots(expr.getSlot());
        
        int argCnt = exprSlotMap.size() - 2; // one slot containing prevailing transition value

        SlotType slot = null;
        
        if (argCnt > 0) {
            // process expression arguments
            result += " ";
            for (int i = 0; i < argCnt; ++i) {
                slot = exprSlotMap.get(i);
                
                assert(slot != null);
                
                // there is no "?" before constant symbols in PDDL actions
                if (slot.getContentType().equals(domainXMLAdapter.VAR_TYPE)) {
                    result += " ?" + slot.getContent();
                    continue;
                }

                if (slot.getContentType().equals(domainXMLAdapter.CONST_TYPE)) {
                    result += " " + slot.getContent();
                }
            }
        }
        
        switch (useCase) {
            case TRANS_UC_EFFECT :
                // get non-prevailing trasition TO-value
                slot = exprSlotMap.get(dmiExpression.TVAL_INDEX);                
                break;
            case TRANS_UC_PRECONDITION :
                // get non-prevailing trasition FROM-value
                slot = exprSlotMap.get(dmiExpression.FVAL_INDEX);
                break;
            default:
                System.err.println("xml2pddlConvertor.buildPropositionFromTransition: unknown useCase");
                assert(true);
        }

        assert(slot != null);
        
        if (slot.getContentType().equals(domainXMLAdapter.VAR_TYPE)) {
            result += " ?" + slot.getContent();
        }

        if (slot.getContentType().equals(domainXMLAdapter.CONST_TYPE)) {
            result += " " + slot.getContent();
        }
        
        if (slot.getContentType().equals(domainXMLAdapter.WILD_TYPE)) {
            return null;
        }

        return result + ")";
    }

    private static void writeActionBody(HashMap<String, String> parameterMap,
                                        Queue<String> preconditionQueue,
                                        Queue<String> effectQueue,
                                        PrintStream ps) {
        // :parameter (arguments )
        writeParameters(parameterMap,ps);
        
        // :precondition
        ps.println(":precondition");
        writeQueue(preconditionQueue,ps);
        
        // :effect
        ps.println(":effect");
        writeQueue(effectQueue,ps);
    }

    private static void writeParameters(HashMap<String,String> parameterMap, PrintStream ps) {
        ps.print(":parameters (");
        // <variableName,variableClass>
        for (String varName: parameterMap.keySet()) {
            if (pddlRequirements.contains(dmDomain.TYPING)) {
                ps.print(" ?" + varName + " - " + parameterMap.get(varName));
            } else {
                ps.print(" ?" + varName);
            }
        }
        
        ps.println(")");
    }

    private static void writeQueue(Queue<String> queue, PrintStream ps) {
        boolean moreThanOneLine = queue.size() > 1;
        if (moreThanOneLine) {
            ps.println("(and");
        }
        
        while (!queue.isEmpty()) {
            ps.println(" " + queue.poll());
        }
        
        if (moreThanOneLine) {
            ps.println(")");
        }
    }

    private static String makeSignatureString(ExpressionType expr) {
        String result = expr.getDelegate();

        HashMap<Integer, SlotType> exprSlotMap = domainXMLAdapter.mapExpressionSlots(expr.getSlot());

        int argCnt = exprSlotMap.size() - 2; // this method only works with non-prevailing transitions !!!
        for (int i = 0; i < argCnt; ++i) {
            SlotType slot = exprSlotMap.get(i);
            result += "_" + slot.getContent();
        }

        return result;
    }

    
    private static boolean constantSymbolsDefined(DomainType domainRoot) {
        return constantInSubtree(domainRoot.getClasses().getNode());
    }
    
    private static boolean constantInSubtree(NodeType node) {
        if (node.getType().equals(domainXMLAdapter.CONST_TYPE)) {
            return true;
        }
        
        boolean result = false;
        if (node.getChildren() != null) {
            for (NodeType child: node.getChildren().getNode()) {
                result = result || constantInSubtree(child);    // recursive call on subtree
            }
        }
        
        // if at least one child has a constant in the subtree this should return true
        return result;
    }
    
    private static void writeConstants(DomainType domainRoot, PrintStream ps) {
        if (constantSymbolsDefined(domainRoot)) {
            ps.println("(:constants");
            writeNodeConstants(domainRoot.getClasses().getNode(), ps);
            ps.println(")");
        }
    }

    private static void writeNodeConstants(NodeType node, PrintStream ps) {
        assert (node.getType().equals(domainXMLAdapter.CLASS_TYPE));
        // write children classes of node to line
        if (node.getChildren() != null) {
            if (hasConstantChild(node)) {
                for (NodeType child : node.getChildren().getNode()) {
                    if (child.getType().equals(domainXMLAdapter.CONST_TYPE)) {
                        ps.print(child.getName() + " ");
                    }
                }

                if (pddlRequirements.contains(dmDomain.TYPING)) {
                    // close line with parent node name
                    ps.println("- " + node.getName());
                } else {
                    ps.println(" ");
                }
            }
            // recursive call to process all children
            for (NodeType child : node.getChildren().getNode()) {
                if (child.getType().equals(domainXMLAdapter.CLASS_TYPE)) {
                    writeNodeConstants(child, ps);
                }
            }
        }
    }

    private static boolean hasConstantChild(NodeType node) {
        if (node.getChildren() != null) {
            for (NodeType child : node.getChildren().getNode()) {
                if (child.getType().equals(domainXMLAdapter.CONST_TYPE)) {
                    // constant found
                    return true;
                }
            }
            // no constant found
            return false;
        } else {
            // no children at all - no constant as well
            return false;
        }
    }

    private static void readRequirements(DomainType domainRoot) throws ConvertorException {
        pddlRequirements.clear();

        DomainProperties properties = domainRoot.getProperties();

        for (String req : properties.getRequirements().getRequirement()) {
            int reqCode = domainXMLAdapter.stringReq2int(req);

            if (reqCode >= 0) {
                pddlRequirements.add(reqCode);
            } else {
                throw new ConvertorException("Unknown PDDL requirement.");
            }
        }
    }

    private static String buildFluentCondition(ExpressionType expr) throws ConvertorException {
        if (!pddlRequirements.contains(dmDomain.EQUALITY)) {
            throw new ConvertorException("missing :equality requirement (processing state variable condition)");
        }

        HashMap<Integer, SlotType> exprSlotMap = domainXMLAdapter.mapExpressionSlots(expr.getSlot());


        SlotType value = null;
        if (expr.getType().equals(dmiExpression.TRANS)) {
            // in case of non-prevailing transition we need FROM value
            value = exprSlotMap.get(dmiExpression.FVAL_INDEX);
            // remove non argument slots from map
            exprSlotMap.remove(dmiExpression.FVAL_INDEX);
            exprSlotMap.remove(dmiExpression.TVAL_INDEX);
        }

        if (expr.getType().equals(dmiExpression.P_TRANS)) {
            // in case of prevailing transition we need simply its value
            value = exprSlotMap.get(dmiExpression.VAL_INDEX);
            // remove non argument slot from map
            exprSlotMap.remove(dmiExpression.VAL_INDEX);
        }

        assert (value != null);
        // get condition value of this fluent
        String condValue = null;
        if (value.getContentType().equals(domainXMLAdapter.VAR_TYPE)) {
            condValue = "?" + value.getContent();
        }
        
        if (value.getContentType().equals(domainXMLAdapter.CONST_TYPE)) {
            condValue = value.getContent();
        }
        
        if (value.getContentType().equals(domainXMLAdapter.WILD_TYPE)) {
            return null;    // no condition needed when wildcard from value found
        }

        // create function term
        String functionTerm = buildFunctionTerm(expr.getDelegate(), exprSlotMap);

        assert (condValue != null);
        return "(= " + functionTerm + " " + condValue + ")";
    }

    private static String buildFluentChangeFromTransition(ExpressionType expr) {
        HashMap<Integer, SlotType> exprSlotMap = domainXMLAdapter.mapExpressionSlots(expr.getSlot());
        // value assigned to state variable (TO VALUE)
        SlotType toValueSlot = exprSlotMap.get(dmiExpression.TVAL_INDEX);

        String assignedValue = null;
        if (toValueSlot.getContentType().equals(domainXMLAdapter.VAR_TYPE)) {
            assignedValue = "?" + toValueSlot.getContent();
        } else {
            if (toValueSlot.getContentType().equals(domainXMLAdapter.CONST_TYPE)) {
                assignedValue = toValueSlot.getContent();
            }
        }

        // remove non-argument slots
        exprSlotMap.remove(dmiExpression.FVAL_INDEX);
        exprSlotMap.remove(dmiExpression.TVAL_INDEX);

        // create function term
        String functionTerm = buildFunctionTerm(expr.getDelegate(), exprSlotMap);

        assert (assignedValue != null);
        return "(assign " + functionTerm + " " + assignedValue + ")";
    }
    
        private static void writeFluents(DomainType domainRoot, PrintStream ps) {
        ps.println("(:functions");
        for (StateVariableType stVar : domainRoot.getStateVariables().getStateVariable() ) {
            ps.println(buildAtomicFunctionSkeleton(stVar));
        }
        
        ps.println(")");
    }
    
    /**
     * Create string representing state variable as \<atomic function skeleton\> (according to PDDL 3.1 BNF).
     * 
     * @param stateVariable processed state variable
     * @return 
     */
    private static String buildAtomicFunctionSkeleton(StateVariableType stateVariable) {
        String functionSkeleton = "(" + stateVariable.getName() + buildArgumentList(stateVariable.getArgument()) + ")";
        String functionType = buildStateVarRangeType(stateVariable);
        
        return functionSkeleton + " - " + functionType;
    }
    
    /**
     * Create string representing type of state variable value.
     * 
     * @param stateVariable
     * @return 
     */
    private static String buildStateVarRangeType(StateVariableType stateVariable) {
        String result = "";

        int rangeClassCnt = stateVariable.getValueRange().getClazz().size();

        if (rangeClassCnt > 1) {
            // either one of following classes
            result += "(either";
            for (String argClass : stateVariable.getValueRange().getClazz()) {
                result += " " + argClass;
            }
            result += ")";
        } else {
            // one class only 
            result += stateVariable.getValueRange().getClazz().get(0);
        }

        return result;
    }
    
        private static String buildFunctionTerm(String fluentName, HashMap<Integer, SlotType> exprArgMap) {
        String result = "(" + fluentName;
        
        int argCnt = exprArgMap.size();

        SlotType slot = null;
        
        if (argCnt > 0) {
            // process expression arguments
            for (int i = 0; i < argCnt; ++i) {
                slot = exprArgMap.get(i);
                
                assert(slot != null);
                
                // write argument value - depends on slot content (variable/constant symbol)
                if (slot.getContentType().equals(domainXMLAdapter.VAR_TYPE)) {
                    result += " ?" + slot.getContent();
                    continue;
                }
                
                if (slot.getContentType().equals(domainXMLAdapter.CONST_TYPE)) {
                    result += " " + slot.getContent();
                }
            }
        }
        return result + ")";
    }

    /**
     * Create string representing relation as \<atomic formula skeleton\> (according to PDDL 3.1 BNF).
     * 
     * @param relation processed relation
     * @return 
     */
    private static String buildAtomicFormulaSkeleton(RelationType relation) {
        String argList = buildArgumentList(relation.getArgument());
        
        return "(" + relation.getName() + argList + ")";
    }
    
    /**
     * Create string representing state variable as \<atomic formula skeleton\> (according to PDDL 3.1 BNF).
     * 
     * @param stateVariable processed state variable
     * @return 
     */
    private static String buildAtomicFormulaSkeleton(StateVariableType stateVariable) {
        String argList = buildArgumentList(stateVariable.getArgument());
        
        // one additional argument will e created to hold state variable value
        int index = stateVariable.getArgument().size(); 
        
        if (pddlRequirements.contains(dmDomain.TYPING)) {
            String stateVarRangeType = buildStateVarRangeType(stateVariable);
            argList += " ?V" + index + " - " + stateVarRangeType;
        } else {
            argList += " ?V" + index; 
        }

        return "(" + stateVariable.getName() + argList + ")";
    }
    
    /****************************************************************************
     *                   Convert Task into PDDL                                 *
     ****************************************************************************/
    
    private static void writePDDLProblem(TaskType xMLRoot, PrintStream ps) throws ConvertorException {
        String taskName = xMLRoot.getProperties().getName();
        String domainName = xMLRoot.getProperties().getDomain();
        ps.println("(define (problem "+ taskName +")");
        ps.println("(:domain " + domainName +")");
        
        readRequirements(xMLRoot);
        
        ps.println(buildRequirements());    // write task requirements
        writeObjects(xMLRoot, ps);
        
        Queue<String> initPropositions = new LinkedList();
        Queue<String> goalPropositions = new LinkedList();

        if (xMLRoot.getRelations() != null) {
            processRelations(xMLRoot.getRelations(),initPropositions);
        }
        
        if (xMLRoot.getStateVariables() != null) {
            processStateVariables(xMLRoot.getStateVariables(),initPropositions,goalPropositions);
        }
        
        writeInit(initPropositions,ps);
        writeGoal(goalPropositions,ps);
        
        ps.println(")");
    }

    private static void writeObjects(TaskType xMLRoot, PrintStream ps) {
        ps.println("(:objects");
        for (ConstantListType constL: xMLRoot.getConstants().getConstantList()) {
            for (ConstantType obj: constL.getConstant()) {
                ps.print(" " + obj.getName());
            }
            if (pddlRequirements.contains(dmDomain.TYPING)) {   // assuming that readRequirements was called
                ps.println(" - " + constL.getClassName());
            }
        }
        ps.println(")");
    }

    private static void processRelations(RelationSectionType relations, Queue<String> initPropositions) throws ConvertorException {
        for (RelationDefType relDef : relations.getRelationDef()) {
            String relName = relDef.getRelationName();
            TableType relDefTable = relDef.getTable();
            
            for (RowType row: relDefTable.getRow()) {
                initPropositions.add(processRelationRow(relName,row));
            }
        }
    }

    private static String processRelationRow(String relName, RowType row) throws ConvertorException {
        String result = "(" + relName;
        
        for (String cell: row.getColumn()) {
            
            if (DefinitionTableModel.prefixMatch(cell,DefinitionTableModel.CONST_PREFIX)) {
                result += " " + DefinitionTableModel.getCellValue(cell);
            } else {
                throw new ConvertorException("Relation has to be defined using constant symbols only!!");
            }
        }
        
        return result + " )";
    }

    private static void processStateVariables(  StateVariablesSectionType stateVariables,
                                                Queue<String> initPropositions,
                                                Queue<String> goalPropositions) throws ConvertorException {
        for (StateVariableDefType stVarDef: stateVariables.getStateVariableDef()) {
            String stVarName = stVarDef.getStateVariableName();
            TableType relDefTable = stVarDef.getTable();

            for (RowType row : relDefTable.getRow()) {
                // check for null value !!
                initPropositions.addAll(processStateVarRow(stVarName, row, INIT_VALUE));
                goalPropositions.addAll(processStateVarRow(stVarName, row, GOAL_VALUE));
            }

        }
    }

    /**
     * Assemble one line of init/goal specification based on row in state variable table
     * @param stVarName - name of state variable
     * @param row - XML representation of processed row
     * @param b - switch between init and goal specification
     * @return 
     */
    private static List<String> processStateVarRow(String stVarName, RowType row, boolean b) throws ConvertorException {
        List<String> columns = row.getColumn();
        List<String> result = null;
        
        if (pddlRequirements.contains(dmDomain.OBJECT_FLUENTS)) {
            result = buildAssignListFromStateVariableDefinitionRow(stVarName,columns,b);
        } else {
            throw new ConvertorException("Export without :object-fluents not supported yet.");
            //result = buildAtomicFormulaFromStateVariable(stVarName,columns,b);
        }
        
        return result;
    }

    private static void writeInit(Queue<String> initPropositions, PrintStream ps) {
        ps.println("(:init");
        
        for (String prop: initPropositions) {
            if (!prop.isEmpty()) {
                ps.println(prop);
            }
        }
        
        ps.println(")");
    }

    private static void writeGoal(Queue<String> goalPropositions, PrintStream ps) {
        ps.println("(:goal");
        
        ps.println("(and");
        for (String prop: goalPropositions) {
            if (! prop.isEmpty()) {
                ps.println(prop);
            }
        }
        ps.println(")");
        
        ps.println(")");
    }

    private static void readRequirements(TaskType taskRoot) throws ConvertorException {
        pddlRequirements.clear();
        
        TaskProperties properties = taskRoot.getProperties();
        
        for (String req : properties.getRequirements().getRequirement()) {
            int reqCode = domainXMLAdapter.stringReq2int(req);

            if (reqCode >= 0) {
                pddlRequirements.add(reqCode);
            } else {
                throw new ConvertorException("Unknown PDDL requirement.");
            }
        }
    }
    
    /**
     * Create string of numbered variables from list of arguments.
     * 
     * @param arguments
     * @return 
     */
    private static String buildArgumentList(List<ArgumentType> arguments) {
        String result = "";
        for (ArgumentType arg: arguments) {
            if (pddlRequirements.contains(dmDomain.TYPING)) {
                result += " ?A" + arg.getNumber() + " - " + arg.getClazz();
            } else {
                result += " ?A" + arg.getNumber();
            }
        }
        
        return result;
    }

    private static String buildAtomicFormulaFromStateVariable(String stVarName, List<String> columns, boolean b) throws ConvertorException {
        String result = "(" + stVarName;

        // process only arguments (leave two last columns)
        for (int i = 0; i < columns.size() - 2; ++i) {
            String cell = columns.get(i);
            
            if (DefinitionTableModel.prefixMatch(cell,DefinitionTableModel.CONST_PREFIX)) {
                result += " " + DefinitionTableModel.getCellValue(cell);
            } else {
                throw new ConvertorException("State variable arguments can contain constant symbols only!!");
            }
        }

        int initValIndex = columns.size() - 2;  // two last columns in the table
        int goalValIndex = columns.size() - 1;  //
        String val = null;
        if (b == INIT_VALUE) {
            val = columns.get(initValIndex);
        } else {
            val = columns.get(goalValIndex);
        }
        
        if (DefinitionTableModel.prefixMatch(val,DefinitionTableModel.CONST_PREFIX)) {
            return result + " " + DefinitionTableModel.getCellValue(val) + ")";
        }
        
        if (DefinitionTableModel.prefixMatch(val,DefinitionTableModel.WILDC_PREFIX)) {
            return ""; // TODO - this handles only "*" wildcard
        }
        
        // if we are here it means that prefix of cell content is something we did not expected 
        throw new ConvertorException("Cell with unknown content prefix encountered.");
    }

    private static String buildBasicFunctionTerm(String stVarName, List<String> columns) throws ConvertorException {
        String result = "(" + stVarName;

        // process only arguments (leave two last columns)
        for (int i = 0; i < columns.size() - 2; ++i) {
            String cell = columns.get(i);
            
            if (DefinitionTableModel.prefixMatch(cell,DefinitionTableModel.CONST_PREFIX)) {
                result += " " + DefinitionTableModel.getCellValue(cell);
            } else {
                throw new ConvertorException("State variable arguments can contain constant symbols only!!");
            }
        }
        return result + ")";
    }

    /**
     * From row in state variable definition table:
     * C:arg1 C:arg2 ... C:init S:goal1,goal2,goal3  // if goal is defined
     *                          W:*                  // if goal is not defined return empty list
     * 
     * build something like:
     * 
     * if b = INIT_VALUE                    // list with one assign
     * (= (stVarName arg1 arg2 ...) init) 
     * 
     * if b = GOAL_VALUE                    // list with more assigns
     * (= (stVarName arg1 arg2 ...) goal1)
     * (= (stVarName arg1 arg2 ...) goal2)
     * (= (stVarName arg1 arg2 ...) goal3)
     * @param stVarName - name of the state variable
     * @param columns - strings C:arg1 C:arg2 ... init S:goal1,goal2,goal3
     * @param b - switch cell from which is taken value that is assigned
     * @return 
     */
    private static List<String> buildAssignListFromStateVariableDefinitionRow(String stVarName, List<String> columns, boolean b) throws ConvertorException {
        List<String> result = new LinkedList<String>();
        
        int targetCellIndex;
        if (b == INIT_VALUE) {
            targetCellIndex = columns.size() - 2; // cel containing Init value
        } else {
            targetCellIndex = columns.size() - 1; // cel containing Goal value
        }
                
        String cell = columns.get(targetCellIndex);

        if (b == INIT_VALUE) {
            if (DefinitionTableModel.prefixMatch(cell,DefinitionTableModel.WILDC_PREFIX)) {
                throw new ConvertorException("Initial value for " + stVarName + " is undefined.");
            }
            String constStr = DefinitionTableModel.getCellValue(cell);
            // only CONST_PREFIX is possible
            String assignStr = "(= " + buildBasicFunctionTerm(stVarName, columns) + " " + constStr + ")";
            result.add(assignStr);
            return result;
        } else { // b == GOAL_VALUE
            if (DefinitionTableModel.prefixMatch(cell,DefinitionTableModel.WILDC_PREFIX)) {
                return result;  // return empty list - no goal values specified
            }
            
            if (DefinitionTableModel.prefixMatch(cell,DefinitionTableModel.CONST_SET_PREFIX)) {
                String goalValueSet = DefinitionTableModel.getCellValue(cell);
                String[] splitSet = goalValueSet.split(""+DefinitionTableModel.GOAL_SET_DELIMITER);
                
                // for each value create one assign
                for (int i=0; i < splitSet.length; ++i) {
                   String assignStr = "(= " + buildBasicFunctionTerm(stVarName, columns) + " " + splitSet[i] + ")"; 
                   result.add(assignStr);
                }
                return result;
            }
            
            throw new ConvertorException("Goal specification contained CONST_PREFIX - ERROR");
        }
    }
}
