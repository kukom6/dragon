package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.DragonManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.*;

public class FindDragon extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable table1;
    private int selectedRow;
    private NewLease parent;

    public int getSelectedRow() {
        return selectedRow;
    }

    public FindDragon(AbstractTableModel tableModel, NewLease parent) {
        this.parent = parent;
        table1.setModel(tableModel);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

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
        selectedRow = table1.getSelectedRow();
        parent.setDragon(this);
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
