/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataModel;

import CustomizedClasses.DefinitionTableModel;
import DataModelException.TaskException;
import DataModelException.TreeModelException;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;
import xmladapter.TaskXMLAdapterException;
import xmladapter.taskXMLAdapter;

/**
 *
 * @author fairfax
 */
public class dmTask implements TreeModelListener {
    private static final int RELATION_DEF = 0;
    private static final int STATE_VAR_DEF_ALL = 1;
    private static final int STATE_VAR_DEF_ARGS_ONLY = 2;
    
    private String taskName;
    private dmDomain taskDomain;
    private dmTreeModel treeModel = null;
    
    private taskXMLAdapter adapter = null;
    
    // constants need to be defined -> for each class list of constants
    private HashMap<String,Set<String>> constantMap = new HashMap<String,Set<String>>();
    
    // key - relation name
    // value - table model
    private HashMap<String,DefinitionTableModel> relationMap = new HashMap<String,DefinitionTableModel>();
    private HashMap<String,DefinitionTableModel> stateVariableMap = new HashMap<String,DefinitionTableModel>();
    
    public dmTask(String name, dmDomain domain){
        this.taskName = name;
        this.taskDomain = domain;
        
        initRelationMap();
        initStateVarMap();
    }
    
    private void clear() {
        constantMap.clear();
        relationMap.clear();
        stateVariableMap.clear();
    }
    
    /**
     * Return name of this task.
     * @return taskName
     */
    @Override
    public String toString() {
        return this.taskName;
    }
    
    /**
     * Set task name.
     * @param name 
     */
    public void setName(String name) {
        this.taskName = name;
    }
    
    public String getDomainName() {
        return taskDomain.toString();
    }
    
    public Set<Integer> getRequirements() {
        return taskDomain.getRequirements();
    }
    
    public void writeTask(File file) {
        adapter = new taskXMLAdapter(file);
        
        XMLwriteTaskProperties();
        XMLwriteTaskConstants();
        XMLwriteTaskRelations();
        XMLwriteTaskStateVariables();

        adapter.marshall();
    }
    
    public void loadTask(File file) throws TaskXMLAdapterException {
        adapter = new taskXMLAdapter(file);
        
        adapter.unmarshall();
        
        this.clear();
        
        XMLreadTaskProperties();
        XMLreadTaskConstants();
        XMLreadTaskRelations();
        XMLreadTaskStateVariables();
    }
    /**
     * When task is activated, task specific constants are displayed in specified classTree.
     * Task is also registered as tree model listener - every change (constant addition/removal)
     * is propagated in dmTask constantMap.
     * 
     * @param classTree 
     */
    public void activate(dmTreeModel classTree) {
        assert(classTree != null);
        treeModel = classTree;

        // add all task specific constants
        for (String domainClass : constantMap.keySet()) {
            insertTaskSpecificConstants(domainClass);
        }
        
        // register self as treeModel listener
        treeModel.addTreeModelListener(this);
        
        // actualize
        reinit();
        
        System.out.println("Task "+ taskName +" activated.");
    }
    
    
    /**
     * Check taskDomain for changes and update stored data.
     */
    private void reinit() {
        // reinit constants
        checkClassTreeForChanges();
        
        // reinit relations
        checkRelationModelForChanges();
        
        // reinit state variables
        checkStateVarModelForChanges();
    }
    
    /**
     * During deactivation task specific constants are preserved in constantMap
     * and after the task is removed from list of treeModel listeners all task specific constants are removed. 
     */
    public void deactivate() {
        
        // unregister from treeModel list of listeners
        assert(treeModel != null);
        treeModel.removeTreeModelListener(this);
        
        // remove all task specific constants from treeModel
        treeModel.clearTaskSpecificConstants();
        
        System.out.println("Task "+ taskName +" deactivated.");
    }

    public DefinitionTableModel getRelationDefinition(String relation) {
        return relationMap.get(relation);
    }
    
    public TableModel getStateVarDefinition(String stateVarName) {
        return stateVariableMap.get(stateVarName);
    }
    
