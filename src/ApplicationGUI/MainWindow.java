/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainWindow.java
 *
 * Created on Jul 18, 2011, 9:27:03 AM
 */
package ApplicationGUI;

import CustomizedClasses.DefinitionTableModel;
import CustomizedClasses.DynamicTableModel;
import CustomizedClasses.Entry;
import DataModel.dmClass;
import DataModel.dmClassSet;
import DataModel.dmConstant;
import DataModel.dmDelegate;
import DataModel.dmDomain;
import DataModel.dmExpressionListModel;
import DataModel.dmOperator;
import DataModel.dmOperatorModel;
import DataModel.dmPrevailingTransition;
import DataModel.dmRelation;
import DataModel.dmRelationDelegate;
import DataModel.dmRelationModel;
import DataModel.dmRelationTemplate;
import DataModel.dmSlot;
import DataModel.dmStateVariableDelegate;
import DataModel.dmStateVariableModel;
import DataModel.dmStateVariableTemplate;
import DataModel.dmTask;
import DataModel.dmTransition;
import DataModel.dmTransitionTemplate;
import DataModel.dmTreeModel;
import DataModel.dmiElementSet;
import DataModel.dmiTreeNode;
import DataModel.dmiExpression;
import DataModel.dmiExpressionListModel;
import DataModel.dmiOperator;
import DataModelException.DomainException;
import DataModelException.TaskException;
import DataModelException.TreeModelException;
import convertor.ConvertorException;
import convertor.xml2pddlConvertor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import xmladapter.DomainXMLAdapterException;
import xmladapter.TaskXMLAdapterException;

/**
 *
 * @author fairfax
 */
public class MainWindow extends javax.swing.JFrame {
    
    private final static String WINDOW_TITLE = "Vizzard";
    public final static String INPUT_STRING_REGEX = "\\w{1,50}";
    /*
     * tree model for class definition
     */
    dmTreeModel classTreeModel = new dmTreeModel(new dmClass("object"));
    /*
     * relation declarations
     * - implemented in customized table model
     */
    dmRelationModel relationTableModel = new dmRelationModel();
    /*
     * state variable declarations   
     */
    dmStateVariableModel stateVarTableModel = new dmStateVariableModel();
    /*
     * operator definitions
     */
    dmOperatorModel operatorListModel = new dmOperatorModel();
    /*
     * task currently displayed on tasksPanel
     */
    dmTask activeTask = null;
    /*
     * custom TreeCellRenderer
     */
    ClassTreeCellRenderer classTreeRenderer = new ClassTreeCellRenderer();
    /*
     * task selector model
     */
    DefaultComboBoxModel taskSelModel = new DefaultComboBoxModel();

    /** Creates new form MainWindow */
    public MainWindow() {
        /*
         * initialization of singleton class dmDomain
         */
        dmDomain domain = dmDomain.getInstance();
        domain.initDomain(classTreeModel, relationTableModel, stateVarTableModel, operatorListModel);

        /*
         * initialization for classTreeModel
         */
        classTreeModel.addTreeModelListener(new TreeModelListener() {
            
            @Override
            public void treeNodesChanged(TreeModelEvent tme) {
                System.out.println("treeNodesChanged");

                // when class is renamed its new name is updated in tables
                relationTable.repaint();
                stateVarTable.repaint();
            }
            
            @Override
            public void treeNodesInserted(TreeModelEvent tme) {
                dmiTreeNode inserted = (dmiTreeNode) tme.getChildren()[0];
                declarationClassTree.scrollPathToVisible(new TreePath(inserted.getPath()));
                taskClassTree.scrollPathToVisible(new TreePath(inserted.getPath()));
                
                System.out.println("treeNodesInserted");
            }
            
            @Override
            public void treeNodesRemoved(TreeModelEvent tme) {
                System.out.println("treeNodesRemoved");
            }
            
            @Override
            public void treeStructureChanged(TreeModelEvent tme) {
                System.out.println("treeStructureChanged");
            }
        });

        /*
         * initialization for relationListModel
         */
        relationTableModel.addTableModelListener(new TableModelListener() {
            
            @Override
            public void tableChanged(TableModelEvent tme) {
                
                if (tme.getType() == TableModelEvent.INSERT) {
                    System.out.println("INSERT in relation table");
                }
                
                if (tme.getType() == TableModelEvent.UPDATE) {
                    System.out.println("UPDATE in relation table");
                }
                
                if (tme.getType() == TableModelEvent.DELETE) {
                    System.out.println("DELETE in relation table");
                    // TODO - when relation is removed identification
                    // of other relations may have changed (worst case - remove first row => all relation id's changed)
                }
            }
        });

        /*
         * initialization for stateVarTableModel
         */
        stateVarTableModel.addTableModelListener(new TableModelListener() {
            
            @Override
            public void tableChanged(TableModelEvent tme) {
                if (tme.getType() == TableModelEvent.INSERT) {
                    System.out.println("INSERT in stateVarTable table");
                }
                
                if (tme.getType() == TableModelEvent.UPDATE) {
                    System.out.println("UPDATE in stateVarTable table");
                }
                
                if (tme.getType() == TableModelEvent.DELETE) {
                    System.out.println("DELETE in stateVarTable table");
                    // TODO - viz. relationModel table listener - basicly the same thing
                }
            }
        });
        
        initComponents();
        /*
         * Settings for declarationClassTree
         */
        declarationClassTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        declarationClassTree.setTransferHandler(new TransferHandler() {
            
            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY;
            }
            
            @Override
            public Transferable createTransferable(JComponent c) {
                JTree treeComp = (JTree) c;
                dmiTreeNode selectedNode = (dmiTreeNode) treeComp.getLastSelectedPathComponent();
                
                if (selectedNode instanceof dmClass) {
                    return selectedNode;
                }
                
                return null;
            }
            
            @Override
            public void exportDone(JComponent c, Transferable t, int action) {
                return;
            }
        });

