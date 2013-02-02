/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmladapter;

import DataModel.dmClass;
import DataModel.dmClassSet;
import DataModel.dmConstant;
import DataModel.dmDelegate;
import DataModel.dmDomain;
import DataModel.dmExpressionListModel;
import DataModel.dmOperator;
import DataModel.dmPrevailingTransition;
import DataModel.dmRelation;
import DataModel.dmRelationDelegate;
import DataModel.dmSlot;
import DataModel.dmStateVariableDelegate;
import DataModel.dmTransition;
import DataModel.dmiTreeNode;
import DataModel.dmiClassHierarchy;
import DataModel.dmiElementSet;
import DataModel.dmiExpression;
import DataModel.dmiExpressionListModel;
import DataModel.dmiOperatorSet;
import DataModelException.DomainException;
import domainXMLModel.ArgumentType;
import domainXMLModel.ChildrenListType;
import domainXMLModel.ClassTreeType;
import domainXMLModel.DomainProperties;
import domainXMLModel.DomainType;
import domainXMLModel.ExpressionType;
import domainXMLModel.NodeType;
import domainXMLModel.ObjectFactory;
import domainXMLModel.OperatorListType;
import domainXMLModel.OperatorType;
import domainXMLModel.PddlRequirementsListType;
import domainXMLModel.RelationListType;
import domainXMLModel.RelationType;
import domainXMLModel.SlotType;
import domainXMLModel.StateVariableListType;
import domainXMLModel.StateVariableType;
import domainXMLModel.ValueRangeType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author fairfax
 */
public class domainXMLAdapter {
    // complete list of requirements from PDDL 3.1 specification (not everything is used)

    public static final String STRIPS_REQ = ":strips";
    public static final String TYPING_REQ = ":typing";
    public static final String NEGATIVE_PRECONDITIONS_REQ = ":negative-preconditions";
    public static final String DISJUNCTIVE_PRECONDITIONS_REQ = ":disjunctive-preconditions";
    public static final String EQUALITY_REQ = ":equality";
    public static final String EXISTENTIAL_PRECONDITIONS_REQ = ":existential-preconditions";
    public static final String UNIVERSAL_PRECONDITIONS_REQ = ":universal-preconditions";
    public static final String CONDITIONAL_EFFECTS_REQ = ":conditional-effects";
    public static final String NUMERIC_FLUENTS_REQ = ":numeric-fluents";
    public static final String OBJECT_FLUENTS_REQ = ":object-fluents";
    public static final String DURATIVE_ACTIONS_REQ = ":durative-actions";
    public static final String DURATION_INEQUALITIES_REQ = ":duration-inequalities";
    public static final String CONTINUOUS_EFFECTS_REQ = ":continuous-effects";
    public static final String DERIVED_PREDICATES_REQ = ":derived-predicates";
    public static final String TIMED_INITIAL_LITERALS_REQ = ":timed-initial-literals"; // implies 10
    public static final String PREFERENCES_REQ = ":preferences";
    public static final String ACTION_COSTS_REQ = ":action-costs";
    // --- shortcuts --- //
    public static final String QUANTIFIED_PRECONDITIONS_REQ = ":quantified-preconditions"; // == 5, 6
    public static final String ADL_REQ = ":adl"; // == 0, 1, 2, 3, 4, 17, 7
    public static final String FLUENTS_REQ = ":fluents"; // == 9, 10
    /**
     * XML slot content type for variable.
     */
    public static final String VAR_TYPE = "variable";
    /**
     * XML slot content type for wildcard.
     */
    public static final String WILD_TYPE = "wildcard";
    /**
     * XML node and slot type for constant symbol.
     */
    public static final String CONST_TYPE = "constant";
    /**
     * XML node type for class node.
     */
    public static final String CLASS_TYPE = "class";
    private ObjectFactory of;
    private File file;
    // root of XML document
    private DomainType domain;
    // mandatory sections
    private ClassTreeType classTree;
    private RelationListType relationList;
    private StateVariableListType stateVariableList;
    private OperatorListType operatorList;