    @Override
    public void treeNodesChanged(TreeModelEvent tme) {
        dmiTreeNode parentNode = (dmiTreeNode) tme.getTreePath().getLastPathComponent();
        assert(parentNode != null);
        System.out.println("treeNodeChanged - task, parent: " + parentNode.getName());
        
        Object[] nodes = tme.getChildren();
        
        for (int i=0; i < nodes.length; ++i) {
            String oldName = ((dmConstant)nodes[i]).getOldName();
            String newName = ((dmConstant)nodes[i]).getName();
            String parentClass = parentNode.getName();
            // System.out.println(oldName + "->" + newName);
            renameConstants(parentClass,oldName,newName);
        }
    }

    @Override
    public void treeNodesInserted(TreeModelEvent tme) {
        // process only insert of new task dependent constant
        Object[] inserted = tme.getChildren();
        
        for (int i=0; i < inserted.length; ++i) {
            if (inserted[i] instanceof dmConstant) {
                dmConstant insConst = (dmConstant) inserted[i];
                if (insConst.isTaskDependent()) {
                    String parentClass = insConst.getParent().getName();
                    Set<String> targetSet = constantMap.get(parentClass); // get list of constants under same class
                    if (targetSet == null) { // parentClass doesn't have record in constantMap -> create it
                        targetSet = new HashSet<String>();
                        constantMap.put(parentClass, targetSet);
                    }
                    
                    targetSet.add(insConst.getName()); // insert constant name
                    
                    updateStateVarTables(parentClass); // update content of state variable tables
                }
            }
        }
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent tme) {
        // if class removed or some of its successors is key in constantMap
        // we need to remove the <key,value> pair
        Object[] removed = tme.getChildren();
        
        HashSet<String> alteredClasses = new HashSet<String>();
        
        for (int i=0; i < removed.length; ++i) {
            if (removed[i] instanceof dmConstant) {
                dmConstant remConst = (dmConstant) removed[i];
                if (remConst.isTaskDependent()) {
                    String parentClass = remConst.getParent().getName();
                    Set<String> targetSet = constantMap.get(parentClass);
                    if (targetSet == null) {
                        throw new RuntimeException("dmTask: removed constant is not present in constantMap");
                    }
                    targetSet.remove(remConst.getName());
                    alteredClasses.add(parentClass);
                }
                System.out.println("Constant was removed.");
                continue;
            }
            
            if (removed[i] instanceof dmClass) {
                dmClass remClass = (dmClass) removed[i];
                
                // if class was removed all children classes are altered (their constants dissapear)
                List<String> childClasses = new LinkedList<String>();
                remClass.makeSubtreeClassSymbolList(childClasses);
                if (!childClasses.isEmpty()) {
                    alteredClasses.addAll(childClasses);
                }
                
                String className = remClass.getName();
                constantMap.remove(className);  // remove all constants of this class
                System.out.println("Class was removed.");
                alteredClasses.add(className);
            }
        }
        
        for (String altered: alteredClasses) {
            updateStateVarTables(altered);
        }
    }

    @Override
    public void treeStructureChanged(TreeModelEvent tme) {
        System.out.println("treeStructureChanged - dmTask");
    }

