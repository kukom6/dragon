package cz.muni.fi.pv168.gui;

import cz.muni.fi.pv168.dragon.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * Created by Matej on 4. 5. 2015.
 */
public class CustomerTableModel extends AbstractTableModel {

    private CustomerManager customerManager;
    private List<Customer> allCustomers;
    private static final Object LOCK = new Object();
    private RefreshCustomersSwingWorker refreshCustomersSwingWorker;
    private UpdateCustomerSwingWorker updateCustomerSwingWorker;
    public CustomerTableModel(CustomerManager customerManager){
        this.customerManager=customerManager;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        synchronized (LOCK) {
            return allCustomers.size();
        }
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Customer customer = allCustomers.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return customer.getId();
            case 1:
                return customer.getName();
            case 2:
                return customer.getSurname();
            case 3:
                return customer.getAddress();
            case 4:
                return customer.getIdentityCard();
            case 5:
                return customer.getPhoneNumber();
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }

    @Override
    public String getColumnName(int column) {
        ResourceBundle lang = ResourceBundle.getBundle("LanguageBundle", Locale.getDefault());
        switch (column) {
            case 0:
                return lang.getString("mainwindow_id");
            case 1:
                return lang.getString("mainwindow_name");
            case 2:
                return lang.getString("mainwindow_surname");
            case 3:
                return lang.getString("mainwindow_address");
            case 4:
                return lang.getString("mainwindow_IdentityCard");
            case 5:
                return lang.getString("mainwindow_PhoneNumber");
            default:
                throw new IllegalArgumentException("column");
        }
    }

    private class RefreshCustomersSwingWorker extends SwingWorker<Integer,Void> {

        @Override
        protected Integer doInBackground() throws Exception {
            synchronized (LOCK){
                allCustomers = new ArrayList<>(customerManager.getAllCustomers());
            }
            return 1;
        }
    }

    private class UpdateCustomerSwingWorker extends SwingWorker<Integer,Void>{
        private Customer customerToUpdate;
        public UpdateCustomerSwingWorker(Customer customerToUpdate) {
            this.customerToUpdate = customerToUpdate;
        }
        @Override
        protected Integer doInBackground() throws Exception {
            synchronized (LOCK){
                customerManager.updateCustomer(customerToUpdate);
            }
            return 1;
        }
        @Override
        protected void done() {
            try {
                if(get() == 1){
                    fireTableDataChanged();
                }
                if (isCancelled()) {
                    throw new ServiceFailureException("Cancelled");
                }
            } catch (ExecutionException ex) {
                throw new ServiceFailureException("Exception thrown in doInBackground() while delete dragon", ex.getCause());
            } catch (InterruptedException ex) {
                // K tomuto by v tomto případě nemělo nikdy dojít (viz níže)
                throw new RuntimeException("Operation interrupted (this should never happen)",ex);
            }
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Customer customer = getCustomerAt(rowIndex);
        switch (columnIndex) {
            case 0:
                customer.setId((Long) aValue);
                break;
            case 1:
                customer.setName((String) aValue);
                break;
            case 2:
                customer.setSurname((String) aValue);
                break;
            case 3:
                customer.setAddress((String) aValue);
                break;
            case 4:
                customer.setIdentityCard((String) aValue);
                break;
            case 5:
                customer.setPhoneNumber((String) aValue);
                break;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
        updateCustomerSwingWorker = new UpdateCustomerSwingWorker(getCustomerAt(rowIndex));
        updateCustomerSwingWorker.execute();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            case 0:
                return false;
            default:
                throw new IllegalArgumentException("columnIndex");
        }
    }
    @Override
    public void fireTableDataChanged() {
        refreshCustomersSwingWorker = new RefreshCustomersSwingWorker();
        refreshCustomersSwingWorker.execute();
        super.fireTableDataChanged();
    }
    public Customer getCustomerAt(int row){
        return allCustomers.get(row);
    }
}