        // transfer handler for relationTable
        TransferHandler relationTableTransferHandler = new TransferHandler() {
            
            @Override
            public boolean canImport(TransferHandler.TransferSupport info) {
                if (!info.isDataFlavorSupported(dmiTreeNode.classFlavor)) {
                    return false;
                }
                
                JTable.DropLocation dLoc = (JTable.DropLocation) info.getDropLocation();
                
                JTable table = (JTable) info.getComponent();
                dmRelationModel model = (dmRelationModel) table.getModel();
                
                if (!model.insideValidArgRange(dLoc.getRow(), dLoc.getColumn())) { // prevent user from leaving undefined arguments
                    return false;
                }
                
                
                return true;
            }
            
            @Override
            public boolean importData(TransferHandler.TransferSupport info) {
                
                if (!info.isDrop()) {
                    return false;
                }
                
                dmClass tClass;
                Transferable content = info.getTransferable();
                try {
                    tClass = (dmClass) content.getTransferData(dmiTreeNode.classFlavor);
                } catch (UnsupportedFlavorException ex) {
                    return false;
                } catch (IOException ex) {
                    return false;
                }
                
                JTable table = (JTable) info.getComponent();
                DynamicTableModel model = (DynamicTableModel) table.getModel();
                
                JTable.DropLocation dLoc = (JTable.DropLocation) info.getDropLocation();
                
                model.setValueAt(tClass, dLoc.getRow(), dLoc.getColumn());
                
                return true;
            }
            
            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }
        };

        /*
         * Settings for relationTable
         */
        relationTable.setDropMode(DropMode.ON);
        relationTable.setDragEnabled(true);
        relationTable.setTransferHandler(relationTableTransferHandler);


        // transfer handler for stateVarTable
        TransferHandler stateVarTableTransferHandler = new TransferHandler() {
            
            @Override
            public boolean canImport(TransferHandler.TransferSupport info) {
                if (!info.isDataFlavorSupported(dmiTreeNode.classFlavor)) {
                    return false;
                }
                
                JTable.DropLocation dLoc = (JTable.DropLocation) info.getDropLocation();
                
                JTable table = (JTable) info.getComponent();
                dmStateVariableModel model = (dmStateVariableModel) table.getModel();
                
                if (!model.insideValidArgRange(dLoc.getRow(), dLoc.getColumn())) { // prevent user from leaving undefined arguments
                    return false;
                }
                
                return true;
            }
            
            @Override
            public boolean importData(TransferHandler.TransferSupport info) {
                
                if (!info.isDrop()) {
                    return false;
                }
                
                dmClass tClass;
                Transferable content = info.getTransferable();
                try {
                    tClass = (dmClass) content.getTransferData(dmiTreeNode.classFlavor);
                } catch (UnsupportedFlavorException ex) {
                    return false;
                } catch (IOException ex) {
                    return false;
                }
                
                JTable table = (JTable) info.getComponent();
                dmStateVariableModel model = (dmStateVariableModel) table.getModel();
                
                JTable.DropLocation dLoc = (JTable.DropLocation) info.getDropLocation();
                
                int row = dLoc.getRow();
                int col = dLoc.getColumn();
                
                if (col == dmiElementSet.STATE_VAR_RANGE) { // extend state variable value range
                    model.extendDomain(tClass, row);                    
                } else {    // define argument
                    model.setValueAt(tClass, row, col);
                }
                
                return true;
            }
            
            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }
        };

        /*
         * Settings for stateVarTable
         */
        stateVarTable.setDropMode(DropMode.ON);
        stateVarTable.setDragEnabled(true);
        stateVarTable.setTransferHandler(stateVarTableTransferHandler);

        /*
         * settings for classTreeRenderer
         */
        ImageIcon classIcon = createImageIcon("images/classIcon.gif");
        ImageIcon constIcon = createImageIcon("images/constIcon.gif");
        ImageIcon rootIcon = createImageIcon("images/rootIcon.gif");
        ImageIcon taskDepConstIcon = createImageIcon("images/taskDepConstIcon.gif");
        classTreeRenderer.setClassIcon(classIcon);
        classTreeRenderer.setConstIcon(constIcon);
        classTreeRenderer.setRootIcon(rootIcon);
        classTreeRenderer.setTaskDepConstIcon(taskDepConstIcon);
        
        declarationClassTree.setCellRenderer(classTreeRenderer);
        taskClassTree.setCellRenderer(classTreeRenderer);

        /*
         * Settings for taskClassTree
         */
        taskClassTree.setTransferHandler(new TransferHandler() {
            
            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY;
            }
            
            @Override
            public Transferable createTransferable(JComponent c) {
                JTree treeComp = (JTree) c;
                dmiTreeNode selectedNode = (dmiTreeNode) treeComp.getLastSelectedPathComponent();
                
                if (selectedNode instanceof dmConstant) {
                    return selectedNode;
                }
                
                return null;
            }
            
            @Override
            public void exportDone(JComponent c, Transferable t, int action) {
                return;
            }
        });

        // transfer handler for task taskRelationTable
        TransferHandler defRelationTableTransferHandler = new TransferHandler() {
            
            @Override
            public boolean canImport(TransferHandler.TransferSupport info) {
                if (!info.isDataFlavorSupported(dmiTreeNode.constantFlavor)) {
                    return false;
                }
                
                return true;
            }
            
            @Override
            public boolean importData(TransferHandler.TransferSupport info) {
                
                if (!info.isDrop()) {
                    return false;
                }
                
                dmConstant constant;
                Transferable content = info.getTransferable();
                try {
                    constant = (dmConstant) content.getTransferData(dmiTreeNode.constantFlavor);
                } catch (UnsupportedFlavorException ex) {
                    return false;
                } catch (IOException ex) {
                    return false;
                }
                
                String value = "C:" + constant.getName();
                
                JTable table = (JTable) info.getComponent();
                DefinitionTableModel model = (DefinitionTableModel) table.getModel();
                
                JTable.DropLocation dLoc = (JTable.DropLocation) info.getDropLocation();
                
                model.setValueAt(value, dLoc.getRow(), dLoc.getColumn());
                
                return true;
            }
            
            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }
        };

        // transfer handler for task taskRelationTable
        TransferHandler defStateVarTableTransferHandler = new TransferHandler() {
            
            @Override
            public boolean canImport(TransferHandler.TransferSupport info) {
                if (!info.isDataFlavorSupported(dmiTreeNode.constantFlavor)) {
                    return false;
                }
                
                JTable table = (JTable) info.getComponent();
                DefinitionTableModel model = (DefinitionTableModel) table.getModel();
                
                JTable.DropLocation dLoc = (JTable.DropLocation) info.getDropLocation();
                int targetColumn = dLoc.getColumn();
                
                if (!(model.isInitColumn(targetColumn) || model.isGoalColumn(targetColumn))) { // accept drops only in the two last columns
                    return false;
                }
                
                return true;
            }
            
            @Override
            public boolean importData(TransferHandler.TransferSupport info) {
                
                if (!info.isDrop()) {
                    return false;
                }
                
                dmConstant constant;
                Transferable content = info.getTransferable();
                try {
                    constant = (dmConstant) content.getTransferData(dmiTreeNode.constantFlavor);
                } catch (UnsupportedFlavorException ex) {
                    return false;
                } catch (IOException ex) {
                    return false;
                }
                
                
                
                
                JTable table = (JTable) info.getComponent();
                DefinitionTableModel model = (DefinitionTableModel) table.getModel();
                
                JTable.DropLocation dLoc = (JTable.DropLocation) info.getDropLocation();
                int row = dLoc.getRow();
                int col = dLoc.getColumn();
                
                String value = DefinitionTableModel.DEFAULT_CELL_VALUE;
                // insert is different for init column and goal column
                if (model.isGoalColumn(col)) { // goal column
                    String cell = (String) model.getValueAt(row, col);
                    
                    if (DefinitionTableModel.prefixMatch(cell, DefinitionTableModel.WILDC_PREFIX)) {
                        // goal value not specified so far
                        value = "" + DefinitionTableModel.CONST_SET_PREFIX + DefinitionTableModel.DELIMITER + constant.getName();
                    }
                    
                    if (DefinitionTableModel.prefixMatch(cell, DefinitionTableModel.CONST_SET_PREFIX)) {
                        // goal value specified
                        value = cell + DefinitionTableModel.GOAL_SET_DELIMITER + constant.getName();
                    }
                } else { // init column
                    value = "C:" + constant.getName();
                }
                
                model.setValueAt(value, dLoc.getRow(), dLoc.getColumn());
                return true;
            }
            
            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }
        };


        // set renderer
        DefinitionTableCellRenderer renderer = new DefinitionTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);

        /*
         * Settings for taskRelationTable
         */
        taskRelationTable.setDropMode(DropMode.ON);
        taskRelationTable.setDragEnabled(true);
        taskRelationTable.setTransferHandler(defRelationTableTransferHandler);
        taskRelationTable.setDefaultRenderer(String.class, renderer);

        /*
         * Settings for taskStateVariableTable
         */
        taskStateVarTable.setDropMode(DropMode.ON);
        taskStateVarTable.setDragEnabled(true);
        taskStateVarTable.setTransferHandler(defStateVarTableTransferHandler);
        taskStateVarTable.setDefaultRenderer(String.class, renderer);

        /*
         * Settings for relationTable
         * 
         */
        relationTable.setDefaultEditor(String.class, new ElementNameEditor(relationTableModel));
        relationTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        /*
         * Settings for stateVarTable
         * 
         */
        stateVarTable.setDefaultEditor(String.class, new ElementNameEditor(stateVarTableModel));
        stateVarTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        /*
         * Settings for operatorList
         */
        operatorList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        relationTablePopup = new javax.swing.JPopupMenu();
        AddNewRelation = new javax.swing.JMenuItem();
        DelRelation = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        AddRelationArgumentColumn = new javax.swing.JMenuItem();
        DelRelationArgumentColumn = new javax.swing.JMenuItem();
        stateVarTablePopup = new javax.swing.JPopupMenu();
        AddNewStateVar = new javax.swing.JMenuItem();
        ClearStateVarRange = new javax.swing.JMenuItem();
        DelStateVar = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        AddStateVarArgumentColumn = new javax.swing.JMenuItem();
        DelStateVarArgumentColumn = new javax.swing.JMenuItem();
        classTreePopup = new javax.swing.JPopupMenu();
        AddNewClass = new javax.swing.JMenuItem();
        AddNewConstant = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        DelItem = new javax.swing.JMenuItem();
        operatorListPopup = new javax.swing.JPopupMenu();
        createOperator = new javax.swing.JMenuItem();
        copyOperator = new javax.swing.JMenuItem();
        deleteOperator = new javax.swing.JMenuItem();
        operatorExpressionsPopup = new javax.swing.JPopupMenu();
        setValue = new javax.swing.JMenu();
        Variable = new javax.swing.JMenuItem();
        Constant = new javax.swing.JMenuItem();
        insertExpression = new javax.swing.JMenu();
        addRelations = new javax.swing.JMenuItem();
        addTransitions = new javax.swing.JMenuItem();
        deleteExpression = new javax.swing.JMenuItem();
        taskClassTreePopup = new javax.swing.JPopupMenu();
        addConstant = new javax.swing.JMenuItem();
        delConstant = new javax.swing.JMenuItem();
        mainTabbedPane = new javax.swing.JTabbedPane();
        declarationsPanel = new javax.swing.JPanel();
        declarationsToolbar = new javax.swing.JToolBar();
        DeclarationsHSplit = new javax.swing.JSplitPane();
        classTreeScrollPane = new javax.swing.JScrollPane();
        declarationClassTree = new javax.swing.JTree();
        jSplitPane2 = new javax.swing.JSplitPane();
        RelationDeclarationPanel = new javax.swing.JPanel();
        relationTableScrollPane = new javax.swing.JScrollPane();
        relationTable = new javax.swing.JTable();
        StateVarDeclarationPanel = new javax.swing.JPanel();
        stateVarTableScrollPane = new javax.swing.JScrollPane();
        stateVarTable = new javax.swing.JTable();
        operatorsPanel = new javax.swing.JPanel();
        jSplitPane4 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        operatorList = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        clearSelectionButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        operatorExpressionView = new ApplicationGUI.ExpressionListView();
        tasksPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        taskSelector = new javax.swing.JComboBox();
        createTask = new javax.swing.JButton();
        delTask = new javax.swing.JButton();
        saveTaskButton = new javax.swing.JButton();
        loadTaskButton = new javax.swing.JButton();
        exportPDDLTaskButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        jSplitPane3 = new javax.swing.JSplitPane();
        taskClassTreeScrollPane = new javax.swing.JScrollPane();
        taskClassTree = new javax.swing.JTree();
        taskDefinitionTabbedPane = new javax.swing.JTabbedPane();
        taskRelationTab = new javax.swing.JPanel();
        jToolBar3 = new javax.swing.JToolBar();
        rowCountSpinner = new javax.swing.JSpinner();
        addRows = new javax.swing.JButton();
        delRows = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        taskRelationList = new javax.swing.JList();
        RelationDefinitionPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taskRelationTable = new javax.swing.JTable();
        taskStateVarTab = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        taskStateVarList = new javax.swing.JList();
        jToolBar4 = new javax.swing.JToolBar();
        resetCellToggle = new javax.swing.JToggleButton();
        resetColumn = new javax.swing.JButton();
        StateVarDefinitionsPanel = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        taskStateVarTable = new javax.swing.JTable();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exportPDDLDomain = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        domainProperties = new javax.swing.JMenuItem();

        relationTablePopup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                relationTablePopupPopupMenuWillBecomeVisible(evt);
            }
        });

        AddNewRelation.setText("Add new relation");
        AddNewRelation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddNewRelationActionPerformed(evt);
            }
        });
        relationTablePopup.add(AddNewRelation);

        DelRelation.setText("Delete relation");
        DelRelation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DelRelationActionPerformed(evt);
            }
        });
        relationTablePopup.add(DelRelation);
        relationTablePopup.add(jSeparator1);

        AddRelationArgumentColumn.setText("Add argument column");
        AddRelationArgumentColumn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddRelationArgumentColumnActionPerformed(evt);
            }
        });
        relationTablePopup.add(AddRelationArgumentColumn);

        DelRelationArgumentColumn.setText("Del argument column");
        DelRelationArgumentColumn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DelRelationArgumentColumnActionPerformed(evt);
            }
        });
        relationTablePopup.add(DelRelationArgumentColumn);

        stateVarTablePopup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                stateVarTablePopupPopupMenuWillBecomeVisible(evt);
            }
        });

        AddNewStateVar.setText("Add state variable");
        AddNewStateVar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddNewStateVarActionPerformed(evt);
            }
        });
        stateVarTablePopup.add(AddNewStateVar);

        ClearStateVarRange.setText("Clear state variable range");
        ClearStateVarRange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearStateVarRangeActionPerformed(evt);
            }
        });
        stateVarTablePopup.add(ClearStateVarRange);

        DelStateVar.setText("Delete state variable");
        DelStateVar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DelStateVarActionPerformed(evt);
            }
        });
        stateVarTablePopup.add(DelStateVar);
        stateVarTablePopup.add(jSeparator2);

        AddStateVarArgumentColumn.setText("Add argument column");
        AddStateVarArgumentColumn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddStateVarArgumentColumnActionPerformed(evt);
            }
        });
        stateVarTablePopup.add(AddStateVarArgumentColumn);

        DelStateVarArgumentColumn.setText("Del argument column");
        DelStateVarArgumentColumn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DelStateVarArgumentColumnActionPerformed(evt);
            }
        });
        stateVarTablePopup.add(DelStateVarArgumentColumn);

        classTreePopup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                classTreePopupPopupMenuWillBecomeVisible(evt);
            }
        });

        AddNewClass.setText("Add class");
        AddNewClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddNewClassActionPerformed(evt);
            }
        });
        classTreePopup.add(AddNewClass);

        AddNewConstant.setText("Add constant");
        AddNewConstant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddNewConstantActionPerformed(evt);
            }
        });
        classTreePopup.add(AddNewConstant);
        classTreePopup.add(jSeparator3);

        DelItem.setText("Delete node");
        DelItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DelItemActionPerformed(evt);
            }
        });
        classTreePopup.add(DelItem);

        operatorListPopup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                operatorListPopupPopupMenuWillBecomeVisible(evt);
            }
        });

        createOperator.setText("Create operator");
        createOperator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createOperatorActionPerformed(evt);
            }
        });
        operatorListPopup.add(createOperator);

        copyOperator.setText("Copy operator");
        copyOperator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyOperatorActionPerformed(evt);
            }
        });
        operatorListPopup.add(copyOperator);

        deleteOperator.setText("Delete operator");
        deleteOperator.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteOperatorActionPerformed(evt);
            }
        });
        operatorListPopup.add(deleteOperator);

        operatorExpressionsPopup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                operatorExpressionsPopupPopupMenuWillBecomeVisible(evt);
            }
        });

        setValue.setText("Set value");

        Variable.setText("Variable");
        Variable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VariableActionPerformed(evt);
            }
        });
        setValue.add(Variable);

        Constant.setText("Constant");
        Constant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConstantActionPerformed(evt);
            }
        });
        setValue.add(Constant);

        operatorExpressionsPopup.add(setValue);

        insertExpression.setText("Insert expression");

        addRelations.setText("Add relations");
        addRelations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRelationsActionPerformed(evt);
            }
        });
        insertExpression.add(addRelations);

        addTransitions.setText("Add transitions");
        addTransitions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTransitionsActionPerformed(evt);
            }
        });
        insertExpression.add(addTransitions);

        operatorExpressionsPopup.add(insertExpression);

        deleteExpression.setText("Delete expression");
        deleteExpression.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteExpressionActionPerformed(evt);
            }
        });
        operatorExpressionsPopup.add(deleteExpression);

        taskClassTreePopup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                taskClassTreePopupPopupMenuWillBecomeVisible(evt);
            }
        });

        addConstant.setText("Add constant");
        addConstant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addConstantActionPerformed(evt);
            }
        });
        taskClassTreePopup.add(addConstant);

        delConstant.setText("Delete constant");
        delConstant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delConstantActionPerformed(evt);
            }
        });
        taskClassTreePopup.add(delConstant);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(WINDOW_TITLE);

        mainTabbedPane.setName("Declarations"); // NOI18N

        declarationsPanel.setName("Declarations"); // NOI18N
        declarationsPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                declarationsPanelComponentShown(evt);
            }
        });

        declarationsToolbar.setRollover(true);

        DeclarationsHSplit.setDividerLocation(200);
        DeclarationsHSplit.setResizeWeight(0.3);

        classTreeScrollPane.setComponentPopupMenu(classTreePopup);

        declarationClassTree.setBorder(javax.swing.BorderFactory.createTitledBorder("Class tree"));
        declarationClassTree.setModel(classTreeModel);
        declarationClassTree.setDragEnabled(true);
        declarationClassTree.setInheritsPopupMenu(true);
        declarationClassTree.setToggleClickCount(0);
        declarationClassTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                declarationClassTreeMouseClicked(evt);
            }
        });
        declarationClassTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                declarationClassTreeKeyPressed(evt);
            }
        });
        classTreeScrollPane.setViewportView(declarationClassTree);

        DeclarationsHSplit.setLeftComponent(classTreeScrollPane);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);

        RelationDeclarationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Relation declaration table"));

        relationTableScrollPane.setComponentPopupMenu(relationTablePopup);

        relationTable.setModel(relationTableModel);
        relationTable.setInheritsPopupMenu(true);
        relationTable.setName("Relation table"); // NOI18N
        relationTableScrollPane.setViewportView(relationTable);

        javax.swing.GroupLayout RelationDeclarationPanelLayout = new javax.swing.GroupLayout(RelationDeclarationPanel);
        RelationDeclarationPanel.setLayout(RelationDeclarationPanelLayout);
        RelationDeclarationPanelLayout.setHorizontalGroup(
            RelationDeclarationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(relationTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
        );
        RelationDeclarationPanelLayout.setVerticalGroup(
            RelationDeclarationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(relationTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
        );

        jSplitPane2.setTopComponent(RelationDeclarationPanel);

        StateVarDeclarationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("State variable declaration table"));

        stateVarTableScrollPane.setComponentPopupMenu(stateVarTablePopup);

        stateVarTable.setModel(stateVarTableModel);
        stateVarTable.setInheritsPopupMenu(true);
        stateVarTableScrollPane.setViewportView(stateVarTable);

        javax.swing.GroupLayout StateVarDeclarationPanelLayout = new javax.swing.GroupLayout(StateVarDeclarationPanel);
        StateVarDeclarationPanel.setLayout(StateVarDeclarationPanelLayout);
        StateVarDeclarationPanelLayout.setHorizontalGroup(
            StateVarDeclarationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(stateVarTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
        );
        StateVarDeclarationPanelLayout.setVerticalGroup(
            StateVarDeclarationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(stateVarTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
        );

        jSplitPane2.setRightComponent(StateVarDeclarationPanel);

        DeclarationsHSplit.setRightComponent(jSplitPane2);

        javax.swing.GroupLayout declarationsPanelLayout = new javax.swing.GroupLayout(declarationsPanel);
        declarationsPanel.setLayout(declarationsPanelLayout);
        declarationsPanelLayout.setHorizontalGroup(
            declarationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(declarationsToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
            .addComponent(DeclarationsHSplit, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
        );
        declarationsPanelLayout.setVerticalGroup(
            declarationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(declarationsPanelLayout.createSequentialGroup()
                .addComponent(declarationsToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(DeclarationsHSplit, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE))
        );

        mainTabbedPane.addTab("Declarations", declarationsPanel);

        operatorsPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                operatorsPanelComponentShown(evt);
            }
        });

        jSplitPane4.setDividerLocation(202);

        jScrollPane3.setComponentPopupMenu(operatorListPopup);

        operatorList.setBorder(javax.swing.BorderFactory.createTitledBorder("Operator list"));
        operatorList.setModel(operatorListModel);
        operatorList.setCellRenderer(new OperatorListCellRenderer());
        operatorList.setInheritsPopupMenu(true);
        operatorList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                operatorListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(operatorList);

        jSplitPane4.setLeftComponent(jScrollPane3);

        jToolBar2.setRollover(true);

        clearSelectionButton.setText("Clear selection");
        clearSelectionButton.setFocusable(false);
        clearSelectionButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        clearSelectionButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        clearSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSelectionButtonActionPerformed(evt);
            }
        });
        jToolBar2.add(clearSelectionButton);

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Expression table"));
        jScrollPane4.setComponentPopupMenu(operatorExpressionsPopup);

        operatorExpressionView.setBackground(new java.awt.Color(160, 160, 160));
        operatorExpressionView.setColumnSelectionAllowed(false);
        operatorExpressionView.setInheritsPopupMenu(true);
        operatorExpressionView.setRowMargin(6);
        operatorExpressionView.setShowHorizontalLines(false);
        operatorExpressionView.setTableHeader(null);
        jScrollPane4.setViewportView(operatorExpressionView);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 660, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE))
        );

        jSplitPane4.setRightComponent(jPanel2);

        javax.swing.GroupLayout operatorsPanelLayout = new javax.swing.GroupLayout(operatorsPanel);
        operatorsPanel.setLayout(operatorsPanelLayout);
        operatorsPanelLayout.setHorizontalGroup(
            operatorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
        );
        operatorsPanelLayout.setVerticalGroup(
            operatorsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
        );

        mainTabbedPane.addTab("Operators", operatorsPanel);

        tasksPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                tasksPanelComponentShown(evt);
            }
        });

        jToolBar1.setRollover(true);

        jLabel1.setText("Task management toolbar");
        jToolBar1.add(jLabel1);
        jToolBar1.add(jSeparator5);

        taskSelector.setModel(taskSelModel);
        taskSelector.setMaximumSize(new java.awt.Dimension(200, 24));
        taskSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                taskSelectorItemStateChanged(evt);
            }
        });
        jToolBar1.add(taskSelector);

        createTask.setText("Create");
        createTask.setFocusable(false);
        createTask.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        createTask.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        createTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createTaskActionPerformed(evt);
            }
        });
        jToolBar1.add(createTask);

        delTask.setText("Delete");
        delTask.setFocusable(false);
        delTask.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        delTask.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        delTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delTaskActionPerformed(evt);
            }
        });
        jToolBar1.add(delTask);

        saveTaskButton.setText("Save");
        saveTaskButton.setFocusable(false);
        saveTaskButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveTaskButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        saveTaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTaskButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(saveTaskButton);

        loadTaskButton.setText("Load");
        loadTaskButton.setFocusable(false);
        loadTaskButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        loadTaskButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        loadTaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadTaskButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(loadTaskButton);

        exportPDDLTaskButton.setText("Export");
        exportPDDLTaskButton.setFocusable(false);
        exportPDDLTaskButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportPDDLTaskButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportPDDLTaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPDDLTaskButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(exportPDDLTaskButton);
        jToolBar1.add(jSeparator4);

        jSplitPane3.setDividerLocation(200);

        taskClassTreeScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Class tree"));
        taskClassTreeScrollPane.setComponentPopupMenu(taskClassTreePopup);

        taskClassTree.setModel(classTreeModel);
        taskClassTree.setDragEnabled(true);
        taskClassTree.setToggleClickCount(0);
        taskClassTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                taskClassTreeMouseClicked(evt);
            }
        });
        taskClassTree.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                taskClassTreeKeyPressed(evt);
            }
        });
        taskClassTreeScrollPane.setViewportView(taskClassTree);

        jSplitPane3.setLeftComponent(taskClassTreeScrollPane);

        taskDefinitionTabbedPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Definition panel"));
        taskDefinitionTabbedPane.setEnabled(false);

        jToolBar3.setRollover(true);
        jToolBar3.setEnabled(taskDefinitionTabbedPane.isEnabled());

        rowCountSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, 100, 1));
        rowCountSpinner.setMaximumSize(new java.awt.Dimension(50, 20));
        rowCountSpinner.setPreferredSize(new java.awt.Dimension(15, 20));
        jToolBar3.add(rowCountSpinner);

        addRows.setText("Add rows");
        addRows.setFocusable(false);
        addRows.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addRows.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        addRows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRowsActionPerformed(evt);
            }
        });
        jToolBar3.add(addRows);

        delRows.setText("Del rows");
        delRows.setFocusable(false);
        delRows.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        delRows.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        delRows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delRowsActionPerformed(evt);
            }
        });
        jToolBar3.add(delRows);

        taskRelationList.setBorder(javax.swing.BorderFactory.createTitledBorder("Relation declaration list"));
        taskRelationList.setModel(relationTableModel);
        taskRelationList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        taskRelationList.setEnabled(taskDefinitionTabbedPane.isEnabled());
        taskRelationList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                taskRelationListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(taskRelationList);

        RelationDefinitionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Relation definition"));

        taskRelationTable.setModel(new DefaultTableModel());
        taskRelationTable.setEnabled(taskDefinitionTabbedPane.isEnabled());
        taskRelationTable.setInheritsPopupMenu(true);
        jScrollPane2.setViewportView(taskRelationTable);

        javax.swing.GroupLayout RelationDefinitionPanelLayout = new javax.swing.GroupLayout(RelationDefinitionPanel);
        RelationDefinitionPanel.setLayout(RelationDefinitionPanelLayout);
        RelationDefinitionPanelLayout.setHorizontalGroup(
            RelationDefinitionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
        );
        RelationDefinitionPanelLayout.setVerticalGroup(
            RelationDefinitionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout taskRelationTabLayout = new javax.swing.GroupLayout(taskRelationTab);
        taskRelationTab.setLayout(taskRelationTabLayout);
        taskRelationTabLayout.setHorizontalGroup(
            taskRelationTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskRelationTabLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(taskRelationTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jToolBar3, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                    .addComponent(RelationDefinitionPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        taskRelationTabLayout.setVerticalGroup(
            taskRelationTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskRelationTabLayout.createSequentialGroup()
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(RelationDefinitionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
        );

        taskDefinitionTabbedPane.addTab("Relations", taskRelationTab);

        taskStateVarList.setBorder(javax.swing.BorderFactory.createTitledBorder("State variable declaration list"));
        taskStateVarList.setModel(stateVarTableModel);
        taskStateVarList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        taskStateVarList.setEnabled(taskDefinitionTabbedPane.isEnabled());
        taskStateVarList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                taskStateVarListValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(taskStateVarList);

        jToolBar4.setRollover(true);
        jToolBar4.setEnabled(taskDefinitionTabbedPane.isEnabled());

        resetCellToggle.setText("Reset cell");
        resetCellToggle.setFocusable(false);
        resetCellToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resetCellToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar4.add(resetCellToggle);

        resetColumn.setText("Reset column");
        resetColumn.setFocusable(false);
        resetColumn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resetColumn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        resetColumn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetColumnActionPerformed(evt);
            }
        });
        jToolBar4.add(resetColumn);

        StateVarDefinitionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("State variable definitions"));

        taskStateVarTable.setModel(new DefaultTableModel());
        taskStateVarTable.setEnabled(taskDefinitionTabbedPane.isEnabled());
        taskStateVarTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                taskStateVarTableMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(taskStateVarTable);

        javax.swing.GroupLayout StateVarDefinitionsPanelLayout = new javax.swing.GroupLayout(StateVarDefinitionsPanel);
        StateVarDefinitionsPanel.setLayout(StateVarDefinitionsPanelLayout);
        StateVarDefinitionsPanelLayout.setHorizontalGroup(
            StateVarDefinitionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
        );
        StateVarDefinitionsPanelLayout.setVerticalGroup(
            StateVarDefinitionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout taskStateVarTabLayout = new javax.swing.GroupLayout(taskStateVarTab);
        taskStateVarTab.setLayout(taskStateVarTabLayout);
        taskStateVarTabLayout.setHorizontalGroup(
            taskStateVarTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, taskStateVarTabLayout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(taskStateVarTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(StateVarDefinitionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToolBar4, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)))
        );
        taskStateVarTabLayout.setVerticalGroup(
            taskStateVarTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(taskStateVarTabLayout.createSequentialGroup()
                .addComponent(jToolBar4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StateVarDefinitionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 515, Short.MAX_VALUE)
        );

        taskDefinitionTabbedPane.addTab("State variables", taskStateVarTab);

        jSplitPane3.setRightComponent(taskDefinitionTabbedPane);
        taskDefinitionTabbedPane.getAccessibleContext().setAccessibleName("Relations");

        javax.swing.GroupLayout tasksPanelLayout = new javax.swing.GroupLayout(tasksPanel);
        tasksPanel.setLayout(tasksPanelLayout);
        tasksPanelLayout.setHorizontalGroup(
            tasksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
            .addComponent(jSplitPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 868, Short.MAX_VALUE)
        );
        tasksPanelLayout.setVerticalGroup(
            tasksPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tasksPanelLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE))
        );

        mainTabbedPane.addTab("Tasks", tasksPanel);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        newMenuItem.setText("New");
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newMenuItem);

        openMenuItem.setMnemonic('o');
        openMenuItem.setText("Open");
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        saveMenuItem.setMnemonic('s');
        saveMenuItem.setText("Save");
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As ...");
        saveAsMenuItem.setDisplayedMnemonicIndex(5);
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);

        exportPDDLDomain.setText("Export PDDL");
        exportPDDLDomain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPDDLDomainActionPerformed(evt);
            }
        });
        fileMenu.add(exportPDDLDomain);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");

        domainProperties.setMnemonic('d');
        domainProperties.setText("Domain properties");
        domainProperties.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                domainPropertiesActionPerformed(evt);
            }
        });
        editMenu.add(domainProperties);

        menuBar.add(editMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        int answer = JOptionPane.showConfirmDialog(this,
                "Do you want to save current domain and tasks ?",
                "Exit program",
                JOptionPane.YES_NO_OPTION);
        
        if (answer == JOptionPane.YES_OPTION) {
            saveAll();
        }
        
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    private void AddNewRelationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddNewRelationActionPerformed
        relationTableModel.addEntry();
    }//GEN-LAST:event_AddNewRelationActionPerformed
    
    private void AddRelationArgumentColumnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddRelationArgumentColumnActionPerformed
        relationTableModel.addArgumentColumn();
    }//GEN-LAST:event_AddRelationArgumentColumnActionPerformed
    
    private void AddNewStateVarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddNewStateVarActionPerformed
        stateVarTableModel.addEntry();
    }//GEN-LAST:event_AddNewStateVarActionPerformed
    
    private void AddStateVarArgumentColumnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddStateVarArgumentColumnActionPerformed
        stateVarTableModel.addArgumentColumn();
    }//GEN-LAST:event_AddStateVarArgumentColumnActionPerformed
    
    private void ClearStateVarRangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearStateVarRangeActionPerformed
        int stateVarIndex = stateVarTable.getSelectedRow();
        stateVarTableModel.clearDomain(stateVarIndex);
    }//GEN-LAST:event_ClearStateVarRangeActionPerformed
    
    private void relationTablePopupPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_relationTablePopupPopupMenuWillBecomeVisible
        // determine which options popup menu will will be available
        boolean delRelation = (relationTable.getSelectedRowCount() > 0);

        // set available options
        DelRelation.setVisible(delRelation);
        int argColCnt = relationTableModel.getArgColumnCount();
        System.out.println(argColCnt);
        DelRelationArgumentColumn.setVisible(argColCnt > 0);
    }//GEN-LAST:event_relationTablePopupPopupMenuWillBecomeVisible
    
    private void stateVarTablePopupPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_stateVarTablePopupPopupMenuWillBecomeVisible
        // determine which options in popup menu will be available
        boolean delStateVar = (stateVarTable.getSelectedRowCount() > 0);

        // set available options
        ClearStateVarRange.setVisible(delStateVar);
        DelStateVar.setVisible(delStateVar);
        int argColCnt = stateVarTableModel.getArgColumnCount();
        System.out.println(argColCnt);
        DelStateVarArgumentColumn.setVisible(argColCnt > 0);
    }//GEN-LAST:event_stateVarTablePopupPopupMenuWillBecomeVisible
    
    private void DelRelationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DelRelationActionPerformed
        relationTableModel.delEntry(relationTable.getSelectedRow());
    }//GEN-LAST:event_DelRelationActionPerformed
    
    private void DelStateVarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DelStateVarActionPerformed
        stateVarTableModel.delEntry(stateVarTable.getSelectedRow());
    }//GEN-LAST:event_DelStateVarActionPerformed
    
    private void classTreePopupPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_classTreePopupPopupMenuWillBecomeVisible
        // if no node is selected select root node by default
        if (declarationClassTree.getSelectionCount() <= 0) {
            // construct path to root node (one node only)
            Object[] rootPath = new Object[1];
            rootPath[0] = classTreeModel.getRoot();
            declarationClassTree.setSelectionPath(new TreePath(rootPath));
        }

        // check option availability based on node selection
        dmiTreeNode node = (dmiTreeNode) declarationClassTree.getLastSelectedPathComponent();
        boolean addClass = node instanceof dmClass;
        boolean addConst = addClass;
        boolean delItem = !node.isRoot();
        
        AddNewClass.setVisible(addClass);
        AddNewConstant.setVisible(addConst);
        DelItem.setVisible(delItem);
    }//GEN-LAST:event_classTreePopupPopupMenuWillBecomeVisible
    
    private void AddNewClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddNewClassActionPerformed
        dmiTreeNode selectedNode = (dmiTreeNode) declarationClassTree.getLastSelectedPathComponent();
        if (selectedNode == null) {     // empty selection
            return;
        }        
        
        String newName = JOptionPane.showInputDialog(this,
                "Enter class name:",
                "",
                JOptionPane.QUESTION_MESSAGE);
        
        if (newName == null) {  // user pressed Cancel
            return;
        }
        
        if (!newName.matches(INPUT_STRING_REGEX)) {
            JOptionPane.showMessageDialog(this, "Invalid class name.");
            return;
        }
        
        try {
            classTreeModel.insertNewClass(newName, (dmClass) selectedNode);
        } catch (TreeModelException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_AddNewClassActionPerformed
    
    private void AddNewConstantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddNewConstantActionPerformed
        dmiTreeNode selectedNode = (dmiTreeNode) declarationClassTree.getLastSelectedPathComponent();
        
        if (selectedNode == null) {     // empty selection
            return;
        }
        
        String newName = JOptionPane.showInputDialog(this,
                "Enter constant name:",
                "",
                JOptionPane.QUESTION_MESSAGE);
        
        if (newName == null) {  // user pressed Cancel
            return;
        }
        
        if (!newName.matches(INPUT_STRING_REGEX)) {
            JOptionPane.showMessageDialog(this, "Invalid constant name.");
            return;
        }
        
        try {
            classTreeModel.insertNewConstant(newName, (dmClass) selectedNode, false);
        } catch (TreeModelException ex) {
            JOptionPane.showMessageDialog(declarationClassTree, ex.getMessage());
        }
    }//GEN-LAST:event_AddNewConstantActionPerformed
    
    private void DelItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DelItemActionPerformed
        deleteClassTreeNode((dmiTreeNode) declarationClassTree.getLastSelectedPathComponent());
    }//GEN-LAST:event_DelItemActionPerformed
    
    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        File targetFile;
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "XML domain file", "xml");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        
        int result = chooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            targetFile = chooser.getSelectedFile();
            try {
                dmDomain.writeDomain(targetFile);
            } catch (DomainException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("File saved.");
        }
    }//GEN-LAST:event_saveAsMenuItemActionPerformed
    
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        
        if (!operatorListModel.isEmpty()) { // domain is worth saving if there are some operators
            int answer = JOptionPane.showConfirmDialog(this,
                    "Do you want to save current domain and tasks ?",
                    "Load domain",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            
            switch (answer) {
                case JOptionPane.YES_OPTION:
                    saveAll();
                    break;
                case JOptionPane.NO_OPTION: // do nothing
                    break;
                case JOptionPane.CANCEL_OPTION:
                    return;
            }
        }
        
        clearAll();     // clear all data structures
        
        File targetFile;
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "XML domain file", "xml");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        
        int result = chooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            targetFile = chooser.getSelectedFile();
            try {
                dmDomain.loadDomain(targetFile);
            } catch (DomainXMLAdapterException ex) {
                JOptionPane.showMessageDialog(this, "Error when loading domain file. (Is this really a DOMAIN file?)");
            }
            System.out.println("File loaded.");
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    /*
     * when state variable is selected in stateVarComboBox list of possible transitions is generated and displayed
     * in transitionList
     */
