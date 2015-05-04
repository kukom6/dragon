package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.Dragon;
import cz.muni.fi.pv168.dragon.DragonManager;
import cz.muni.fi.pv168.dragon.ServiceFailureException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Michal on 3.5.2015.
 */
public class NewDragon extends JFrame{
    private JPanel panel1;
    private JComboBox raceBox;
    private JButton sendButton;
    private JTextField nameField;
    private JTextField numberOfHeadsField;
    private JTextField weightBox;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JSpinner secondSpinner;
    private JSpinner daySpinner;
    private JSpinner monthSpinner;
    private JSpinner yearSpinner;

    public NewDragon(final AbstractTableModel tableModel,final DragonManager dragonManager){
        ResourceBundle lang = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault());
        setTitle(lang.getString("newdragon_title"));
        setRaceBoxContent();
        setDateLimits();
        setContentPane(panel1);
        pack();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                String date = daySpinner.getValue()
                        + "-"
                        + minuteSpinner.getValue()
                        + "-"
                        + yearSpinner.getValue()
                        + " "
                        + hourSpinner.getValue()
                        + ":"
                        + minuteSpinner.getValue()
                        + ":"
                        + secondSpinner.getValue();
                try {
                    dragonManager.createDragon(
                            new Dragon(nameField.getText(),
                                sdf.parse(date),
                                String.valueOf(raceBox.getSelectedItem()),
                                Integer.parseInt(numberOfHeadsField.getText()),
                                Integer.parseInt(weightBox.getText())
                                )
                            );

                } catch (ParseException | NumberFormatException ex){
                    throw new ServiceFailureException("Parse exception while getting new dragon",ex);
                }
                tableModel.fireTableDataChanged();
                NewDragon.this.dispose();
            }
        });
    }

    private void setRaceBoxContent(){
        raceBox.addItem("scratcher");
        raceBox.addItem("burster");
        raceBox.addItem("lung");
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
