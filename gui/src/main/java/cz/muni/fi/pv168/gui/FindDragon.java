package cz.muni.fi.pv168.gui;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.*;

public class FindDragon extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable table1;
    private DragonAndCustomerChangeable parent;
    private AbstractTableModel tableModel;


    public FindDragon(AbstractTableModel tableModel, DragonAndCustomerChangeable parent) {
        this.parent = parent;
        this.tableModel = tableModel;
        table1.setModel(tableModel);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        pack();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        if(table1.getSelectedRow() != -1) {
            if(tableModel instanceof DragonTableModel) {
                parent.setDragon(table1.getSelectedRow());
            }else{
                parent.setCustomer(table1.getSelectedRow());
            }
        }
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