private void createOperatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createOperatorActionPerformed
    
    String operatorName = JOptionPane.showInputDialog(this,
            "Enter operator name",
            "New operator",
            JOptionPane.QUESTION_MESSAGE);
    
    if (operatorName == null) { // user clicked Cancel
        return;
    }
    
    if (!operatorName.matches(INPUT_STRING_REGEX)) {
        JOptionPane.showMessageDialog(this, "Invalid operator name.");
        return;
    }
    
    if (!(operatorListModel.isUniqueName(operatorName))) {
        JOptionPane.showMessageDialog(this, "Operator name must be unique.");
        return;
    }
    
    operatorListModel.addElement(new dmOperator(operatorName, new dmExpressionListModel()));
    
}//GEN-LAST:event_createOperatorActionPerformed
    
private void operatorListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_operatorListValueChanged
    // adjusting value change is from quick change
    // (for example user holding mouse and moving up and down) - we do not want to process this
    if (evt.getValueIsAdjusting()) {
        return;
    }
    
    dmiOperator oper = getActiveOperator();
    
    if (oper == null) // when operator is deleted
    {
        return;
    }
    
    operatorExpressionView.setListModel(oper.getExpressionList());
    operatorExpressionView.setEnabled(true);
}//GEN-LAST:event_operatorListValueChanged

    /**
     * this method is inserting relation expressions into operator expression list
     * similar to addTransitionsActionPerformed
     * @param evt 
     */