    /**
     * insert task specific constants of given class
     * @param domainClass 
     */
    private void insertTaskSpecificConstants(String domainClass){
        Set<String> constSet = constantMap.get(domainClass); // list of constant names
        
        dmClass classNode = treeModel.getClassByName(domainClass);
        
        if (classNode != null) {    // classNode == null -> currently there is no declaration for domainClass
            // insert all constants listed
            for (String constName: constSet) {
                try {
                    treeModel.insertNewConstant(constName, classNode, true);
                } catch (TreeModelException ex) {
                    Logger.getLogger(dmTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void initRelationMap() {
        dmiElementSet relationDeclarations = taskDomain.getRelationSet();
        for (dmDelegate deleg: relationDeclarations.getDelegateList()) {
            initRelationDefinition(deleg);
        }
    }

    private void initStateVarMap() {
        dmiElementSet stateVarDeclarations = taskDomain.getStateVariableSet();
        for (dmDelegate deleg: stateVarDeclarations.getDelegateList()) {
            stateVariableMap.put(deleg.toString(), initStateVarModel(deleg));
        }
    }

    /**
     * Create all possible rows in state variable table - each combination of constants yield one state variable.
     * Argument classes constrain possible combinations.
     * 
     * @param i definition table column count 
     * @param labels definition table column names
     * @param deleg
     * @return 
     */
    private DefinitionTableModel createStateVariables(List<String> labels, dmDelegate deleg) {
        DefinitionTableModel stateVarModel = new DefinitionTableModel(labels);
        
        List<dmClass> argumentClasses = deleg.getArgumentValues();
        List<List<String>> constantSets = new LinkedList<List<String>>(); // set of constants for each argument
        
        for (dmClass clazz: argumentClasses) {
            LinkedList<String> constList = new LinkedList<String>();
            clazz.makeSubtreeConstantSymbolList(constList);
            constantSets.add(constList);
        }
        // constantSets is initialized
        
        int argCnt = argumentClasses.size();
        
        // count all state variables, initialize size array
        // and index array
        // Example: stateVar(class1,class2,...)
        // class1 has N1 constants
        // class2 has N2 constants
        // ...
        // stateVarCnt = N1*N2*...
        int stateVarCnt = 1;
        int[] sizeArray = new int[argCnt]; // [N1,N2,...]
        int[] actIndex = new int[argCnt]; // initialized as [0,0,...]
        int index = 0;
        for (List<String> list: constantSets) {
            stateVarCnt *= list.size();
            sizeArray[index] = list.size();
            actIndex[index++] = 0;    // initialization
        }
        
        // create table rows
        for (int row=0; row < stateVarCnt; ++row) {
            stateVarModel.addRow(initStateVariableRow(actIndex,constantSets));
            nextIndex(actIndex,sizeArray);
        }
        
        return stateVarModel;
    }

    private void nextIndex(int[] act, int[] max) {
        assert (act.length == max.length);
        int col = act.length - 1;
        if (col < 0) {
            return; // act.length == 0 - possible when no state variable arguments defined
        }
            

        act[col] += 1;
        while ((col >= 0) && (act[col] == max[col])) {
            act[col--] = 0;
            if (col < 0) {
                break;
            }
            act[col] += 1;
        }
    }

    private List<dmClass> getRelationArgumentClasses(String relationName) {
        dmRelationDelegate delegate = (dmRelationDelegate)taskDomain.getRelationSet().getElementDelegateByName(relationName);
        assert(delegate != null);
        return delegate.getArgumentValues();
    }
    
    /**
     * Method for retrieving set of classes used among state variable arguments.
     * @param stateVarName
     * @return set of dmClasses
     */
    private List<dmClass> getStateVarArgumentClasses(String stateVarName) {
        dmStateVariableDelegate delegate = (dmStateVariableDelegate)taskDomain.getStateVariableSet().getElementDelegateByName(stateVarName);
        assert(delegate != null);
        return delegate.getArgumentValues();
    }
    
    private void updateStateVarTables(String className) {
        
        // check every state variable
        for (String stateVar: stateVariableMap.keySet()) {
            Set<Integer> relevantColumns = getRelevantColumns(className, stateVar,STATE_VAR_DEF_ARGS_ONLY);
            if (relevantColumns.size() < 1) { // skip unafected state variables
                continue;
            }
            
            dmStateVariableDelegate deleg = (dmStateVariableDelegate) taskDomain.getStateVariableSet().getElementDelegateByName(stateVar);
            
            updateStateVariablesInTask(deleg);
        }
    }

    private void transferValues(DefinitionTableModel oldModel, DefinitionTableModel newModel) {
        // indexes of columns we would like to compare (excluding Init and Goal values => -2)
        int[] colIndexes = new int[oldModel.getColumnCount() - 2];
        for (int i=0; i<colIndexes.length; ++i) {
            colIndexes[i] = i;
        }
        // indexes for last two columns 
        int initValIndex = colIndexes.length;
        int goalValIndex = colIndexes.length + 1;
        
        // walk through all rows of old model and transfer existing Init/Goal values if matching row exists
        for (int oldRow=0; oldRow < oldModel.getRowCount(); ++oldRow) {
            int newRow = 0;
            while ( newRow < newModel.getRowCount() ) {
                if ( DefinitionTableModel.matchingRow(colIndexes,oldRow,newRow,oldModel,newModel) ) {
                    break;  // we found matching row
                } else {
                    newRow++;
                }
            }
            
            if (newRow < newModel.getRowCount()) { // matching row was found (else newRow == newModel.getRowCount())
                // transfer values stored in old model into matching row of new model
                Object oldVal = oldModel.getValueAt(oldRow,initValIndex);
                if (oldVal != null) {
                    newModel.setValueAt(oldVal, newRow, initValIndex);
                }
                oldVal = oldModel.getValueAt(oldRow,goalValIndex);
                
                if (oldVal != null) {
                    newModel.setValueAt(oldVal, newRow, goalValIndex);
                }
            } 
        }
    }

    /**
     * Check for classes present in constantMap and missing in domain - those are to be 
     * removed from constantMap.
     */
    private void checkClassTreeForChanges() {
        for (String className: constantMap.keySet()) {
            if (taskDomain.getClassHierarchy().getClassByName(className) == null) { // class not found
                constantMap.remove(className);
            }
        }
    }

    /**
     * Compare relations declared in domain and relations defined in this task.
     * Each declared relation must have corresponding model in relationMap.
     * This method is maintaining following invariants:
     * 1. for each declared relation there is definition table in this task with matching signature
     * 2. there are no definition tables without relation (with matching signature) declared
     * 
     * Some data can be lost when change made in relation definition is propagated into definition.
     * - when reinitializing because of signature mismatch
     * - when declaration is removed definitions are automatically deleted as well
     */
    private void checkRelationModelForChanges() {
        Set<String> definedRelations = new HashSet<String>(relationMap.keySet()); // temporary set
        for (dmDelegate deleg: taskDomain.getRelationSet().getDelegateList() ) {
            definedRelations.remove(deleg.toString()); // each relation declared in the domain is removed
            DefinitionTableModel taskRelDef = relationMap.get(deleg.toString());
            
            if (taskRelDef == null) { // definition table model not found - new relation
                initRelationDefinition(deleg);
                continue;
            } else { // relation definition found in task
                if (!checkSignature(deleg,taskRelDef)) { // in case of different signature reinit model
                    initRelationDefinition(deleg);
                }                                       // we don't need to change model if signaures matches
            }
        }
        
        // clear redundant definitions
        for (String undeclared: definedRelations) {
            relationMap.remove(undeclared);
        }
    }

    /**
     * Check if state variable declarations corresponds to definitions.
     * If signatures matches, update state variable definition tables (task independent constants may have been added)
     */
    private void checkStateVarModelForChanges() {
        Set<String> definedStateVars = new HashSet(stateVariableMap.keySet()); // we will cross out state vars which are defined
        
        for (dmDelegate deleg: taskDomain.getStateVariableSet().getDelegateList()) {
            definedStateVars.remove(deleg.toString()); // each state variable declared in the domain is removed from set
            updateStateVariablesInTask(deleg);
        }
        
        // clear redundant records - state variables which are not defined
        for (String undeclared: definedStateVars) {
            stateVariableMap.remove(undeclared);
        }
    }
    
    private void initRelationDefinition(dmDelegate deleg) {
        String relationName = deleg.toString();
        DefinitionTableModel relationModel = new DefinitionTableModel(deleg.getArgumentNames());

        relationMap.put(relationName, relationModel);
    }
    
    /**
     * Check delegate arguments with DefinitionTableModel 
     * @param deleg
     * @param taskDefTable
     * @return 
     */
    private boolean checkSignature(dmDelegate deleg, DefinitionTableModel taskDefTable) {
        List<String> declaredArgs = deleg.getArgumentNames();
        
        // check argument count
        if (deleg instanceof dmRelationDelegate) { // relation delegate: argument count and column count should be equal
            if (deleg.getArgumentCount() != taskDefTable.getColumnCount()) {
                return false;
            }
        }
        
        if (deleg instanceof dmStateVariableDelegate) { // state var. delegate: argument count should be equal to column count-2 (init and goal value)
            if (deleg.getArgumentCount() != (taskDefTable.getColumnCount() - 2)) {
                return false;
            }
        }
        
        // check signature
        for (int i=0; i < declaredArgs.size(); ++i ) {
            if ( !declaredArgs.get(i).equals(taskDefTable.getColumnName(i)) ) {
                return false;
            }
        }
        
        return true;
    }

    public void renameClass(String oldName, String newName) throws TaskException {
        // find corresponding record in constantMap
        if (constantMap.containsKey(oldName)) {
            // remember constants in renamed class and remove old record
            Set<String> constants = constantMap.remove(oldName);
            // insert altered record
            constantMap.put(newName, constants);   
        }
        
        // change relation table headers
        for (DefinitionTableModel table : relationMap.values()) {
            table.renameClassInHeader(oldName, newName);
        }

        // change state variable table headers
        for (DefinitionTableModel table : stateVariableMap.values()) {
            table.renameClassInHeader(oldName, newName);
        }
    }

    public void renameConstants(String parentClass, String oldName, String newName) {
        // rename constants in constant map
        if (constantMap.containsKey(parentClass)) { // is there a record for constants from parentClass ?
            Set<String> constants = constantMap.get(parentClass); // get list of constants
            
            // change its content
            constants.remove(oldName); 
            constants.add(newName);
        }
        
        Set<Integer> relevantColumns;
        // rename constants in relation definition table
        for (String rel : relationMap.keySet()) {
            relevantColumns = getRelevantColumns(parentClass,rel,RELATION_DEF);
            if (relevantColumns.size() < 1) {   // no relevant columns found
                continue;
            }
            
            DefinitionTableModel tableModel = relationMap.get(rel); // get DefinitionTableModel for relation
            tableModel.renameConstantsInTable(relevantColumns, oldName, newName);
        }
        // rename constanst in state variable definition table
        for (String stateVar: stateVariableMap.keySet()) {
            relevantColumns = getRelevantColumns(parentClass,stateVar,STATE_VAR_DEF_ALL);
            if (relevantColumns.size() < 1) { // no relevant columns found
                continue;
            }
   
            DefinitionTableModel tableModel = stateVariableMap.get(stateVar);
            tableModel.renameConstantsInTable(relevantColumns,oldName,newName);
        }
    }

    private void XMLwriteTaskConstants() {
        for (String className: constantMap.keySet()) {
            adapter.exportConstantSet(className,constantMap.get(className));
        }
    }

    private void XMLwriteTaskRelations() {
        adapter.exportRelationMap(relationMap);
    }

    private void XMLwriteTaskStateVariables() {
        adapter.exportStateVariableMap(stateVariableMap);
    }

    private void XMLreadTaskConstants() {
        adapter.importTaskConstantMap(constantMap,taskDomain.getClassHierarchy());
    }

    private void XMLreadTaskRelations() {
        adapter.importRelationMap(relationMap,taskDomain.getRelationSet());
    }

    private void XMLreadTaskStateVariables() {
        adapter.importStateVariableMap(stateVariableMap,taskDomain.getStateVariableSet());
    }

    private void updateStateVariablesInTask(dmDelegate deleg) {
        assert(deleg != null);
        
        if (stateVariableMap.containsKey(deleg.toString())) {
            // state variable is defined in task
            DefinitionTableModel newTableModel = initStateVarModel(deleg);
            DefinitionTableModel oldTableModel = stateVariableMap.get(deleg.toString());
            
            if (checkSignature(deleg, oldTableModel)) { // transfer values only if signature was preserved
                // transfer existing values
                transferValues(oldTableModel, newTableModel);
            }
            stateVariableMap.put(deleg.toString(), newTableModel);
            
        } else {
            // state variable was not defined in task
            stateVariableMap.put(deleg.toString(),initStateVarModel(deleg));
        }
    }

    /**
     * Wrapper method for createStateVariables
     * @param deleg
     * @return 
     */
    private DefinitionTableModel initStateVarModel(dmDelegate deleg) {
        List<String> labels = deleg.getArgumentNames();
        labels.add(taskXMLAdapter.INIT_STRING);
        labels.add(taskXMLAdapter.GOAL_STRING);
        return createStateVariables(labels, deleg);
    }

    private void XMLwriteTaskProperties() {
        adapter.exportTaskProperties(this);
    }

    private void XMLreadTaskProperties() throws TaskXMLAdapterException {
        adapter.importTaskProperties(this);
    }

    private Set<Integer> getRelevantColumns(String queryClass, String name, int usageSwitch) {
        Set<Integer> result = new HashSet<Integer>();
        List<dmClass> argClassList;
        
        if (usageSwitch == RELATION_DEF) {
            argClassList = getRelationArgumentClasses(name); // get list of relation argument classes
        } else {
            assert((usageSwitch == STATE_VAR_DEF_ALL) || (usageSwitch == STATE_VAR_DEF_ARGS_ONLY));
            argClassList = getStateVarArgumentClasses(name); // get list of state variable argument classes
        }
        
        // Argument column selection (no difference between relation and state variable) 
        for (int col = 0; col < argClassList.size(); ++col) {
            dmClass arg = argClassList.get(col);            
            if (queryClass.equals(arg.getName()) || arg.isChildClass(queryClass)) {
                // col is relevant column
                result.add(col);
            }
        }
        
        // Init & Goal column selection (only for state variables)
        if (usageSwitch == STATE_VAR_DEF_ALL) {
            int InitValIndex = argClassList.size();
            int GoalValIndex = argClassList.size() + 1;
            
            Set<dmClass> rangeSet = getValueRangeSet(name);   // get value range set for this state variable
            
            // check if queryClass is relevant for init/goal value column
            for (dmClass valClass: rangeSet) {
                if (queryClass.equals(valClass.getName()) || valClass.isChildClass(queryClass)) {
                    result.add(InitValIndex);
                    result.add(GoalValIndex);
                    break;
                }
            }
        } 
        
        return result;
    }

    private Set<dmClass> getValueRangeSet(String name) {
        dmStateVariableDelegate stateVarDelegate = (dmStateVariableDelegate) taskDomain.getStateVariableSet().getElementDelegateByName(name);
        return stateVarDelegate.getValueRange().toSet();
    }
    
    /**
     * Add initialized row to relation model for selectedRelation.
     * @param selectedRelation
     */
    public void addRelationDefinitionRow(String selectedRelation, int rowCnt) {
        DefinitionTableModel model = (DefinitionTableModel)relationMap.get(selectedRelation);
        assert(model != null);
        for (int i=0; i < rowCnt; ++i) {
            model.addRow(initNewRelationRow(model.getColumnCount()));
        }
    }

    private String[] initNewRelationRow(int columnCount) {
        String[] result = new String[columnCount];
        
        for (int i=0; i < result.length; ++i) {
            result[i] = ""+DefinitionTableModel.WILDC_PREFIX + DefinitionTableModel.DELIMITER + dmDomain.W_UNIVERSAL;
        }
        
        return result;
    }

    /**
     * Initialize row for state variable definition table.
     * @param actIndex for each argument contains index determining which constant should be used in this row
     * @param constantSets for each argument contains list of available constants
     * @return row filled with constants in place of arguments and default values in last two cells (init and goal value)
     */
    private String[] initStateVariableRow(int[] actIndex, List<List<String>> constantSets) {
        assert(actIndex.length == constantSets.size());
        String[] result = new String[actIndex.length + 2];  // last two cells contain init and goal value

        // initialize arguments
        for (int i=0; i < actIndex.length; ++i) {
            String constName = constantSets.get(i).get(actIndex[i]);
            result[i] = ""+DefinitionTableModel.CONST_PREFIX + DefinitionTableModel.DELIMITER + constName;
        }
        
        // initialize init and goal values
        int initValIndex = actIndex.length;
        int goalValIndex = actIndex.length + 1;
        String wildcardValue = ""+DefinitionTableModel.WILDC_PREFIX + DefinitionTableModel.DELIMITER + dmDomain.W_UNIVERSAL;
        result[initValIndex] = wildcardValue;
        result[goalValIndex] = wildcardValue;
        
        return result;
    }

    public void delRelationDefinitionRows(String selectedRelation, int[] selectedRows) {
        DefinitionTableModel model = (DefinitionTableModel)relationMap.get(selectedRelation);
        assert(model != null);
        model.delRows(selectedRows);
    }

}