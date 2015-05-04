package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.DragonManager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Michal on 3.5.2015.
 */
public class NewLease extends JFrame{
    private JButton findDragon;
    private JButton najstButton;
    private JSpinner daySpinner;
    private JSpinner monthSpinner;
    private JSpinner yearSpinner;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JSpinner secondSpinner;
    private JTextField dragonField;
    private JTextField textField4;
    private JButton button1;
    private JPanel panel1;

    public NewLease(final AbstractTableModel tableModel) {
        setDateLimits();

        ResourceBundle lang = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault());
        setTitle(lang.getString("newdragon_title"));
        setDateLimits();
        setContentPane(panel1);
        pack();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        findDragon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindDragon dialog = new FindDragon(tableModel, NewLease.this);
                dialog.setVisible(true);
            }
        });
    }

    public void setDragon(FindDragon dialog){
        dragonField.setText(String.valueOf(dialog.getSelectedRow()));
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