private void addRelationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRelationsActionPerformed
    List<dmRelationTemplate> selection = SelectRelationsDialog.showDialog(this, relationTableModel.getAvailableRelations());
    
    dmiOperator oper = getActiveOperator();
    
    assert (oper != null); // if no operator is selected option should not be available
    dmiExpressionListModel operExprList = oper.getExpressionList();
    
    assert (operExprList != null);
    for (dmRelationTemplate expr : selection) {       // add selected expressions into operator
        dmRelationDelegate deleg = (dmRelationDelegate) relationTableModel.getElementDelegateByName(expr.getName());
        dmiExpression relation = new dmRelation((dmRelationDelegate) deleg, operExprList.getSlotContentMap()); // create new instance of dmRelation
        operExprList.insertExpression(relation, true); // true - initialize slots in inserted expression
    }
}//GEN-LAST:event_addRelationsActionPerformed
    
private void operatorExpressionsPopupPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_operatorExpressionsPopupPopupMenuWillBecomeVisible
    boolean operatorSelected = (getActiveOperator() != null);
    boolean expressionSelected = (operatorExpressionView.getSelectedRowCount() != 0);
    insertExpression.setEnabled(operatorSelected);
    deleteExpression.setVisible(expressionSelected);
    
    setValue.setVisible(operatorExpressionView.getSelectedSlotCount() > 0);
}//GEN-LAST:event_operatorExpressionsPopupPopupMenuWillBecomeVisible

    /**
     * this method is inserting transition expressions into operator expression list
     * similar to addRelationsActionPerformed
     * @param evt 
     */