    public domainXMLAdapter(File targetFile) {
        file = targetFile;
        of = new ObjectFactory();

        domain = of.createDomainType();
        classTree = of.createClassTreeType();
        relationList = of.createRelationListType();
        stateVariableList = of.createStateVariableListType();
        operatorList = of.createOperatorListType();

        domain.setClasses(classTree);
        domain.setRelations(relationList);
        domain.setStateVariables(stateVariableList);
        domain.setOperators(operatorList);
    }

    /**
     * Method used in convertor to PDDL.
     * @return domain tag of XML document
     */
    public DomainType getXMLRoot() {
        return domain;
    }

    /**
     * Used when marshaling data into XML.
     * @param deleg delegate of marshalled relation 
     */
    public void exportRelation(dmDelegate deleg) {
        String name = deleg.toString();
        List<String> arguments = deleg.getArgumentNames();
        // create relation
        RelationType rel = of.createRelationType();
        // set name
        rel.setName(name);

        for (int i = 0; i < arguments.size(); ++i) {
            // create argument
            ArgumentType arg = of.createArgumentType();
            arg.setClazz(arguments.get(i));
            arg.setNumber(i);

            // add argument
            rel.getArgument().add(i, arg);
        }

        // add new relation
        relationList.getRelation().add(rel);
    }

    /**
     * Used when marshaling data into XML.
     * @param deleg delegate of marshalled state variable
     */
    public void exportStateVariable(dmDelegate templ) {
        assert templ instanceof dmStateVariableDelegate;
        dmStateVariableDelegate svDelegate = (dmStateVariableDelegate) templ;
        String name = svDelegate.toString();
        List<String> arguments = svDelegate.getArgumentNames();
        List<String> valueRange = svDelegate.getValueRange().toStringList();

        StateVariableType stVar = of.createStateVariableType();
        // set name
        stVar.setName(name);

        // set arguments
        for (int i = 0; i < arguments.size(); ++i) {
            // create argument
            ArgumentType arg = of.createArgumentType();
            arg.setClazz(arguments.get(i));
            arg.setNumber(i);

            // add argument
            stVar.getArgument().add(i, arg);
        }

        // set value range
        ValueRangeType range = of.createValueRangeType();

        for (String item : valueRange) {
            range.getClazz().add(item);
        }

        stVar.setValueRange(range);

        // add to the list
        stateVariableList.getStateVariable().add(stVar);
    }

    /**
     * Used when marshaling data into XML.
     * @param oper marshalled operator
     */
    public void exportOperator(dmOperator oper) throws DomainException {
        String name = oper.getName();

        OperatorType operator = of.createOperatorType();    // create XML element <operator>
        operator.setName(name);

        for (dmiExpression expr : oper.getExpressionList().getExpressions()) {
            ExpressionType expression = of.createExpressionType();  // create XML element <expression>

            // delegate name
            expression.setDelegate(expr.getDelegateName());

            // slots
            Map<Integer, dmSlot> slotMap = expr.getSlotMap();
            Map<Integer, dmClass> classMap = expr.getClassMap();

            assert (slotMap.size() == classMap.size()); // there is correspondence 1:1 between the two maps

            for (int i = 0; i < expr.getArgumentCount(); ++i) { // loop over arguments
                SlotType slot = createXMLSlot(i, classMap.get(i), slotMap.get(i)); // create XML element <slot>
                expression.getSlot().add(slot);     // insert XML element <slot>   
            }

            byte exprType = expr.getExpressionType();

            // type - Relation/Prevailing Transition/Transition
            expression.setType(exprType);

            // based on expression type we have to handle state variable origin and target slots
            switch (exprType) {
                case dmiExpression.TRANS: {   // expression is based on non-prevailing transition
                    int i = dmiExpression.FVAL_INDEX;    // transition origin value   
                    SlotType slot = createXMLSlot(i, classMap.get(i), slotMap.get(i));
                    expression.getSlot().add(slot);

                    i = dmiExpression.TVAL_INDEX;       // transition target value
                    slot = createXMLSlot(i, classMap.get(i), slotMap.get(i));
                    expression.getSlot().add(slot);

                    operator.getExpression().add(expression);   // insert XML element <expression>
                    break;
                }

                case dmiExpression.P_TRANS: {   // expression is based on transition
                    int i = dmiExpression.VAL_INDEX;    // prevailing transition value   
                    SlotType slot = createXMLSlot(i, classMap.get(i), slotMap.get(i));
                    expression.getSlot().add(slot);

                    operator.getExpression().add(expression);   // insert XML element <expression>
                    break;
                }

                case dmiExpression.RELATION: { // expression is based on relation
                    operator.getExpression().add(expression);   // insert XML element <expression>
                    break;
                }
                default:
                    throw new DomainException("exportOperator: unknown expression type");
            }
        }

        operatorList.getOperator().add(operator);   // insert XML element <operator> 
    }

