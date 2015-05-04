package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.*;

import javax.swing.*;
import java.awt.event.*;

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
    //TODO: private NewCustomer newCustomerWindow;
    private DragonTableModel dragonTableModel;
    //TODO: private CustomerTableModel customerTableModel;
    private LeaseTableModel leaseTableModel;


    public MainWindow(final DragonManager dragonManager,final CustomerManager customerManager,final LeaseManager leaseManager){
        super("Dragon manager");

        this.dragonManager = dragonManager;
        this.customerManager = customerManager;
        this.leaseManager = leaseManager;

        dragonTableModel = new DragonTableModel(dragonManager);
        //TODO: customerTableModel = new CustomerTableModel(customerManager);
        leaseTableModel = new LeaseTableModel(leaseManager);

        dragonTable.setModel(dragonTableModel);
        //
        leaseTable.setModel(leaseTableModel);

        setContentPane(mainPanel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
                if(dragonTable.getSelectedRow() != -1) {
                    dragonManager.deleteDragon(dragonTableModel.getDragonAt(dragonTable.getSelectedRow()));
                    dragonTableModel.fireTableDataChanged();
                }
            }
        });

        deleteLease.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(leaseTable.getSelectedRow() != -1) {
                    leaseManager.deleteLease(leaseTableModel.getLeaseAt(leaseTable.getSelectedRow()));
                    leaseTableModel.fireTableDataChanged();
                }
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
