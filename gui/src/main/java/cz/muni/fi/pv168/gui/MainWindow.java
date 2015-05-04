package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.CustomerManager;
import cz.muni.fi.pv168.dragon.DragonManager;
import cz.muni.fi.pv168.dragon.LeaseManager;

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
    private JTable dragonsTable;
    private JButton newLeaseWithDragon;
    private JButton deleteDragon;
    private JTable leaseTable;
    private JButton deleteLease;


    public MainWindow(final DragonManager dragonManager, CustomerManager customerManager, LeaseManager leaseManager){
        super("Dragon manager");
        //setJMenuBar(createMenu());
        setContentPane(mainPanel);
        final DragonsTableModel tableModel = new DragonsTableModel(dragonManager);
        //TODO: create tableModel for customers and leases
        dragonsTable.setModel(tableModel);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        newDragonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewDragon newDragon = new NewDragon(tableModel, dragonManager);
                newDragon.setVisible(true);
            }
        });

        deleteDragon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(dragonsTable.getSelectedRow() != -1) {
                    dragonManager.deleteDragon(tableModel.getDragonAt(dragonsTable.getSelectedRow()));
                    tableModel.fireTableDataChanged();
                }
            }
        });

        newLeaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewLease newLease = new NewLease(tableModel);
                newLease.setVisible(true);
            }
        });
    }


    private JMenuBar createMenu() {
        //hlavní úroveň menu
        JMenuBar menubar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        final JMenu helpMenu = new JMenu("Help");
        menubar.add(fileMenu);
        menubar.add(Box.createHorizontalGlue());
        menubar.add(helpMenu);
        //menu File
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        fileMenu.add(exitMenuItem);
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(1);
            }
        });
        //menu Help
        JMenuItem aboutMenuItem = new JMenuItem("About");
        helpMenu.add(aboutMenuItem);
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(helpMenu,"Skvělá aplikace (c) Já","About",JOptionPane.INFORMATION_MESSAGE);
            }
        });
        return menubar;
    }
}