    /**
     * Method used for filling data into XML element slot
     * 
     * @param index slotIndex
     * @param contentClass slot contentClass
     * @param exprSlot slot from dmiExpression
     * @return initialized instance of SlotType with data from dmSlot ready for insert into XML structure
     */
    private SlotType createXMLSlot(int index, dmClass contentClass, dmSlot exprSlot) {
        SlotType result = of.createSlotType();

        result.setContentClass(contentClass.getName()); // slot attribute: contentClass
        result.setSlotIndex(index);               // slot attribute: index
        Object slotData = exprSlot.getData();

        assert ((slotData instanceof String) || (slotData instanceof dmConstant)); // slotData should be instance of one of these classes

        if (slotData instanceof String) {
            if (slotData.equals(dmDomain.W_UNIVERSAL)) {
                result.setContentType(WILD_TYPE);
            } else {
                result.setContentType(VAR_TYPE); // slot attribute: contentType
            }
        }

        if (slotData instanceof dmConstant) {
            result.setContentType(CONST_TYPE); // slot attribute: contentType (if not set before)
        }

        result.setContent(exprSlot.toString());     // slot element: content

        return result;
    }

    public void exportClassRoot(dmiTreeNode root) {
        // create root node and attach it to the structure
        NodeType XMLroot = of.createNodeType();
        classTree.setNode(XMLroot);

        // fill structure with data
        fillNode(XMLroot, root);
    }

    private void fillNode(NodeType XMLNode, dmiTreeNode dataNode) {
        // node name
        XMLNode.setName(dataNode.toString());

        // node type
        String type = "";
        if (dataNode instanceof dmClass) {
            type = domainXMLAdapter.CLASS_TYPE;
        }

        if (dataNode instanceof dmConstant) {
            type = domainXMLAdapter.CONST_TYPE;
        }

        XMLNode.setType(type);

        if (dataNode.getChildCount() == 0) {
            // end of recursive call
            return;
        } else {
            ChildrenListType children = of.createChildrenListType();

            for (int i = 0; i < dataNode.getChildCount(); ++i) {
                dmiTreeNode childNode = dataNode.getChildAt(i);

                // skip task dependent constants
                if (childNode instanceof dmConstant) {
                    dmConstant constNode = (dmConstant) childNode;
                    if (constNode.isTaskDependent()) {
                        continue;
                    }
                }

                NodeType child = of.createNodeType();
                // we have to recursively descend the tree structure
                fillNode(child, childNode);
                children.getNode().add(child);
            }

            XMLNode.setChildren(children);
        }
    }

    public dmiTreeNode importRootNode() {
        NodeType XMLRoot = classTree.getNode();

        // root node always have to be of type class
        assert XMLRoot.getType().equals(domainXMLAdapter.CLASS_TYPE);

        dmiTreeNode root = extractNode(XMLRoot);

        return root;
    }

    private dmiTreeNode extractNode(NodeType XMLNode) {
        dmiTreeNode dmNode = null;
        if (XMLNode.getType().equals(domainXMLAdapter.CLASS_TYPE)) {
            dmNode = new dmClass();
        }

        if (XMLNode.getType().equals(domainXMLAdapter.CONST_TYPE)) {
            dmNode = new dmConstant(false);
        }

        // node is always either of type Class or Constant
        assert dmNode != null;

        // name
        dmNode.setName(XMLNode.getName());

        // end of recursion
        ChildrenListType children = XMLNode.getChildren();
        if (children == null) {
            return dmNode;
        }

        // node of type Constant don't have any siblings
        assert dmNode instanceof dmClass;

        // recursive call for all siblings
        for (NodeType child : XMLNode.getChildren().getNode()) {
            dmNode.insert(extractNode(child));
        }

        return dmNode;
    }

