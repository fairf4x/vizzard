/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SelectConstantDialog.java
 *
 * Created on Oct 12, 2011, 2:55:37 PM
 */
package ApplicationGUI;

import DataModel.dmClass;
import DataModel.dmConstant;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;

/**
 *
 * @author fairfax
 */
public class SelectConstantDialog extends javax.swing.JDialog {

    /** Creates new form SelectConstantDialog */
    public SelectConstantDialog(java.awt.Frame parent, boolean modal, List<dmConstant> constList) {
        super(parent, modal);
        initComponents();
        
        DefaultComboBoxModel constListModel = new DefaultComboBoxModel();
        
        // initialize combo box model with available constants
        for (dmConstant constObject: constList) {
            constListModel.addElement(constObject);
        }
        
        dialogComboBox.setModel(constListModel);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        dialogComboBox = new javax.swing.JComboBox();
        okButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Select constant:");

        dialogComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dialogComboBox, 0, 297, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(okButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dialogComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(okButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        this.setVisible(false); // setVisible(true) returns now
    }//GEN-LAST:event_okButtonActionPerformed

    public static dmConstant showDialog(java.awt.Frame parent, List<dmConstant> constList) {
        SelectConstantDialog dialog = new SelectConstantDialog(parent,true,constList);
        
        dialog.setVisible(true);
        
        
        dmConstant result = (dmConstant)dialog.dialogComboBox.getSelectedItem();
        
        return result;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox dialogComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
