package cz.muni.fi.pv168.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Michal on 3.5.2015.
 */
public class NewLease extends JFrame{
    private JButton findDragon;
    private JButton findCustomer;
    private JSpinner daySpinner;
    private JSpinner monthSpinner;
    private JSpinner yearSpinner;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JSpinner secondSpinner;
    private JTextField dragonIdField;
    private JTextField customerIdField;
    private JButton createButton;
    private JPanel panel1;
    private JTextField dragonNameField;
    private JTextField customerNameField;

    private DragonTableModel dragonTableModel;
    private CustomerTableModel customerTableModel;

    public NewLease(final DragonTableModel dragonTableModel, final CustomerTableModel customerTableModel) {
        setDateLimits();
        this.dragonTableModel = dragonTableModel;
        this.customerTableModel = customerTableModel;
        ResourceBundle lang = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault());
        setTitle(lang.getString("newdragon_title"));
        setDateLimits();
        customerIdField.setVisible(false);
        dragonIdField.setVisible(false);
        setContentPane(panel1);
        pack();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        findDragon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindDragon dialog = new FindDragon(dragonTableModel, NewLease.this);
                dialog.setVisible(true);
            }
        });

        findCustomer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindDragon dialog = new FindDragon(customerTableModel, NewLease.this);
                dialog.setVisible(true);
            }
        });
    }

    public void setDragon(int selectedRow){
        dragonIdField.setText(dragonTableModel.getDragonAt(selectedRow).getId().toString());
        dragonNameField.setText(dragonTableModel.getDragonAt(selectedRow).getName());
    }

    public void setCustomer(int selectedRow){
        customerIdField.setText(customerTableModel.getCustomerAt(selectedRow).getId().toString());
        customerNameField.setText(customerTableModel.getCustomerAt(selectedRow).getName());
    }

    private void setDateLimits(){
        SpinnerModel model = new SpinnerNumberModel(1, 1, 31, 1);
        daySpinner.setModel(model);
        model = new SpinnerNumberModel(1, 1, 12, 1);
        monthSpinner.setModel(model);
        model = new SpinnerNumberModel(1990, 0, 2015, 1);
        yearSpinner.setModel(model);
        model = new SpinnerNumberModel(0, 0, 23, 1);
        hourSpinner.setModel(model);
        model = new SpinnerNumberModel(0, 0, 60, 1);
        minuteSpinner.setModel(model);
        model = new SpinnerNumberModel(0, 0, 60, 10);
        secondSpinner.setModel(model);
    }
}