    public void marshall() {

        OutputStream ps = null;

        try {
            ps = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            System.out.println("Output file error.");
            Logger.getLogger(domainXMLAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }

        assert ps != null;

        try {
            JAXBElement<DomainType> rl = of.createDomain(domain);
            JAXBContext jc = JAXBContext.newInstance("domainXMLModel");
            Marshaller m = jc.createMarshaller();
            m.marshal(rl, ps);
        } catch (JAXBException jbe) {
            System.out.println(jbe.getMessage());
        }
        try {
            ps.close();
        } catch (IOException ex) {
            Logger.getLogger(domainXMLAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void unmarshall() throws DomainXMLAdapterException {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            System.out.println("Input file error.");
            Logger.getLogger(domainXMLAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            JAXBContext jc = JAXBContext.newInstance("domainXMLModel");

            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<DomainType> rootEl = (JAXBElement<DomainType>) u.unmarshal(in);
            domain = rootEl.getValue();
        } catch (JAXBException ex) {
            System.out.println(ex.toString());

            throw new DomainXMLAdapterException("Error parsing file.");
            //Logger.getLogger(domainXMLAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }

        classTree = domain.getClasses();
        relationList = domain.getRelations();
        stateVariableList = domain.getStateVariables();
        operatorList = domain.getOperators();
    }

    /**
     * Used for inserting unmarshalled XML relations into data model holding relation information
     * 
     * @param relationSet data model storing all defined relations 
     * @param classHierarchy data model storing defined classes
     */
    public void importRelationsToModel(dmiElementSet relationSet, dmiClassHierarchy classHierarchy) {
        // go through list and add every relation into model
        for (RelationType rel : relationList.getRelation()) {
            LinkedList<dmClass> arguments = new LinkedList<dmClass>();

            for (ArgumentType arg : rel.getArgument()) {
                arguments.add(arg.getNumber(), (dmClass) classHierarchy.getNodeByName(arg.getClazz()));
            }
            Object[] itemData = new Object[2];
            itemData[dmiElementSet.ITEM_NAME] = rel.getName();
            itemData[dmiElementSet.ITEM_ARGS] = arguments;
            relationSet.insertItem(itemData);
        }
    }

    /**
     * Used for inserting unmarshalled XML state variables into model holding state variable information
     * 
     * @param stateVariableSet data model for state variable storage
     * @param classHierarchy data model for class storage
     */
    public void importStateVariablesToModel(dmiElementSet stateVariableSet, dmiClassHierarchy classHierarchy) {
        // go through list and add every state variable into model
        for (StateVariableType stVar : stateVariableList.getStateVariable()) {
            LinkedList<dmClass> arguments = new LinkedList<dmClass>();

            for (ArgumentType arg : stVar.getArgument()) {
                arguments.add(arg.getNumber(), (dmClass) classHierarchy.getNodeByName(arg.getClazz()));
            }

            dmClassSet range = new dmClassSet();
            for (String item : stVar.getValueRange().getClazz()) {
                range.addClass((dmClass) classHierarchy.getNodeByName(item));
            }
            Object[] itemData = new Object[3];
            itemData[dmiElementSet.ITEM_NAME] = stVar.getName();
            itemData[dmiElementSet.ITEM_ARGS] = arguments;
            itemData[dmiElementSet.ITEM_RANG] = range;
            stateVariableSet.insertItem(itemData);
        }
    }

    /**
     * Used for inserting unmarshalled operators into data model holding operator data.
     * 
     * @param classHierarchy data model containing classes
     * @param relationSet   data model containing relations
     * @param stateVariableSet data model containing state variables
     */
    public void importOperatorsToModel(dmiOperatorSet operatorSet, dmiClassHierarchy classHierarchy, dmiElementSet relationSet, dmiElementSet stateVariableSet) {
        // go through list and add every operator into model
        for (OperatorType oper : operatorList.getOperator()) {

            dmExpressionListModel expressions = new dmExpressionListModel();

            HashMap<Object, HashSet<dmSlot>> slotConnections = new HashMap<Object, HashSet<dmSlot>>();

            for (ExpressionType expr : oper.getExpression()) {
                byte exprType = expr.getType();

                dmDelegate delegate = null;

                if (exprType == dmiExpression.RELATION) {
                    delegate = relationSet.getElementDelegateByName(expr.getDelegate());
                }

                if ( (exprType == dmiExpression.P_TRANS) || (exprType == dmiExpression.TRANS) ) {
                    delegate = stateVariableSet.getElementDelegateByName(expr.getDelegate());
                }
            
                assert (delegate != null);
                processExpressionSlots(expr, slotConnections, delegate, classHierarchy, expressions);
                
            }

            for (Object obj : slotConnections.keySet()) {
                dmSlot.connectSlots(obj, slotConnections.get(obj));
            }

            operatorSet.addElement(new dmOperator(oper.getName(), expressions));
        }
    }

    private void processExpressionSlots(ExpressionType expr,
            HashMap<Object, HashSet<dmSlot>> slotConnections,
            dmDelegate deleg,
            dmiClassHierarchy classHier,
            dmExpressionListModel exprListModel) {
        dmiExpression result = null;

        HashMap<Integer, SlotType> exprSlotMap = mapExpressionSlots(expr.getSlot()); // for acces to SlotType by index

        Map<Integer, Object> slotContentMap = exprListModel.getSlotContentMap();

        if (deleg instanceof dmStateVariableDelegate) {
            dmStateVariableDelegate stVarDeleg = (dmStateVariableDelegate) deleg;

            if (expr.getType().equals(dmiExpression.P_TRANS)) {
                SlotType valSlot = exprSlotMap.get(dmiExpression.VAL_INDEX);

                dmClass valClass = classHier.getClassByName(valSlot.getContentClass());

                assert (valClass != null);

                result = new dmPrevailingTransition(stVarDeleg, slotContentMap, valClass);

                processSlot(valSlot, result, slotConnections, classHier);
            }

            if (expr.getType().equals(dmiExpression.TRANS)) {
                SlotType fromSlot = exprSlotMap.get(dmiExpression.FVAL_INDEX);
                SlotType toSlot = exprSlotMap.get(dmiExpression.TVAL_INDEX);

                dmClass fromClass = classHier.getClassByName(fromSlot.getContentClass());
                dmClass toClass = classHier.getClassByName(toSlot.getContentClass());
                assert (fromClass != null);
                assert (toClass != null);

                result = new dmTransition(stVarDeleg, slotContentMap, fromClass, toClass);

                processSlot(fromSlot, result, slotConnections, classHier);
                processSlot(toSlot, result, slotConnections, classHier);
            }

        }

        if (deleg instanceof dmRelationDelegate) {                                  // prepare new instance of dmiExpression
            result = new dmRelation((dmRelationDelegate) deleg, slotContentMap);
        }

        // This loop runs for each expression. It filters argument slots and sort them by common content
        // into slotConnections map which is later used to connect slots with equal content.
        assert (result != null);

        for (int i = 0; i < result.getArgumentCount(); ++i) {    // read argument slot data
            System.out.println(i);
            SlotType slot = exprSlotMap.get(i);

            processSlot(slot, result, slotConnections, classHier);
        }

        // insert new expressions into model
        exprListModel.insertExpression(result, false);
    }

    /**
     * Slot with index i goes to position i in expr. Slot connections and content are maintained at
     * slotConections and class hierarchy is accessible through classHier.
     * 
     * @param slot
     * @param slotConnections
     * @param expr
     * @param i - target index (argument number starting from 0 or FVAL_INDEX/TVAL_INDEX/VAL_INDEX)
     * @param classHier
     */
    private void processSlot(SlotType slot, dmiExpression expr, HashMap<Object, HashSet<dmSlot>> slotConnections,
            dmiClassHierarchy classHier) {

        assert (slot != null);
        assert (expr != null);
        assert (slotConnections != null);
        assert (classHier != null);

        int slotIndex = slot.getSlotIndex();
        // TODO check slot class String slotClass = slot.getContentClass();
        String slotContentType = slot.getContentType();
        String slotContent = slot.getContent();

        dmSlot targetSlot = expr.getSlot(slotIndex);

        if (slotContentType.equals(VAR_TYPE) || slotContentType.equals(WILD_TYPE)) {

            // TODO check slot class

            // update slotConnections
            if (slotConnections.containsKey(slotContent)) {
                // this "slotContent" is already present in the set
                slotConnections.get(slotContent).add(targetSlot);
            } else {
                // this "slotContent" was encountered for the first time
                HashSet<dmSlot> newSet = new HashSet<dmSlot>();
                newSet.add(targetSlot);
                slotConnections.put(slotContent, newSet);
            }
            return;
        }

        if (slotContentType.equals(CONST_TYPE)) {
            // get constant from classTree
            dmConstant slotContentConst = classHier.getConstantByName(slotContent);
            assert (slotContentConst != null);

            // TODO check slot class
            // update slotConnections
            if (slotConnections.containsKey(slotContentConst)) {
                // this "slotContentConst" is already present in the set
                slotConnections.get(slotContentConst).add(expr.getSlot(slotIndex));
            } else {
                // this "slotContentConst" was encountered for the first time
                HashSet<dmSlot> newSet = new HashSet<dmSlot>();
                newSet.add(expr.getSlot(slotIndex));
                slotConnections.put(slotContentConst, newSet);
            }
        }
    }

    /**
     * Create map where keys for given slot is its index.
     * 
     * @param slot list of slots
     * @return 
     */
    public static HashMap<Integer, SlotType> mapExpressionSlots(List<SlotType> slotList) {
        HashMap<Integer, SlotType> result = new HashMap<Integer, SlotType>();

        for (SlotType slot : slotList) {
            result.put(slot.getSlotIndex(), slot);
        }

        return result;
    }

    public void importDomainProperties(dmDomain instance) {
        DomainProperties properties = domain.getProperties();
        assert (properties != null);
        instance.setDomainName(properties.getName());

        for (String req : properties.getRequirements().getRequirement()) {
            int reqCode = domainXMLAdapter.stringReq2int(req);

            if (reqCode >= 0) {
                instance.getRequirements().add(reqCode);
            } else {
                System.out.println("domainXMLAdapter.setDomainProperties: Unrecognized requirement found !!");
            }
        }
    }

    public void exportDomainProperties(dmDomain instance) {
        DomainProperties properties = of.createDomainProperties();

        properties.setName(instance.toString());

        PddlRequirementsListType reqList = of.createPddlRequirementsListType();

        for (int reqCode : instance.getRequirements()) {
            String reqString = domainXMLAdapter.reqCode2string(reqCode);
            if (!reqString.equals("ERROR")) {
                reqList.getRequirement().add(reqString);
            } else {
                System.out.println("domainXMLAdapter.getDomainProperties: Unrecognized requirement found !!");
            }
        }

        // Add into XML tree
        properties.setRequirements(reqList);
        domain.setProperties(properties);
    }

    public static int stringReq2int(String req) {
        if (req.equals(domainXMLAdapter.STRIPS_REQ)) {
            return dmDomain.STRIPS;
        }
        if (req.equals(domainXMLAdapter.TYPING_REQ)) {
            return dmDomain.TYPING;
        }
        if (req.equals(domainXMLAdapter.NEGATIVE_PRECONDITIONS_REQ)) {
            return dmDomain.NEGATIVE_PRECONDITIONS;
        }
        if (req.equals(domainXMLAdapter.DISJUNCTIVE_PRECONDITIONS_REQ)) {
            return dmDomain.DISJUNCTIVE_PRECONDITIONS;
        }
        if (req.equals(domainXMLAdapter.EQUALITY_REQ)) {
            return dmDomain.EQUALITY;
        }
        if (req.equals(domainXMLAdapter.EXISTENTIAL_PRECONDITIONS_REQ)) {
            return dmDomain.EXISTENTIAL_PRECONDITIONS;
        }
        if (req.equals(domainXMLAdapter.UNIVERSAL_PRECONDITIONS_REQ)) {
            return dmDomain.UNIVERSAL_PRECONDITIONS;
        }
        if (req.equals(domainXMLAdapter.CONDITIONAL_EFFECTS_REQ)) {
            return dmDomain.CONDITIONAL_EFFECTS;
        }
        if (req.equals(domainXMLAdapter.NUMERIC_FLUENTS_REQ)) {
            return dmDomain.NUMERIC_FLUENTS;
        }
        if (req.equals(domainXMLAdapter.OBJECT_FLUENTS_REQ)) {
            return dmDomain.OBJECT_FLUENTS;
        }
        if (req.equals(domainXMLAdapter.DURATIVE_ACTIONS_REQ)) {
            return dmDomain.DURATIVE_ACTIONS;
        }
        if (req.equals(domainXMLAdapter.DURATION_INEQUALITIES_REQ)) {
            return dmDomain.DURATION_INEQUALITIES;
        }
        if (req.equals(domainXMLAdapter.CONTINUOUS_EFFECTS_REQ)) {
            return dmDomain.CONTINUOUS_EFFECTS;
        }
        if (req.equals(domainXMLAdapter.DERIVED_PREDICATES_REQ)) {
            return dmDomain.DERIVED_PREDICATES;
        }
        if (req.equals(domainXMLAdapter.TIMED_INITIAL_LITERALS_REQ)) {
            return dmDomain.TIMED_INITIAL_LITERALS;
        }
        if (req.equals(domainXMLAdapter.PREFERENCES_REQ)) {
            return dmDomain.PREFERENCES;
        }
        if (req.equals(domainXMLAdapter.ACTION_COSTS_REQ)) {
            return dmDomain.ACTION_COSTS;
        }
        if (req.equals(domainXMLAdapter.QUANTIFIED_PRECONDITIONS_REQ)) {
            return dmDomain.QUANTIFIED_PRECONDITIONS;
        }
        if (req.equals(domainXMLAdapter.ADL_REQ)) {
            return dmDomain.ADL;
        }
        if (req.equals(domainXMLAdapter.FLUENTS_REQ)) {
            return dmDomain.FLUENTS;
        }

        System.err.println("stringReq2int : unknown requirement");
        assert (true);
        return -1;
    }

    public static String reqCode2string(int code) {
        switch (code) {
            case dmDomain.STRIPS:
                return domainXMLAdapter.STRIPS_REQ;
            case dmDomain.TYPING:
                return domainXMLAdapter.TYPING_REQ;
            case dmDomain.DISJUNCTIVE_PRECONDITIONS:
                return domainXMLAdapter.DISJUNCTIVE_PRECONDITIONS_REQ;
            case dmDomain.EQUALITY:
                return domainXMLAdapter.EQUALITY_REQ;
            case dmDomain.EXISTENTIAL_PRECONDITIONS:
                return domainXMLAdapter.EXISTENTIAL_PRECONDITIONS_REQ;
            case dmDomain.UNIVERSAL_PRECONDITIONS:
                return domainXMLAdapter.UNIVERSAL_PRECONDITIONS_REQ;
            case dmDomain.CONDITIONAL_EFFECTS:
                return domainXMLAdapter.CONDITIONAL_EFFECTS_REQ;
            case dmDomain.NUMERIC_FLUENTS:
                return domainXMLAdapter.NUMERIC_FLUENTS_REQ;
            case dmDomain.OBJECT_FLUENTS:
                return domainXMLAdapter.OBJECT_FLUENTS_REQ;
            case dmDomain.DURATIVE_ACTIONS:
                return domainXMLAdapter.DURATIVE_ACTIONS_REQ;
            case dmDomain.DURATION_INEQUALITIES:
                return domainXMLAdapter.DURATION_INEQUALITIES_REQ;
            case dmDomain.CONTINUOUS_EFFECTS:
                return domainXMLAdapter.CONTINUOUS_EFFECTS_REQ;
            case dmDomain.DERIVED_PREDICATES:
                return domainXMLAdapter.DERIVED_PREDICATES_REQ;
            case dmDomain.TIMED_INITIAL_LITERALS:
                return domainXMLAdapter.TIMED_INITIAL_LITERALS_REQ;
            case dmDomain.PREFERENCES:
                return domainXMLAdapter.PREFERENCES_REQ;
            case dmDomain.ACTION_COSTS:
                return domainXMLAdapter.ACTION_COSTS_REQ;
            case dmDomain.QUANTIFIED_PRECONDITIONS:
                return domainXMLAdapter.QUANTIFIED_PRECONDITIONS_REQ;
            case dmDomain.ADL:
                return domainXMLAdapter.ADL_REQ;
            case dmDomain.FLUENTS:
                return domainXMLAdapter.FLUENTS_REQ;
            default:
                assert (true);
                return "ERROR";
        }
    }
}