private void addTransitionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTransitionsActionPerformed
    List<dmStateVariableTemplate> availableStVars = stateVarTableModel.getAvailableStateVariables();
    List<Entry<String, dmTransitionTemplate>> selection = SelectTransitionsDialog.showDialog(this, availableStVars);
    
    dmiOperator oper = getActiveOperator();
    
    assert (oper != null); // if no operator is selected option should not be available
    dmiExpressionListModel operExprList = oper.getExpressionList();
    
    assert (operExprList != null);
    for (Entry<String, dmTransitionTemplate> entry : selection) {       // add selected expressions into operator
        
        String stateVarString = (String) entry.getKey(); // stateVar(arg1,...,argn)
        String[] splitedString = stateVarString.split("\\("); // split by "("
        String stateVarName = splitedString[0]; // stateVar
        
        dmStateVariableDelegate deleg = (dmStateVariableDelegate) stateVarTableModel.getElementDelegateByName(stateVarName);
        
        dmTransitionTemplate templ = (dmTransitionTemplate) entry.getValue();
        dmiExpression expr;
        Map<Integer, Object> dataMap = operExprList.getSlotContentMap();
        if (templ.isPrevailing()) {
            dmClass value = classTreeModel.getClassByName(templ.getValue());
            expr = new dmPrevailingTransition(deleg, dataMap, value);
        } else {
            dmClass origin = classTreeModel.getClassByName(templ.getOrigin());
            dmClass value = classTreeModel.getClassByName(templ.getValue());
            expr = new dmTransition(deleg, dataMap, origin, value);
        }
        
        operExprList.insertExpression(expr, true); // true - initialize slots in inserted expression
    }
}//GEN-LAST:event_addTransitionsActionPerformed
    
private void deleteExpressionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteExpressionActionPerformed
    int selected = operatorExpressionView.getSelectedRow();
    if (selected < 0) // -1 if no rows are selected
    {
        return;
    }
    
    operatorExpressionView.getListModel().removeExpression(selected);
    operatorExpressionView.clearSelection();
    operatorExpressionView.clearSlotSelection();
}//GEN-LAST:event_deleteExpressionActionPerformed
    
private void VariableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VariableActionPerformed
    if (operatorExpressionView.getSelectedSlotCount() > 0) {
        String varName;
        
        varName = JOptionPane.showInputDialog(this,
                "Enter variable name",
                "Name",
                JOptionPane.QUESTION_MESSAGE);
        
        if (varName == null) {
            return;
        }
        
        if (!varName.matches(INPUT_STRING_REGEX)) {
            JOptionPane.showMessageDialog(this, "Invalid variable name.");
            return;
        }
        
        if (!operatorExpressionView.isVariableNameUnique(varName)) {
            dmSlot slotWithName = operatorExpressionView.isVariableNameInSelection(varName);
            if (slotWithName == null) {
                JOptionPane.showMessageDialog(operatorExpressionView, "Name " + varName + " is already used and it is not included in selection.");
                return;
            }
            
            operatorExpressionView.connectSelectedToOne(slotWithName);  // connect all selected slots to the one slot with varName
            return;
        }
        
        operatorExpressionView.connectSlots(varName);
    } else {
        JOptionPane.showMessageDialog(operatorExpressionView, "No slot selected");
    }
}//GEN-LAST:event_VariableActionPerformed
    
