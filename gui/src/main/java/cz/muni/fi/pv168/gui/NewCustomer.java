package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.*;

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
 * Created by Matej on 4. 5. 2015.
 */
public class NewCustomer extends JFrame {
    private JTextField nameField;
    private JTextField surnameField;
    private JTextField addressField1;
    private JTextField addressField2;
    private JTextField IDfield;
    private JTextField phone;
    private JPanel panel1;
    private JButton addButton;
    private CustomerManager customerManager;
    private CustomerTableModel customerTableModel;

    private CreateCustomerSwingWorker createCustomerSwingWorker;
    private static final Logger log = Logger.getLogger(LeaseManagerImpl.class.getCanonicalName());

    public NewCustomer(final CustomerTableModel tableModel, final CustomerManager customerManager) {
        this.customerTableModel = tableModel;
        this.customerManager = customerManager;
        ResourceBundle lang = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault());
        setTitle(lang.getString("newcustomer_title"));

        setContentPane(panel1);
        pack();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addButton.setEnabled(false);
                Customer customer;
                String address = addressField1.getText() +" "+ addressField2.getText();
                customer = new Customer(nameField.getText(), surnameField.getText(), address, IDfield.getText(), phone.getText());
                createCustomerSwingWorker = new CreateCustomerSwingWorker(customer);
                createCustomerSwingWorker.execute();
            }
        });
    }

    private class CreateCustomerSwingWorker extends SwingWorker<Integer,Void> {

        private Customer customerToCreate;
        public CreateCustomerSwingWorker(Customer customerToCreate) {
            this.customerToCreate = customerToCreate;
        }

        @Override
        protected Integer doInBackground() throws Exception {
            customerManager.createCustomer(customerToCreate);
            return 1;
        }

        @Override
        protected void done() {
            try {
                if(get() == 1){
                    addButton.setEnabled(true);
                    customerTableModel.fireTableDataChanged();
                    NewCustomer.this.dispose();
                }
            } catch (ExecutionException ex) {
                log.log(Level.SEVERE, "NewDragon window,done(), ExecutionException: ",ex);
                throw new ServiceFailureException("Exception thrown in doInBackground() while create dragon", ex.getCause());
            } catch (InterruptedException ex) {
                log.log(Level.SEVERE, "NewDragon window,done(), InterruptedException: ",ex);
                throw new RuntimeException("Operation interrupted (this should never happen)",ex);
            }
        }
    }
}
