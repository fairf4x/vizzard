/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xmladapter;

import CustomizedClasses.DefinitionTableModel;
import DataModel.dmDelegate;
import DataModel.dmDomain;
import DataModel.dmTask;
import DataModel.dmiClassHierarchy;
import DataModel.dmiElementSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import taskXMLModel.ArgumentType;
import taskXMLModel.ConstantListType;
import taskXMLModel.ConstantType;
import taskXMLModel.ConstantsSectionType;
import taskXMLModel.ObjectFactory;
import taskXMLModel.PddlRequirementsListType;
import taskXMLModel.RelationDefType;
import taskXMLModel.RelationSectionType;
import taskXMLModel.RowType;
import taskXMLModel.SignatureType;
import taskXMLModel.StateVariableDefType;
import taskXMLModel.StateVariablesSectionType;
import taskXMLModel.TableType;
import taskXMLModel.TaskProperties;
import taskXMLModel.TaskType;

/**
 *
 * @author fairfax
 */
public class taskXMLAdapter {
    public static final String INIT_STRING="Init";
    public static final String GOAL_STRING="Goal";
    
    private File file;
    private ObjectFactory of;
    
    // root of XML document
    private TaskType task = null;
    
    // mandatory sections
    private ConstantsSectionType constants = null;
    private RelationSectionType relations = null;
    private StateVariablesSectionType stateVariables = null; 
     
    public taskXMLAdapter(File targetFile) {
        file = targetFile;
        of = new ObjectFactory();
        
        task = of.createTaskType();
        
        constants = of.createConstantsSectionType();
        relations = of.createRelationSectionType();
        stateVariables = of.createStateVariablesSectionType();
        
        task.setConstants(constants);
        task.setRelations(relations);
        task.setStateVariables(stateVariables);
    }
    
