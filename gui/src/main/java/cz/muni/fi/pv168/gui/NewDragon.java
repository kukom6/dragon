package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.Dragon;
import cz.muni.fi.pv168.dragon.DragonManager;
import cz.muni.fi.pv168.dragon.LeaseManagerImpl;
import cz.muni.fi.pv168.dragon.ServiceFailureException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private DragonManager dragonManager;
    private DragonTableModel dragonTableModel;

    private CreateDragonSwingWorker createDragonSwingWorker;
    private static final Logger log = Logger.getLogger(LeaseManagerImpl.class.getCanonicalName());

    private class CreateDragonSwingWorker extends SwingWorker<Integer,Void> {

        private Dragon dragonToCreate;
        public CreateDragonSwingWorker(Dragon dragonToCreate) {
            this.dragonToCreate = dragonToCreate;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            dragonManager.createDragon(dragonToCreate);
            return 1;
        }

        @Override
        protected void done() {
            try {
                if(get() == 1){
                    sendButton.setEnabled(true);
                    dragonTableModel.fireTableDataChanged();
                    NewDragon.this.dispose();
                }
            } catch (ExecutionException ex) {
                log.log(Level.SEVERE, "NewDragon window, done(),ExecutionException: ",ex);
                throw new ServiceFailureException("Exception thrown in doInBackground() while create dragon", ex.getCause());
            } catch (InterruptedException ex) {
                log.log(Level.SEVERE, "NewDragon window, done(),InterruptedException: ",ex);
                throw new RuntimeException("Operation interrupted (this should never happen)",ex);
            }
        }
    }

    public NewDragon(final DragonTableModel tableModel,final DragonManager dragonManager){
        this.dragonManager = dragonManager;
        this.dragonTableModel = tableModel;
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
                sendButton.setEnabled(false);
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
                Dragon dragon;
                try {
                   dragon = new Dragon(nameField.getText(),
                                sdf.parse(date),
                                String.valueOf(raceBox.getSelectedItem()),
                                Integer.parseInt(numberOfHeadsField.getText()),
                                Integer.parseInt(weightBox.getText())
                                );

                } catch (ParseException | NumberFormatException ex){
                    log.log(Level.SEVERE, "NewDragon window, ParseException | NumberFormatException: ",ex);
                    throw new ServiceFailureException("Parse exception while getting new dragon",ex);
                }
                createDragonSwingWorker = new CreateDragonSwingWorker(dragon);
                createDragonSwingWorker.execute();
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
        model = new SpinnerNumberModel(0, 0, 59, 1);
        minuteSpinner.setModel(model);
        model = new SpinnerNumberModel(0, 0, 59, 10);
        secondSpinner.setModel(model);
    }

}
