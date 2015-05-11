package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
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
public class NewLease extends JFrame implements DragonAndCustomerChangeable {
    private JButton findDragon;
    private JButton findCustomer;
    private JSpinner daySpinner;
    private JSpinner monthSpinner;
    private JSpinner yearSpinner;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JSpinner secondSpinner;
    private JTextField dragonSelectedRowField;
    private JTextField customerSelectedRowField;
    private JButton createButton;
    private JPanel panel1;
    private JTextField dragonNameField;
    private JTextField customerNameField;
    private JTextField priceField;


    private DragonTableModel dragonTableModel;
    private CustomerTableModel customerTableModel;
    private LeaseTableModel leaseTableModel;
    private LeaseManager leaseManager;

    private CreateLeaseSwingWorker createLeaseSwingWorker;
    private static final Logger log = Logger.getLogger(LeaseManagerImpl.class.getCanonicalName());

    private class CreateLeaseSwingWorker extends SwingWorker<Integer,Void> {

        private Lease leaseToCreate;
        public CreateLeaseSwingWorker(Lease leaseToCreate) {
            this.leaseToCreate = leaseToCreate;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            leaseManager.createLease(leaseToCreate);
            return 1;
        }

        @Override
        protected void done() {
            try {
                if(get() == 1){
                    createButton.setEnabled(true);
                    leaseTableModel.fireTableDataChanged();
                    NewLease.this.dispose();
                }
            } catch (ExecutionException ex) {
                log.log(Level.SEVERE, "NewLease window, done(),ExecutionException: ",ex);
                throw new ServiceFailureException("Exception thrown in doInBackground() while create dragon", ex.getCause());
            } catch (InterruptedException ex) {
                log.log(Level.SEVERE, "NewLease window, done(),InterruptedException: ",ex);
                throw new RuntimeException("Operation interrupted (this should never happen)",ex);
            }
        }
    }

    public NewLease(LeaseManager leaseManager, final DragonTableModel dragonTableModel, final CustomerTableModel customerTableModel, final LeaseTableModel leaseTableModel) {
        setDateLimits();

        this.leaseManager = leaseManager;
        this.dragonTableModel = dragonTableModel;
        this.customerTableModel = customerTableModel;
        this.leaseTableModel = leaseTableModel;

        ResourceBundle lang = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault());
        setTitle(lang.getString("newlease_title"));
        setDateLimits();
        customerSelectedRowField.setVisible(false);
        dragonSelectedRowField.setVisible(false);
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

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createButton.setEnabled(false);
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
                Lease lease = new Lease();
                try {
                    lease.setEndDate(sdf.parse(date));
                } catch (ParseException | NumberFormatException ex){
                    log.log(Level.SEVERE, "NewLease window, ParseException | NumberFormatException: ",ex);
                    throw new ServiceFailureException("Parse exception while getting new dragon",ex);
                }

                try {
                    if (!dragonSelectedRowField.getText().isEmpty() && !dragonSelectedRowField.getText().equals("-1")) {
                        lease.setDragon(dragonTableModel.getDragonAt(Integer.parseInt(dragonSelectedRowField.getText())));
                    }
                    if(!customerSelectedRowField.getText().isEmpty() && !customerSelectedRowField.getText().equals("-1")) {
                        lease.setCustomer(customerTableModel.getCustomerAt(Integer.parseInt(customerSelectedRowField.getText())));
                    }
                } catch (NumberFormatException ex){
                    log.log(Level.SEVERE, "NewLease window, NumberFormatException: ",ex);
                    throw new ServiceFailureException("cannot parse selected row",ex);
                }

                lease.setPrice(new BigDecimal(priceField.getText()));
                createLeaseSwingWorker = new CreateLeaseSwingWorker(lease);
                createLeaseSwingWorker.execute();
            }
        });
    }

    public void setDragon(int selectedRow){
        dragonSelectedRowField.setText(String.valueOf(selectedRow));
        dragonNameField.setText(dragonTableModel.getDragonAt(selectedRow).getName());
    }

    public void setCustomer(int selectedRow){
        customerSelectedRowField.setText(String.valueOf(selectedRow));
        customerNameField.setText(customerTableModel.getCustomerAt(selectedRow).getName());
    }

    private void setDateLimits(){
        SpinnerModel model = new SpinnerNumberModel(1, 1, 31, 1);
        daySpinner.setModel(model);
        model = new SpinnerNumberModel(1, 1, 12, 1);
        monthSpinner.setModel(model);
        model = new SpinnerNumberModel(1990, 0, 2200, 1);
        yearSpinner.setModel(model);
        model = new SpinnerNumberModel(0, 0, 23, 1);
        hourSpinner.setModel(model);
        model = new SpinnerNumberModel(0, 0, 60, 1);
        minuteSpinner.setModel(model);
        model = new SpinnerNumberModel(0, 0, 60, 10);
        secondSpinner.setModel(model);
    }
}
