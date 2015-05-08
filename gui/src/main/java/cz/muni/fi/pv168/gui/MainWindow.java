package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.*;

/**
 * Created by Michal on 21.4.2015.
 */


public class MainWindow extends JFrame implements DragonAndCustomerChangeable {
    private static final Logger log = Logger.getLogger(CustomerManagerImpl.class.getCanonicalName());

    private void configureLogging() {
        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("mainLog.log");
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Unable to initialize FileHandler", ex);
        } catch (SecurityException ex) {
            log.log(Level.SEVERE, "Unable to initialize FileHandler.", ex);
        }

        Logger.getLogger("").addHandler(fileHandler);
    }

    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JButton newDragonButton;
    private JButton newCustomerButton;
    private JButton newLeaseButton;
    private JTable customerTable;
    private JButton newLeaseWithCustomer;
    private JButton deleteCustomer;
    private JTable dragonTable;
    private JButton newLeaseWithDragon;
    private JButton deleteDragon;
    private JTable leaseTable;
    private JButton deleteLease;
    private JButton changeDragonButton;
    private JButton changeCustomerButton;

    private DragonManager dragonManager;
    private CustomerManager customerManager;
    private LeaseManager leaseManager;
    private NewLease newLeaseWindow;
    private NewDragon newDragonWindow;
    private NewCustomer newCustomerWindow;
    private DragonTableModel dragonTableModel;
    private CustomerTableModel customerTableModel;
    private LeaseTableModel leaseTableModel;
    ResourceBundle lang = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault());

    private DeleteCustomerSwingWorker deleteCustomerSwingWorker;

    private class DeleteCustomerSwingWorker extends SwingWorker<Integer,Void> {

        @Override
        protected Integer doInBackground() throws Exception {
            if(customerTable.getSelectedRow() != -1) {
                customerManager.deleteCustomer(customerTableModel.getCustomerAt(customerTable.getSelectedRow()));
                return 1;
            }else {
                return 0;
            }
        }

        @Override
        protected void done() {
            try {
                if(get() == 1){
                    deleteCustomer.setEnabled(true);
                    customerTableModel.fireTableDataChanged();
                }
                if(get()==0) {
                    ErrorWindow error=new ErrorWindow(lang.getString("mainwindow_deleteCustomerError"));
                    error.setVisible(true);
                    deleteCustomer.setEnabled(true);
                    dragonTableModel.fireTableDataChanged();
                }
            } catch (ExecutionException ex) {
                throw new ServiceFailureException("Exception thrown in doInBackground() while delete customer", ex.getCause());
            } catch (InterruptedException ex) {
                // K tomuto by v tomto případě nemělo nikdy dojít (viz níže)
                throw new RuntimeException("Operation interrupted (this should never happen)",ex);
            }
        }
    }

    private DeleteDragonSwingWorker deleteDragonSwingWorker;

    private class DeleteDragonSwingWorker extends SwingWorker<Integer,Void> {

        @Override
        protected Integer doInBackground() throws Exception {
            if(dragonTable.getSelectedRow() != -1) {
                dragonManager.deleteDragon(dragonTableModel.getDragonAt(dragonTable.getSelectedRow()));
                return 1;
            }else {
                return 0;
            }
        }

        @Override
        protected void done() {
            try {
                if(get() == 1){
                    deleteDragon.setEnabled(true);
                    dragonTableModel.fireTableDataChanged();
                }
                if(get()==0){
                    /*ErrorWindow error=new ErrorWindow(lang.getString("mainwindow_deleteDragonError"));
                    error.setVisible(true);*/
                    JOptionPane.showMessageDialog(MainWindow.this, lang.getString("mainwindow_deleteDragonError"));
                    deleteDragon.setEnabled(true);
                    dragonTableModel.fireTableDataChanged();
                }
            } catch (ExecutionException ex) {
                throw new ServiceFailureException("Exception thrown in doInBackground() while delete dragon", ex.getCause());
            } catch (InterruptedException ex) {
                // K tomuto by v tomto případě nemělo nikdy dojít (viz níže)
                throw new RuntimeException("Operation interrupted (this should never happen)",ex);
            }
        }
    }

    DeleteLeaseSwingWorker deleteLeaseSwingWorker;

    private class DeleteLeaseSwingWorker extends SwingWorker<Integer,Void> {

        @Override
        protected Integer doInBackground() throws Exception {
            if(leaseTable.getSelectedRow() != -1) {
                leaseManager.deleteLease(leaseTableModel.getLeaseAt(leaseTable.getSelectedRow()));
                return 1;
            }else {
                return 0;
            }
        }

        @Override
        protected void done() {
            try {
                if(get() == 1){
                    leaseTableModel.fireTableDataChanged();
                    deleteLease.setEnabled(true);
                }
                if(get()==0){
                    //ErrorWindow error=new ErrorWindow(lang.getString("mainwindow_deleteLeaseError"));
                    //error.setVisible(true);
                    JOptionPane.showMessageDialog(MainWindow.this, lang.getString("mainwindow_deleteLeaseError"));
                    deleteLease.setEnabled(true);
                }
            } catch (ExecutionException ex) {
                throw new ServiceFailureException("Exception thrown in doInBackground() while delete dragon", ex.getCause());
            } catch (InterruptedException ex) {
                // K tomuto by v tomto případě nemělo nikdy dojít (viz níže)
                throw new RuntimeException("Operation interrupted (this should never happen)",ex);
            }
        }
    }

    private UpdateLeaseSwingWorker updateLeaseSwingWorker;

    private class UpdateLeaseSwingWorker extends SwingWorker<Integer,Void> {

        private Lease leaseToUpdate;
        public UpdateLeaseSwingWorker(Lease leaseToUpdate) {
            this.leaseToUpdate = leaseToUpdate;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            leaseManager.updateLease(leaseToUpdate);
            return 1;
        }

        @Override
        protected void done() {
            try {
                if(get() == 1){
                    leaseTableModel.fireTableDataChanged();
                }
            } catch (ExecutionException ex) {
                throw new ServiceFailureException("Exception thrown in doInBackground() while delete dragon", ex.getCause());
            } catch (InterruptedException ex) {
                // K tomuto by v tomto případě nemělo nikdy dojít (viz níže)
                throw new RuntimeException("Operation interrupted (this should never happen)",ex);
            }
        }
    }

    public MainWindow(final DragonManager dragonManager,final CustomerManager customerManager,final LeaseManager leaseManager){
        super("Dragon manager");
        configureLogging();
        this.dragonManager = dragonManager;
        this.customerManager = customerManager;
        this.leaseManager = leaseManager;

        dragonTableModel = new DragonTableModel(dragonManager);
        customerTableModel = new CustomerTableModel(customerManager);
        leaseTableModel = new LeaseTableModel(leaseManager);

        dragonTable.setModel(dragonTableModel);
        customerTable.setModel(customerTableModel);
        leaseTable.setModel(leaseTableModel);

        JComboBox raceComboBox = new JComboBox();
        for (DragonRace f : DragonRace.values()) {
            raceComboBox.addItem(f);
        }
        dragonTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(raceComboBox));

        setContentPane(mainPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        newCustomerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewCustomer newCustomer = new NewCustomer(customerTableModel, customerManager);
                newCustomer.setVisible(true);
            }
        });

        newDragonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewDragon newDragon = new NewDragon(dragonTableModel, dragonManager);
                newDragon.setVisible(true);
            }
        });

        deleteDragon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteDragonSwingWorker = new DeleteDragonSwingWorker();
                deleteDragon.setEnabled(false);
                deleteDragonSwingWorker.execute();
            }
        });

        deleteCustomer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCustomerSwingWorker = new DeleteCustomerSwingWorker();
                deleteCustomer.setEnabled(false);
                deleteCustomerSwingWorker.execute();
            }
        });

        deleteLease.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteLeaseSwingWorker = new DeleteLeaseSwingWorker();
                deleteLease.setEnabled(false);
                deleteLeaseSwingWorker.execute();
            }
        });

        newLeaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newLeaseWindow = new NewLease(leaseManager, dragonTableModel, customerTableModel, leaseTableModel);
                newLeaseWindow.setVisible(true);
            }
        });

        newLeaseWithDragon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newLeaseWindow = new NewLease(leaseManager, dragonTableModel, customerTableModel, leaseTableModel);
                if(dragonTable.getSelectedRow() != -1){
                    newLeaseWindow.setDragon(dragonTable.getSelectedRow());
                }
                newLeaseWindow.setVisible(true);
            }
        });

        newLeaseWithCustomer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newLeaseWindow = new NewLease(leaseManager, dragonTableModel, customerTableModel, leaseTableModel);
                if(customerTable.getSelectedRow() != -1){
                    newLeaseWindow.setCustomer(customerTable.getSelectedRow());
                }
                newLeaseWindow.setVisible(true);
            }
        });

        changeDragonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindDragon dialog = new FindDragon(dragonTableModel, MainWindow.this);
                dialog.setVisible(true);
            }
        });

        changeCustomerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FindDragon dialog = new FindDragon(customerTableModel, MainWindow.this);
                dialog.setVisible(true);
            }
        });
    }

    @Override
    public void setCustomer(int selectedRow) {
        Customer newCustomer = customerTableModel.getCustomerAt(selectedRow);
        Lease leaseToUpdate = leaseTableModel.getLeaseAt(leaseTable.getSelectedRow());
        leaseToUpdate.setCustomer(newCustomer);

        updateLeaseSwingWorker = new UpdateLeaseSwingWorker(leaseToUpdate);
        updateLeaseSwingWorker.execute();
    }

    @Override
    public void setDragon(int selectedRow) {
        Dragon newDragon = dragonTableModel.getDragonAt(selectedRow);
        Lease leaseToUpdate = leaseTableModel.getLeaseAt(leaseTable.getSelectedRow());
        leaseToUpdate.setDragon(newDragon);

        updateLeaseSwingWorker = new UpdateLeaseSwingWorker(leaseToUpdate);
        updateLeaseSwingWorker.execute();
    }
}
