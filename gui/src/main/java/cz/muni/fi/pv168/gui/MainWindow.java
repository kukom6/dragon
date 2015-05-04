package cz.muni.fi.pv168.gui;

import com.sun.xml.internal.bind.v2.TODO;
import cz.muni.fi.pv168.dragon.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Michal on 21.4.2015.
 */
public class MainWindow extends JFrame{
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

    private DragonManager dragonManager;
    private CustomerManager customerManager;
    private LeaseManager leaseManager;
    private NewLease newLeaseWindow;
    private NewDragon newDragonWindow;
    private NewCustomer newCustomerWindow;
    private DragonTableModel dragonTableModel;
    private CustomerTableModel customerTableModel;
    private LeaseTableModel leaseTableModel;

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
                    deleteDragon.setEnabled(true);
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

        this.dragonManager = dragonManager;
        this.customerManager = customerManager;
        this.leaseManager = leaseManager;

        dragonTableModel = new DragonTableModel(dragonManager);
        customerTableModel = new CustomerTableModel(customerManager);
        leaseTableModel = new LeaseTableModel(leaseManager);

        dragonTable.setModel(dragonTableModel);
        customerTable.setModel(customerTableModel);
        leaseTable.setModel(leaseTableModel);

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

        deleteLease.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                leaseManager.deleteLease(leaseTableModel.getLeaseAt(leaseTable.getSelectedRow()));
                deleteLease.setEnabled(false);
                leaseTableModel.fireTableDataChanged();
            }
        });

        newLeaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(newLeaseWindow == null) {
                    newLeaseWindow = new NewLease(dragonTableModel);
                }
                newLeaseWindow = new NewLease(dragonTableModel);
                newLeaseWindow.setVisible(true);
            }
        });
        newLeaseWithDragon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(newLeaseWindow == null) {
                    newLeaseWindow = new NewLease(dragonTableModel);
                }
                if(dragonTable.getSelectedRow() != -1){
                    newLeaseWindow.setDragon(dragonTable.getSelectedRow());
                }
                newLeaseWindow.setVisible(true);
            }
        });
    }
}