private void deleteOperatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteOperatorActionPerformed
    operatorExpressionView.setListModel(new dmExpressionListModel()); // set empty list model
    operatorExpressionView.setEnabled(false);
    
    operatorListModel.removeElement(getActiveOperator());
}//GEN-LAST:event_deleteOperatorActionPerformed
    
    private void createTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createTaskActionPerformed
        // create new task based on current domain
        
        String taskName;

        // TODO: should be unique !!
        taskName = JOptionPane.showInputDialog(this,
                "Enter task name",
                "Name",
                JOptionPane.QUESTION_MESSAGE);
        
        if (taskName == null) { // user clicked Cancel
            return;
        }
        
        if (!taskName.matches(INPUT_STRING_REGEX)) {
            JOptionPane.showMessageDialog(this, "Invalid task name.");
            return;
        }
        
        dmTask newTask = new dmTask(taskName, dmDomain.getInstance());

        // add item into taskSelector
        taskSelModel.addElement(newTask);
    }//GEN-LAST:event_createTaskActionPerformed
    
    private void taskSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_taskSelectorItemStateChanged
        if (evt.getStateChange() == ItemEvent.DESELECTED) {
            System.out.println("Task Deselected");
            if (activeTask != null) {
                activeTask.deactivate();
                resetTaskTables();
                enableDefinitionPanel(false);
            }
            return;
        }
        
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            System.out.println("Task Selected");
            activeTask = (dmTask) evt.getItem();
            activeTask.activate(classTreeModel);
            enableDefinitionPanel(true);
            
            taskClassTree.setInheritsPopupMenu(true);
        }
    }//GEN-LAST:event_taskSelectorItemStateChanged
    
    private void delTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delTaskActionPerformed
        removeCurrentTask();
    }//GEN-LAST:event_delTaskActionPerformed
    
    private void taskClassTreePopupPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_taskClassTreePopupPopupMenuWillBecomeVisible
        boolean addConst = false;
        boolean delConst = false;

        // if no node is selected select root node by default
        if (taskClassTree.getSelectionCount() <= 0) {
            // construct path to root node (one node only)
            Object[] rootPath = new Object[1];
            rootPath[0] = classTreeModel.getRoot();
            taskClassTree.setSelectionPath(new TreePath(rootPath));
        } else { // more than one node are selected
            for (TreePath path : taskClassTree.getSelectionPaths()) { // check if there is at least one removable node
                dmiTreeNode node = (dmiTreeNode) path.getLastPathComponent();
                
                if (node instanceof dmConstant) {
                    if (((dmConstant) node).isTaskDependent()) {
                        delConst = true;
                        break;
                    }
                }
            }
        }
        
        if (taskClassTree.getSelectionCount() == 1) { // one node is selected
            dmiTreeNode node = (dmiTreeNode) taskClassTree.getLastSelectedPathComponent();
            
            if (node instanceof dmClass) {
                addConst = true;
            }
        }
        
        addConstant.setVisible(addConst);
        delConstant.setVisible(delConst);
    }//GEN-LAST:event_taskClassTreePopupPopupMenuWillBecomeVisible
    
    private void addConstantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addConstantActionPerformed
        if (activeTask == null) {   // no task selected - can not add constants
            return;
        }
        
        dmiTreeNode selectedNode = (dmiTreeNode) taskClassTree.getLastSelectedPathComponent();
        
        if (selectedNode == null) {     // empty selection
            return;
        }
        
        String newName = JOptionPane.showInputDialog(this,
                "Enter constant name:",
                "",
                JOptionPane.QUESTION_MESSAGE);
        
        if (newName == null) {  // user pressed Cancel
            return;
        }
        
        if (!newName.matches(INPUT_STRING_REGEX)) {
            JOptionPane.showMessageDialog(this, "Invalid constant name.");
            return;
        }
        
        try {
            classTreeModel.insertNewConstant(newName, (dmClass) selectedNode, true);
        } catch (TreeModelException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        
        updateTaskStateVarTable();
    }//GEN-LAST:event_addConstantActionPerformed
    
    private void delConstantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delConstantActionPerformed
        if (activeTask == null) {   // no task selected - can not delete constants
            return;
        }
        
        deleteSelectedConstantNodes();
        
        taskStateVarTable.repaint();
    }//GEN-LAST:event_delConstantActionPerformed
    
    private void taskRelationListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_taskRelationListValueChanged
        if (evt.getValueIsAdjusting()) {
            return;
        }
        
        updateTaskRelationTable();
    }//GEN-LAST:event_taskRelationListValueChanged
    
    private void taskStateVarListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_taskStateVarListValueChanged
        if (evt.getValueIsAdjusting()) {
            return;
        }
        
        updateTaskStateVarTable();
    }//GEN-LAST:event_taskStateVarListValueChanged
    
    private void saveTaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveTaskButtonActionPerformed
        if (activeTask != null) {
            File targetFile;
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "XML task file", "xml");
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(filter);
            
            int result = chooser.showSaveDialog(this);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                targetFile = chooser.getSelectedFile();
                activeTask.writeTask(targetFile);
                System.out.println("File saved.");
            }
        }
    }//GEN-LAST:event_saveTaskButtonActionPerformed
    
    private void loadTaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadTaskButtonActionPerformed
        File targetFile;
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "XML task file", "xml");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        
        int result = chooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            targetFile = chooser.getSelectedFile();
            
            dmTask loadedTask = new dmTask(targetFile.getName(), dmDomain.getInstance());
            
            try {
                loadedTask.loadTask(targetFile);
            } catch (TaskXMLAdapterException e) {
                if (e.getTaskName() == null) {
                    JOptionPane.showMessageDialog(this, "Error when loading problem file. (Is this really a PROBLEM file?)");
                    return;
                }
                
                JOptionPane.showMessageDialog(this, "Task " + e.getTaskName() + " could not be loaded: " + e.getMessage());
                return;
            }

            // add item into taskSelector
            taskSelModel.addElement(loadedTask);
            
            System.out.println("File loaded.");
        }
    }//GEN-LAST:event_loadTaskButtonActionPerformed
    
    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        saveDomain();   // save current domain to current directory
    }//GEN-LAST:event_saveMenuItemActionPerformed
    
    private void ConstantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConstantActionPerformed
        // find out class of selected slot
        if (operatorExpressionView.getSelectedSlotCount() > 0) {
            dmClass slotClass = operatorExpressionView.getCommonClass();
            if (slotClass != null) {
                List<dmConstant> constList = new LinkedList<dmConstant>();
                slotClass.makeSubtreeConstantList(constList); // TODO which constants from the subtree shoul be allowed ???
                if (constList.size() > 0) {
                    // show dialog
                    dmConstant result = SelectConstantDialog.showDialog(this, constList);
                    // insert into slot
                    operatorExpressionView.connectSlots(result);
                } else {
                    // no constants available for this class
                    JOptionPane.showMessageDialog(operatorExpressionView, "No constant symbols available for class \"" + slotClass.getName() + "\"");
                }
            } else {
                System.err.println("No slot selected.");                
            }
        } else {
            JOptionPane.showMessageDialog(operatorExpressionView, "No slot selected");
        }
    }//GEN-LAST:event_ConstantActionPerformed
    
    private void exportPDDLDomainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPDDLDomainActionPerformed
        File targetFile;
        
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Planning domain definition", "pddl");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        
        int result = chooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            targetFile = chooser.getSelectedFile();
            
            try {
                targetFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            File tempFile;  // temporary file to save domain as XML file (method exports current state of planning domain)
            try {
                tempFile = File.createTempFile("vizz_", null);
                try {
                    dmDomain.writeDomain(tempFile);
                } catch (DomainException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                xml2pddlConvertor.domainXML2PDDL(tempFile, targetFile);
                System.out.println("Domain successfully exported.");
            } catch (DomainXMLAdapterException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ConvertorException ex) {
                System.err.println("Problem during domain export: " + ex.getMessage());
            }
        }        
    }//GEN-LAST:event_exportPDDLDomainActionPerformed
    
    private void exportPDDLTaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPDDLTaskButtonActionPerformed
        if (activeTask != null) {
            File targetFile;
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "PDDL task file", "pddl");
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(filter);
            
            int result = chooser.showSaveDialog(this);
            
            if (result == JFileChooser.APPROVE_OPTION) {
                targetFile = chooser.getSelectedFile();
                
                try {
                    targetFile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                File tempFile;
                try {
                    tempFile = File.createTempFile("vizz_", null);
                    activeTask.writeTask(tempFile);
                    xml2pddlConvertor.problemXML2PDDL(tempFile, targetFile);
                    System.out.println("Task successfully exported.");
                } catch (TaskXMLAdapterException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ConvertorException ex) {
                    System.err.println("Problem during task export: " + ex.getMessage());
                }
            }
        } else {
            System.err.println("exportPDDLTaskButtonActionPerformed: No task to export!!");
        }
    }//GEN-LAST:event_exportPDDLTaskButtonActionPerformed
    
    private void domainPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_domainPropertiesActionPerformed
        DomainPropertiesDialog.showDialog(this, dmDomain.getInstance());
    }//GEN-LAST:event_domainPropertiesActionPerformed
    
    private void clearSelectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSelectionButtonActionPerformed
        operatorExpressionView.clearSlotSelection();
    }//GEN-LAST:event_clearSelectionButtonActionPerformed
    
    private void declarationClassTreeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_declarationClassTreeKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_DELETE:
                System.out.println("DELETE");
                deleteClassTreeNode((dmiTreeNode) declarationClassTree.getLastSelectedPathComponent());
                break;
            case KeyEvent.VK_INSERT:
                System.out.println("INSERT");
                insertClassTreeClassNode((dmiTreeNode) declarationClassTree.getLastSelectedPathComponent());
                break;
            default:
            // do nothing
        }
    }//GEN-LAST:event_declarationClassTreeKeyPressed
    
    private void taskClassTreeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taskClassTreeKeyPressed
        if (activeTask == null) {   // no task selected - can not manipulate constants in the tree
            return;
        }
        
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_DELETE:
                System.out.println("DELETE");
                deleteSelectedConstantNodes();
                break;
            case KeyEvent.VK_INSERT:
                System.out.println("INSERT");
                insertClassTreeConstNode((dmiTreeNode) taskClassTree.getLastSelectedPathComponent(), true);
                break;
            default:
            // do nothing
        }
        
        taskStateVarTable.repaint();
    }//GEN-LAST:event_taskClassTreeKeyPressed
    
    private void declarationClassTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_declarationClassTreeMouseClicked
        if ((evt.getButton() == MouseEvent.BUTTON1) && (evt.getClickCount() == 2)) { // process double-click of left button
            int x = evt.getX();
            int y = evt.getY();
            TreePath nodePath = declarationClassTree.getPathForLocation(x, y);
            
            if (nodePath.getPathCount() == 1) {
                JOptionPane.showMessageDialog(declarationClassTree, "Root node name can not be changed.");
                return;
            }
            
            dmiTreeNode renamed = (dmiTreeNode) nodePath.getLastPathComponent();
            if (renamed == null) {
                return; // no node is selected
            }
            
            String newName = JOptionPane.showInputDialog(this,
                    "Enter new name:",
                    "",
                    JOptionPane.QUESTION_MESSAGE);
            
            if (newName == null) {  // user pressed Cancel
                return;
            }
            
            if (!newName.matches(INPUT_STRING_REGEX)) {
                JOptionPane.showMessageDialog(this, "Invalid name.");
                return;
            }

            // rename node in tree model
            try {
                classTreeModel.renameNode(renamed, newName);
            } catch (TreeModelException ex) {
                JOptionPane.showMessageDialog(declarationClassTree, ex.getMessage());
            }

            // propagate change into associated tasks
            if (renamed instanceof dmConstant) {
                System.out.println("Domain independent constant renamed");
                renameConstantsInTasks(renamed); // rename of domain independent constants
            }
            
            if (renamed instanceof dmClass) {
                System.out.println("Class renamed");
                try {
                    renameClassInTasks(renamed); // rename class
                } catch (TaskException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
        }
    }//GEN-LAST:event_declarationClassTreeMouseClicked
    
    private void newMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMenuItemActionPerformed
        
        
        int answer = JOptionPane.showConfirmDialog(this,
                "Do you want to save current domain and tasks ?",
                "Create new domain",
                JOptionPane.YES_NO_CANCEL_OPTION);
        
        switch (answer) {
            case JOptionPane.YES_OPTION:
                saveAll();
                clearAll();
                break;
            case JOptionPane.NO_OPTION:
                clearAll();
                break;
            case JOptionPane.CANCEL_OPTION:
                return;
        }
    }//GEN-LAST:event_newMenuItemActionPerformed
    
    private void addRowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRowsActionPerformed
        int rowCount = (Integer) rowCountSpinner.getValue();
        activeTask.addRelationDefinitionRow(getSelectedRelationName(), rowCount);
        
        updateTaskRelationTable();
    }//GEN-LAST:event_addRowsActionPerformed
    
    private void delRowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delRowsActionPerformed
        activeTask.delRelationDefinitionRows(getSelectedRelationName(), taskRelationTable.getSelectedRows());
    }//GEN-LAST:event_delRowsActionPerformed
    
    private void DelRelationArgumentColumnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DelRelationArgumentColumnActionPerformed
        relationTableModel.delArgumentColumn();
    }//GEN-LAST:event_DelRelationArgumentColumnActionPerformed
    
    private void DelStateVarArgumentColumnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DelStateVarArgumentColumnActionPerformed
        stateVarTableModel.delArgumentColumn();
    }//GEN-LAST:event_DelStateVarArgumentColumnActionPerformed
    
    private void taskClassTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_taskClassTreeMouseClicked
        if ((evt.getButton() == MouseEvent.BUTTON1) && (evt.getClickCount() == 2)) { // process double-click of left button
            int x = evt.getX();
            int y = evt.getY();
            TreePath nodePath = taskClassTree.getPathForLocation(x, y);
            
            dmiTreeNode renamed = (dmiTreeNode) nodePath.getLastPathComponent();
            if (renamed == null) {
                return; // no node is selected
            }
            
            if (!(renamed instanceof dmConstant)) { // rename only constants
                return;
            }
            
            dmConstant renamedConst = (dmConstant) renamed;
            
            if (!renamedConst.isTaskDependent()) { // rename only task dependent constants
                return;
            }
            
            String newName = JOptionPane.showInputDialog(this,
                    "Enter new name:",
                    "",
                    JOptionPane.QUESTION_MESSAGE);
            
            if (newName == null) {  // user pressed Cancel
                return;
            }
            
            if (!newName.matches(INPUT_STRING_REGEX)) {
                JOptionPane.showMessageDialog(this, "Invalid name.");
                return;
            }
            
            try {
                classTreeModel.renameNode(renamed, newName);
            } catch (TreeModelException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
        
        if (evt.getButton() == MouseEvent.BUTTON3) {
            if (taskSelector.getSelectedIndex() < 0) {                
                JOptionPane.showMessageDialog(this, "Select some task first.");
            }
        }
    }//GEN-LAST:event_taskClassTreeMouseClicked
    
    private void copyOperatorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyOperatorActionPerformed
        dmiOperator selectedOperator = getActiveOperator();
        
        if (selectedOperator == null) {
            return;
        }
        
        String copyName = JOptionPane.showInputDialog(this,
                "Enter operator copy name",
                "",
                JOptionPane.QUESTION_MESSAGE);
        
        if (copyName == null) { // user clicked Cancel
            return;
        }
        
        if (!copyName.matches(INPUT_STRING_REGEX)) {
            JOptionPane.showMessageDialog(this, "Invalid operator copy name.");
            return;
        }
        
        if (!operatorListModel.isUniqueName(copyName)) {
            JOptionPane.showMessageDialog(this, "Operator copy name must be unique.");
            return;
        }
        try {
            operatorListModel.addElement(new dmOperator(selectedOperator, copyName));
        } catch (DomainException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_copyOperatorActionPerformed
    
    private void operatorListPopupPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_operatorListPopupPopupMenuWillBecomeVisible
        boolean show = operatorList.getSelectedIndex() >= 0; // selection is not empty
        copyOperator.setVisible(show);
        deleteOperator.setVisible(show);
    }//GEN-LAST:event_operatorListPopupPopupMenuWillBecomeVisible
    
    private void taskStateVarTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_taskStateVarTableMouseClicked
        if ((evt.getClickCount() == 1) && (evt.getButton() == MouseEvent.BUTTON1)) { // one click with left mouse button
            if (resetCellToggle.getModel().isSelected()) { // if the resetCleeButton is togled
                Point clickPoint = evt.getPoint();
                int row = taskStateVarTable.rowAtPoint(clickPoint);
                int col = taskStateVarTable.columnAtPoint(clickPoint);
                
                resetTastStateVarTableCell(row, col, (DefinitionTableModel) taskStateVarTable.getModel());
            }
        }
    }//GEN-LAST:event_taskStateVarTableMouseClicked
    
    private void resetColumnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetColumnActionPerformed
        if (taskStateVarTable.getSelectedColumnCount() <= 0) {
            JOptionPane.showMessageDialog(this, "Select one column in the State variable definitions table");
            return;
        }
        int col = taskStateVarTable.getSelectedColumn();
        DefinitionTableModel model = (DefinitionTableModel) taskStateVarTable.getModel();
        if (model.isInitColumn(col) || model.isGoalColumn(col)) {            
            resetDefinitionTableColumn(col, taskStateVarTable.getModel());
        }
    }//GEN-LAST:event_resetColumnActionPerformed
    
    private void declarationsPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_declarationsPanelComponentShown
        System.out.println("Declarations panel active");
        if (activeTask != null) {
            taskSelector.setSelectedIndex(-1);
        }
    }//GEN-LAST:event_declarationsPanelComponentShown
    
    private void operatorsPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_operatorsPanelComponentShown
        System.out.println("Operators panel active");
        if (activeTask != null) {
            taskSelector.setSelectedIndex(-1);  // no task is selected
            // taskSelectorItemStateChanged method will be called
        }
        
        operatorListModel.updateOperators(); // react to possible changes made in declarations
    }//GEN-LAST:event_operatorsPanelComponentShown
    
    private void tasksPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_tasksPanelComponentShown
        System.out.println("Tasks panel active");

        // check whether some task is defined
        if (taskSelModel.getSize() > 0) {
            // Select first item in list. This should also call taskSelectorItemStateChanged method 
            taskSelector.setSelectedIndex(0);
        } else {
            // no task is defined - we don't allow definitions of relations and state variables
            enableDefinitionPanel(false);
        }
    }//GEN-LAST:event_tasksPanelComponentShown

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem AddNewClass;
    private javax.swing.JMenuItem AddNewConstant;
    private javax.swing.JMenuItem AddNewRelation;
    private javax.swing.JMenuItem AddNewStateVar;
    private javax.swing.JMenuItem AddRelationArgumentColumn;
    private javax.swing.JMenuItem AddStateVarArgumentColumn;
    private javax.swing.JMenuItem ClearStateVarRange;
    private javax.swing.JMenuItem Constant;
    private javax.swing.JSplitPane DeclarationsHSplit;
    private javax.swing.JMenuItem DelItem;
    private javax.swing.JMenuItem DelRelation;
    private javax.swing.JMenuItem DelRelationArgumentColumn;
    private javax.swing.JMenuItem DelStateVar;
    private javax.swing.JMenuItem DelStateVarArgumentColumn;
    private javax.swing.JPanel RelationDeclarationPanel;
    private javax.swing.JPanel RelationDefinitionPanel;
    private javax.swing.JPanel StateVarDeclarationPanel;
    private javax.swing.JPanel StateVarDefinitionsPanel;
    private javax.swing.JMenuItem Variable;
    private javax.swing.JMenuItem addConstant;
    private javax.swing.JMenuItem addRelations;
    private javax.swing.JButton addRows;
    private javax.swing.JMenuItem addTransitions;
    private javax.swing.JPopupMenu classTreePopup;
    private javax.swing.JScrollPane classTreeScrollPane;
    private javax.swing.JButton clearSelectionButton;
    private javax.swing.JMenuItem copyOperator;
    private javax.swing.JMenuItem createOperator;
    private javax.swing.JButton createTask;
    private javax.swing.JTree declarationClassTree;
    private javax.swing.JPanel declarationsPanel;
    private javax.swing.JToolBar declarationsToolbar;
    private javax.swing.JMenuItem delConstant;
    private javax.swing.JButton delRows;
    private javax.swing.JButton delTask;
    private javax.swing.JMenuItem deleteExpression;
    private javax.swing.JMenuItem deleteOperator;
    private javax.swing.JMenuItem domainProperties;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem exportPDDLDomain;
    private javax.swing.JButton exportPDDLTaskButton;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu insertExpression;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JSplitPane jSplitPane4;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    private javax.swing.JButton loadTaskButton;
    private javax.swing.JTabbedPane mainTabbedPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private ApplicationGUI.ExpressionListView operatorExpressionView;
    private javax.swing.JPopupMenu operatorExpressionsPopup;
    private javax.swing.JList operatorList;
    private javax.swing.JPopupMenu operatorListPopup;
    private javax.swing.JPanel operatorsPanel;
    private javax.swing.JTable relationTable;
    private javax.swing.JPopupMenu relationTablePopup;
    private javax.swing.JScrollPane relationTableScrollPane;
    private javax.swing.JToggleButton resetCellToggle;
    private javax.swing.JButton resetColumn;
    private javax.swing.JSpinner rowCountSpinner;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JButton saveTaskButton;
    private javax.swing.JMenu setValue;
    private javax.swing.JTable stateVarTable;
    private javax.swing.JPopupMenu stateVarTablePopup;
    private javax.swing.JScrollPane stateVarTableScrollPane;
    private javax.swing.JTree taskClassTree;
    private javax.swing.JPopupMenu taskClassTreePopup;
    private javax.swing.JScrollPane taskClassTreeScrollPane;
    private javax.swing.JTabbedPane taskDefinitionTabbedPane;
    private javax.swing.JList taskRelationList;
    private javax.swing.JPanel taskRelationTab;
    private javax.swing.JTable taskRelationTable;
    private javax.swing.JComboBox taskSelector;
    private javax.swing.JList taskStateVarList;
    private javax.swing.JPanel taskStateVarTab;
    private javax.swing.JTable taskStateVarTable;
    private javax.swing.JPanel tasksPanel;
    // End of variables declaration//GEN-END:variables

    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MainWindow.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    private dmiOperator getActiveOperator() {
        if (operatorList.getSelectedIndex() < 0) { // no selection
            return null;
        }
        
        int operIndex = operatorList.getSelectedIndex();
        
        return operatorListModel.getOperatorAt(operIndex);
    }
    
    private String getSelectedRelationName() {
        return (String) taskRelationList.getSelectedValue();
    }
    
    private String getSelectedStateVarName() {
        if (taskStateVarList.getSelectionModel().isSelectionEmpty()) {
            return null;
        }
        
        return (String) taskStateVarList.getSelectedValue();
    }
    
    private void enableDefinitionPanel(boolean b) {
        taskDefinitionTabbedPane.setEnabled(b);
        taskRelationList.setEnabled(b);
        taskStateVarList.setEnabled(b);
        taskRelationTable.setEnabled(b);
        taskStateVarTable.setEnabled(b);
        
        updateTaskRelationTable();
        updateTaskStateVarTable();
    }
    
    private void resetTaskTables() {
        if (activeTask != null) {
            taskRelationTable.setModel(new DefaultTableModel());
            taskStateVarTable.setModel(new DefaultTableModel());
        }
    }

    /**
     * When model of taskStateVar table has changed, we need to replace it.
     */
    private void updateTaskStateVarTable() {
        if (activeTask != null) {
            
            String stateVarName = getSelectedStateVarName();
            if (stateVarName == null) {
                return;
            }
            
            DefinitionTableModel selectedModel = (DefinitionTableModel) activeTask.getStateVarDefinition(stateVarName);
            if (selectedModel == null) {
                return;
            }
            
            taskStateVarTable.setModel(selectedModel);
        }
    }
    
    private void updateTaskRelationTable() {
        if (activeTask != null) {
            
            String relName = getSelectedRelationName();
            if (relName == null) {
                return;
            }
            
            DefinitionTableModel selectedModel = (DefinitionTableModel) activeTask.getRelationDefinition(relName);
            if (selectedModel == null) {
                return;
            }
            
            taskRelationTable.setModel(selectedModel);
        }
    }
    
    private void deleteClassTreeNode(dmiTreeNode selectedNode) {
        if (selectedNode == null) {     // empty selection
            return;
        }
        
        if (selectedNode.isRoot()) {    // root can not be deleted
            return;
        }
        
        classTreeModel.removeNodeFromParent(selectedNode);
    }
    
    private void insertClassTreeClassNode(dmiTreeNode selectedNode) {
        if (selectedNode == null) {     // empty selection
            return;
        }        
        try {
            classTreeModel.insertNewClass(selectedNode);
        } catch (TreeModelException ex) {
            JOptionPane.showMessageDialog(declarationClassTree, ex.getMessage());
        }
    }
    
    private void insertClassTreeConstNode(dmiTreeNode selectedNode, boolean taskDep) {
        if (selectedNode == null) {     // empty selection
            return;
        }
        
        try {
            classTreeModel.insertNewConstant(selectedNode, taskDep);
        } catch (TreeModelException ex) {
            JOptionPane.showMessageDialog(declarationClassTree, ex.getMessage());
        }
    }
    
    private void clearAll() {
        dmDomain domain = dmDomain.getInstance();
        
        resetGUIComponents();
        
        try {
            domain.clearDomain();
        } catch (DomainException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        clearTasks();
    }
    
    private void clearTasks() {
        taskSelModel.removeAllElements();
        taskClassTree.setInheritsPopupMenu(false);
    }
    
    private void removeCurrentTask() {
        int index = taskSelector.getSelectedIndex();    // selected index=-1 if no tasks are defined
        
        if (index >= 0) {
            taskSelModel.removeElementAt(index);
            taskRelationTable.setModel(new DefaultTableModel());
            taskStateVarTable.setModel(new DefaultTableModel());
            System.out.println("Task deleted.");
            
            if (taskSelModel.getSize() == 0) {
                taskClassTree.setInheritsPopupMenu(false);
            }
        }
    }
    
    private void saveAll() {
        saveDomain();
        saveProblems();
    }
    
    private void saveDomain() {
        String userDir = System.getProperty("user.dir");
        String filename = userDir + File.separator + dmDomain.getInstance().toString() + "_domain.xml";
        
        File targetFile = new File(filename);
        
        if (!targetFile.exists()) {
            try {
                targetFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            dmDomain.writeDomain(targetFile);
        } catch (DomainException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveProblems() {
        String userDir = System.getProperty("user.dir");
        String basename = userDir + File.separator;
        
        for (int i = 0; i < taskSelModel.getSize(); ++i) {
            dmTask actTask = (dmTask) taskSelModel.getElementAt(i);
            String filename = basename + actTask.toString() + ".xml";
            
            File targetFile = new File(filename);
            
            if (!targetFile.exists()) {
                try {
                    targetFile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            actTask.writeTask(targetFile);
        }
    }
    
    private void deleteSelectedConstantNodes() {
        for (TreePath path : taskClassTree.getSelectionPaths()) { // delete all removable nodes
            dmiTreeNode node = (dmiTreeNode) path.getLastPathComponent();
            
            if (node instanceof dmConstant) {
                if (((dmConstant) node).isTaskDependent()) {
                    deleteClassTreeNode(node);
                }
            }
        }
    }
    
    private void resetTastStateVarTableCell(int row, int col, DefinitionTableModel model) {        
        if ((!model.isInitColumn(col)) && (!model.isGoalColumn(col))) {
            JOptionPane.showMessageDialog(this, "Only the cells in the two last columns can be reseted.");
            return;
        }
        
        model.setValueAt(DefinitionTableModel.DEFAULT_CELL_VALUE, row, col);
    }
    
    private void resetDefinitionTableColumn(int col, TableModel model) {
        for (int row = 0; row < model.getRowCount(); ++row) {
            model.setValueAt(DefinitionTableModel.DEFAULT_CELL_VALUE, row, col);
        }
    }

    /**
     * Rename task independent constants used in task definitions
     * 
     * @param renamed 
     */
    private void renameConstantsInTasks(dmiTreeNode renamed) {
        assert (renamed instanceof dmConstant);
        
        String className = renamed.getParent().getName();
        String oldName = renamed.getOldName();
        String newName = renamed.getName();

        // need to do rename constants in all tasks
        for (int i = 0; i < taskSelModel.getSize(); ++i) {
            dmTask actTask = (dmTask) taskSelModel.getElementAt(i);
            
            actTask.renameConstants(className, oldName, newName);
        }
    }
    
    private void renameClassInTasks(dmiTreeNode renamed) throws TaskException {
        assert (renamed instanceof dmClass);
        
        String oldName = renamed.getOldName();
        String newName = renamed.getName();

        // need to do rename class in all tasks
        for (int i = 0; i < taskSelModel.getSize(); ++i) {
            dmTask actTask = (dmTask) taskSelModel.getElementAt(i);
            
            actTask.renameClass(oldName, newName);
        }
    }
    
    private void resetGUIComponents() {
        // in order to prevent program freezing we have to reset some components
        operatorExpressionView.setListModel(new dmExpressionListModel()); // set empty list model
        operatorExpressionView.setEnabled(false); // disable
    }
}

class ClassTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private ImageIcon classIcon = null;
    private ImageIcon constIcon = null;
    private ImageIcon rootIcon = null;
    private ImageIcon taskDepConstIcon = null;
    
    @Override
    public Component getTreeCellRendererComponent(JTree jtree, Object node,
            boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (node instanceof dmClass) {
            this.setClosedIcon(classIcon);
            this.setOpenIcon(classIcon);
            this.setLeafIcon(classIcon);
        }
        
        if (node instanceof dmConstant) {
            if (((dmConstant) node).isTaskDependent()) {
                this.setClosedIcon(taskDepConstIcon);
                this.setOpenIcon(taskDepConstIcon);
                this.setLeafIcon(taskDepConstIcon);
            } else {
                this.setClosedIcon(constIcon);
                this.setOpenIcon(constIcon);
                this.setLeafIcon(constIcon);
            }
        }
        
        if (node instanceof dmiTreeNode) {
            dmiTreeNode treeNode = (dmiTreeNode) node;
            this.setText(treeNode.toString());
            if (treeNode.isRoot()) {
                this.setClosedIcon(rootIcon);
                this.setOpenIcon(rootIcon);
                this.setLeafIcon(rootIcon);
            }
        } else {
            throw new UnsupportedOperationException("Can display only instances of dmTreeNode");
        }
        
        return super.getTreeCellRendererComponent(jtree, node, selected, expanded, leaf, row, hasFocus);
    }
    
    public void setClassIcon(ImageIcon icon) {
        this.classIcon = icon;
    }
    
    public void setConstIcon(ImageIcon icon) {
        this.constIcon = icon;
    }
    
    public void setRootIcon(ImageIcon icon) {
        this.rootIcon = icon;
    }
    
    public void setTaskDepConstIcon(ImageIcon icon) {
        this.taskDepConstIcon = icon;
    }
}

class NonRootTreeEditor extends DefaultTreeCellEditor {
    
    public NonRootTreeEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }
    
    @Override
    public boolean isCellEditable(EventObject event) {
        if (!super.isCellEditable(event)) {
            return false;
        }
        if (event != null && event.getSource() instanceof JTree && event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) event;
            JTree thisTree = (JTree) event.getSource();
            TreePath path = thisTree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
            return path.getPathCount() > 1; // root node is not editable
        }
        return false;
    }
}

class TaskClassTreeEditor extends DefaultTreeCellEditor {
    
    public TaskClassTreeEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }
    
    @Override
    public boolean isCellEditable(EventObject event) {
        if (!super.isCellEditable(event)) {
            return false;
        }
        if (event != null && event.getSource() instanceof JTree && event instanceof MouseEvent) {
            MouseEvent mouseEvent = (MouseEvent) event;
            JTree thisTree = (JTree) event.getSource();
            TreePath path = thisTree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
            Object node = path.getLastPathComponent();
            if (node instanceof dmClass) {
                return false;
            }
            
            if (node instanceof dmConstant) {
                dmConstant constNode = (dmConstant) node;
                if (constNode.isTaskDependent()) {
                    return true;
                }
            }
        }
        return false;
    }
}

class ElementNameEditor extends DefaultCellEditor {
    
    private String oldValue = null;
    private dmiElementSet dataModel = null;
    
    public ElementNameEditor(dmiElementSet model) {
        super(new JTextField());
        dataModel = model;
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected,
            int row, int column) {
        
        oldValue = (String) value;   // remember old value

        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    
    @Override
    public Object getCellEditorValue() {
        assert (dataModel != null);  // value should be always initialized in constructor
        Object result = super.getCellEditorValue();
        
        if (result == null) { // null value should never be here
            return oldValue;
        }
        
        assert (result instanceof String);
        
        String newName = (String) result;
        
        if (!newName.matches(MainWindow.INPUT_STRING_REGEX)) {
            System.err.println("Invalid element name - leaving old name.");
            return oldValue;
        }
        
        if (dataModel.isNameUnique((String) result)) {
            System.out.println("OK name is unique.");
            return (String) result;
        } else {
            System.out.println("Name is not unique!! Returning old value instead.");
            return oldValue;
        }
    }
}

class OperatorListCellRenderer extends DefaultListCellRenderer {
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (!(value instanceof dmOperator)) {
            throw new UnsupportedOperationException("Can display only instances of dmOperator");
        }
        
        String text = ((dmOperator) value).getName();
        
        return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
    }
}

class DefinitionTableCellRenderer extends DefaultTableCellRenderer {
    
    public static final Color DEFAULT_VALUE_COLOR = new Color(255, 200, 200);
    public static final Color DEFINED_VALUE_COLOR = new Color(190, 255, 190);
    
    @Override
    public Component getTableCellRendererComponent(JTable jtable, Object value, boolean selected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(jtable, value, selected, hasFocus, row, column);
        
        if (value instanceof String) { // all values in DefinitionTableModel are Strings
            String cellStr = (String) value;
            
            this.setText(DefinitionTableModel.getCellValue(cellStr));
            
            if (DefinitionTableModel.prefixMatch(cellStr, DefinitionTableModel.CONST_PREFIX)
                    || DefinitionTableModel.prefixMatch(cellStr, DefinitionTableModel.CONST_SET_PREFIX)) {
                this.setBackground(DEFINED_VALUE_COLOR);
            } else {
                this.setBackground(DEFAULT_VALUE_COLOR);
            }
        }
        
        return this;
    }
}