    public TaskType getXMLRoot() {
        return task;
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
            JAXBElement<TaskType> rl = of.createTask(task);
            JAXBContext jc = JAXBContext.newInstance("taskXMLModel");
            Marshaller m = jc.createMarshaller();
            m.marshal(rl, ps);
        } catch (JAXBException jbe) {
            System.out.println(jbe.getMessage());
        }
        try {
            ps.close();
        } catch (IOException ex) {
            Logger.getLogger(taskXMLAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void unmarshall() throws TaskXMLAdapterException {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            System.out.println("Input file error.");
            Logger.getLogger(domainXMLAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        try {
            JAXBContext jc = JAXBContext.newInstance( "taskXMLModel" );
            
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<TaskType> rootEl = (JAXBElement<TaskType>) u.unmarshal(in);
            task = rootEl.getValue();
        } catch (JAXBException ex) {
            throw new TaskXMLAdapterException("Error parsing file.",null);
            //Logger.getLogger(domainXMLAdapter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        constants = task.getConstants();
        relations = task.getRelations();
        stateVariables = task.getStateVariables();
    }
        
    public void exportConstantSet(String className, Set<String> taskConstSet) {
        
        // make new constant list for task dependent constants of class className
        ConstantListType constList = of.createConstantListType();
        constList.setClassName(className);
        // insert all defined constants
        for (String taskConst: taskConstSet) {
            ConstantType newConst = of.createConstantType();
            newConst.setName(taskConst);
            
            constList.getConstant().add(newConst);
        }

        // insert constList into XML
        constants.getConstantList().add(constList);
    }

    private void exportRelationDefinition(String relationName, DefinitionTableModel tableModel) {
        // create new relation definition
        RelationDefType relationDef = of.createRelationDefType();
        relationDef.setRelationName(relationName);
        
        // create signature
        SignatureType signature = of.createSignatureType();
        for (int col=0; col < tableModel.getColumnCount(); ++col) {
            ArgumentType arg = of.createArgumentType();
            arg.setNumber(col);
            arg.setClazz(tableModel.getColumnName(col));
            signature.getArgument().add(col, arg);
        }
        
        // set signature
        relationDef.setSignature(signature);
        
        // create table
        TableType table = of.createTableType();
        
        // export values
        exportTableModel(tableModel,table);
        
        // export table
        relationDef.setTable(table);
        
        // add relationDef into list
        relations.getRelationDef().add(relationDef);
    }

    private void exportStateVariableDefinitions(String stateVarName, DefinitionTableModel tableModel) {
        StateVariableDefType stateVarDef = of.createStateVariableDefType();
        stateVarDef.setStateVariableName(stateVarName);
        
        // create signature
        SignatureType signature = of.createSignatureType();
        for (int col=0; col < tableModel.getColumnCount(); ++col) {
            ArgumentType arg = of.createArgumentType();
            arg.setNumber(col);
            arg.setClazz(tableModel.getColumnName(col));
            signature.getArgument().add(col, arg);
        }
        
        // insert signature
        stateVarDef.setSignature(signature);
        
        // create table
        TableType table = of.createTableType();
        
        // export values
        exportTableModel(tableModel,table);
        
        // export table
        stateVarDef.setTable(table);
        
        // insert into XML
        stateVariables.getStateVariableDef().add(stateVarDef);
    }

    public void importTaskConstantMap(HashMap<String, Set<String>> constantMap, dmiClassHierarchy classHierarchy) {
        assert(constantMap != null);
        assert(classHierarchy != null);
        
        for (ConstantListType constList: constants.getConstantList() ) {
            String className = constList.getClassName();
            
            if (classHierarchy.getClassByName(className) != null) {// test if class exists in domain
                Set<String> constantSet = new HashSet<String>();
                
                // read all defined constants
                for (ConstantType actConst: constList.getConstant()) {
                    String constName = actConst.getName();
                    
                    if (classHierarchy.getConstantByName(constName) == null) {// verify that the constant name is unique
                        constantSet.add(constName);
                    }
                }
                
                // insert retrieved data into constantMap
                constantMap.put(className, constantSet);
                
            } else {
                System.err.println("Class '" + className + "' referenced in task is not defined in domain. (taskXMLAdapter)");
            }
        }
    }

    /**
     * Inside this method are moved data from relation definitions (in task) which are saved in XML into definition tables.
     * Information which is displayed and can be manipulated by user is stored in DefinitionTableModel for each relation.
     * @param relationMap <relation name, relation definition> - initialized map
     * @param relationSet declared relations are accessible here (relation tables can be checked against the domain)
     */
    public void importRelationMap(HashMap<String, DefinitionTableModel> relationMap, dmiElementSet relationSet) {
        assert(relationMap != null);
        assert(relationSet != null);
        
        for (RelationDefType relation: relations.getRelationDef()) {
            // verify relation against declaration in domain
            String relName = relation.getRelationName();
            dmDelegate delegate = relationSet.getElementDelegateByName(relName);
            if ( delegate != null) {
                // relation with this name is defined in domain
                List<String> declaredSignature = delegate.getArgumentNames();
                SignatureType signature = relation.getSignature();
                
                // check argument classes
                boolean checkOk = true;
                for (ArgumentType arg: signature.getArgument()) {
                    if ( !(arg.getClazz().equals(declaredSignature.get(arg.getNumber()))) ) {
                        checkOk = false;
                        break;
                    }
                }
                
                if (checkOk) {
                    TableType definedTable = relation.getTable();
                    DefinitionTableModel tableModel = new DefinitionTableModel(declaredSignature);
                    
                    // copy data from XML structure to table model
                    importTableModel(definedTable,tableModel);
                    
                    // insert relation into relationMap
                    relationMap.put(relName, tableModel);
                    
                } else {
                    System.err.println("Relation '" + relName + "' defined in task has wrong signature. (taskXMLAdapter)");
                }
            } else {
                System.err.println("Relation '" + relName + "' defined in task was not found in the domain. (taskXMLAdapter)");
            }
        }
    }

    public void importStateVariableMap(HashMap<String, DefinitionTableModel> stateVariableMap, dmiElementSet stateVariableSet) {
        assert(stateVariableMap != null);
        assert(stateVariableSet != null);
        
        for (StateVariableDefType stateVar: stateVariables.getStateVariableDef()) {
            // verify state variable against declaration
            String stateVarName = stateVar.getStateVariableName();
            dmDelegate delegate = stateVariableSet.getElementDelegateByName(stateVarName);
            
            if (delegate != null) {
                List<String> declaredSignature = delegate.getArgumentNames();
                SignatureType signature = stateVar.getSignature();
                
                boolean checkOk = true;
                List<ArgumentType> definedSignature = signature.getArgument();
                for ( int i=0; i < declaredSignature.size(); ++i ) {
                    if ( !declaredSignature.get(i).equals(definedSignature.get(i).getClazz()) ) {
                        checkOk = false;
                        break;
                    }
                }
                
                if (checkOk) {
                    TableType definedTable = stateVar.getTable();
                    declaredSignature.add(INIT_STRING);
                    declaredSignature.add(GOAL_STRING);
                    DefinitionTableModel tableModel = new DefinitionTableModel(declaredSignature);
                    
                    // copy data from XML structure to table model
                    importTableModel(definedTable,tableModel);
                    
                    stateVariableMap.put(stateVarName, tableModel);
                    
                } else {
                    System.err.println("State variable '" + stateVarName + "' defined in task has wrong signature. (taskXMLAdapter)");
                }
                
            } else {
                System.err.println("State variable '" + stateVarName + "' defined in task was not found in the domain. (taskXMLAdapter)");
            }
        }
    }

    private void importTableModel(TableType definedTable, DefinitionTableModel tableModel) {
        assert(tableModel != null);
        assert(definedTable != null);
        List<RowType> rowList = definedTable.getRow();

        for (int row = 0; row < rowList.size(); ++row) {
            List<String> columnList = rowList.get(row).getColumn();
            tableModel.addRow(stringListToStringArray(columnList)); // alocate new row
        }
    }

    private void exportTableModel(DefinitionTableModel tableModel, TableType xmlTable) {
        assert (tableModel != null);
        assert (xmlTable != null);

        for (int row = 0; row < tableModel.getRowCount(); ++row) {
            RowType newRow = of.createRowType();
            // add columns
            for (int col = 0; col < tableModel.getColumnCount(); ++col) {
                newRow.getColumn().add((String) tableModel.getValueAt(row, col));
            }
            // add row into XML
            xmlTable.getRow().add(newRow);
        }
    }

    public void exportTaskProperties(dmTask planningTask) {
        TaskProperties properties = of.createTaskProperties();
        
        properties.setName(planningTask.toString());
        properties.setDomain(planningTask.getDomainName());
        
        PddlRequirementsListType reqList = of.createPddlRequirementsListType();
        
        for (int reqCode: planningTask.getRequirements()) {
            String reqString = domainXMLAdapter.reqCode2string(reqCode);
            if (!reqString.equals("ERROR")) {
                reqList.getRequirement().add(reqString);
            } else {
                System.out.println("domainXMLAdapter.getDomainProperties: Unrecognized requirement found !!");
            }
        }
        
        // Add into XML tree
        properties.setRequirements(reqList);
        task.setProperties(properties);
    }

    public void importTaskProperties(dmTask planningTask) throws TaskXMLAdapterException {
        TaskProperties properties = task.getProperties();
        
        String domainName = properties.getDomain();
        String taskName = properties.getName();
        
        planningTask.setName(taskName);
        
        // check if task dmain name is corresponding with current domain
        if (!domainName.equals(planningTask.getDomainName())) {
            throw new TaskXMLAdapterException("task is not defined for current domain",taskName);
        }
        
        // create set of task requirements
        Set<Integer> taskRequirements = new HashSet<Integer>();
        for (String req: properties.getRequirements().getRequirement()) {
            int reqCode = domainXMLAdapter.stringReq2int(req);
            
            if (reqCode >=0) {
                taskRequirements.add(reqCode);
            } else {
                System.out.println("domainXMLAdapter.setTaskProperties: Unrecognized requirement found !!");
            }
        }
        
        Set<Integer> domainRequirements = planningTask.getRequirements();
        
        if (!taskRequirements.equals(domainRequirements)) {
            throw new TaskXMLAdapterException("domain and task requirements mismatch",taskName);
        }
    }

    public void exportRelationMap(HashMap<String, DefinitionTableModel> relationMap) {
        for (String relName: relationMap.keySet()) {
            DefinitionTableModel relDefTable = relationMap.get(relName);
            assert(relDefTable != null);
            exportRelationDefinition(relName,relDefTable); 
        }
    }

    public void exportStateVariableMap(HashMap<String, DefinitionTableModel> stateVariableMap) {
        for (String stVarName: stateVariableMap.keySet()) {
            DefinitionTableModel stVarDefTable = stateVariableMap.get(stVarName);
            assert(stVarDefTable != null);
            exportStateVariableDefinitions(stVarName,stVarDefTable);
        }
    }

    private String[] stringListToStringArray(List<String> columnList) {
        String[] result = new String[columnList.size()];
        
        for (int i=0; i < result.length; ++i) {
            result[i] = columnList.get(i);
        }
        
        return result;
    }
}
